/**
 * @author Administrator
 * 2019年1月19日 下午6:19:21 
 */
package com.kilotrees.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.kilotrees.model.po.tb_weixinaccount;
import com.kilotrees.model.po.tb_weixinlink;
import com.kilotrees.services.WeixinService;
import com.kilotrees.util.DESUtil;
import com.kilotrees.util.StringUtil;

@WebServlet("/WeixinServlet")
public class WeixinServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private WeixinService service = WeixinService.getInstance();

	public static final String confidentialDirecotry = "D:/WebServerPrivateConfidential/";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		byte[] encryptResponseBytes = new byte[1];
		
		try {
			resp.setContentType("text/html;charset=utf-8");
			resp.setCharacterEncoding("utf-8");

			// get post bytes
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = req.getInputStream();
			int length = -1;
			byte[] buffer = new byte[1024];
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();

			// xor
			byte[] requestBytes = out.toByteArray();
			int len = requestBytes.length;
			for (int j = len - 1; j >= 0; j--) {
				if (j == len - 1) {
					requestBytes[j] = (byte) (requestBytes[j] ^ requestBytes[0]);
				} else {
					requestBytes[j] = (byte) (requestBytes[j] ^ requestBytes[j + 1]);
				}
			}

			// get post string
			String jsonString = new String(requestBytes);
			JSONObject requestJson = new JSONObject(jsonString);

			JSONObject responseJson = new JSONObject();
			String errorMessage = null;

			if (!checkRequestJson(requestJson, "action")) {
				errorMessage = "request missing parameter [action] or request is not json format";
			} else if (checkRequestJson(requestJson, "error_message")) {
				errorMessage = requestJson.optString("error_message");
				service.writeLog("error", errorMessage, "客户端主动上传的error", new Date());
			}

			if (errorMessage != null) {
				responseJson.put("errorMessage", errorMessage);
				encryptResponseBytes = responseJson.toString().getBytes();

			} else {

				String action = requestJson.optString("action");
				String strAccount = requestJson.optString("account");
				//only wxid_rqdzi0vhv7h222 wxid_3kehd9ys2m1922
				if(strAccount.equals("wxid_rqdzi0vhv7h222") || strAccount.equals("wxid_u7j58r3t6lqp22")) {
					return;
				}
				
				// 1. action is links
				if (action.equals("links") && !StringUtil.isStringEmpty(strAccount)) {
					tb_weixinaccount account = service.getSingleAccount(strAccount);

					if (account == null) {
						account = service.createAccount(strAccount);
						if (account == null) {
							service.writeLog("error", "第一次登录的微信号，创建Account失败", "", new Date());
							responseJson.put("errorMessage", "Server create Account fail");
						}
					}

					if (account != null) {
						// 先处理上次的结果，再拿link
						String str_brush_status = requestJson.optString("brush_status");
						JSONObject brush_status = new JSONObject();
						if(!StringUtil.isStringEmpty(str_brush_status)) {
							brush_status = new JSONObject(str_brush_status);
						}
						
						int successCount = 0;
						int failedCount = 0;
						Iterator it = brush_status.keys();
						while (it.hasNext()) {
							// key : link value:true false null
							String strLink = (String) it.next();
							tb_weixinlink link = service.getSingleLink(strLink);
							if(link == null) {
								continue;
							}
							
							Object value = brush_status.opt(strLink);
							boolean success = false;
							if(value instanceof String ) {
								String str = (String) value;
								if(str.equals("null")) {
									success = false;
								}
							}
							if(value instanceof Boolean) {
								Boolean boo = (Boolean) value;
								if(boo) {
									success = true;
								}else {
									success = false;
								}
							}
							if(success) {
								//去fail表删除记录，link阅读数+1，判断是否全部做完了，
								service.handleSuccess(account,link);
								successCount++;
							}else {
								//放入fail表
								service.handleFail(account, link);
								failedCount++;
							}
						}
						
						if (!StringUtil.isStringEmpty(str_brush_status)) {
							brush_status = new JSONObject(str_brush_status);
							String init = brush_status.optString("init");
							service.writeLog("info", "init : "+init + ", successCount:" + successCount + " ,failedCount:" + failedCount, "account : "+account.getAccount(), new Date());
						}
						responseJson = service.fetchLink(account);
					}
					encryptResponseBytes = responseJson.toString().getBytes();

				}

				// 2. action is script
				if (action.equals("script")) {
					String outterToken = requestJson.optString("token");
					String encInnerJsonString = requestJson.optString("content");

					byte[] outterDesKey = translateKey(16, outterToken);
					encInnerJsonString = encInnerJsonString.replaceAll("\r|\n", "");
					encInnerJsonString = encInnerJsonString.replace("\\s", "");
					encInnerJsonString = encInnerJsonString.replace("\n", "");
					String innerJsonString = DESUtil.decryptString(encInnerJsonString, outterDesKey);
					JSONObject innerJsonObject = new JSONObject(innerJsonString);
					String innerScriptFileToken = innerJsonObject.optString("token");
					String fileName = innerJsonObject.optString("file");
					byte[] innerScriptDesKey = translateKey(16, innerScriptFileToken);
					String fullFileName = confidentialDirecotry + fileName;

					File scriptFile = new File(fullFileName);
					FileInputStream scriptFileInputStream = new FileInputStream(scriptFile);

					byte[] bufferOut = new byte[1024];
					ByteArrayOutputStream scriptByteArray = new ByteArrayOutputStream();
					int len2 = 0;
					while ((len2 = scriptFileInputStream.read(bufferOut)) != -1) {
						scriptByteArray.write(bufferOut, 0, len2);
					}
					scriptFileInputStream.close();
					scriptByteArray.close();
					byte[] scriptBytes = scriptByteArray.toByteArray();

					encryptResponseBytes = DESUtil.encrypt(scriptBytes, innerScriptDesKey);
				}

				if (encryptResponseBytes.length == 1) {
					service.writeLog("error", "传入的account为空", "", new Date());
					responseJson.put("errorMessage", "the incoming account is null or ''");
					encryptResponseBytes = responseJson.toString().getBytes();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			encryptResponseBytes = e.toString().getBytes();
		}

		for (int j = 0; j < encryptResponseBytes.length; j++) {
			if (j == encryptResponseBytes.length - 1) {
				encryptResponseBytes[j] = (byte) (encryptResponseBytes[j] ^ encryptResponseBytes[0]);
			} else {
				encryptResponseBytes[j] = (byte) (encryptResponseBytes[j] ^ encryptResponseBytes[j + 1]);
			}
		}

		resp.setContentLength(encryptResponseBytes.length);
		resp.getOutputStream().write(encryptResponseBytes);

	}
	
	private boolean checkRequestJson(JSONObject reqJson, String key) {
		if (reqJson == null || StringUtil.isStringEmpty(reqJson.optString(key))) {
			return false;
		}
		return true;
	}

	public static byte[] translateKey(int length, String key) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < length; i++) {
			char c = key.charAt(i);
			int index = (int) c;

			char s = key.charAt(index);
			builder.append(s);
		}

		String string = builder.toString();
		char[] chars = string.toCharArray();

		// char array to byte array
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) {
			bytes[i] = (byte) (0xFF & (int) chars[i]);
		}

		int len = bytes.length;
		for (int j = 0; j < len; j++) {
			if (j == len - 1) {
				bytes[j] = (byte) (bytes[j] ^ bytes[0]);
			} else {
				bytes[j] = (byte) (bytes[j] ^ bytes[j + 1]);
			}
		}

		return bytes;
	}

	public static byte[] charArrayToByteArray(char[] c_array) {
		byte[] b_array = new byte[c_array.length];
		for (int i = 0; i < c_array.length; i++) {
			b_array[i] = (byte) (0xFF & (int) c_array[i]);
		}
		return b_array;
	}

}
