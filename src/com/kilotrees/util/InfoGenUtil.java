package com.kilotrees.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.log.LogFile;
import com.kilotrees.model.po.ServerConfig;

public class InfoGenUtil {
	/**
	 * imei由15位数字组成， 前6位(TAC)是型号核准号码，代表手机类型。 接着2位(FAC)是最后装配号，代表产地。
	 * 后6位(SNR)是串号，代表生产顺序号。 最后1位 (SP)是检验码。
	 * 
	 * 检验码计算： (1).将偶数位数字分别乘以2，分别计算个位数和十位数之和 (2).将奇数位数字相加，再加上上一步算得的值
	 * (3).如果得出的数个位是0则校验位为0，否则为10减去个位数
	 * 
	 * @author sonzer
	 * 
	 */
	public static String genImei() {
		int r1 = 1000000 + new java.util.Random().nextInt(9000000);
		int r2 = 1000000 + new java.util.Random().nextInt(9000000);
		String input = r1 + "" + r2;
		char[] ch = input.toCharArray();
		int a = 0, b = 0;
		for (int i = 0; i < ch.length; i++) {
			int tt = Integer.parseInt(ch[i] + "");
			if (i % 2 == 0) {
				a = a + tt;
			} else {
				int temp = tt * 2;
				b = b + temp / 10 + temp % 10;
			}
		}
		int last = (a + b) % 10;
		if (last == 0) {
			last = 0;
		} else {
			last = 10 - last;
		}
		return input + last;
	}

	public static String genImsi() {
		// 中国移动,cmcc,china mobile; 中国联通 ,china unicom,cucc; 中国电信,china
		// telecom,ctcc
		String title = "4600";
		// 0, 2, 7 移动 ; 1 联通; 3, 6 电信
		int second = (int) randOnSpecScale(new Integer[] { 0, 2, 7, 1, 3, 6 }, new int[] { 30, 10, 10, 25, 10, 15 });
		int third = 0;
		do {
			third = new Random().nextInt(8);
		} while (third == 3);
		int r1 = 10000 + new Random().nextInt(90000);
		int r2 = 10000 + new Random().nextInt(90000);
		return title + "" + second + third + "" + r1 + "" + r2;
	}

	public static String genAndroidID() {
		// HEX
		ArrayList<String> hexList = new ArrayList<String>();
		String hex = "0123456789abcdef";
		for (int i = 0; i < hex.length(); i++) {
			char c = hex.charAt(i);
			String s = String.valueOf(c);
			hexList.add(s);
		}
		Collections.shuffle(hexList);
		
		StringBuffer mBuffer = new StringBuffer();
		for (int i = 0; i < 16; i++) {
			int t = new java.util.Random().nextInt(hexList.size());
			String h = hexList.get(t);
			mBuffer.append(h);
		}
		return mBuffer.toString();
	}

	public static String getMd5(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			// 32位加密
			// return buf.toString();
			// 16位的加密
			return buf.toString().substring(8, 24);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String genACCID() {
		return "";
	}

	public static String genMac() {
		char[] char1 = "abcdef".toCharArray();
		char[] char2 = "0123456789".toCharArray();
		StringBuffer mBuffer = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			int t = new java.util.Random().nextInt(char1.length);
			int y = new java.util.Random().nextInt(char2.length);
			int key = new java.util.Random().nextInt(2);
			if (key == 0) {
				mBuffer.append(char2[y]).append(char1[t]);
			} else {
				mBuffer.append(char1[t]).append(char2[y]);
			}

			if (i != 5) {
				mBuffer.append(":");
			}
		}
		return mBuffer.toString();
	}

	static String[] SSID_Fix = new String[] { "TP-LINK_", "D-LINK_", "XIAOMI_", "360WIFI-", "FAST_", "MERCURY_",
			"CWIFI-", "Pizza Hut", "STARBUCKS", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
			"", "", "", "", "", "", "", "", "", "" };

