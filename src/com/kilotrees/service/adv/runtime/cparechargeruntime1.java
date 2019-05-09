package com.kilotrees.service.adv.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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

/**
 * 按时间曲线充值任务，适合于小量冲值，如果要大量无时间限制冲值，就用cparechargeruntime2这个类，在任务列表中，此任务的adv_type=2，并自动hand_lock固定的设备，为其它任务的apk充值。
 * 此任务的apkidf填写为0，没有自己真正的cpa广告操作，此任务的dayusercount只要设置大于0就行了，我们称为反向分配
 * 可以在json文件中按日期，小时分配多个充值任务比如： {
 * "recharge_advs":[{"date":"2018-12-29","hour":7,"advs":"12;13;12"},{"date":"2018-12-29","hour":19,"advs":"15"}]
 * } 上面表示2018-12-29 07小时给advid=12充值2次，advid=13的任务充值一次;在18小时给advid=15的任务充值1次,type表示这种是按小时分配，
 * 如果不按时间 的话，就用cparechargeruntime2这个
 */
public class cparechargeruntime1 extends advruntimebase {
	private static Logger log = Logger.getLogger(cparechargeruntime1.class);
	// 记录每个小时要做的广告HashMap，key表示小时，可以多个，用分号分开
	HashMap<Integer, ArrayList<Integer>> charge_advs = new HashMap<Integer, ArrayList<Integer>>();	
	//保存充值结果
	JSONObject myJson = new JSONObject();
	//暂时把结果保存在本地，以后需要的话，保存在数据库中
	static String myJSonPath = "d:/webapps/yunctrl/charge/";
	int json_version = -1;
	//cpanewruntime cpanewInst;
	int daycount = 0;
	

	public void setAdvinfo(advtaskinfo _advinfo) {
		super.setAdvinfo(_advinfo);
		this.advinfo.setHandle_Locked(1);
		this.refresh();
		//caclRuntimeHouursCount();
	}

	public cparechargeruntime1() {
		// caclRuntimeHouursCount();
	}
	/**
	 * 这个不需要
	 * @param _advinfo
	 */
//	public cparechargeruntime(advtaskinfo _advinfo) {
//		// this.advinfo = _advinfo;
//		// caclRuntimeHouursCount();
//	}
	
	public int getAllTodocount()
	{
		return this.daycount;
	}

