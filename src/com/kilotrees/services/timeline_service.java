package com.kilotrees.services;

import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.kilotrees.dao.timelinedao;
import com.kilotrees.model.po.timelineinfo;
import com.kilotrees.util.CommonUtil;
import com.kilotrees.util.StringUtil;

/**
 * 根据留存曲线计算从现在开始到23点每小时的任务量
 * 
 * @author Administrator
 */
public class timeline_service {
	private static Logger log = Logger.getLogger(timeline_service.class);
	// 时间曲线列表很小，加到内存中
	HashMap<Integer, timelineinfo> timelineList = new HashMap<Integer, timelineinfo>();

	private static timeline_service inst;

	public timeline_service() {

	}

	public static timeline_service getInstance() {
		synchronized (timeline_service.class) {
			if (inst == null) {
				inst = new timeline_service();
			}
		}
		return inst;
	}

	/**
	 * 刷新timelineList
	 * 
	 */
	public void refresh() {
		HashMap<Integer, timelineinfo> ls = timelinedao.getTimeLineList();
		if (ls.size() > 0) {
			synchronized (timelineList) {
				timelineList.clear();
				timelineList.putAll(ls);

			}
		}
	}

	/**
	 * 计算每小时做的数量
	 * @param timelineId
	 * @param all_day
	 * @param daycount
	 * @return
	 */
	public int[] caclDistributeOfHours(int timelineId, boolean all_day, int daycount) {
		int[] Distribute = new int[24];
		if (daycount <= 0)
			return Distribute;
		Calendar dateNow = Calendar.getInstance();
		int curHour = dateNow.get(Calendar.HOUR_OF_DAY);
		int curMin = dateNow.get(Calendar.MINUTE);
		if (all_day) {
			// 指定
			curHour = 0;
			curMin = 0;
		}

		String s = "";
		if (timelineId != -1) {
			s = getTimedistributeByid(timelineId);
		}
		String[] slineHour = s.replace(',', ';').split(";");
		if (timelineId != -1 && slineHour.length != 24) {
			ErrorLog_service.system_errlog(getClass().getName() + "时间曲线描述有错，timelineId=" + timelineId + ",Distribute_hours: " + s);
			log.error("时间曲线描述有错，timelineId=" + timelineId + ",Distribute_hours: " + s);
			s = "";
			// return Distribute;
		}
		if (StringUtil.isStringEmpty(s)) {
			// 无曲线限制
			int c = 24 - curHour - 1;
			// 总分钟数
			int retMin = c * 60 + 60 - curMin;
			float cc = (float) daycount / retMin;// 每分钟多少个
			int tmp = daycount;
			boolean isfloat = ((int) (60 - curMin) * cc) % 1 == 0 ? false : true;
			if (isfloat)
				Distribute[curHour] = (int) ((int) (60 - curMin) * cc) + 1;
			else
				Distribute[curHour] = (int) ((int) (60 - curMin) * cc);
			tmp -= Distribute[curHour];
			for (int i = curHour + 1; i < 24; i++) {
				if (tmp <= 0)
					Distribute[i] = 0;
				else {
					// Distribute[i] = (int)(60*cc) + 1;
					Distribute[i] = (60 * cc) % 1 == 0 ? (int) (60 * cc) : (int) (60 * cc) + 1;
					tmp -= Distribute[i];
				}
			}
		} else {
			// 走曲线
			int[] ilineHour = new int[24];
			float c = 0; // 当天timeline剩下所有时间总比例
			for (int i = 0; i < ilineHour.length; i++) {
				ilineHour[i] = Integer.parseInt(slineHour[i]);
				if (i == curHour)
					// c = ilineHour[i]*(60-curMin)/60;
					c = (float) ilineHour[i] * (60 - curMin) / 60;
				if (i > curHour)
					c += ilineHour[i];
			}
			int tmp = daycount;
			// 2018-12-29,这里还是有问题，当任务量很小时，比如只有5个任务，那么按下面的策略，早上5点就执行完了，后面全是0
			// 按理说我们应该尽量在白天执行，所以下面应该把哪个小时分到的最大数值的先执行，要排序一下，按最大到最小分配。
			if (tmp >= 24 - curHour) {
				// 任务量大于当日剩余小时数量
				float cc = 0;
				for (int i = curHour; i < 24; i++) {
					if (i == curHour)
						cc = daycount * ilineHour[i] * (((float) 60 - curMin) / 60) / c;
					else {
						cc = (float) daycount * ilineHour[i] / c;
					}
					boolean isfloat = (cc % 1 == 0) ? false : true;
					if (tmp <= 0)
						Distribute[i] = 0;
					else {
						if (ilineHour[i] != 0)
							if (isfloat)
								cc += 1;
						Distribute[i] = (int) cc;
						tmp -= Distribute[i];
					}
				}
			} else {// 任务量小于当日剩余小时数量
				int[] temp = new int[24 - curHour];
				for (int i = 0; i < temp.length; i++) {
					temp[i] = ilineHour[i+curHour];   //表里面剩余 每小时的
				}
				int[] indexOfMax = CommonUtil.sortTimeLine(temp);
				for(int i=0;i<indexOfMax.length;i++) {
					indexOfMax[i] += curHour;    //每小时排序后的
				}
				for(int i=0;tmp>0 && i<indexOfMax.length;i++ ) {
					Distribute[indexOfMax[i]] = 1;   //最终结果
					tmp--;		
				}
			}
		}
		return Distribute;
	}

