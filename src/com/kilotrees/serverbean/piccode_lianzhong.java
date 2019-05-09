package com.kilotrees.serverbean;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.model.bo.error_result;
/**
 * 联众取图片验证码接口。使用v1版本
 */
public class piccode_lianzhong extends ServerBeanBase {
	private static Logger log = Logger.getLogger(piccode_lianzhong.class);
	static piccode_lianzhong inst;
	static long softwareId = 10231;
	static String softwareSecret = "5PJUuUI870vlsq7ajHt3bJLY4KeO71NUueZJI57n";
	static String username = "dasheng01";
	static String password = "LYGlyg769394";

	static String uploadurl = "http://v1-http-api.jsdama.com/api.php?mod=php&act=upload";


	public piccode_lianzhong() {
		this.serverbeanid = "lianzhong";
		init(0);
	}

	public static piccode_lianzhong getInstance() {

		synchronized (piccode_lianzhong.class) {
			if (inst == null) {
				inst = new piccode_lianzhong();
			}
		}
		return inst;
	}

	@Override
	public void init(int adv_id) {
		// TODO Auto-generated method stub
		//在这里读取帐号和密码等参数，但这里不实现了，直接硬编码算了
	}

	@Override
	public JSONObject handleBeanReqeust(JSONObject _jsoRequest, byte[] content) {
		// TODO Auto-generated method stub
		return uploadpic(_jsoRequest, content);
	}

