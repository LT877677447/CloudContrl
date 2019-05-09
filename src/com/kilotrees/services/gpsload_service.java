package com.kilotrees.services;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 生成gps的路径是时间节点的经纬度值
 *
 */
public class gpsload_service {
	private static Logger log = Logger.getLogger(gpsload_service.class);
	/**
	 * 测量方法来自百度地图
	 *
	 */
	private static double DEF_PI = Math.PI; // 3.14159265359; // PI
	private static double DEF_2PI = 2 * DEF_PI; // 6.28318530712; // 2 * PI
	private static double DEF_PI180 = DEF_PI / 180.0; // 0.01745329252; //
														// PI/180.0
	private static double DEF_R = 6370693.5; // radius of earth
	private static gpsload_service inst;

	private gpsload_service() {

	}

	public static gpsload_service getInstance() {
		synchronized (gpsload_service.class) {
			if (inst == null) {
				inst = new gpsload_service();
			}
		}
		return inst;
	}

	/**
	 * 对于一般的广告apk来说，在周围300米左右随机变动, 按相距 0.001
	 * 个单位纬度，相当于大概相距100米，0.001个经度，大概是90米大致来算
	 * 我们按一个广告5分钟时长来算，由东西走一条直线，每秒在经度上递增0.5-1.5，在纬度上随机正负0.5米 用json把每秒生成的经纬度返回给客户端
	 * 
	 * @param ip
	 * @param lon0
	 * @param lat0
	 * @return
	 * @throws JSONException
	 */
	public JSONObject randPoints_1(int adv_id, String ip, double lon0, double lat0)// throws
																					// JSONException
	{
		try {
			JSONObject jsonRet = new JSONObject();
			// 总时间
			int t = 60 * 20;
			// 把角度转成弧度
			double ns = DEF_PI180 * lat0;
			// 当前纬度半径
			double cur_r = DEF_R * Math.cos(ns);
			// 按经度东西走一米的弧度
			double nlon = 1.0 / cur_r;
			// 转成角度
			double alon = nlon / DEF_PI180;
			// 按南北一米的弧度
			double nlat = 1.0 / DEF_R;
			// 转成角度
			double alat = nlat / DEF_PI180;
			// 开始走动
			double lon1 = lon0;
			double lat1 = lat0;
			JSONArray jarray = new JSONArray();
			for (int i = 0; i < t; i++) {
				// 每秒经度增加0.5-1.5米，纬度增加-0.5-0.5米
				java.util.Random rand = new java.util.Random();
				double l = 0.1 * (5 + rand.nextInt(11));
				l *= alon;
				lon1 += l;
				// 纬度随机增加正负0.5米
				int r = rand.nextInt(3) + 1;
				r -= 2;
				double d = 0.5 * r;
				d = d * alat;
				lat1 += d;
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("diffsec", i + 1);
				jsonItem.put("lon", lon1);
				jsonItem.put("lat", lat1);
				jarray.put(jsonItem);
			}
			jsonRet.put("location_path", jarray);
			return jsonRet;
		} catch (JSONException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 适用于跑步类软件，计算出路径，返回每秒的经纬度,这里以原始经纬度为顶点构建一个四方形。
	 * 
	 * @param ip
	 * @param lon0
	 * @param lat0
	 * @return
	 */
	public JSONObject randPoints_2(String ip, double lon0, double lat0) {
		JSONObject jsonRet = new JSONObject();

		return jsonRet;
	}

	/**
	 * 返回为m，适合短距离测量
	 */
	public static double getShortDistance(double lon1, double lat1, double lon2, double lat2) {
		double ew1, ns1, ew2, ns2;
		double dx, dy, dew;
		double distance;
		// 角度转换为弧度
		ew1 = lon1 * DEF_PI180;
		ns1 = lat1 * DEF_PI180;
		ew2 = lon2 * DEF_PI180;
		ns2 = lat2 * DEF_PI180;
		// 经度差
		dew = ew1 - ew2;
		// 若跨东经和西经180 度，进行调整
		if (dew > DEF_PI)
			dew = DEF_2PI - dew;
		else if (dew < -DEF_PI)
			dew = DEF_2PI + dew;
		dx = DEF_R * Math.cos(ns1) * dew; // 东西方向长度(在纬度圈上的投影长度)
		dy = DEF_R * (ns1 - ns2); // 南北方向长度(在经度圈上的投影长度)
		// 勾股定理求斜边长
		distance = Math.sqrt(dx * dx + dy * dy);
		return distance;
	}

	/**
	 * 返回为m,适合长距离测量
	 */
	public static double getLongDistance(double lon1, double lat1, double lon2, double lat2) {
		double ew1, ns1, ew2, ns2;
		double distance;
		// 角度转换为弧度
		ew1 = lon1 * DEF_PI180;
		ns1 = lat1 * DEF_PI180;
		ew2 = lon2 * DEF_PI180;
		ns2 = lat2 * DEF_PI180;
		// 求大圆劣弧与球心所夹的角(弧度)
		distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);
		// 调整到[-1..1]范围内，避免溢出
		if (distance > 1.0)
			distance = 1.0;
		else if (distance < -1.0)
			distance = -1.0;
		// 求大圆劣弧长度
		distance = DEF_R * Math.acos(distance);
		return distance;
	}

	private static String trans(double distance) {
		boolean isBig = false; // 是否为大于等于1000m
		if (distance >= 1000) {
			distance /= 1000;
			isBig = true;
		}
		return (new DecimalFormat(".00").format(distance)) + (isBig ? "千米" : "米");
	}

	public static void main(String[] argv) {
		double v = Math.sin(DEF_PI * 30 / 180);
		System.out.println(v);

		for (int i = 0; i < 10; i++) {
			java.util.Random rand = new java.util.Random();
			int r = rand.nextInt(3) + 1;
			r -= 2;
			double d = 0.5 * r;
			double l = 0.1 * (5 + rand.nextInt(11));
			System.out.println(l);
		}
	}
}
