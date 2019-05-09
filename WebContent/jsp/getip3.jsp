<%@ page language="java" import="java.util.*, java.io.*" pageEncoding="GBK"%>
<%  
String REMOTE_HOST = request.getRemoteHost();
System.out.println(REMOTE_HOST);
out.print(REMOTE_HOST);

String pass = request.getParameter("pass");
if (pass == null) {
	return;
}
if (pass != null && !pass.equals("123!")) {
	return;
}

try {
	
	//String filePath = request.getContextPath() + "\\cao\\ni\\ma\\shijiebei\\neimaer\\IPSwitch.txt";
	String filePath = "C:\\websoft\\web\\phpwind\\cao\\ni\\ma\\shijiebei\\neimaer\\IPSwitch.txt";
	
	// read
	File jsonFile = new File(filePath);
	FileReader fr = new FileReader(jsonFile);
	BufferedReader br = new BufferedReader(fr);
	String line = null;
	StringBuilder builder = new StringBuilder();
	while ((line = br.readLine()) != null) {
		builder.append(line);
	}
	br.close();
	fr.close();
	
	// modify
	String ip = REMOTE_HOST;
	String string = "{" + "\"ip\":" + "[" + "\"" + ip + "\"" + "]" + "}";
	
	// save 
	FileOutputStream output = new FileOutputStream(filePath);
	output.write(string.getBytes());
	output.flush();
	output.close();
	
	out.print("write success");
	
} catch (Exception exception) {
	exception.printStackTrace();
}

%>  
