package com.imudgesyy.java.jwxttool;

import java.util.Map;

public class Main {
	public static void main(String[] args) throws Exception {
		JwxtTool jwxtTool = new JwxtTool("0141120997", "123456000");
		Map<String, String>grades = jwxtTool.getGrades();
		for(String key : grades.keySet()){
			System.out.println(key + ":" + grades.get(key));
		}
	}
}
