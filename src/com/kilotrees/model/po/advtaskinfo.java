package com.kilotrees.model.po;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import org.json.JSONObject;

import com.kilotrees.services.advgroup_service;

/**
 * 广告任务表，程序启动时扫描这张表，执行上面有效的任务
 *
CREATE TABLE [dbo].[tb_advtaskinfo](
	[adv_id] [int] NOT NULL,
	[name] [varchar](50) NULL,
	[prior] [int] NULL,
	[adv_type] [int] NULL,
	[apkid] [int] NULL,
	[cpid] [int] NULL,
	[channelid] [int] NULL,
	[bdid] [int] NULL,
	[start_date] [datetime] NULL,
	[end_date] [datetime] NULL,
	[adv_content] [varchar](500) NULL,
	[serverbean] [varchar](255) NULL,
	[params] [varchar](500) NULL,
	[clientbean_info] [varchar](300) NULL,
	[requesttime] [int] NULL,
	[remaintime] [int] NULL,
	[timeline] [int] NULL,
	[rem_timeline] [int] NULL,
	[remain_lock_dev] [int] NULL,
	[remain_rule] [varchar](500) NULL,
	[allcount] [int] NULL,
	[dayopencount] [int] NULL,
	[dayusercount] [int] NULL,
	[alldocount] [int] NULL,
	[ext] [varchar](1000) NULL,
	[onlineflag] [int] NULL,
	[online_time] [datetime] NULL,
	[alias] [nvarchar](50) NULL,
 CONSTRAINT [PK_tb_advtaskinfo] PRIMARY KEY CLUSTERED 
(
	[adv_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 * 
 * GO
 */
