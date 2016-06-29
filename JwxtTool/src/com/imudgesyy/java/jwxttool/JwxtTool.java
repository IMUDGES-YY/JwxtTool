package com.imudgesyy.java.jwxttool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;


public class JwxtTool {
	private static String BASE_URL = "http://jwxt.imu.edu.cn";
	
	
	private String username;
	private String password;
	private static String cookieStr = null;
	
	public JwxtTool(String username,String password){
		this.username = username;
		this.password = password;
	}
	
	private void getCookie(){
		cookieStr = HttpRequest.getCookie(BASE_URL, "");
	}
	
	private boolean login(){
		
		String url = BASE_URL + "/loginAction.do";
		String param = "zjh=" + username + "&mm=" + password;
		String result = HttpRequest.sendPost(url, param);
		if (result.contains("您的密码不正确，请您重新输入！")) {
			return false;
		}
		return true;
	}
	
	/**
	 * 得到方案计划号
	 * */
	private String getFajhh(){
		String url = BASE_URL + "/gradeLnAllAction.do";
		String param = "type=ln&oper=fa";
		String result = HttpRequest.sendGet(url, param);
		String patternStr = "fajhh=[0-9]{5}";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher m = pattern.matcher(result);
	    if (m.find()) {
			
		}
		if (m.find()) {
			return m.group().replaceAll("fajhh=", "");
		}
		return null;
	}
	
	
	public Map<String, String>getGrades() throws Exception{
		getCookie();
		if (!login()) {
			throw new Exception("用户名或者密码错误");
		}
		
		String fajhh = getFajhh();
		if (fajhh == null) {
			throw new Exception("方案计划号未找到");
		}
		
		Map<String, String>grades = new TreeMap<String, String>();
		
		String url = BASE_URL + "/gradeLnAllAction.do";
		String param = "type=ln&oper=fainfo&fajhh=" + fajhh;
		String result = HttpRequest.sendGet(url, param);
		//System.out.println(result);
		//result = "<td align=\"center\">([\\d\\D]*){0-30} </td>";
		
		
		HtmlCleaner htmlCleaner = new HtmlCleaner();
		TagNode tagNode = htmlCleaner.clean(result);
		Object[] os =  tagNode.evaluateXPath("//td[@align=\"center\"]");

		
		
		//String patternStr = "<td align=\"center\">[\\d\\D]{0,101} </td>";
		//Pattern pattern = Pattern.compile(patternStr);
		//Matcher m = pattern.matcher(result);
	    
		//List<String>list = new ArrayList<>();
		int i = 0;
		String courseString = "" ;
		String gradeString ;
		for (Object object : os) {
			//System.out.println(m.group());
			
			TagNode tna = (TagNode) object;  
            String str = tna.getText().toString();  
            
            //System.out.println("****   " + str);
			if (i % 7 == 2) {
				courseString = str;
				courseString = courseString.replaceAll("<td align=\"center\">", "");
				courseString = courseString.replaceAll("</td>", "");
				courseString = courseString.replaceAll(" ", "");
				//System.out.println(courseString);
			}
			
			if (i % 7 == 6) {
				gradeString = str;
				gradeString = gradeString.replaceAll("<td align=\"center\">","");
				gradeString = gradeString.replaceAll("<p align=\"center\">","");
				gradeString = gradeString.replaceAll("</P>","");
				gradeString = gradeString.replaceAll("&nbsp;","");
				gradeString = gradeString.replaceAll("</td>","");
				gradeString = gradeString.replaceAll(" ","");
				gradeString = gradeString.replaceAll("\t","");
				//System.out.println(gradeString);
				grades.put(courseString, gradeString);
			}
			i++;
		}

		return grades;
	}
	
