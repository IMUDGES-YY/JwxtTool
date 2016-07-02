# JwxtTool
提供了查询成绩的接口
调用方式如下：
```java
  JwxtTool jwxtTool = new JwxtTool("你的学号", "你的密码");
	Map<String, String>grades = jwxtTool.getGrades();
	for(String key : grades.keySet()){
		System.out.println(key + ":" + grades.get(key));
	}
```
目前支持内蒙古大学教务系统的成绩查询。