public class advtaskinfo {
	public static String tablename = "tb_advtaskinfo";
	//普通cpa
	public final static int ADVTYPE_CPA_NORMAL = 1;
	//充值广告，专门对几个任务进行充值，而且按时间曲线来充值,如果充值只对一个任务，那么应该用上面ADVTYPE_CPA_NORMAL模式
	public final static int ADVTYPE_CPA_RECHARGE1 = 2;
	public final static int ADVTYPE_CPA_RECHARGE2 = 3;
	//诸如QQ，抖音，头条，快手之类的用户帐号定期活跃任务。
	public final static int ADVTYPE_FANSACT = 10;
	int adv_id;
	// 广告名秒
	String name;
	// 优先级
	int prior;
	// 广告类别，用数值10左右的范围模式，方便以后拓展，比如cpa正常用1-10之间数值,如果想标明cpa中是否冲量或其它特别的区分之类，就用2
	// 1-9:cpa新增之类，1：普通cpa，2：充值，可以为多个广告充值（反向分配)
	// 10-19:养粉
	// 20-29:某通网站或视频(不修改机器信息，但切换vpn)
	// 30-:云控例行性工作(不修改机器信息，不切换vpn，一台机只执行一次),30表示每台机每天都执行一次，31表示每台机器只执行一次．用户手工自行关闭任务
	// 例行广告每个设备都需执行，不用程序主动分配，手工直接上线
	int adv_type;
	int apkid;
	int cpid;
	int channelid;// 暂时没有用
	int bdid;// 商务id
	// 分组id，对于可以同一时间并行执行的广告来说，可以分成组，设备每次取一组数据，同时执行几个广告,如果不分组的话，此广告单一执行
	// 默认为0,表示不分组
	// int groupid;
	// 开始日期
	Date start_date;
	// 结束日期，一般无限大
	Date end_date;
	// 象url这种信息，备用
	String adv_content;
	// 广告服务组件，专门处理客户端脚本交互，这里记录bean的class名，如果为空就用默认值com.zfcom.cft.serverbean.serverbean_'advid'
	//2018-12-27这里改为serverbenid,比如httplinks_1，直接用id表，也可以用原来的class名
	//String serverbeanclass;
	String serverbeanid;
	// 参数这个暂时没有用，脚本运行参数在apkinfo中定义
	String params;
	// 广告客户端动态加载bean，这里包括bean名称;版本;class名，参数．用分号分开
	String clientbean_info = "";
	// 新增广告预计平均执行时间，以秒为单位，要加上vpn拔号和替换机器信息时间，如果此广告属于某个组，则使用advgroup的maxdotime
	int requesttime;
	// 留存预计平均时间,如果小于等于0，表示不做留存
	int remaintime;
	// 新增是否要时间曲线，0，通用留存曲线(在1-7点适当降一下数量)，其它表示留存曲线的id值，存在另一个表中
	int timeline;
	// 留存曲线
	int rem_timeline;
	// 做留存时是否要和新增在同一台机上象养粉这种就最好在同一机器上，另外如果新增用到的vpn和留存用到的vpn不是同一地区，比较麻烦
	int remain_lock_dev;
	// 留存规则，日活，周，双周，月，两月，格式：40;30;20;15;10;
	// 如果遇到-1,表示后面的不做了，比如40;20;-1,-1;-1表示做完周留存就不做了
	// 如果是空，则不做留存
	String remain_rule;
	// 执行总数，-1，表示每个设备每天执行一次，-2表示每个设备只执行一次,0表示停止
	int allcount;
	// 每个用户一天打开次数（活跃数），之前由于旧的经验，广告主要求每个广告一天打开多少次，我们就固定一天必须打开多少次。
	// 这个理解已经过时了，正常情况下，一个广告不管新增或留存,一天应该随机一天打开1-5次，我们把3次以上的个数限制在极少数量
	// 现在dayopencount设为0时，按上面随机打开几次来做，当大于0时，说觉广告主强制要求象以前旧经验一样做。
	// 如果广告主非要每天固定打开多少次，我们就把这个数设置为固定打开数，以后再实现
	// modify 2018-10-22
	int dayopencount;
	// 每天新增用户数，这里最好取名为dayusercount.
	//int daycount;
	//2018-12-7 改成dayusercount
	int dayusercount;
	// 已经执行完成总数（包括以前)，每做完一个广告加1
	int alldocount;
	// 扩展参数,广告用到的参数实在太多了，这里引用一个外部文件，
	// 比如指定每天开始时间结束时间(小时)，或者地区，机型之类内容用json形式表示，
	// 列如限定机型:{phonetype:"huawei;xiaomi";area:"广东;江苏",litmithours:"0,1,2,3,4,5,6"}
	String ext;
	// 加入自定义
	// 分组id
	int groupid;
	/*
	 * 上线标志， 0:表示已经下线,但如果有留存继续做; 1:表示上线，任务的设备已经分配好了；
	 * 2:新增和留存都停由于质量问题或cp要求，连留存也强制关闭，
	 * 3:表示准备上线，服务器自动计算需要的设备数量，如果有足够空闲设备，自动分配后修改标识后设为０并上线 4:由手工分配设备，手工设置上线
	 * 我们上广告时，最好先把这个数设定为
	 */
	int onlineflag;
	// 第一次分配机器后上线时间
	Date online_time;
	// 护展参数
	/**
	 * 广告拓展json文件 例如:advext_12_网易云音乐.json
	 */
	JSONObject extJso = new JSONObject();
	/**
	 * 28号测试任务
	 */
	// 新增随机打开次数比例
	// 2018-12-7，使用HashMap来严格随机,不能用静态
	// int[] openCountScale_new;
	//下面功能移到advruntimebase中实现
//	HashMap<Object, Integer> opencount_org = new HashMap<Object, Integer>();
//	HashMap<Object, Integer> opencount_rand = null;// new HashMap<Object,
//	HashMap<Object, Integer> opencount_rand_remain;
//													// Integer>();
//	String openCountScale = "";
	//一般用于自动充值这种专做某种特别任务机器,这种情况下任务由指定的handle_locded的机器来执行，其它机器不要执行
	//2018-12-13对于留存的任务话，以后考虑我们分情况，对于其它没有分配此任务的机器：1，不能执行新增和留存，2：不能做新增，但可以做留存?
	int handle_Locked;
	String alias;