	private static String gen1(int length) {
		String str = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(str.length());
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	public static String gen2(int length) {
		String str = "0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(str.length());
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	public String genSSID() {
		String SSID = "";
		int t = new Random().nextInt(5);
		if (t == 0) {
			// 纯数字
			SSID = gen2(new Random().nextInt(6 - 3) + 3);
		} else if (t == 1) {
			// 英文+数字
			SSID = gen1(new Random().nextInt(5 - 2) + 2) + gen2(new Random().nextInt(4 - 1) + 1);
		} else {
			// 纯英文
			SSID = gen1(new Random().nextInt(10 - 3) + 3);
		}

		// 大小写
		t = new Random().nextInt(3);
		if (t == 0) {
			SSID = SSID.toUpperCase();
		}

		SSID = SSID_Fix[new Random().nextInt(SSID_Fix.length)] + SSID;
		return SSID;
	}

	// 随机生成名字
	public static String genName() {
		String name = "";
		//
		int letter_len = 3;
		// 生成3-8个字符
		letter_len += new Random().nextInt(6);
		int num_len = new Random().nextInt(5);
		name += gen1(letter_len) + gen2(num_len);
		return name;
	}

	public static int genNetworkType() {
		return (Integer) randRatioValue("kNetworkType", new Integer[] { 0, 1 }, new int[] { 50, 50 });
	}

	public static String genCapabilities() {
		JSONObject extConfig = ServerConfig.getConfigJson();
		JSONObject phinfoConfig = extConfig.optJSONObject("phone_info");
		JSONArray capabilities = phinfoConfig.optJSONArray("kWIFI.Capabilities");

		String result = "";
		int length = new Random().nextInt(capabilities.length());
		for (int i = 0; i < length; i++) {
			result += capabilities.optString(i);
		}

		String[] lastest = new String[] { "[WPS]", "[ESS]" };
		result += new Random().nextInt(10) == 0 ? lastest[0] : lastest[1];
		return result;
	}

	public static Object randRatioValue(String defKey, Object[] defValues, int[] defScales) {
		JSONObject extConfig = ServerConfig.getConfigJson();
		JSONObject phinfoConfig = extConfig.optJSONObject("phone_info");

		String valuesKey = defKey + ".Values";
		String ratiosKey = defKey + ".Ratios";
		JSONArray values = phinfoConfig.optJSONArray(valuesKey);
		JSONArray ratios = phinfoConfig.optJSONArray(ratiosKey);

		if (values != null && ratios != null) {
			Object[] valueArray = new Object[values.length()];
			for (int i = 0; i < values.length(); i++) {
				valueArray[i] = values.opt(i);
			}

			int[] ratioArray = new int[ratios.length()];
			for (int i = 0; i < ratios.length(); i++) {
				ratioArray[i] = ratios.optInt(i);
			}

			return randOnSpecScale(valueArray, ratioArray);
		}

		return randOnSpecScale(defValues, defScales);
	}

	/**
	 * 从几个设定比重可选项中随机生成某个可选值，比如我们随机选择运营商(移动，联通，电信)，在配置文件中设定移动占比重为60,联通占15,电信占25
	 * 那么我们在100内随机一个数，如果数值在0-59则为移动，如果是60-74则为联通，75-99为电信 返回第二个随机数序号
	 * 
	 * @param scales
	 * @return
	 */
	public static Object randOnSpecScale(Object[] values, int[] scales) {
		int c = scales.length;
		int sum = 0;
		for (int i = 0; i < scales.length; i++)
			sum += scales[i];
		if (sum != 100)
			System.out.println("randOnSpecScale 几个数值比重总和不为100");
		int r = new Random().nextInt(100);

		int s = 0;
		sum = 0;
		while (s < c) {
			sum += scales[s];
			if (r < sum - 1)
				break;
			s += 1;
		}
		// System.out.println("r = " + r + ";s=" + s);
		if (s >= values.length) {
			s = values.length - 1;
		}

		return values[s];
	}
    /**
     * 加个日志测试一下是否随机有问题。
     * @param values
     * @param scales
     * @param logFile
     * @return
     */
	public static Object randOnSpecScale(Object[] values, int[] scales, String logFile) {
		int c = scales.length;
		int sum = 0;
		for (int i = 0; i < scales.length; i++)
			sum += scales[i];
		if (sum != 100)
			System.out.println("randOnSpecScale 几个数值比重总和不为100");
		int r = new Random().nextInt(100);

		int s = 0;
		sum = 0;
		while (s < c) {
			sum += scales[s];
			if (r < sum - 1)
				break;
			s += 1;
		}
		// System.out.println("r = " + r + ";s=" + s);
		if (s >= values.length) {
			s = values.length - 1;
		}
		// 写log跟踪一下r的值
		if (logFile.length() > 0) {
			String log = "";
			for (int i = 0; i < scales.length; i++)
				log += scales[i] + "-";
			log += "r=" + r + ";s=" + s + ";v=" + values[s];
			LogFile.writeLogFile(logFile, log + "\r\n");
		}
		return values[s];
	}
	
	/**
	 * 
	 * 严格按几个设定比重项随机,外部调用时，一定要同步scales这个对象
	 * 由于上面的randOnSpecScale的随机性不是很好，这里改进一下，把项目值放到HashMap中随机，如果选中某值，则减1，如果为0时，
	 * 从HashMap中删除它，下次只能从剩下的随机。
	 * @param scales
	 *            把条目序号和对应的比重值加到HashMap中
	 * @param org
	 *            原始比重,当scales用光后，把原始Map拷到scales中。
	 * @return
	 */
	static boolean test_randOnSpecScaleConst = false;
	public static Object randOnSpecScaleConst(HashMap<Object, Integer> scales, HashMap<Object, Integer> org) {
		Object ret = null;
		int size = scales.size(); //map(RegionRand,int)
		int r = new Random().nextInt(size);
		Object[] v = new Object[size];
		scales.keySet().toArray(v);
		ret = v[r];   //RegionRand(min,max)
		Integer value = scales.get(ret);
		value -= 1;
		if (value == 0) {
			//System.out.println("key=" + ret);
			scales.remove(ret);
			if (scales.size() == 0) {
				scales.putAll(org); //scales.putAll((HashMap<Integer, Integer>) org.clone());
			}
		} else
			scales.put(ret, value);
		if(test_randOnSpecScaleConst){
			String log = "scales=" + scales + ";ret=" + ret + ":" + testPrint(scales);
			LogFile.writeLogFile("D:/work/randOnSpecScale.txt", log + "\r\n");
		}
		return ret;
	}
	
	static String  testPrint(HashMap<Object, Integer> v) {
		//对hashMap来说，hascode好象没有意义
		String s = "";//+ v.hashCode() + ";";
		Iterator<Map.Entry<Object, Integer>> it = v.entrySet().iterator();
		for (; it.hasNext();) {
			Map.Entry<Object, Integer> e = it.next();
			Object key = e.getKey();
			Object value = e.getValue();
			s += "" + key + ":" + value + ";";
		}
		//System.out.println();
		return s;
	}

	private static JSONObject __ipIntJson__ = null;
	private static JSONObject __intIpJson__ = null;
	private static List<Integer> __ipIntList__ = null;

	public synchronized static Integer getOneRandomIntIP() {
		try {
			if (__ipIntList__ == null) {
				String fileName = ServerConfig.contextRealPath + "\\files\\extjson/IpToInt.json";
				String ipJson = FileUtil.read(fileName);

				__ipIntJson__ = new JSONObject(ipJson);
				__intIpJson__ = new JSONObject();
				__ipIntList__ = new ArrayList<>();

				Iterator<?> keys = __ipIntJson__.keys();
				List<String> names = new ArrayList<>();
				while (keys.hasNext()) {
					names.add((String) keys.next());
				}

				// sort the IP
				names.sort(new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						String values1[] = o1.split("\\.");
						String values2[] = o2.split("\\.");
						int len = values1.length;
						for (int i = 0; i < len; i++) {
							String ele1 = values1[i];
							String ele2 = values2[i];
							Integer num1 = Integer.parseInt(ele1);
							Integer num2 = Integer.parseInt(ele2);
							if (num1.intValue() != num2.intValue()) {
								return num1.compareTo(num2);
							}
						}
						return o1.compareTo(o2);
					}
				});

				for (int i = 0; i < names.size(); i++) {
					String ipStr = (String) names.get(i);
					Integer intVal = __ipIntJson__.optInt(ipStr);
					__ipIntList__.add(intVal);
					__intIpJson__.put(String.valueOf(intVal), ipStr);
				}
			}
			int index = new Random().nextInt(500);
			int ipIntResult = __ipIntList__.get(index);
			return ipIntResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (new Random().nextInt(2) == 1) ? 83994816 : -2080200512;
	}

	public static Integer getGatewayFromIntIP(Integer intVal) {
		String ipStr = __intIpJson__.optString(String.valueOf(intVal));
		String values[] = ipStr.split("\\.");
		String maskIp = "";
		for (int i = 0; i < values.length; i++) {
			if (i == values.length - 1) {
				maskIp = maskIp + "1";
			} else {
				maskIp = maskIp + values[i] + ".";
			}
		}
		Integer maskIpInt = __ipIntJson__.optInt(maskIp);
		return maskIpInt;
	}

	public static void main(String[] args) {
		int[] sc = new int[3];
		sc[0] = 60;
		sc[1] = 15;
		sc[2] = 25;
		for (int i = 0; i < 20; i++) {
			System.out.println((Integer) randOnSpecScale(new Integer[] { 1, 2, 3 }, sc));
		}

		ServerConfig.contextRealPath = "E:\\J2EE-eclipse-workspace\\zfyuncontrol\\WebContent";
		Integer randomIp = getOneRandomIntIP();
		Integer maskIp = getGatewayFromIntIP(randomIp);
		System.out.println(maskIp);
	}
}
