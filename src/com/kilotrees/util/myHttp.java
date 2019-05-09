package com.kilotrees.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class myHttp {

	// 暂时不支持chunk
	// 对字符集没有处理
	// 禁止在真正的app程序使用，只是內部使用，因為沒用測試過�?�用性�?�有好多http頭的情況無處�?
	// 302等跳转无处理

	static long TRYSLEEPTIME = 3 * 1000;

	HashMap<String, String> headrsp;
	HashMap<String, String> head = new HashMap<String, String>();

	int respcode;
	String protocol;
	String error;
	InputStream isSocket;
	OutputStream osSocket;
	URL url;
	int timeoutcon = 10 * 1000;
	int timeso = 50 * 1000;
	Socket sk;
	boolean bolEnd = false;
	byte[] content = null;

	private void trySleep() {
		try {
			Thread.sleep(TRYSLEEPTIME);
		} catch (Exception e) {

		}
	}

	public myHttp() {
		// TODO Auto-generated constructor stub
	}

	private void reset() {
		headrsp = new HashMap<String, String>();
		respcode = -1;
		protocol = "";
		error = "";
		isSocket = null;
		osSocket = null;
		url = null;
		sk = null;
		bolEnd = false;
		content = null;
	}

	public int getResponCode() {
		return respcode;
	}

	public void setTimeout(int _timeoutcon, int _timeso) {
		timeoutcon = _timeoutcon;
		timeso = _timeso;
	}

	/**
	 * 添加head信息（"Connection", "Close"）
	 */
	private void addDefaultHead() {
		addHead("Connection", "Close");
	}

	/**从socket输入流isSocket中读取响应头信息
	 * @throws Exception
	 */
	private void getHead() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int[] hb = new int[] { -1, -1, -1, -1 };
		while (true) {
			int b = isSocket.read();
			if (b == -1)
				break;
			bos.write(b);
			hb[0] = hb[1];
			hb[1] = hb[2];
			hb[2] = hb[3];
			hb[3] = b;
			if (hb[0] == '\r' && hb[1] == '\n' && hb[2] == '\r'
					&& hb[3] == '\n') {
				break;
			}
		}
		String hd = new String(bos.toByteArray());
		bos.close();
		/*   hd: protocol 202 [error]
		 *       content-type: json
		 *		 accept-type: json	
		 */ 	 
		String[] hds = hd.split("\r\n");
		String[] sline = hds[0].split(" ");
		protocol = sline[0];
		respcode = Integer.parseInt(sline[1]);
		if (sline.length >= 3) {
			error = sline[2];
		} else {
			error = "none";
		}
		for (int i = 1; i < hds.length; i++) {
			sline = hds[i].split(":");
			headrsp.put(sline[0].trim(), sline[1].trim());
		}
	}

	public void addHead(String key, String value) {
		head.put(key, value);
	}

	/**开始socket连接，得到inputStream\outputStream
	 * @throws Exception
	 */
	private void connect() throws Exception {
		sk = new Socket();
		SocketAddress address = new InetSocketAddress(url.getHost(),
				url.getPort() == -1 ? 80 : url.getPort());
		System.out.println("connect to " + address);
		sk.setSoTimeout(timeso);
		sk.connect(address, timeoutcon);
		isSocket = sk.getInputStream();
		osSocket = sk.getOutputStream();
	}

	/**采用指定方式，初始化的URL开始通讯
	 * @param method
	 * @throws Exception
	 */
	private void postHead(String method) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String s = method + " " + (url.getPath() == "" ? "/" : url.getPath())
				+ (url.getQuery() != null ? ("?" + url.getQuery()) : "")
				+ " HTTP/1.1";
		bos.write(s.getBytes());
		bos.write("\r\n".getBytes());
		bos.write(("Host:" + url.getHost()).getBytes());
		bos.write("\r\n".getBytes());
		Iterator<String> it = head.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = head.get(key);
			bos.write(key.getBytes());
			bos.write(":".getBytes());
			bos.write(value.getBytes());
			bos.write("\r\n".getBytes());
		}
		bos.write("\r\n".getBytes());
		osSocket.write(bos.toByteArray());
		bos.close();
		osSocket.flush();
		// os.close();
	}

	/**返回Head里的value，value必须是数字，否则返回默认
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public long optHeadLong(String name, long defaultValue) {
		if (headrsp == null) {
			return 0;
		}
		String r = headrsp.get(name);
		if (r == null) {
			return defaultValue;
		}
		if (r.matches("\\d+")) {
			return Long.parseLong(r);
		}
		return defaultValue;
	}

	public String optHeadString(String name, String defaultValue) {
		if (headrsp == null) {
			return null;
		}
		String r = headrsp.get(name);
		if (r != null) {
			return r;
		}
		return defaultValue;
	}

	public void close() {
		try {
			isSocket.close();
		} catch (Exception e) {
		}
		try {
			osSocket.close();
		} catch (Exception e) {
		}

		try {
			sk.close();
		} catch (Exception e) {
		}
	}

	public boolean downLoadFile(String _url, String filename, boolean _append) {

		// httpRequest.setHeader("Range", "bytes=" + curReadLen + "-");
		RandomAccessFile rf = null;
		try {

			rf = new RandomAccessFile(filename, "rw");
			if (_append) {
				rf.seek(rf.length());
			} else {
				rf.setLength(0);
				rf.seek(0);
			}

			reset();
			url = new URL(_url);
			connect();
			addDefaultHead();
			if (_append) {
				addHead("Range", "bytes=" + rf.length() + "-");
				System.out.println("set range " + rf.length());
			}
			postHead("GET");
			getHead();

			boolean result = false;

			if (getResponCode() == 200 || getResponCode() == 206) {
				long ctlen = 0;
				if (_append) {
					ctlen = optHeadLong("Content-Range", Long.MAX_VALUE);
					System.out.println("get Content-Range=" + ctlen);
					if (ctlen == Long.MAX_VALUE) {
						ctlen = optHeadLong("Content-Length", Long.MAX_VALUE);
						System.out.println("get Content-Length=" + ctlen);
					}
				} else {
					ctlen = optHeadLong("Content-Length", Long.MAX_VALUE);
					System.out.println("get Content-Length=" + ctlen);
				}
				if (ctlen > 0) {
					byte[] buff = new byte[1024 * 100];
					while (ctlen > 0) {
						int len = isSocket.read(buff);
						if (len == -1)
							break;
						rf.write(buff, 0, len);
						ctlen -= len;
					}
				}
				if (ctlen == 0) {
					result = true;
				}
			}
			
			rf.close();
			rf = null;

			return result;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (rf != null)
					rf.close();
			} catch (Exception e) {

			}
			close();
		}
		bolEnd = true;
		return false;
	}

	/**根据_url开始socket通讯，返回响应字节
	 * @param _url
	 * @return
	 */
	public byte[] getUrl(String _url) {
		try {
			reset();
			url = new URL(_url);
			connect();
			addDefaultHead();
			postHead("GET");
			getHead();
			byte[] r = readContent();
			return r;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			close();
		}
		bolEnd = true;
		return null;
	}

	/**读取socket响应输入流
	 * @return
	 * @throws Exception
	 */
	private byte[] readContent() throws Exception {
		long ctlen = optHeadLong("Content-Length", Long.MAX_VALUE);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buff = new byte[1024*100];
		while (ctlen > 0) {
			int len = isSocket.read(buff);
			if (len == -1)
				break;
			bos.write(buff, 0, len);
			ctlen -= len;
		}
		byte[] r = bos.toByteArray();
		bos.close();
		content = r;
		return r;
	}

	public byte[] postContent(String _url, byte[] buffPost) {
		try {

			if (buffPost == null || buffPost.length == 0) {
				return null;
			}

			reset();
			url = new URL(_url);
			connect();
			addDefaultHead();
			addHead("Content-Length", String.valueOf(buffPost.length));
			postHead("POST");

			osSocket.write(buffPost);
			
			getHead();
			byte[] r = readContent();

			return r;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			close();
		}
		bolEnd = true;
		return null;
	}

	/*
	 * 上传文件
	 */
	public boolean postFile(String _url, String filename) {
		return postFile(_url, filename, false, 1);
	}

	/*
	 * 上传文件，并且续传，但要服务器配合操�?
	 */
	public boolean postFile(String _url, String filename, boolean _append,
			int _trycount) {
		try {
			Long[] istart = new Long[] { 0L };
			if (_append == false) {
				FileInputStream fis = new FileInputStream(filename);
				postStream(_url, fis, fis.available(), null);
				fis.close();
				return getResponCode() == 200 ? true : false;
			} else {
				boolean r = false;

				long maxLen = new File(filename).length();

				while (_trycount > 0 && istart[0] < maxLen) {
					long lastistart = istart[0];
					_trycount--;
					System.out.println("istart=" + istart[0] + " _trycount="
							+ _trycount + " maxLen=" + maxLen);
					FileInputStream fis = new FileInputStream(filename);
					fis.skip(istart[0]);
					postStream(_url, fis, (int) maxLen, istart);
					fis.close();
					// 位置不匹�?
					if (getResponCode() == 904) {
						istart[0] = optHeadLong("bytes", 0);
						System.out.println("reset istart " + istart[0]);
						continue;
					}
					if (getResponCode() != 200) {
						istart[0] = lastistart;
						System.out.println("wait 10 sec");
						Thread.sleep(10000);
					}
				}

				if (istart[0] == maxLen && getResponCode() == 200) {
					r = true;
				}

				return r;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		bolEnd = true;
		return false;
	}

	public byte[] postStream(String _url, InputStream _isPost, int _islen,
			Long[] _istart) {

		try {

			reset();
			url = new URL(_url);
			connect();
			addDefaultHead();
			addHead("Content-Length", String.valueOf(_islen));
			if (_istart != null) {
				addHead("bytes", String.valueOf(_istart[0]));
			}
			postHead("POST");

			byte[] buff = new byte[1024*100];
			while (true) {
				int len = _isPost.read(buff);
				if (len == -1)
					break;
				osSocket.write(buff, 0, len);
				if (_istart != null) {
					_istart[0] += len;
				}
			}
			osSocket.flush();

			getHead();

			byte[] r = readContent();
			return r;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			close();
		}

		return null;
	}

	public long getContentLen() {
		return optHeadLong("Content-Length", -1);
	}

	private static void test(Long a) {
		a = 10L;
	}

	public static void main(String[] args) {
		// myHttp ht = new myHttp();
		// String r =
		// ht.getUrl("http://www.ip138.com");

		// myhttp ht = new myhttp();
		// String r = ht
		// .postFile(
		// "http://127.0.0.1:8080/adweb/zip?packageName=test&autoid=1234&tag=pho12",
		// "f:\\LJM1130_M1210_MFP_Full_Solution.exe");

		// myhttp ht = new myhttp();
		// boolean r = ht.downLoadFile(
		// "http://127.0.0.1:8080/adweb/data/gameWeb.dat", "d:\\test.dat",
		// true);

		// System.out.println(ht.respcode + " " + ht.getContentLen());
		// System.out.println(r);

	}

	public byte[] getContent() {
		return content;
	}

	/**socket通讯结果200 就是true，否则false
	 * @param _url
	 * @param _trycount
	 * @return
	 */
	public boolean getUrlSynTry(String _url, int _trycount) {
		while (_trycount > 0) {
			_trycount--;
			getUrl(_url);
			if (getResponCode() == 200) {
				return true;
			}
			if (_trycount > 0) {
				trySleep();
			}
		}
		return false;
	}

	public boolean downloadFileSyn(String _url, String _filename,
			boolean _append, int _trycount) {
		while (_trycount > 0) {
			_trycount--;
			if (downLoadFile(_url, _filename, _append)) {
				return true;
			}
			if (_trycount > 0) {
				trySleep();
			}
		}
		return false;
	}

	public int getHttpstatus() {
		return getResponCode();
	}

	public boolean postContentSyn(String _url, byte[] _postBuff, int _ctrycount) {
		while (_ctrycount > 0) {
			_ctrycount--;
			byte[] ret=postContent(_url, _postBuff);
			String result="nu";
			if (ret!=null) {
				result=new String(ret);
			}
			System.out.println("postContentSyn :post result to :"+_url+" "+getResponCode() +" ret:"+result );
			if (getResponCode() == 200) {
				return true;
			}
			if (_ctrycount > 0) {
				trySleep();
			}
		}
		return false;
	}

	public boolean postFileSyn(String _url, String _filename, int _trycount,boolean _append) {
		
			if (postFile(_url, _filename, _append, _trycount)) {
				return true;
			}		
		return false;
	}

	public void setBuffSize(int i) {
		// TODO Auto-generated method stub

	}

}
