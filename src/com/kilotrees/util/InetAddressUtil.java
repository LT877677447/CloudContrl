package com.kilotrees.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public class InetAddressUtil {

	public static String getLANIPAddress() {
		try {
			
			List<String> ipList = new ArrayList<String>();
			
			// 遍历所有的网络接口
			for (Enumeration<?> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// 在所有的接口下再遍历IP
				for (Enumeration<?> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					// 排除loopback类型地址
					if (!inetAddr.isLoopbackAddress()) {
						if (inetAddr.isSiteLocalAddress()) {
							// 如果是site-local地址，就是它了
							String ip = inetAddr.getHostAddress();
							ipList.add(ip);
						}
					}
				}
			}
			
			// 1, 只有一个IP地址时，直接返回
			if (ipList.size() == 1) {
				return ipList.get(0);
			}
			
			// 2, 多个IP地址时，排序一下，拿最小的那个来返回
			if (ipList.size() > 1) {
				
				Collections.sort(ipList, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						String[] s1 = o1.split("\\.");
						String[] s2 = o2.split("\\.");
						int size = Math.max(s1.length, s1.length);
						
						for(int i = 0; i < size; i++) {
							String e1 = s1[i];
							String e2 = s2[i];
							if (e1.equals(e2)) {
								continue;
							}
							return Integer.valueOf(e1).compareTo(Integer.valueOf(e2));
						}
						
						return o1.compareTo(o2);
					}
				});
				
				return ipList.get(0);
			}
		
			// 3, 当上面都找不到，用次选方案
			return getLocalHostLANAddress().getHostAddress();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
		try {
			InetAddress candidateAddress = null;
			// 遍历所有的网络接口
			for (Enumeration<?> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// 在所有的接口下再遍历IP
				for (Enumeration<?> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					// 排除loopback类型地址
					if (!inetAddr.isLoopbackAddress()) {
						if (inetAddr.isSiteLocalAddress()) {
							// 如果是site-local地址，就是它了
							if(inetAddr.getHostAddress().equals("192.168.3.116")) {
								
								return inetAddr;
								
							}
							
						} else if (candidateAddress == null) {
							// site-local类型的地址未被发现，先记录候选地址
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (candidateAddress != null) {
				return candidateAddress;
			}
			// 如果没有发现 non-loopback地址.只能用最次选的方案
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null) {
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress;
		} catch (Exception e) {
			UnknownHostException unknownHostException = new UnknownHostException(
					"Failed to determine LAN address: " + e);
			unknownHostException.initCause(e);
			throw unknownHostException;
		}
	}
	
}
