package com.kilotrees.model.po;

import java.util.Date;

/**
 * 客户post的调试信息，这里先写数据库，以后可能写文件
 *
 * 
 * CREATE TABLE [dbo].[tb_devpostmsg]( [log_type] [varchar](120) NOT NULL,
 * [message] [text] NOT NULL, [posttime] [datetime] NOT NULL ) ON
 * [PRIMARY]
 * 
 * @author Administrator
 *
 */
public class devpostmsg {
	public static final String tablename = "tb_devpostmsg";
	String dev_tag;
	String message;
	Date posttime;

	public String getDev_tag() {
		return dev_tag;
	}

	public void setDev_tag(String dev_tag) {
		this.dev_tag = dev_tag;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getPosttime() {
		return posttime;
	}

	public void setPosttime(Date posttime) {
		this.posttime = posttime;
	}


}