	@Override
	public int getReqDevDoCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void refresh() 
	{
		if(this.isOffline())
			return;
		String path = ServerConfig.getConfigJson().optString("chargeresult_path","");
		if(path.length() > 0)
			myJSonPath = path;
		if(myJSonPath.endsWith("/") == false)
			myJSonPath += "/";
		//super.refresh();
		JSONObject jsoExt = this.advinfo.getExtJso();
		//修改配置文件时要增加version的值
		int version = jsoExt.optInt("version", 0);
		if (version == json_version)
			return;
		json_version = version;
		JSONArray array = jsoExt.optJSONArray("recharge_advs");
		String todayDate = DateUtil.getDateBeginString(new Date());
		Calendar date = Calendar.getInstance();
		int curhour = date.get(Calendar.HOUR_OF_DAY);
		daycount = 0;
		// int hour = 0;//date.get(Calendar.HOUR_OF_DAY);
		if (array != null && array.length() > 0) {
			for (int i = 0; i < array.length(); i++) {
				try {
					JSONObject jsItem = array.getJSONObject(i);
					String doDay = jsItem.optString("date", "");
					if(doDay.length() == 0)
					{
						log.error("缺少date这个key");
						continue;
					}
					if (todayDate.equals(doDay) == false)
						continue;// 和今天不是同一日期
					int hour = jsItem.optInt("hour", -1);
					if (hour == -1) {
						log.error("缺少hour这个key");
						continue;
					}

					// log.info("");
					String advs = jsItem.optString("advs", "");
					if (advs.length() == 0) {
						log.error("缺少advs这个key");
						continue;
					}
					advs = advs.replaceAll(",", ";");
					String[] ss = advs.split(";");
					ArrayList<Integer> ls = new ArrayList<Integer>();
					for (int j = 0; j < ss.length; j++)
						ls.add(Integer.parseInt(ss[j]));
					if(hour >= curhour) {
						synchronized (charge_advs) {
							charge_advs.put(hour, ls);
						}
						this.docount_hours[hour] = ls.size();
						this.retcount_hours[hour] = ls.size();
					}
					daycount += ls.size();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					log.error(e.getMessage(), e);
				}
			}
		}
		//重启时看哪些已经做过了，剔除做过了的
		loadFinishJson();
		// Calendar date = Calendar.getInstance();

		ArrayList<Integer> ls = null;
		synchronized (charge_advs) {
			ls = charge_advs.get(curhour);
		}
		if (ls != null) {
			//为方便读出对应日期和小时的值，直接把日期加小时保存起来
			String jsoDone = myJson.optString("chargedone_" + todayDate + ":" + curhour, "");
			if (jsoDone.length() > 0) {
				String[] ss = jsoDone.split(";");
				for (int i = 0; i < ss.length; i++) {
					int advid = Integer.parseInt(ss[i]);
					// 一定要用new Integer类型，不然会按序列号删除
					ls.remove(new Integer(advid));
				}
				// this.docount_hours[curhour] = ls.size();
				this.retcount_hours[curhour] = ls.size();
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
		//String fileName = serverconfig.contextRealPath + "files/extjson/charge_" + adv_id + "_" + name.trim() + ".json";
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

	void handleFechTaskFinish(int curhour, int adv_id) {
		String todayDate = DateUtil.getDateBeginString(new Date());
		String jsoDone = myJson.optString("chargedone_" + todayDate + ":" + curhour, "");
		if (jsoDone.length() > 0)
			jsoDone += ";" + adv_id;
		else
			jsoDone = "" + adv_id;
		try {
			myJson.put("chargedone_" + todayDate + ":" + curhour, jsoDone);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.retcount_hours[curhour] -= 1;
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
		if (charge_advs.isEmpty())
			return new TaskBase[0];
		int advid = -1;
		TaskBase[] adtasks = new TaskBase[0];
		Calendar date = Calendar.getInstance();
		int curhour = date.get(Calendar.HOUR_OF_DAY);
		ArrayList<Integer> ls = charge_advs.get(curhour);
		if (ls != null && ls.size() > 0) {
			adtasks = new TaskBase[1];
			
			advid = ls.get(0);
			if (advid < 0) {
				// 负数表示留存，暂时不处理
			} else if (advid > 0) {
				ITaskRuntime ari = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(advid);
				adtasks[0] = ari.fetchTaskStrong();
			}
			adtasks[0].setParent_advid(this.advinfo.getAdv_id());
			ls.remove(new Integer(advid));
			if (ls.isEmpty()) {
				synchronized (charge_advs) {
					charge_advs.remove(curhour);
				}
			}

			handleFechTaskFinish(curhour, advid);
		}
		if(adtasks.length > 0 && adtasks[0] != null)
			this.incDoingCount(dev_tag);
		return adtasks;
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
		sruntime += "多任务充值:" + advinfo.getName() + "[id:" + advinfo.getAdv_id() + "]]\r\n";

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
		sruntime += "当前小时分布数:" + this.docount_hours[curhour] + ";剩下数:" + this.retcount_hours[curhour];

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
		if (charge_advs.size() > 0)
			return true;
		return false;
	}

	public static void main(String[] argv) {
		String[] a = "".split(";");

		String s = String.join(";", a);
		int[] c = new int[24];
		System.out.println(s + c);
		ArrayList<Integer> list = new ArrayList<Integer>();
		int k = 4;

		list.add(2);
		list.add(3);
		list.add(1);
		list.add(k);
		list.add(1);
		list.add(1);

		System.out.println(list);
		list.remove(new Integer(1));
		System.out.println(list);
		list.remove(new Integer(1));
		list.remove(new Integer(3));
		System.out.println(list);
		list.remove(1);
		System.out.println(list);
	}
}
