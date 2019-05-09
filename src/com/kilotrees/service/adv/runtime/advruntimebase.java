package com.kilotrees.service.adv.runtime;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.model.bo.ad_task_report;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.advtodayresult;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;
import com.kilotrees.services.advnewtask_service;
import com.kilotrees.services.timeline_service;
import com.kilotrees.util.DateUtil;
import com.kilotrees.util.InfoGenUtil;
import com.kilotrees.util.RegionRand;
import com.kilotrees.util.StringUtil;

public abstract class advruntimebase implements ITaskRuntime {
	private static Logger log = Logger.getLogger(advruntimebase.class);
	advtaskinfo advinfo;
	advtodayresult result;
	boolean remain;
	// 需要多少台手机做新增
	int reqDevDoCount;
	// 预先分配的机器
	// String[] devTags;
	// 一天每小时要做的数量,这个数量是随广告每天要做的数量(用户后台修改)advinfo.getDateCount()变化而不断变化
	int[] docount_hours = new int[24];
	// 每小时剩余的数量，初始值和docount_hours一样，但实时计算提取任务，每提取一个就少一个．
	int[] retcount_hours = new int[24];
	// docount_hours 24小时中最大量的
	int max_docountOfHours;
	// 是否下线
	boolean offline;
	// 记录正在执行还没有完成的任务
	HashMap<String, Long> doingDevs = new HashMap<String, Long>();
	/**
	 * 28号测试任务
	 */
	// 新增随机打开次数比例
	// 2018-12-7，使用HashMap来严格随机,不能用静态
	// int[] openCountScale_new;
	HashMap<Object, Integer> opencount_org = new HashMap<Object, Integer>();
	HashMap<Object, Integer> opencount_rand = null;// new HashMap<Object,
	HashMap<Object, Integer> opencount_rand_remain;
	// 上次配置，如果发现一样的话，就不用刷新了
	String openCountScale = "";
	/*
	 * 新增时长配置，时长比较麻烦，要按比例在某个区间中浮动，比如60:300-600，表示60%用户在在5分钟和10分钟之间， 涉及
	 * 3个维度,要定义一个类：指定最小和最大值的区间com.zfcom.cft.util.RegionRand用来代替上面的ct
	 */
	HashMap<Object, Integer> newtime_org = new HashMap<Object, Integer>();
	HashMap<Object, Integer> newtime_rand = null;// new HashMap<Object,
	// 上次配置
	String requesttime_new = "";
	/**
	 * 留存时长配置，一般情况下，3天内跟新增一样，其它时间按比例减小
	 */
	HashMap<Object, Integer> newtime_rand_remain;
	//2019-1-17
	boolean need_reCaclRuntime;

	public boolean isRemain() {
		return remain;
	}

	public void setRemain(boolean r) {
		remain = r;
	}

	/**
	 * 从advext_XXX.json文件拿用户自己定义的hand_locked,dayopencount,requesttime,dayusercount来设置
	 */
	public void refresh() {
		JSONObject jsoExt = this.advinfo.getExtJso();
		if (jsoExt != null) {
			int handle_Locked = jsoExt.optInt("hand_locked", 0);
			// 加上这个条件，因为cparechargeruntime即使json没有设这个参数，也一样默认是1
			if (handle_Locked > 0)
				this.advinfo.setHandle_Locked(handle_Locked);
		}
		loadOpenCountConf(jsoExt);
		loadRequestTimeConfig(jsoExt);
		loadDayUserCount(jsoExt);
	}

