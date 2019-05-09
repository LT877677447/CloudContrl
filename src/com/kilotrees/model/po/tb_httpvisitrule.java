package com.kilotrees.model.po;
/**
 * 
CREATE TABLE [dbo].[tb_httpvisitrule](
	[deep] [int] NOT NULL,
	[visitor_rule] [varchar](100) NOT NULL,
 CONSTRAINT [PK_tb_httpvisitrule] PRIMARY KEY CLUSTERED 
(
	[deep] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 * @author Administrator
 *
 */
/*
 * 访问一个网站时，各层网页访问深度比例规则，有点类似时间曲线
 *
 */
public class tb_httpvisitrule {
	public static String tablename = "tb_httpvisitrule";
	//爬虫最大深度
	int deep;
	//各层深度占的访问比例，由1到deep层，用分号分开,比如5层深度："30;35;20;10;5"
	String visitor_rule;
}
