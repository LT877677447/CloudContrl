package com.kilotrees.model.po;
/**
 * 大点击用到的浏览器user-agent
 * 
CREATE TABLE [dbo].[tb_useragent](
	[type] [varchar](50) NOT NULL,
	[min_type] [varchar](100) NOT NULL,
	[user_agent] [varchar](500) NOT NULL
) ON [PRIMARY]


 * @author Administrator
 *
 */
public class useragent {
	public static String tablename= "tb_useragent";
	//设备品牌：如HuaWei,对应于tb_devicepiex的type
	String type;
	//具体型号，这个暂时为空
	String min_type = "";	
	String user_agent;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMin_type() {
		return min_type;
	}
	public void setMin_type(String min_type) {
		this.min_type = min_type;
	}
	public String getUser_agent() {
		return user_agent;
	}
	public void setUser_agent(String user_agent) {
		this.user_agent = user_agent;
	}
}