	// void loadExtenter
	/**
	 * 有些任务的dayusercount会动态设置，我们可以在json文件中指定日期(格式yyyy-mm-dd)的数量，如果今天日期的数量
	 * 就直接用json文件的数据，而不是用advtaskinfo的数据表的数据
	 * 
	 * @param jsoExt
	 *            广告拓展json文件 例如:advext_12_网易云音乐.json
	 */
	private void loadDayUserCount(JSONObject jsoExt) {
		// 只对新增任务
		if (isRemain()) {
			return;
		}
		String key = DateUtil.getDateBeginString(new Date());
		try {
			JSONObject jsoDayUser = jsoExt.optJSONObject("dayusercount");
			if (jsoDayUser != null) {
				int c = jsoDayUser.optInt(key, -1);
				if (c != -1) {
					int oldcount = this.advinfo.getDayusercount();
					if (c != oldcount) {
						this.advinfo.setDayusercount(c);
						advnewtask_service.getInstance().updateAdvDayUserCountByJson(this.advinfo.getAdv_id(), c);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 当advtaskinfo的dayopencount<=0时，从广告的extjson文件或者servconfig.json文件中取得每个用户一天打开次数配置
	 * 
	 * @param jsoExt
	 *            例如:advext_12_网易云音乐.json
	 */
	void loadOpenCountConf(JSONObject jsoExt) {
		if (jsoExt != null) {
			// 把advtaskinfo中的功能移过来
			// 2018-10-22,正常情况下，dayopencount=0，我们随机让用户做1-5次，按json文件定义好的比重来处理
			// 格式如下:ad_dayopencount:"50;20;20;7;3"表示打开1次的占50%，打开2次的占20，依次类推

			// 先从自己的配置文件中读出,下面可以把新增和留存分开设置(以后再说)
			// 如果客户指定次数，就不用随机，不过这不切实际
			if (this.advinfo.getDayopencount() <= 0) {
				// 之前总数一定要设为１００，现在可以总数任意值，最好１０，比如＂3;5;2＂
				String ad_dayopencount = "";
				if (jsoExt.optString("dayopencount_new", "").length() > 0)
					ad_dayopencount = jsoExt.optString("dayopencount_new", "");
				else// 使用通用模板
				{
					ad_dayopencount = ServerConfig.getConfigJson().optString("ad_dayopencount_0", "3;5;2");
				}
				// 判断有没有变化
				if (openCountScale.equals(ad_dayopencount)) {
					return;
				}
				openCountScale = ad_dayopencount;
				// 防止不小心用了逗号
				ad_dayopencount = ad_dayopencount.replaceAll(",", ";");
				String[] as = ad_dayopencount.split(";");
				// cc表示百分数
				Integer[] cc = new Integer[as.length];
				// c表示打开次数
				Integer[] c = new Integer[cc.length];
				synchronized (this.opencount_org) {
					opencount_org.clear();
					for (int i = 0; i < cc.length; i++) {
						cc[i] = Integer.parseInt(as[i]);
						c[i] = i + 1;
						opencount_org.put(c[i], cc[i]);
					}
					if (opencount_rand == null) {
						opencount_rand = new HashMap<Object, Integer>();
						opencount_rand.putAll(opencount_org);
					}
				}
			}
		} else if (this.advinfo.getDayopencount() == 0) {
			opencount_org.put(1, 3);
			opencount_org.put(2, 5);
			opencount_org.put(3, 2);
		}
	}

	/**
	 * 从json文件中取得新增时间和留存时间配置 初始化newtime_rand,newtime_org
	 * 
	 * @param jsoExt
	 *            广告拓展json文件 例如:advext_12_网易云音乐.json
	 */
	void loadRequestTimeConfig(JSONObject jsoExt) {
		if (jsoExt == null) {
			return;
		}
		// 新增时长配置
		String timeStrNew = jsoExt.optString("requesttime_new", "");
		if (timeStrNew.length() == 0) {
			return;
		}
		// 判断是否跟上次一样
		if (requesttime_new.equals(timeStrNew)) {
			return;
		}
		requesttime_new = timeStrNew;
		timeStrNew = timeStrNew.replaceAll(",", ";");
		String[] ss = timeStrNew.split(";");
		int pos;
		synchronized (this.newtime_org) {
			newtime_org.clear();
			for (String s : ss) {
				pos = s.indexOf(':');
				String sScale = s.substring(0, pos);
				s = s.substring(pos + 1);
				pos = s.indexOf('-');
				String smintime = s.substring(0, pos);
				String smaxtime = s.substring(pos + 1);
				RegionRand rr = new RegionRand();
				rr.setMin(Integer.parseInt(smintime));
				rr.setMax(Integer.parseInt(smaxtime));
				Integer scale = Integer.parseInt(sScale);
				newtime_org.put(rr, scale);
			}
			if (newtime_rand == null) {
				newtime_rand = new HashMap<Object, Integer>();
				newtime_rand.putAll(newtime_org);
			}
		}
	}

	public int getReqDevDoCount() {
		return reqDevDoCount;
	}

	public void setReqDevDoCount(int reqDevDoCount) {
		this.reqDevDoCount = reqDevDoCount;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean isOffline) {
		this.offline = isOffline;
	}

	/**
	 * 设置advruntimebase绑定的advinfo，并从advext_XXX.json文件设置hand_locked,dayopencount,requesttime,dayusercount
	 */
	public void setAdvinfo(advtaskinfo _advinfo) {

		advinfo = _advinfo;
		refresh();
	}

	public advtaskinfo getAdvinfo() {
		return advinfo;
	}

	public advtodayresult getResult() {
		return result;
	}

	public void setResult(advtodayresult result) {
		this.result = result;
	}


	/**
	 * 新增增加正在做的用户数 HashMap<设备，超时时间>
	 * 
	 * @param dev_tag
	 */
	public void incDoingCount(String dev_tag) {
		// doingCount++;
		Long time = new Date().getTime();
		time += (this.advinfo.getRequesttime() + 120 + 60) * 1000;
		synchronized (doingDevs) {
			doingDevs.put(dev_tag, time);
		}

	}

	/**
	 * 任务完成后（第一次）减回去
	 */
	public void decDoingCount(String dev_tag) {
		// if(doingCount > 0)
		// doingCount--;
		synchronized (doingDevs) {
			doingDevs.remove(dev_tag);
		}
	}

	public synchronized int getDoingCount() {
		return doingDevs.size();
	}

	public void checkDoingTimeout() {
		long now = new Date().getTime();
		synchronized (doingDevs) {
			Iterator<Map.Entry<String, Long>> it = doingDevs.entrySet().iterator();
			for (; it.hasNext();) {
				Map.Entry<String, Long> e = it.next();
				long timeout = e.getValue();
				if (now > timeout) {
					log.warn(e.getKey() + ":ding timeout diff=" + (now - timeout));
					it.remove();
				}
			}
		}

	}

	public int getMax_docountOfHours() {
		return max_docountOfHours;
	}

	public void setMax_docountOfHours(int max_docountOfHours) {
		this.max_docountOfHours = max_docountOfHours;
	}

	/**
	 * 返回当前小时还有多少量要做
	 * 
	 * @return
	 */
	public int getCurRetCount() {
		Calendar date = Calendar.getInstance();
		int curhour = date.get(Calendar.HOUR_OF_DAY);
		return retcount_hours[curhour];
	}

	public int[] getDoCountOf24Houurs() {
		return this.docount_hours;
	}

	/**
	 * 由时间曲线，设定某些小时执行数为0也能实现下面功能，这个没有什么作用，暂时保留，
	 * 
	 * @param adv_id
	 * @param limitStr
	 * @param allDay
	 * @param count
	 * @return
	 */
	public static int[] caclHourCountByLimitHours(int adv_id, String[] limitStr, boolean allDay, int count) {
		int[] h = new int[24];
		log.warn("新增广告adv_id=" + adv_id + "由于时间段限制:" + limitStr);
		int[] limitHours = new int[limitStr.length];
		// 定义需要做任务的小时数量
		int workhour_count = 0;
		for (int i = 0; i < limitHours.length; i++) {
			limitHours[i] = Integer.parseInt(limitStr[i]);

		}
		int curhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int curMin = Calendar.getInstance().get(Calendar.MINUTE);
		if (allDay) {
			curhour = 0;
			curMin = 0;
		}
		int[] tempH = new int[24];
		for (int i = curhour; i < 24; i++) {
			boolean bwork = true;
			for (int j = 0; j < limitHours.length; j++) {
				if (limitHours[j] == i) {
					bwork = false;
					tempH[i] = -1;
					break;
				}
			}
			if (bwork) {
				workhour_count++;
			}
		}
		if (workhour_count > 0) {
			int retMin = (workhour_count - 1) * 60 + 60 - curMin;
			if (tempH[curhour] == -1)
				retMin = workhour_count * 60;
			float cc = (float) count / retMin;// 每分钟多少个
			for (int i = curhour; i < 24; i++) {
				if (tempH[i] == -1) {
					// 在时间限制段内
					tempH[i] = 0;
					continue;
				}
				if (i == curhour) {
					tempH[i] = (int) (cc * (60 - curMin)) + 1;
				} else
					tempH[i] = (int) (cc * 60) + 1;
				// 如果静态计算全天每小时，不需按上面计算
				if (allDay)
					tempH[i] = count / workhour_count + 1;

				h[i] = tempH[i];
			}

		} else {
			log.warn("新增广告adv_id=" + adv_id + "由于时间段限制，从当前" + curhour + "小时起，没有任务执行,限制时间段:" + limitStr);
		}
		return h;
	}

	public static int caclMaxHoureCountOfAllDateByTimeline(int count, int timelineid) {
		int h[] = timeline_service.getInstance().caclDistributeOfHours(timelineid, true, count);
		int max = 0;
		for (int i = 0; i < h.length; i++)
			if (h[i] > max)
				max = h[i];
		return max;
	}

	public static int setDocount_hours(int[] do_hours, int[] _docount_hours, int[] _retcount_hours) {
		// this.docount_hours = do_hours;
		int curhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int maxdocountOfHours = 0;
		// 当前小时之前的数据不重置，否则之前的数据可能为0
		for (int i = curhour; i < do_hours.length; i++) {
			_docount_hours[i] = do_hours[i];
			_retcount_hours[i] = _docount_hours[i];
		}
		// 如果用户把每天新增任务量减少后，这里是否返回当前时间之后的最大值，还是全天的最大值？
		for (int i = 0; i < _docount_hours.length; i++) {
			if (_docount_hours[i] > maxdocountOfHours)
				maxdocountOfHours = _docount_hours[i];
		}
		return maxdocountOfHours;
	}

	/**
	 * 100个用户中平均一个用户打开次数(按配置文件) ２０１８－１２－８修改一下，value总基数不一定设为１００个，可以１０或任意数，
	 * 
	 * @return
	 */
	public int getDayOpenCountRand(int daydiff) {
		if(this.remain)
			return getDayOpenCountRandRemain(daydiff);
		
		if (this.advinfo.getDayopencount() > 0)
			return advinfo.getDayopencount();

		if (opencount_rand == null) {
			opencount_rand = new HashMap<Object, Integer>();
			opencount_rand.putAll(opencount_org);
		}

		// Integer[] c = new Integer[openCountScale_new.length];
		// for (int i = 0; i < openCountScale_new.length; i++)
		// c[i] = i + 1;
		// Integer opencount = (Integer) InfoGenUtils.randOnSpecScale(c,
		// openCountScale_new,
		// "D:/work/randOnSpecScale.txt");
		Integer opencount = 1;
		synchronized (opencount_org) {
			opencount = (Integer) InfoGenUtil.randOnSpecScaleConst(opencount_rand, opencount_org);
		}
		return opencount;
	}

	/**
	 * 2018-11-2 计算留存打开次数,按配置文件要求随机算出，暂时和新增一样 以后按daydiff计算,这里硬编码了，不再用配置文件
	 * 
	 * @return
	 */
	public int getDayOpenCountRandRemain(int daydiff) {
		if (this.advinfo.getDayopencount() > 0) {
			int count = advinfo.getDayopencount();
			int randpos = new Random().nextInt(100);
			if (daydiff > 10) {
				// 10天后，90%的用户是做一次，其它随机
				if (randpos < 90)
					count = 1;
				else
					count = new Random().nextInt(advinfo.getDayopencount());
			} else if (daydiff > 7) {
				if (randpos < 80)
					count = 1;
				else
					count = new Random().nextInt(advinfo.getDayopencount());
			} else if (daydiff > 3) {
				if (randpos < 50)
					count = 1;
				else
					count = new Random().nextInt(advinfo.getDayopencount());
			}
			return count;
		}

		Integer opencount = 1;

		if (daydiff <= 3) {
			if (opencount_rand_remain == null) {
				opencount_rand_remain = new HashMap<Object, Integer>();
				opencount_rand_remain.putAll(opencount_org);
			}

			synchronized (opencount_org) {
				opencount = (Integer) InfoGenUtil.randOnSpecScaleConst(opencount_rand_remain, opencount_org);
			}
		} else {
			int maxopencount = 1;
			Object[] v = new Object[opencount_org.size()];
			opencount_org.keySet().toArray(v);
			// 找到最大打开次数
			for (Object k : v) {
				Integer kk = (Integer) k;
				if (kk > maxopencount)
					maxopencount = kk;
			}
			int randpos = new Random().nextInt(100);
			if (daydiff > 10) {
				// 10天后，90%的用户是做一次，其它随机
				if (randpos < 90)
					opencount = 1;
				else
					opencount = new Random().nextInt(maxopencount);
			} else if (daydiff > 7) {
				if (randpos < 80)
					opencount = 1;
				else
					opencount = new Random().nextInt(maxopencount);
			} else if (daydiff > 3) {
				if (randpos < 50)
					opencount = 1;
				else
					opencount = new Random().nextInt(maxopencount);
			}
		}
		return opencount;
	}

	// 取平均一天用户数，在任务中是固定的，但也有可能是按计划每日变化,这个以后按json处理，无用，不应该这样处理
	// public float getDayUserCountAver() {
	// return this.advinfo.getDayusercount();
	// }

	// 取平均一天一个用户打开次数
	public float getDayOpenCountAver() {
		if (this.advinfo.getDayopencount() > 0)
			return advinfo.getDayopencount();
		float adv_open = 0;
		// for (int i = 0; i < openCountScale_new.length; i++)
		// adv_open += openCountScale_new[i] * (i + 1);
		// 2018-12-8
		int all_value_count = 0;
		synchronized (opencount_org) {
			Iterator<Map.Entry<Object, Integer>> it = opencount_org.entrySet().iterator();
			for (; it.hasNext();) {
				Map.Entry<Object, Integer> e = it.next();
				int key = (Integer) e.getKey();
				int value = (Integer) e.getValue();
				adv_open += key * value;
				all_value_count += value;
			}
		}
		// adv_open /= 100;
		adv_open /= all_value_count;
		return adv_open;
	}

	/*
	 * 取一个新增平均时长,象打开次数一样，由json文件来指定 象某个游戏，指定60%用户5-10分钟，20%用户10-15分钟，20%用户是15-20分钟
	 * 规则上我们我们设定是60%的用户是300-600秒的中间值，就是450秒，如此类推，json文件是60:45020:
	 * 
	 * getRequestTimeAverForNew()
	 */
	public float getWorkingTimeAver(int daydiff) {

		if (this.remain) {
			return getRequestTimeAverForRemain(daydiff);
		}
		
		if (newtime_org == null)
			return this.advinfo.getRequesttime();
		float avrtime = 0;// this.advinfo.getRequesttime();
		int all_scale = 0;
		synchronized (newtime_org) {
			Iterator<Map.Entry<Object, Integer>> it = newtime_org.entrySet().iterator();
			for (; it.hasNext();) {
				Map.Entry<Object, Integer> e = it.next();
				RegionRand r = (RegionRand) e.getKey();
				Integer scale = e.getValue();
				float mid = r.getMin() + r.getMax();
				mid /= 2;
				avrtime += scale * mid;
				all_scale += scale;
			}

		}
		avrtime /= all_scale;
		return avrtime;
			
	}

	/**
	 * 硬编码，7天后，90%用户是最小时间的一半，10%的用户在最小和最大值之前随机，但这个取平均时长工程没有用到，先不实现
	 * 因为留存用户只有上来取的时候才知其离第一天注册相隔多少天,极难平均的
	 */
	public float getRequestTimeAverForRemain(int daydiff) {
		// if()
		return this.advinfo.getRemaintime();
	}

	/**
	 * 利用从任务的advext_xx.json文件初始化的newtime_rand, newtime_org，按比例随机一个执行时间
	 * @param daydiff 新增任务调用的话为0，留存就是实际天数
	 */
	public int getWorkingTimeRand() { 
		if(this.remain)
			return getRequestTimeRandForRemain();
		
		if (newtime_org.size() == 0)
			return -1;// return this.advinfo.getRequesttime();
		
		int reqtime = this.advinfo.getRequesttime();
		synchronized (newtime_org) {
			RegionRand r = (RegionRand) InfoGenUtil.randOnSpecScaleConst(newtime_rand, newtime_org);
			int min = r.getMin();
			int max = r.getMax();
			if (max < min) {
				min = r.getMax();
				max = r.getMin();
			}
			int d = new Random().nextInt(max - min + 1);
			reqtime = min + d;
		}
		return reqtime;
	}

	/**
	 * 硬编码，3天后50%是第一天时长的一半，50%随机最小时间和最大小间
	 * 7天后90%是平时的一半（如果第一天时间特别大，可以强制是2-3分钟)，其余在最小和最大值之前
	 * 这种有时每个广告处理手法不一样，我们是否可以通过加载js脚本文件实现
	 */
	public int getRequestTimeRandForRemain() {
		return this.advinfo.getRemaintime();
	}

	public static void main(String[] argv) {
		HashMap<Object, Integer> newtime_org = new HashMap<Object, Integer>();
		String timeStrNew = "5:10-20;5:20-30";
		timeStrNew = timeStrNew.replaceAll(",", ";");
		String[] ss = timeStrNew.split(";");
		int pos;

		for (String s : ss) {
			pos = s.indexOf(':');
			String sScale = s.substring(0, pos); // 5
			s = s.substring(pos + 1); // 10-20
			pos = s.indexOf('-');
			String smintime = s.substring(0, pos); // 10
			String smaxtime = s.substring(pos + 1);// 20
			RegionRand rr = new RegionRand();
			rr.setMin(Integer.parseInt(smintime));
			rr.setMax(Integer.parseInt(smaxtime));
			Integer scale = Integer.parseInt(sScale);
			newtime_org.put(rr, scale);
		}
		float avrtime = 0;// this.advinfo.getRequesttime();
		int all_scale = 0;
		synchronized (newtime_org) {
			Iterator<Map.Entry<Object, Integer>> it = newtime_org.entrySet().iterator();
			for (; it.hasNext();) {
				Map.Entry<Object, Integer> e = it.next();
				RegionRand r = (RegionRand) e.getKey();
				Integer scale = e.getValue();
				float mid = r.getMin() + r.getMax();
				mid /= 2;
				avrtime += scale * mid; // 总共的平均用时
				all_scale += scale; // 总共的数量
			}
		}
		avrtime /= all_scale;

		System.out.println(avrtime);
	}

	public void caclRuntimeHouursCount() {
		this.refresh();
	}
	/**
	 * 处理结果返回，对于一些特殊的业务流程，需要用到自身的业务表和记录处理结果表，就使用自身的处理
	 * 这里用server_bean或别名对应的组件来处理，可以用其中一个接口，也可以用反射方法
	 * 当返回true时，表示自身处理就行了，上层（比如advnewtask_service不需要继续处理了)
	 * 一般返返回false
	 */
	public boolean handleTaskReport(ad_task_report report)
	{
		boolean result = false;
		String server_bean_name = this.advinfo.getAlias();
		if(StringUtil.isStringEmpty(server_bean_name) == false)
		{
			//在这里处理自身的结果表
		}
		return result;
	}

}
