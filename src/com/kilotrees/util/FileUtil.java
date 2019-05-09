package com.kilotrees.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

	public static String readTextFile(String fileName) {
		try {
			File file = new File(fileName);
			FileInputStream in = new FileInputStream(fileName);
			byte[] buffer = new byte[(int) file.length()];
			in.read(buffer);
			in.close();
			String string = new String(buffer, "utf-8");
			return string;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String read(String fileName) {
		try {
			byte[] bytes = readBytes(fileName);
			String string = new String(bytes,"UTF-8");
			return string;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] readBytes(String fileName) {
		try {
			
			FileInputStream fileInputStream = new FileInputStream(fileName);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			int length = -1;
			byte[] buffer = new byte[1024 * 100];
			while ((length = fileInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}

			fileInputStream.close();
			byte[] bytes = outputStream.toByteArray();
			return bytes;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void append(String fileName, String contents) {
		write(fileName, contents, true);
	}
	
	public static void write(String fileName, String contents) {
		write(fileName, contents, false);
	}
	
	public static void write(String fileName, String contents, boolean isAppend) {
		try {
			write(fileName, contents.getBytes("UTF-8"), isAppend);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		try {
//			File file = new File(fileName);
//			if (!file.exists()) {
//				if (!file.getParentFile().exists()) {
//					file.getParentFile().mkdirs();
//				}
//				file.createNewFile();
//			}
//			FileWriter writer = new FileWriter(file,isAppend);
//			writer.write(contents);
//			writer.flush();
//			writer.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}

	public static void write(String fileName, byte[] buffer) {
		write(fileName, buffer, false);
	}
	
	public static void write(String fileName, byte[] buffer, boolean isAppend) {
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			FileOutputStream fileOutputStream = new FileOutputStream(file, isAppend);
			fileOutputStream.write(buffer, 0, buffer.length);
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void copy(File source, File dest) {
		try {
			InputStream input = null;
			OutputStream output = null;
			try {
				input = new FileInputStream(source);
				output = new FileOutputStream(dest);
				byte[] buf = new byte[1024];
				int bytesRead;
				while ((bytesRead = input.read(buf)) != -1) {
					output.write(buf, 0, bytesRead);
				}
			} finally {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
