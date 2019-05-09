package com.kilotrees.model.po;
/*
 * 大点击广告的来源url
 * 
CREATE TABLE [dbo].[tb_sourceurls](
	[type] [int] NOT NULL,
	[typename] [varchar](50) NOT NULL,
	[url] [varchar](500) NOT NULL,
	[weigth] [int] NOT NULL,
 CONSTRAINT [PK_tb_sourceurls] PRIMARY KEY CLUSTERED 
(
	[url] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 */
public class sourceurls {
	public static String tablename = "tb_sourceurls";
	//类型，游戏,社交，
	int type;
	String typename = "";
	String url;
	int weigth;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getTypename() {
		return typename;
	}
	public void setTypename(String typename) {
		if(typename == null)
			typename = "";
		this.typename = typename;
		
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getWeigth() {
		return weigth;
	}
	public void setWeigth(int weigth) {
		this.weigth = weigth;
	}	
}
