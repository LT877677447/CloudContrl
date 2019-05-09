package com.kilotrees.service.adv.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;
import com.kilotrees.services.advnewtask_service;
import com.kilotrees.util.DateUtil;

/*
 * 不按时间曲线充值任务，的任务列表中，此任务的adv_type=3，和cparechargeruntime1一样自动hand_lock固定的设备，为其它任务的apk充值。
 * 一般是针对某几个任务无时间限制的充值，而且充值次数很大，用cparechargeruntime1的格式就花费很多精力，以前充猫扑那样，种格式为：
 * {"recharge_advs2":[{"date":"2018-12-29","advid":13,"count":10},{"date":"2018-12-29","advid":15,"count":6}]}
 * 这样表示advid=13的广告充值10次，advid=15广告充值6次 
 */
public class cparechargeruntime2 extends advruntimebase {
	private static Logger log = Logger.getLogger(cparechargeruntime2.class);
	// key表示adv_id，value表示要做的数量
	HashMap<Integer, Integer> charge_adv_notimelimit = new HashMap<Integer, Integer>();
	static String myJSonPath = "d:/webapps/yunctrl/charge/";
	//保存充值结果
	JSONObject myJson = new JSONObject();
	int json_version = -1;
	int daycount = 0;

	public void setAdvinfo(advtaskinfo _advinfo) {
		super.setAdvinfo(_advinfo);
		this.advinfo.setHandle_Locked(1);
		caclRuntimeHouursCount();
	}

	@Override
	public int getReqDevDoCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getAllTodocount()
	{
		return this.daycount;
	}