	static JSONObject uploadpic(JSONObject _jsoRequest, byte[] picdata) {
		try {
			
			int adv_id = _jsoRequest.getInt("adv_id");
			long autoid = _jsoRequest.getLong("autoid");
			String dev_tag = _jsoRequest.getString("dev_tag");
			int type = _jsoRequest.optInt("type");
			String fileName = dev_tag + "_" + autoid + "_" + adv_id;// + ".jpeg";
			writeFile(fileName + ".jpg", picdata);//把传上来的图片写到本地D:\piccode
			JSONObject ret = getlianzhongv1(type,fileName,picdata);
			return ret;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	// 返回: {"result":true,"data":{"id":24512100664,"val":"NHAK"}}
	public static JSONObject getlianzhongv1(int type,String fileName, byte[] fileData) {
		String captchaId = "";
		String piccode = "";
		String BOUNDARY = "---------------------------68163001211748"; // boundary就是request头和上传文件内容的分隔符
		//String str = "http://v1-http-api.jsdama.com/api.php?mod=php&act=upload";
		// String filePath="D:\\codeTemp2.jpg";//本地验证码图片路径
		Map<String, String> paramMap = getParamMap(type);
		JSONObject resp = new JSONObject();
		error_result err = new error_result();
		try {
			URL url = new URL(uploadurl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("content-type", "multipart/form-data; boundary=" + BOUNDARY);
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);

			OutputStream out = new DataOutputStream(connection.getOutputStream());
			// 普通参数
			if (paramMap != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator<Entry<String, String>> iter = paramMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = iter.next();
					String inputName = entry.getKey();
					String inputValue = entry.getValue();
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}
			String filename = fileName + ".jpg";//autoid + ".jpeg";
			// 图片文件
			if (fileData != null) {
				// File file = new File(filePath);
				// String filename = file.getName();
				String contentType = "image/jpeg";// 这里看情况设置
				StringBuffer strBuf = new StringBuffer();
				strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
				strBuf.append(
						"Content-Disposition: form-data; name=\"" + "upload" + "\"; filename=\"" + filename + "\"\r\n");
				strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
				out.write(strBuf.toString().getBytes());
				out.write(fileData);
				
			}
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();

			// 读取URLConnection的响应
			if(connection.getResponseCode() == 200){
				InputStream in = connection.getInputStream();
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				while (true) {
					int rc = in.read(buf);
					if (rc <= 0) {
						break;
					} else {
						bout.write(buf, 0, rc);
					}
				}
				in.close();
				writeFile(fileName + ".json", bout.toByteArray());
				String strresp = new String(bout.toByteArray());
				// 结果输出
				System.out.println(strresp);
				JSONObject temp = new JSONObject(strresp);
				
				if(temp.optBoolean("result") == true)
				{
					JSONObject repData = temp.getJSONObject("data");
					piccode = repData.optString("val");
					captchaId = repData.optString("id");
				}
				else
				{
					err.setErr_code(error_result.get_piccode_error);
					err.setErr_info(temp.optString("data"));
				}
			}
			else
			{
				err.setErr_code(error_result.server_http_error);
				err.setErr_info(uploadurl + ":connection.respcode:" + connection.getResponseCode());
			}
			resp.put("err_result", err.toJSONObject());
			resp.put("piccode", piccode);
			resp.put("captchaId", captchaId);

		} catch (Exception e) {
			log.error(e.getMessage(),e);
			err.setErr_code(error_result.System_exception_error);
			err.setErr_info(e.getMessage());
		}
		return resp;
	}

	/**
	 * 参数信息
	 * 
	 * @return
	 */
	private static Map<String, String> getParamMap(int type) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("user_name", username);
		paramMap.put("user_pw", password);
		if(type == 1318)
		{
			paramMap.put("yzm_minlen", "1");
			paramMap.put("yzm_maxlen", "1");
		}
		else{
		paramMap.put("yzm_minlen", "4");
		paramMap.put("yzm_maxlen", "4");
		}
		//paramMap.put("yzmtype_mark", "0");
		//象滑动卡片这种是用1318类型，之前是0
		paramMap.put("yzmtype_mark", "" + type);
		paramMap.put("zztool_token", "123");

		return paramMap;
	}

	/**
	 * v2.0版本暂时不用
	 * @param picdata
	 * @return
	 * @throws JSONException
	 */
	static JSONObject createRequestJson(byte[] picdata) throws JSONException {
		String base64 = java.util.Base64.getEncoder().encodeToString(picdata);
		JSONObject jso = new JSONObject();
		jso.put("softwareId", softwareId);
		jso.put("softwareSecret", softwareSecret);
		jso.put("username", username);
		jso.put("password", password);
		jso.put("captchaData", base64);
		jso.put("captchaType", 1);
		jso.put("captchaMinLength", 0);
		jso.put("captchaMaxLength", 0);
		jso.put("workerTipsId", 0);
		return jso;
	}

	static void writeFile(String fileName, byte[] fileDate) {
		//暂时写文件中跟踪
		new File("d:/piccode/").mkdirs();
		String filePath = "d:/piccode/" + fileName;//serverconfig.contextRealPath + "files/piccode/";
		//filePath += fileName;
		new Thread() {
			public void run() {
				try {
					FileOutputStream fos = new FileOutputStream(filePath);
					fos.write(fileDate);
					fos.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();
	}

	public static void test1() {

		String BOUNDARY = "---------------------------68163001211748"; // boundary就是request头和上传文件内容的分隔符
		String str = "http://v1-http-api.jsdama.com/api.php?mod=php&act=upload";
		String filePath = "d:/ph/okok.png";// 本地验证码图片路径
		Map<String, String> paramMap = getParamMap(1318);
		try {
			URL url = new URL(str);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("content-type", "multipart/form-data; boundary=" + BOUNDARY);
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);

			OutputStream out = new DataOutputStream(connection.getOutputStream());
			// 普通参数
			if (paramMap != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator<Entry<String, String>> iter = paramMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = iter.next();
					String inputName = entry.getKey();
					String inputValue = entry.getValue();
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}

			// 图片文件
			if (filePath != null) {
				File file = new File(filePath);
				String filename = file.getName();
				String contentType = "image/jpeg";// 这里看情况设置
				StringBuffer strBuf = new StringBuffer();
				strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
				strBuf.append(
						"Content-Disposition: form-data; name=\"" + "upload" + "\"; filename=\"" + filename + "\"\r\n");
				strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
				out.write(strBuf.toString().getBytes());
				DataInputStream in = new DataInputStream(new FileInputStream(file));
				int bytes = 0;
				byte[] bufferOut = new byte[1024];
				while ((bytes = in.read(bufferOut)) != -1) {
					out.write(bufferOut, 0, bytes);
				}
				in.close();
			}
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();

			// 读取URLConnection的响应
			InputStream in = connection.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while (true) {
				int rc = in.read(buf);
				if (rc <= 0) {
					break;
				} else {
					bout.write(buf, 0, rc);
				}
			}
			in.close();
			// 结果输出
			System.out.println(new String(bout.toByteArray()));
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}

	public static void main(String[] argv) {
		// test1();
		byte[] fileData = null;
		long autoid = 123;
		try{
		File f = new File("d:/ph/okok.png");
		fileData = new byte[(int) f.length()];
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		dis.readFully(fileData);
//		Date d1 = new Date();
//		getlianzhongv1(autoid + "", fileData);
//		Date d2 = new Date();
//		System.out.println((d2.getTime() - d1.getTime()) / 1000);
		JSONObject jso = new JSONObject();
		jso.put("dev_tag", "A102");
		jso.put("autoid",113);
		jso.put("adv_id", 5);
		jso.put("type", 1318);
		jso = uploadpic(jso,fileData);
		System.out.println(jso.toString());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void handleTaskParasm(JSONObject jsoTask) throws JSONException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleTaskResport(JSONObject _jsoResponse) throws JSONException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh(int adv_id) {
		// TODO Auto-generated method stub
		
	}
}
