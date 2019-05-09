package com.kilotrees.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import com.kilotrees.dao.connectionmgr;

public class CommonUtil {

	/**
	 * 排序timeline数组，从大到小返回下标数组
	 * 
	 * @param timeLine
	 *            要排序的timeline数组
	 * @return 元素由大到小的下标数组
	 */
	public static int[] sortTimeLine(int[] timeLine) {
		int[] arrayAfterOrder = new int[timeLine.length];
		int out, in, max, indexOfMax;
		int[] tempArray = new int[timeLine.length];
		for (int i = 0; i < timeLine.length; i++) {
			tempArray[i] = timeLine[i];
		}

		for (out = 0; out < tempArray.length; out++) {
			max = tempArray[0];
			indexOfMax = 0;
			for (in = 1; in < tempArray.length; in++) {
				if (max < tempArray[in]) {
					max = tempArray[in];
					indexOfMax = in;
				}
			}
			arrayAfterOrder[out] = indexOfMax;
			tempArray[indexOfMax] = -1;
		}
		return arrayAfterOrder;
	}
	
}