	public void refresh() {
		// super.refresh();
		if(this.isOffline())
			return;
		String path = ServerConfig.getConfigJson().optString("chargeresult_path","");
		if(path.length() > 0)
			myJSonPath = path;
		if(myJSonPath.endsWith("/") == false)
			myJSonPath += "/";
		JSONObject jsoExt = this.advinfo.getExtJso();
		// 修改配置文件时要增加version的值
		int version = jsoExt.optInt("version", 0);
		if (version == json_version)
			return;
		synchronized (charge_adv_notimelimit) {
			charge_adv_notimelimit.clear();
			daycount = 0;
			json_version = version;
			JSONArray array = jsoExt.optJSONArray("recharge_advs2");
			String todayDate = DateUtil.getDateBeginString(new Date());
			Calendar date = Calendar.getInstance();
			//int curhour = date.get(Calendar.HOUR_OF_DAY);
			// int hour = 0;//date.get(Calendar.HOUR_OF_DAY);
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					try {
						JSONObject jsItem = array.getJSONObject(i);
						String doDay = jsItem.optString("date", "");
						if (doDay.length() == 0) {
							log.error("缺少date这个key");
							continue;
						}
						if (todayDate.equals(doDay) == false)
							continue;// 和今天不是同一日期

						// log.info("");
						int advid = jsItem.optInt("advid", 0);
						if (advid == 0) {
							log.error("缺少advid这个key");
							continue;
						}
						int count = jsItem.optInt("count");

						synchronized (charge_adv_notimelimit) {
							charge_adv_notimelimit.put(advid, count);
						}
						daycount += count;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			// 重启时看哪些已经做过了，剔除做过了的
			loadFinishJson();
			if (charge_adv_notimelimit.size() > 0) {
				Iterator<Map.Entry<Integer, Integer>> it = charge_adv_notimelimit.entrySet().iterator();
				for (; it.hasNext();) {
					// 为方便读出对应日期和小时的值，直接把日期加小时保存起来
					Map.Entry<Integer, Integer> item = it.next();
					int advid = item.getKey();
					int count = item.getValue();
					int done = myJson.optInt("chargedone_" + todayDate + "|" + advid, 0);
					if (done > 0) {
						int ret = count - done;
						if (ret <= 0) {
							it.remove();
						} else {
							charge_adv_notimelimit.put(advid, ret);
						}
					}
				}
			}
		}
	}

	@Override
	// 注意重启服务器时，已经做的任务反写回去
	public void caclRuntimeHouursCount() {
		// TODO Auto-generated method stub
		// 调用父类advruntimebase实现refresh()刷新作用
		super.caclRuntimeHouursCount();

	}

	void loadFinishJson() {
		int adv_id = this.advinfo.getAdv_id();
		String name = this.advinfo.getName();
		//这里如果服务器重启，很容易造成文件丢失，所以最好换条路径
		new File(myJSonPath).mkdirs();
		//serverconfig.contextRealPath + "files/extjson/charge_" + adv_id + "_" + name.trim() + ".json";
		String fileName = myJSonPath + "charge_" + adv_id + "_" + name.trim() + ".json";//
		File file = new File(fileName);
		if (file.exists()) {
			try {
				FileInputStream fins = new FileInputStream(file);
				byte[] buf = new byte[(int) file.length()];
				fins.read(buf);
				fins.close();
				String s = new String(buf, "utf-8");
				myJson = new JSONObject(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void saveFinishJson() {
		int adv_id = this.advinfo.getAdv_id();
		String name = this.advinfo.getName();
		//String fileName = serverconfig.contextRealPath + "files/extjson/charge_" + adv_id + "_" + name.trim() + ".json";
		new File(myJSonPath).mkdirs();
		//serverconfig.contextRealPath + "files/extjson/charge_" + adv_id + "_" + name.trim() + ".json";
		String fileName = myJSonPath + "charge_" + adv_id + "_" + name.trim() + ".json";//
		File file = new File(fileName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			byte[] buf = myJson.toString().getBytes("utf-8");
			fos.write(buf);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void handleFechTaskFinish(int advid) {
		String todayDate = DateUtil.getDateBeginString(new Date());
		int c = myJson.optInt("chargedone_" + todayDate + "|" + advid, 0);
		c++;
		try {
			myJson.put("chargedone_" + todayDate + "|" + advid, c);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// this.retcount_hours[curhour] -= 1;
		this.saveFinishJson();
	}

	@Override
	public boolean allocNewReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allocDevForNew() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reAllocDev(boolean inited) {
		// TODO Auto-generated method stub

	}

	@Override
	public TaskBase[] fetchTask(String dev_tag) {
		// TODO Auto-generated method stub
		synchronized (charge_adv_notimelimit) {
			if (charge_adv_notimelimit.isEmpty())
				return new TaskBase[0];
			int advid = -1;
			TaskBase[] adtasks = new TaskBase[1];
			// 随机选一个
			int size = charge_adv_notimelimit.size();
			int r = new Random().nextInt(size);
			Integer[] v = new Integer[size];
			charge_adv_notimelimit.keySet().toArray(v);
			advid = v[r];
			int count = charge_adv_notimelimit.get(advid);
			if (advid < 0) {
				// 负数表示留存，暂时不处理
			} else if (advid > 0) {
				ITaskRuntime ari = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(advid);
				adtasks[0] = ari.fetchTaskStrong();
			}
			adtasks[0].setParent_advid(this.advinfo.getAdv_id());
			count--;
			if (count <= 0) {
				charge_adv_notimelimit.remove(advid);
			} else {
				charge_adv_notimelimit.put(advid, count);
			}
			handleFechTaskFinish(advid);
			if(adtasks.length > 0 && adtasks[0] != null)
				this.incDoingCount(dev_tag);
			return adtasks;
		}
	}

	@Override
	public TaskBase fetchTaskStrong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkCurHourFinishStatus() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getTaskRuntimeInfo() {
		// TODO Auto-generated method stub
		String sruntime = "";
		// int adv_id = this.advinfo.getAdv_id();
		sruntime += "多任务充值2:" + advinfo.getName() + "[id:" + advinfo.getAdv_id() + "]]\r\n";

		int ret_count = daycount;// advinfo.getDayusercount();
		if (this.result != null) {
			ret_count -= result.getNewuser_success_count();
		}
		sruntime += "状态:";
		if (ret_count <= 0)
			sruntime += "***完成***\r\n";
		else
			sruntime += "未完成(" + ret_count + ")\r\n";
		Calendar date = Calendar.getInstance();
		int curhour = date.get(Calendar.HOUR_OF_DAY);
		sruntime += "每天执行用户总数:" + daycount + "\r\n";
		sruntime += "正在执行的数:" + this.getDoingCount() + "\r\n";
		//sruntime += "当前小时分布数:" + this.docount_hours[curhour] + ";剩下数:" + this.retcount_hours[curhour];

		if (this.result != null) {
			sruntime += "今天成功用户数:" + result.getNewuser_success_count() + "\r\n";
			sruntime += "今天失败用户数:" + result.getNewuser_err_count() + "\r\n";
			sruntime += "今天成功活跃数:" + result.getNewuser_success_opentcount() + "\r\n";
			sruntime += "今天失败活跃:" + result.getNewuser_err_opentcount() + "\r\n";
		} else {
			sruntime += "今天成功执行数:" + 0 + "\r\n";
			// sruntime += "今天失败执行数:" + 0 + "\r\n";
		}
		log.info(sruntime);
		return sruntime;
	}

	@Override
	public int caclMaxHoureCountOfAllDate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFinish() {
		// TODO Auto-generated method stub
		if (charge_adv_notimelimit.size() > 0)
			return true;
		return false;
	}

}