	public static class HttpRequest {
	    /**
	     * 向指定URL发送GET方法的请求
	     * 
	     * @param url
	     *            发送请求的URL
	     * @param param
	     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	     * @return URL 所代表远程资源的响应结果
	     */
	    public static String sendGet(String url, String param) {
	        String result = "";
	        BufferedReader in = null;
	        try {
	            String urlNameString = url + "?" + param;
	            URL realUrl = new URL(urlNameString);
	            // 打开和URL之间的连接
	            URLConnection connection = realUrl.openConnection();
	            // 设置通用的请求属性
	            connection.setRequestProperty("accept", "*/*");
	            connection.setRequestProperty("connection", "Keep-Alive");
	            connection.setRequestProperty("user-agent",
	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	            if (cookieStr != null) {
	            	connection.setRequestProperty("cookie", cookieStr);
				}
	            
	            // 建立实际的连接
	            connection.connect();
	            // 获取所有响应头字段
	            Map<String, List<String>> map = connection.getHeaderFields();
	            // 遍历所有的响应头字段
	            for (String key : map.keySet()) {
	                //System.out.println(key + "--->" + map.get(key));
	            }
	            // 定义 BufferedReader输入流来读取URL的响应
	            in = new BufferedReader(new InputStreamReader(
	                    connection.getInputStream(),"gbk"));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	        } catch (Exception e) {
	            System.out.println("发送GET请求出现异常！" + e);
	            e.printStackTrace();
	        }
	        // 使用finally块来关闭输入流
	        finally {
	            try {
	                if (in != null) {
	                    in.close();
	                }
	            } catch (Exception e2) {
	                e2.printStackTrace();
	            }
	        }
	        return result;
	    }

	    /**
	     * 向指定 URL 发送POST方法的请求
	     * 
	     * @param url
	     *            发送请求的 URL
	     * @param param
	     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	     * @return 所代表远程资源的响应结果
	     */
	    public static String sendPost(String url, String param) {
	        PrintWriter out = null;
	        BufferedReader in = null;
	        String result = "";
	        try {
	            URL realUrl = new URL(url);
	            // 打开和URL之间的连接
	            URLConnection conn = realUrl.openConnection();
	            // 设置通用的请求属性
	            conn.setRequestProperty("accept", "*/*");
	            conn.setRequestProperty("connection", "Keep-Alive");
	            conn.setRequestProperty("user-agent",
	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	            if (cookieStr != null) {
	            	conn.setRequestProperty("cookie", cookieStr);
				}
	            // 发送POST请求必须设置如下两行
	            conn.setDoOutput(true);
	            conn.setDoInput(true);
	            // 获取URLConnection对象对应的输出流
	            out = new PrintWriter(conn.getOutputStream());
	            // 发送请求参数
	            out.print(param);
	            // flush输出流的缓冲
	            out.flush();
	            // 定义BufferedReader输入流来读取URL的响应
	            in = new BufferedReader(
	                    new InputStreamReader(conn.getInputStream(),"gbk"));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	        } catch (Exception e) {
	            System.out.println("发送 POST 请求出现异常！"+e);
	            e.printStackTrace();
	        }
	        //使用finally块来关闭输出流、输入流
	        finally{
	            try{
	                if(out!=null){
	                    out.close();
	                }
	                if(in!=null){
	                    in.close();
	                }
	            }
	            catch(IOException ex){
	                ex.printStackTrace();
	            }
	        }
	        return result;
	    }
	    
	    
	    

	    public static String getCookie(String url, String param) {
	        String result = "";
	        BufferedReader in = null;
	        String cookieString = null;
	        try {
	            String urlNameString = url + "?" + param;
	            URL realUrl = new URL(urlNameString);
	            // 打开和URL之间的连接
	            URLConnection connection = realUrl.openConnection();
	            // 设置通用的请求属性
	            connection.setRequestProperty("accept", "*/*");
	            connection.setRequestProperty("connection", "Keep-Alive");
	            connection.setRequestProperty("user-agent",
	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	            // 建立实际的连接
	            connection.connect();
	            // 获取所有响应头字段
	            Map<String, List<String>> map = connection.getHeaderFields();
	            // 遍历所有的响应头字段
	            for (String key : map.keySet()) {
	                //System.out.println(key + "--->" + map.get(key));
	                if (key != null ) {
						if (key.equals("Set-Cookie")) {
							cookieString = map.get(key).get(0);
						}
					}
	            }
	            // 定义 BufferedReader输入流来读取URL的响应
	            in = new BufferedReader(new InputStreamReader(
	                    connection.getInputStream(),"gbk"));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	        } catch (Exception e) {
	            System.out.println("发送GET请求出现异常！" + e);
	            e.printStackTrace();
	        }
	        // 使用finally块来关闭输入流
	        finally {
	            try {
	                if (in != null) {
	                    in.close();
	                }
	            } catch (Exception e2) {
	                e2.printStackTrace();
	            }
	        }
	        //cookieString = cookieString.replaceAll("JSESSIONID=", "");
	        cookieString = cookieString.replaceAll("; path=/", "");
	        return cookieString;
	    }
	}
}
