package com.kilotrees.model.bo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.json.JSONObject;

import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.services.autoid_service;
import com.kilotrees.util.JSONObjectUtil;

/*
 * 属性名字对应接口API JSON的KEY值, 因此改动属性名需要与前端沟通规范好 
 */
@SuppressWarnings("unused")
public class TaskBase {

	public static final int TASK_ID_NEWLY_TASK = 0; // 新增任务ID
	public static final int TASK_ID_REMAIN_TASK = -9; // 留存任务ID

	
	public static final int TASK_PHASE_NEWLY = 1; // 新增 阶段
	public static final int TASK_PHASE_REMAIN = 2; // 留存 阶段
	
	
	private int taskPhase;
	private String taskType;
	private String taskClass = this.getClass().getName();
	

	

	// 任务id，如果是新增任务，其值是0，如果是留存任务，其值为-9
	private int taskid = TASK_ID_NEWLY_TASK;

	private long autoid;

	private int adv_id;

	// 广告类别，具体使用看advtaskinfo有说明
	private int adv_type;

	private String dev_tag;

	// 临时分配给对应设备，设备原本分配了某个广告，但因为某个时间比较空闲而去执行任务量比较重的广告
	private boolean alloctemp;

	// 是否锁定机器，如果是，只能用上面dev_tag的机器做留存，这个太复杂了，暂时不做这种广告
	private int lock_dev;

	// 下发任务时指定使用时长
	private int scriptTimeout = 5 * 60;

	// 脚本运行参数
	private String scriptRunParam = "";

	// 要压缩的文件名,包括路径
	private String zipfiles = "";

	private String packageName;

	// 增加一个字段，appinfo，分离之前的phonetypinfo，把那些留存中经常变化的放在appinfo中
	private JSONObject appInfo;
	
	// 新增时的机器信息
	private JSONObject phoneInfo;

	private String alias;

	// 增加一个父id，一般是充值时的充值任务id
	private int parent_advid;
	
	
	
	

	public int getTaskPhase() {
		return taskPhase;
	}

	public void setTaskPhase(int taskPhase) {
		this.taskPhase = taskPhase;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	
	
	
	public int getTaskid() {
		return taskid;
	}

	public void setTaskid(int taskid) {
		this.taskid = taskid;
	}

	public long getAutoid() {
		return autoid;
	}

	public void setAutoid(long autoid) {
		this.autoid = autoid;
	}

	public int getAdv_id() {
		return adv_id;
	}

	public void setAdv_id(int adv_id) {
		this.adv_id = adv_id;
	}

	public int getAdv_type() {
		return adv_type;
	}

	public void setAdv_type(int adv_type) {
		this.adv_type = adv_type;
	}

	public String getDev_tag() {
		return dev_tag;
	}

	public void setDev_tag(String dev_tag) {
		this.dev_tag = dev_tag;
	}

	public boolean isAlloctemp() {
		return alloctemp;
	}

	public void setAlloctemp(boolean alloctemp) {
		this.alloctemp = alloctemp;
	}

	public int getLock_dev() {
		return lock_dev;
	}

	public void setLock_dev(int lock_dev) {
		this.lock_dev = lock_dev;
	}

	public int getScriptTimeout() {
		return scriptTimeout;
	}

	public void setScriptTimeout(int scriptTimeout) {
		this.scriptTimeout = scriptTimeout;
	}

	public String getScriptRunParam() {
		return scriptRunParam;
	}

	public void setScriptRunParam(String scriptRunParam) {
		this.scriptRunParam = scriptRunParam;
	}

	public String getZipfiles() {
		return zipfiles;
	}

	public void setZipfiles(String zipfiles) {
		this.zipfiles = zipfiles;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public JSONObject getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(JSONObject appInfo) {
		this.appInfo = appInfo;
	}

	public JSONObject getPhoneInfo() {
		return phoneInfo;
	}

	public void setPhoneInfo(JSONObject phoneInfo) {
		this.phoneInfo = phoneInfo;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getParent_advid() {
		return parent_advid;
	}

	public void setParent_advid(int parent_advid) {
		this.parent_advid = parent_advid;
	}
	
	/*
	 * PO TO BO/VO
	 */
	public void setTaskInfo(advtaskinfo advinfo) {
		this.setTaskid(TASK_ID_NEWLY_TASK);
		
		this.setAutoid(autoid_service.getMaxAutoid());

		this.setAdv_id(advinfo.getAdv_id());
		
		this.setAdv_type(advinfo.getAdv_type());
		
//		this.setDev_tag(dev_tag); 	// set it outside now...
		
//		this.setAlloctemp(alloctemp);	// unknown now ....
		
		this.setLock_dev(advinfo.getRemain_lock_dev());
		
//		this.setScripttimeout(advinfo.getRequesttime());
		
//		this.setScriptRunParam(apkInfo.getRegscriptparams());	// set it outside now...
		
//		this.setZipfiles(apkInfo.getZipfiles());	// set it outside now...
		
//		this.setPackageName(apkInfo.getPackagename());	// set it outside now...
		
//		this.setAppInfo(appInfo);	// set it outside now...
		
//		this.setPhoneInfo(appInfo);	// set it outside now...
		
		this.setAlias(advinfo.getAlias());
		
//		this.setParent_advid(xxx);		// set it outside now...
	}

	/**TaskBase及其子类的 !static，!final 属性放到JsonObject中
	 * @return
	 */
	public JSONObject toJSONObject() {
		int superClassDepth = 0;
		Class<?> clazz = this.getClass();
		
		while (clazz != TaskBase.class) {
			superClassDepth++;
			clazz = clazz.getSuperclass();
		}
		
		// i.e TaskBase -> TaskFeedFans -> TaskFeedFansQQ -> TaskFeedFansQQZone : superClassDepth = 3
			
		JSONObject json = JSONObjectUtil.objectToJSONObject(this, superClassDepth, new JSONObjectUtil.FieldFilter() {
			@Override
			public boolean filterAction(Object obj, Field field) {
				boolean isFinal = Modifier.isFinal(field.getModifiers());
				boolean isStatic = Modifier.isStatic(field.getModifiers());
				return isFinal || isStatic;
			}
		});
		return json;
	}
	
	/**从JSON中拿出object直到TaskBase类的所有属性，如果有就设置
	 * @param json 拿来设置的JSON对象
	 */
	public void setWithJSONObject(JSONObject json) {
		int superClassDepth = 0;
		Class<?> clazz = this.getClass();
		
		while (clazz != TaskBase.class) {
			superClassDepth++;
			clazz = clazz.getSuperclass();
		}
		
		JSONObjectUtil.setJSONObjectToObject(superClassDepth, this, json);
	}

}