	//
	// String clientBeanName = "";
	// String clientBeanClass = "";
	// String clientBeanParams = "";

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getAdv_id() {
		return adv_id;
	}

	public void setAdv_id(int adv_id) {
		this.adv_id = adv_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrior() {
		return prior;
	}

	public void setPrior(int prior) {
		this.prior = prior;
	}

	public int getAdv_type() {
		return adv_type;
	}

	public void setAdv_type(int adv_type) {
		this.adv_type = adv_type;
	}

	public int getApkid() {
		return apkid;
	}

	public void setApkid(int apkid) {
		this.apkid = apkid;
	}

	public int getCpid() {
		return cpid;
	}

	public void setCpid(int cpid) {
		this.cpid = cpid;
	}

	public int getChannelid() {
		return channelid;
	}

	public void setChannelid(int channelid) {
		this.channelid = channelid;
	}

	public int getBdid() {
		return bdid;
	}

	public void setBdid(int bdid) {
		this.bdid = bdid;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public String getAdv_content() {
		return adv_content;
	}

	public void setAdv_content(String adv_content) {
		this.adv_content = adv_content;
	}

	

	public String getServerbeanid() {
		return serverbeanid;
	}

	public void setServerbeanid(String serverbeanid) {
		this.serverbeanid = serverbeanid;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params.trim();
	}

	public int getAllcount() {
		return allcount;
	}

	public void setAllcount(int allcount) {
		this.allcount = allcount;
	}

	public int getDayopencount() {
		return dayopencount;
	}

	public void setDayopencount(int dayopencount) {
		if (dayopencount < 0)
			dayopencount = 0;
		this.dayopencount = dayopencount;
	}
//
//	public int getDaycount() {
//		return daycount;
//	}
//
//	public void setDaycount(int daycount) {
//		this.daycount = daycount;
//	}

	
	public int getAlldocount() {
		return alldocount;
	}

	public int getDayusercount() {
		return dayusercount;
	}

	public void setDayusercount(int dayusercount) {
		this.dayusercount = dayusercount;
	}

	public void setAlldocount(int alldocount) {
		this.alldocount = alldocount;
	}

	public int getRequesttime() {
		// 如果这里是组成员，返回组成员表的maxdotime
		advgroup g = advgroup_service.getInstance().getGroupIncludeAdv(adv_id, false);
		if (g != null)
			return g.getMaxdotime();
		return requesttime;
	}

	public void setRequesttime(int requesttime) {
		this.requesttime = requesttime;
	}

	public int getRemaintime() {
		return remaintime;
	}

	public void setRemaintime(int remaintime) {
		this.remaintime = remaintime;
	}

	public int getTimeline() {
		return timeline;
	}

	public void setTimeline(int timeline) {
		this.timeline = timeline;
	}

	public int getRem_timeline() {
		return rem_timeline;
	}

	public void setRem_timeline(int rem_timeline) {
		this.rem_timeline = rem_timeline;
	}

	public String getRemain_rule() {
		return remain_rule;
	}

	public void setRemain_rule(String remain_rule) {
		this.remain_rule = remain_rule;
	}

	public int getRemain_lock_dev() {
		return remain_lock_dev;
	}

	public void setRemain_lock_dev(int remain_lock_dev) {
		if(remain_lock_dev != 0)
			remain_lock_dev = 0;
		this.remain_lock_dev = remain_lock_dev;
	}
	//
	// public String getExt() {
	// return ext;
	// }

	public void setExt(String ext) {
		if (ext == null) {
			ext = "";
		}
		this.ext = ext.trim();
		loadExtJsonFile();
	}

	/**
	 * 加载advtaskinfo的json文件，例如:advext_12_网易云音乐.json
	 */
	private void loadExtJsonFile() {
		String fileName = ServerConfig.contextRealPath + "files/extjson/advext_" + adv_id + "_" + name.trim() +".json";
		File file = new File(fileName);
		// 2018-12-7，这里直接用advext_id.json，不需要在ext中设置文件名的值，但这个时候id一定要设置完成
		if (file.exists()) {
			try {
				FileInputStream fins = new FileInputStream(file);
				byte[] buf = new byte[(int) file.length()];
				fins.read(buf);
				fins.close();
				String s = new String(buf, "utf-8");
				this.extJso = new JSONObject(s);
//				
				//下面逻辑处理放到advruntimebase中实现
//				this.handle_Locked = this.extJso.optInt("hand_locked", 0);
//
//				String key = DateUtil.getDateBeginString(new Date());
//				JSONObject jsoDayUser = extJso.optJSONObject("dayusercount");
//				if(jsoDayUser != null) {
//					int c = jsoDayUser.optInt(key);
//					if(c != -1) {
//						this.dayusercount = c;
//					}
//				}
				
				
				//下面功能移到advruntimebase中实现
//				// modify
//				// 2018-10-22,正常情况下，dayopencount=0，我们随机让用户做1-5次，按json文件定义好的比重来处理
//				// 格式如下:ad_dayopencount:"50;20;20;7;3"表示打开1次的占50%，打开2次的占20，依次类推
//
//				// 先从自己的配置文件中读出,下面可以把新增和留存分开设置(以后再说)
//				// 如果客户指定次数，就不用随机，不过这不切实际
//				if (this.dayopencount > 0) {
//					return;
//				}
//				//之前总数一定要设为１００，现在可以总数任意值，最好１０，比如＂3;5;2＂
//				String ad_dayopencount = "";
//				if (this.getExtJso() != null && this.getExtJso().optString("dayopencount_new", "").length() > 0)
//					ad_dayopencount = this.getExtJso().optString("dayopencount_new", "");
//				else// 使用通用模板
//				{
//					//在系统初始化时,serviceconfig_service.getInstance().getConfig()为空;// serverconfigdao.getServerConfig();
//					serverconfig sc  = serviceconfig_service.getInstance().getConfig();
//					if(sc == null){
//						sc = new serverconfig();
//						sc.loadFile();
//					}
//					ad_dayopencount = serviceconfig_service.getInstance().getConfig().getConfigJso()
//							.optString("ad_dayopencount_0", "3;5;2");
//				}
//				//判断有没有变化
//				if (openCountScale.equals(ad_dayopencount))
//					return;
//				openCountScale = ad_dayopencount;
//				//防止不小心用了逗号
//				ad_dayopencount = ad_dayopencount.replaceAll(",", ";");
//				String[] as = ad_dayopencount.split(";");
//				Integer[] cc = new Integer[as.length];
//				Integer[] c = new Integer[cc.length];
//				synchronized (this.opencount_org) {
//					opencount_org.clear();
//					for (int i = 0; i < cc.length; i++) {
//						cc[i] = Integer.parseInt(as[i]);
//						c[i] = i + 1;
//						opencount_org.put(c[i], cc[i]);
//					}
//					if (opencount_rand == null){
//						opencount_rand = new HashMap<Object, Integer>();
//						opencount_rand.putAll(opencount_org);
//					}
//				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		else if(this.dayopencount == 0){
//			opencount_org.put(1,3);
//			opencount_org.put(2,5);
//			opencount_org.put(3,2);
//		}
	}

	public String getExt() {
		return ext;
	}

	/**
	 * @return 广告拓展json文件 例如:advext_12_网易云音乐.json
	 */
	public JSONObject getExtJso() {
		return extJso;
	}

	public int getOnlineflag() {
		return onlineflag;
	}

	public void setOnlineflag(int onlineflag) {
		this.onlineflag = onlineflag;
	}

	public int getGroupid() {
		return groupid;
	}

	public void setGroupid(int groupid) {
		this.groupid = groupid;
	}

	public Date getOnline_time() {
		return online_time;
	}

	public void setOnline_time(Date online_time) {
		this.online_time = online_time;
	}

	public String getClientbean_info() {
		return clientbean_info;
	}

	public void setClientbean_info(String clientbean_info) {
		if (clientbean_info == null)
			clientbean_info = "";
		clientbean_info = clientbean_info.trim();
		this.clientbean_info = clientbean_info;
	}
	//不再在这里生成server_beanbase,而是由server_beanbase的静态函数生成
//	public server_beanbase getServerBean() {
//		server_beanbase sbean = null;
//		if (StringUtil.isStringEmpt(serverbeanclass) == false) {
//			try {
//				Class<?> clsBean = Class.forName(serverbeanclass);
//				Method method = clsBean.getMethod("getInstance", new Class[] {});
//				sbean = (server_beanbase) method.invoke(null);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return sbean;
//	}

	/**
	 * 2018-11-2 新增时，如果没有设定打开次数,针对某个用户按配置文件要求随机算出
	 * 奇怪，当真正跑起来时，并不随机，比如测试打开一次30，2次50,3次20时，总是出现2次很多
	 * 为了使用更严格的随机，保证每个打开的比例更接近配置
	 * @return
	 */
//	public int getDayOpenCountRand() {
//		if (this.dayopencount > 0)
//			return dayopencount;
//
//		if (opencount_rand == null)
//		{
//			opencount_rand = new HashMap<Object, Integer>();
//			opencount_rand.putAll(opencount_org);
//		}
//
//		// Integer[] c = new Integer[openCountScale_new.length];
//		// for (int i = 0; i < openCountScale_new.length; i++)
//		// c[i] = i + 1;
//		// Integer opencount = (Integer) InfoGenUtils.randOnSpecScale(c,
//		// openCountScale_new,
//		// "D:/work/randOnSpecScale.txt");
//		Integer opencount = 1;
//		synchronized (opencount_org) {
//			opencount = (Integer) InfoGenUtils.randOnSpecScaleConst(opencount_rand, opencount_org);
//		}
//		return opencount;
//
//	}

	/**
	 * 100个用户中平均一个用户打开次数(按配置文件)
	 * ２０１８－１２－８修改一下，value总基数不一定设为１００个，可以１０或任意数
	 * 转到advruntimebase中实现
	 * @return
	 */
//	public float getDayOpenCountAdver() {
//		if (this.dayopencount > 0)
//			return dayopencount;
//		float adv_open = 0;
//		// for (int i = 0; i < openCountScale_new.length; i++)
//		// adv_open += openCountScale_new[i] * (i + 1);
//		//2018-12-8
//		int all_value_count = 0;
//		synchronized (opencount_org) {
//			Iterator<Map.Entry<Object, Integer>> it = opencount_org.entrySet().iterator();
//			for (; it.hasNext();) {
//				Map.Entry<Object, Integer> e = it.next();
//				int key = (Integer) e.getKey();
//				int value = (Integer) e.getValue();
//				adv_open += key * value;
//				all_value_count += value;
//			}
//		}
//		//adv_open /= 100;
//		adv_open /= all_value_count;
//		return adv_open;
//	}

	/**
	 * 2018-11-2 计算留存打开次数,按配置文件要求随机算出，暂时和新增一样
	 * 
	 * @return
	 */
//	public int getDayOpenCountRandRemain() {
//		if (this.dayopencount > 0)
//			return dayopencount;
//
//		Integer opencount = 1;
//		if (opencount_rand_remain == null)
//		{
//			opencount_rand_remain = new HashMap<Object, Integer>();
//			opencount_rand_remain.putAll(opencount_org);
//		}
//
//		synchronized (opencount_org) {
//			opencount = (Integer) InfoGenUtils.randOnSpecScaleConst(opencount_rand_remain, opencount_org);
//		}
//		return opencount;
//
//	}

	// 2018-12-7 因为增加了opencount_org，这里实现clone，而不是每次refresh都重新生成一个新的赋值它,这样opencount_org就会每次生成一个了
	public void clone(advtaskinfo _advinfo) {
		// advinfo.setAdv_id(rs.getInt("adv_id"));
		setName(_advinfo.getName());
		setPrior(_advinfo.getPrior());
		setAdv_type(_advinfo.getAdv_type());
		setApkid(_advinfo.getApkid());
		setCpid(_advinfo.getCpid());
		setChannelid(_advinfo.getChannelid());
		setBdid(_advinfo.getBdid());
		// .setGroupid(rs.getInt("groupid"));
		setStart_date(_advinfo.getStart_date());
		setEnd_date(_advinfo.getEnd_date());
		setAdv_content(_advinfo.getAdv_content());
		setServerbeanid(_advinfo.getServerbeanid());
		setParams(_advinfo.getParams());
		setClientbean_info(_advinfo.getClientbean_info());
		setRequesttime(_advinfo.getRequesttime());
		setRemaintime(_advinfo.getRemaintime());
		setTimeline(_advinfo.getTimeline());
		setRem_timeline(_advinfo.getRem_timeline());
		setRemain_lock_dev(_advinfo.getRemain_lock_dev());
		setRemain_rule(_advinfo.getRemain_rule());
		setAllcount(_advinfo.getAllcount());
		setDayopencount(_advinfo.getDayopencount());
		setDayusercount(_advinfo.getDayusercount());
		setAlldocount(_advinfo.getAlldocount());
		setExt(_advinfo.getExt());
		setOnlineflag(_advinfo.getOnlineflag());
		setAlias(_advinfo.getAlias());
	}
//2018-12-13 把handle_locked改为int，主要是可以灵活设置留存可以由其它机器执行（当值为2）
//	public boolean isHandle_Locked() {
//		return handle_Locked;
//	}
//
//	public void setHandle_Locked(boolean isHandle_Locked) {
//		this.handle_Locked = isHandle_Locked;
//	}
	

	public String toString() {
		String s = "";
		s += "adv_id=" + adv_id + "\r\n";
		s += "name=" + name + "\r\n";
		s += "adv_type=" + adv_type + "\r\n";
		s += "apkid=" + apkid + "\r\n";
		s += "cpid=" + cpid + "\r\n";
		s += "channelid=" + channelid + "\r\n";
		s += "bdid=" + bdid + "\r\n";
		// s += "groupid=" + groupid + "\r\n";

		s += "start_time=" + start_date + "\r\n";
		s += "end_time=" + end_date + "\r\n";
		s += "adv_content=" + adv_content + "\r\n";
		s += "params=" + params + "\r\n";

		s += "requesttime=" + requesttime + "\r\n";
		s += "remaintime=" + remaintime + "\r\n";

		s += "allcount=" + allcount + "\r\n";
		s += "dayopencount=" + dayopencount + "\r\n";

		s += "daycount=" + dayusercount + "\r\n";
		s += "alldocount=" + alldocount + "\r\n";
		s += "onlineflag=" + onlineflag + "\r\n";
		return s;
	}

	public int getHandle_Locked() {
		return handle_Locked;
	}

	public void setHandle_Locked(int handle_Locked) {
		this.handle_Locked = handle_Locked;
	}

	public static void main(String[] argv) {
		float adv_open = 0;
		int[] openCountScale_new = { 50, 30, 20 };
		for (int i = 0; i < openCountScale_new.length; i++)
			adv_open += openCountScale_new[i] * (i + 1);
		adv_open /= 100;
		System.out.println(adv_open);
	}
}
