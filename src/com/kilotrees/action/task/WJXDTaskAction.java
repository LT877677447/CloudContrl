/**
 * @author Administrator
 * 2019年3月19日 下午10:21:11 
 */
package com.kilotrees.action.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.services.JsonActionService;

public class WJXDTaskAction implements ITaskAction{
	private static List<String> sDvices = new ArrayList<>();//设备第一次做任务
	private static Map<String, Integer> map1 = null;//30游客 || 70注册用户
	private static Map<String, Integer> map2 = null;//8% 15-20 minute
	private static String packageName = "game.app.m.wjxd";
	private Logger log = Logger.getLogger(WJXDTaskAction.class);
	
	private void MyInitMap() {
		if(null == map1) {
			map1  = new HashMap<>();
			map1.put("游客", 3);
			map1.put("注册用户", 7);
		}
		if(null == map2) {
			map2 = new HashMap<>();
			map2.put("long", 2);
			map2.put("short", 23);
		}
	}

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		
		String dev_tag = response.optString("dev_tag");
		
		//不让AOS设备做
		if(dev_tag.startsWith("AOS")) {
			adtasks = new JSONArray();
			response.put("tasks", new JSONArray());
		}
		
		if(adtasks.length() > 0) {
			//wuji20
			JSONObject jsonVPN = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_20.apk", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_20.apk");
			prefix_task_actions.put(jsonVPN);
			
			//初始化map ======================================================================================================
			MyInitMap();
			//游客 or 注册用户
			boolean Tourists = Tourists(); //是否下发游客
			
			// 是否 8% 15-20 minute
			boolean longTime = LongTime(); //是否 8% 15-20 minute
			
			adtask.put("8Percent", longTime); //是否 8% 的 做 15-20 分钟   true:是  false:不是
			adtask.put("Tourists", Tourists); //是否游客  true:是  false:不是
			//初始化map ======================================================================================================
			
			//处理解压
			JSONObject unjson = JsonActionService.createAction_UNZIP_REMOTE_FILE(adtask.optString("packageName"), "hotUpdate.zip", ServerConfig.getStorageBaseURL() + "Resources/wjxd/hotUpdate.zip");
			prefix_task_actions.put(unjson);
			
			
			//处理每个设备第一次拿任务
			boolean deviceDone = false;
			for(String string : sDvices) {
				if(string.equals(dev_tag)) {
					deviceDone = true;
					break;
				}
				continue;	
			}
			
			if(!deviceDone) {
				JSONArray array = new JSONArray();
				array.put("/sdcard/.ccache/");
				
				JSONObject jsonClearDirs = JsonActionService.createAction_CLEAR_DIRS(array);
				prefix_task_actions.put(jsonClearDirs);
				sDvices.add(dev_tag);
			}
			//end	
			
		}
		
		
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		
	}

	
	private boolean Tourists() {
		boolean choose = new Random().nextInt(2) == 0;
		if(choose) {
			//游客
			Integer tourists = map1.get("游客");
			if(tourists <= 0) {
				//游客已用完
				Integer user = map1.get("注册用户");
				if(user <= 0) {
					//需要重新装填
					map1.put("游客", 3);
					map1.put("注册用户", 7);
					return Tourists();
				}else {
					//下发注册用户
					user--;
					map1.put("注册用户", user);
					return false;
				}
			}else {
				//下发游客
				tourists--;
				map1.put("游客", tourists);
				return true;
			}
		}else {
			//注册用户
			Integer user = map1.get("注册用户");
			if(user <= 0) {
				//注册用户已用完
				Integer tourists = map1.get("游客");
				if(tourists <= 0) {
					//需要重新装填
					map1.put("游客", 3);
					map1.put("注册用户", 7);
					return Tourists();
				}else {
					//下发游客
					tourists--;
					map1.put("游客", tourists);
					return true;
				}
			}else {
				//下发注册用户
				user--;
				map1.put("注册用户", user);
				return false;
			}
		}
	}
	
	private boolean LongTime() {
		boolean choose2 = new Random().nextInt(2) == 0;
		if(choose2) {
			//预备下发short
			Integer int_short = map2.get("short"); 
			if(int_short <= 0) {
				//short用完了
				Integer int_long = map2.get("long"); 
				if(int_long <= 0) {
					//重新装填
					map2.put("long", 2);
					map2.put("short", 23);
					return LongTime();
				}else {
					//下发long
					int_long--;
					map2.put("long", int_long);
					return true;
				}
			}else {
				//下发short
				int_short--;
				map2.put("short", int_short);
				return false;
			}
		}else {
			//预备下发long
			Integer int_long = map2.get("long"); 
			if(int_long <= 0) {
				//long用完了
				Integer int_short = map2.get("short"); 
				if(int_short <= 0) {
					//重新装填
					map2.put("long", 2);
					map2.put("short", 23);
					return LongTime();
				}else {
					//下发short
					int_short--;
					map2.put("short", int_short);
					return false;
				}
			}else {
				//下发long
				int_long--;
				map2.put("long", int_long);
				return true;
			}
		}
	}
	
}
