package com.kilotrees.services;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.qqaccountdao;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.qqaacount_info;
import com.kilotrees.util.StringUtil;
/**
 * 
 * QQ帐号辅助登录，实现2大功能
 * 1:客户端请求QQ帐号密码，此服务从adv_id对应的业务名称(要英文名,可能要加个别名)的表，取未使用的帐号密码
 * 2:如果客户端报告QQ登录结果，如果登录失败，要更改此Q的状态
 * 数据表处理：
 * 我们建一张原始表tb_qqacount记录买回来的QQ帐号密码，并有个字段设置状态，比如此Q是否还有用
 * 当我们开启一个新业务时，因为每个业务QQ用过一次就不能用了，要新建一张业务表，名称用tb_qqaccount_advalias把原始表的记录加到新业务表中，这个业务上来取帐号密码时，就在这张新表中取
 * 分离业务表和原始表，是为了多个业务都用到QQ帐号时，避免取未使用的QQ帐号时交叉查询
 * @author Administrator
 *
 */
public class qqaccount_service {
	private static Logger log = Logger.getLogger(qqaccount_service.class);
	private static qqaccount_service inst;

	private qqaccount_service() {

	}

	public static qqaccount_service getInstance() {
		synchronized (qqaccount_service.class) {
			if (inst == null) {
				inst = new qqaccount_service();
			}
		}
		return inst;
	}
	
	public JSONObject handleRequest(JSONObject jsoRequest) throws JSONException
	{
		JSONObject rsp = null;
		int action = jsoRequest.optInt("action");
		int adv_id = jsoRequest.optInt("adv_id");
		long autoid = jsoRequest.optLong("autoid");
		if(action == 1) {
			String dev_tag = jsoRequest.optString("dev_tag");
			rsp = handleGetAccount(adv_id,dev_tag);
		}
		else if(action == 2)
		{
			String qqnum = jsoRequest.optString("qqnum");
			int result = jsoRequest.optInt("result",-3);
			String info = jsoRequest.optString("info","not_upload");
			rsp = handleGetAccountResult(adv_id,autoid,qqnum,result,info);			
		}
		return rsp;
	}
	
	/**拿QQ号
	 * @param adv_id 
	 * @param dev_tag
	 * @return
	 * @throws JSONException
	 */
	private JSONObject handleGetAccount(int adv_id,String dev_tag) throws JSONException
	{
		JSONObject rsp = null;
		String tableName = "tb_qqaccount_";
		advtaskinfo ai = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
		String alias = ai.getAlias().trim();
		if(!StringUtil.isStringEmpty(alias)) {
			tableName += alias;
		}else {
			log.error(this.getClass().getName()+" : 缺少alias，拿不到QQ号");
		}
		qqaacount_info qqi = qqaccountdao.getQQAccountFirstTime(tableName);
		if(qqi == null) {
			qqi = qqaccountdao.getQQAccount(tableName);
		}
		qqaccountdao.updateFetchStatus(tableName,qqi);
		rsp = new JSONObject();
		rsp.put("qqnum", qqi.getQqnum());
		rsp.put("pass", qqi.getPass());		
		rsp.put("autoid", qqi.getAutoid());		
		return rsp;
	}
	
	/**action = 2 返回结果
	 * @param adv_id
	 * @param autoid
	 * @param qqnum
	 * @param result -1:login_failure -2:network_failure -3:not_upload 0:nouse 1:success 2:inuse 
	 * @param info
	 * @return
	 * @throws JSONException
	 */
	private JSONObject handleGetAccountResult(int adv_id,long autoid,String qqnum,int result,String info) throws JSONException
	{
		String tableName = "tb_qqaccount_";
		advtaskinfo ai = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
		tableName += ai.getAlias().trim();
		
		
		qqaacount_info qqi = new qqaacount_info();
		qqi.setAutoid(autoid);
		qqi.setQqnum(qqnum);
		qqi.setResult(result);
		qqi.setInfo(info);
		qqaccountdao.updateResultStatus(tableName, qqi);
		
		//2019-1-17 登陆结果不成功时，回复QQ号状态，让它下次继续被拿到
		if(result == -1 || result == -2) {
			qqaccountdao.resetStatus(qqi);
		}
		
		
		//登录失败，此帐号可能有问题，更新原始表
		if(result != 1)
		{
			//有可能是因为网络原因导致失败，所以先不更新原始表了
//			qqaccountdao.updateOrgTableStatus(qqi);
		}
		
		
		
		JSONObject rsp = new JSONObject();
		rsp.put("result", "ok");
		return rsp;
	}
	
}
