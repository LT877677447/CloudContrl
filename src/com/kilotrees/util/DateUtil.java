package com.kilotrees.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	// 取日期的年月日部分
	public static String getShortDateString(java.util.Date d) {
		if (d == null)
			return "null";
		SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
		return fm.format(d);
	}

	public static String getDateString(java.util.Date d) {
		if (d == null)
			return "null";
		SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return fm.format(d);
	}

	public static boolean isSameDate(java.util.Date d1, java.util.Date d2) {
		String sd1 = getShortDateString(d1);
		String sd2 = getShortDateString(d2);
		return sd1.equals(sd2);
	}

	public static String getDateBeginString(java.util.Date d) {
		if (d == null)
			return "null";
		SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
		return fm.format(d);
	}

	/**
	 * @param Date1
	 * @param Date2
	 * @return Date2-Date1 的天数
	 */
	public static int differDayQty(Date Date1, Date Date2) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.clear();
		calendar.setTime(Date1);
		int day1 = calendar.get(java.util.Calendar.DAY_OF_YEAR);
		int year1 = calendar.get(java.util.Calendar.YEAR);
		calendar.setTime(Date2);
		int day2 = calendar.get(java.util.Calendar.DAY_OF_YEAR);
		int year2 = calendar.get(java.util.Calendar.YEAR);
		if (year1 == year2) {// 同一年
			return day2 - day1;
		} else if (year1 < year2) {// Date1<Date2
			int days = 0;
			for (int i = year1; i < year2; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {// 闰年
					days += 366;
				} else {
					days += 365;
				}
			}
			return days + (day2 - day1);
		} else {// Date1>Date2
			int days = 0;
			for (int i = year2; i < year1; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
					days += 366;
				} else {
					days += 365;
				}
			}
			return 0 - days + (day2 - day1);
		}
	}

	public static Date getDate(String strDate)  {
		Date date = null;
		SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			date = fm.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static void main(String[] argv) {
		String sDate = getDateString(new Date());
		System.out.println(sDate);
	}
}