	@Test
	public void t1() {
		Calendar dateNow = Calendar.getInstance();
		int curHour = dateNow.get(Calendar.HOUR_OF_DAY);
		int tmp = 2; // 任务量
		int[] ilineHour = new int[] { 0, 0, 0, 0, 0, 9, 9, 0, 9, 0, 0, 8, 0, 7, 6, 5, 4, 3, 2, 2, 1, 9, 11, 2};
		int[] Distribute = new int[24];
		int[] temp = new int[24 - curHour];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = ilineHour[i+curHour];
		}
		int[] indexOfMax = CommonUtil.sortTimeLine(temp);
		for(int i=0;i<indexOfMax.length;i++) {
			indexOfMax[i] += curHour;
		}
		
		System.out.print("temp(表里面的剩余每小时的): ");
		for (int i : temp) {
			System.out.print(i + " ");
		}
		System.out.println();
//		System.out.println(ilineHour.length);
		System.out.println("curHour: "+curHour);
		System.out.println();
		System.out.print("indexOfMax(表里面剩余每小时排序后的): ");
		for (int i : indexOfMax) {
			System.out.print(i + " ");
		}
		
		for(int i=0;tmp>0 && i<indexOfMax.length;i++ ) {
			Distribute[indexOfMax[i]] = 1; 
			tmp--;		
		}
		
		System.out.println();
		System.out.print("Distribute(最终的结果): ");
		for (int i : Distribute) {
			System.out.print(i + " ");
		}
	}

	public String getTimedistributeByid(int timelineId) {
		synchronized (timelineList) {
			timelineinfo e = timelineList.get(timelineId);
			if (e != null)
				return e.getDistribute_hours();
		}
		return "";
	}

	/**
	 * 已知一台设备假定一小时内可以做某个广告最大数量，按时间曲线一天能做的数量
	 * 
	 * @param timelineId
	 * @param hourCount
	 * @return
	 */
	public int getDayCountOnTimeLine(int timelineId, int hourCount) {
		int count = hourCount * 24;
		if (timelineId == -1)
			return count;
		String s = getTimedistributeByid(timelineId);
		s = s.replaceAll(",", ";");
		String[] slineHour = s.split(";");
		if (timelineId != -1 && slineHour.length != 24) {
			ErrorLog_service.system_errlog(getClass().getName() + "时间曲线描述有错，timelineId=" + timelineId);
			log.error("时间曲线描述有错，timelineId=" + timelineId);
			return count;
		}
		int[] Distribute = new int[24];
		int max = 0;
		int dcount = 0;
		for (int i = 0; i < 24; i++) {
			Distribute[i] = Integer.parseInt(slineHour[i]);
			if (Distribute[i] > max)
				max = Distribute[i];
			dcount += Distribute[i];
		}
		return (hourCount * dcount) / max;
	}

	boolean btest = true;

	void test() {
		if (btest)
			return;
		caclDistributeOfHours(1, true, 1500);
		btest = true;
	}

	public static void main(String[] argv) {
		Calendar dateNow = Calendar.getInstance();
		int curHour = dateNow.get(Calendar.HOUR_OF_DAY);
		System.out.println("curHour=" + curHour);
		int curMin = dateNow.get(Calendar.MINUTE);
		System.out.println("curMin=" + curMin);

		int[] Distribute = new int[24];
		for (int i = 0; i < 24; i++)
			System.out.println("i=" + Distribute[i]);
		int allcount = 600;
		int c = 24 - curHour - 1;
		float cc = (float) allcount / (c * 60 + 60 - curMin);// 第分钟多少个
		Distribute[curHour] = (int) ((int) (60 - curMin) * cc);
		for (int i = curHour + 1; i < 24; i++)
			Distribute[i] = (int) (60 * cc);
		for (int i = 0; i < 24; i++)
			System.out.println("" + i + "=" + Distribute[i]);
	}
}
