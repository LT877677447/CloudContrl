package com.kilotrees.log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFile {
	public static void writeLogFile(String logfile,String log) {
		//String logfile = work_dir + logFileName;
		File f = new File(logfile);
		if (!f.exists())
			return;

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String t = sdf.format(date);
		log = t + " " + log;
		try {
			FileOutputStream fos = new FileOutputStream(f, true);
			fos.write(log.getBytes("utf-8"));
			fos.close();
		} catch (Exception e) {
			
		}

	}
}
