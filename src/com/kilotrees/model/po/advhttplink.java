package com.kilotrees.model.po;
/**
 * 

CREATE TABLE [dbo].[tb_advlink](
	[adv_id] [int] NOT NULL,
	[url_id] [int] NOT NULL,
	[url_value] [varchar](1000) NOT NULL,
	[childens_id] [varchar](200) NOT NULL,
	[ext] [varchar](100) NULL,
	[http_timeout] [int] NOT NULL,
 CONSTRAINT [PK_tb_advlink] PRIMARY KEY CLUSTERED 
(
	[adv_id] ASC,
	[url_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]


 * @author Administrator
 *
 */
/*大点击页面的链接搜索树结构，首页链接的id为1，每搜到一个url分配一个id，爬虫在不同的页面搜到一个链接后，要先看前面是不是有相同的，
 *如果是有相同的则不要再爬这个url页面，这个新的相同的url的childens_id必须为空
 * 
 */

public class advhttplink {
	public static final String tablename = "tb_advlink";
	int adv_id;
	int url_id;
	String url_value;
	//当前页面的子链接id，用分号分开
	String childens_id = "";
	//扩展字段，可以设置这个页面浏览时间,比如3;10表示3到10秒,空表示不停留，直接刷
	String ext = "3;10";
	//此连接深度
	int deep_lever;
	int httptimeout = 10;
	
	public int getHttptimeout() {
		return httptimeout;
	}
	public void setHttptimeout(int httptimeout) {
		this.httptimeout = httptimeout;
	}
	public int getAdv_id() {
		return adv_id;
	}
	public void setAdv_id(int adv_id) {
		this.adv_id = adv_id;
	}
	public int getUrl_id() {
		return url_id;
	}
	public void setUrl_id(int url_id) {
		this.url_id = url_id;
	}
	public String getUrl_value() {
		return url_value;
	}
	public void setUrl_value(String url_value) {
		this.url_value = url_value;
	}
	public String getChildens_id() {
		return childens_id;
	}
	public void setChildens_id(String childens_id) {
		if(childens_id == null)
			childens_id = "";
		childens_id.trim();
		this.childens_id = childens_id;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		if(ext == null)
			ext = "";
		this.ext = ext;
	}
	public int getDeep_lever() {
		return deep_lever;
	}
	public void setDeep_lever(int deep_lever) {
		this.deep_lever = deep_lever;
	}
	
	public boolean isChild(int childid)
	{
		if(childens_id.length() == 0)
			return false;
		String[] sc = childens_id.split(";");
		for(String s : sc)
		{
			int id = Integer.parseInt(s);
			if(id == childid)
				return true;
		}
		return false;
	}
	
}
