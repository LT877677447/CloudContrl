package com.kilotrees.servlets;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.dao.devpostmsgdao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.devpostmsg;
import com.kilotrees.services.actdeviceinfo_service;
import com.kilotrees.util.StringUtil;

@WebServlet("/DeviceLogMessage")
public class DeviceLogMessage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static Logger log = Logger.getLogger(actdeviceinfo_service.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String dev_tag = req.getParameter("dev_tag");
		if (StringUtil.isStringEmpty(dev_tag)) {
			dev_tag = req.getHeader("dev_tag");
		}
		if(StringUtil.isStringEmpty(dev_tag)) {
			return;
		}
		
		String log_type = req.getParameter("log_type");
		if (StringUtil.isStringEmpty(log_type)) {
			log_type = req.getHeader("log_type");
		}
		if(StringUtil.isStringEmpty(log_type)) {
			manualDrivenLOG(req, resp);
			return;
		}
		
		try {
			int type = Integer.parseInt(log_type);
			
			String directory = ServerConfig.getConfigJson().optString("client_log_save_path");
			String subDirectory = null;
			if (type == 1) {
				subDirectory = "memory";
			} else if (type == 2) {
				subDirectory = "logcat";
			} else {
				return;
			}
			

			String directoryPath = directory + subDirectory + "/";
			File directoryFile = new File(directoryPath);
			if (!directoryFile.exists()) {
				directoryFile.mkdirs();
			}
			
			String zipFilePath = directoryPath + dev_tag + "_" + new Date().getTime() + ".zip";
			File logFile = new File(zipFilePath);
			FileOutputStream outputStream = new FileOutputStream(logFile);
			
			InputStream inputStream = req.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			
			int length = -1;
			byte[] buffer = new byte[1024 * 100];
			while ((length = dataInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}
			outputStream.flush();
			outputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}

	}
	
	
	private void manualDrivenLOG(HttpServletRequest request, HttpServletResponse response) {
		try {
			JSONObject json = servlet_proc.proc_request(request);
			
			String dev_tag = json.optString("dev_tag");
			if (StringUtil.isStringEmpty(dev_tag)) {
				return;
			}
			
			String msg = json.optString("msg");
			if (StringUtil.isStringEmpty(msg)) {
				return;
			}
			
			devpostmsg log = new devpostmsg();
			log.setDev_tag(dev_tag);
			log.setMessage(msg);
			log.setPosttime(new Date());
			devpostmsgdao.addPostMsgLog(log);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
