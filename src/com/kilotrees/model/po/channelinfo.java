package com.kilotrees.model.po;

/**
 * 渠道信息，我们本身就是渠道，暂时用不上
 * 
 *	

CREATE TABLE [dbo].[tb_channelinfo](
	[channel_id] [int] NOT NULL,
	[channel_name] [varchar](50) NULL,
 CONSTRAINT [PK_tb_channelinfo] PRIMARY KEY CLUSTERED 
(
	[channel_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 */
public class channelinfo {
	int channel_id;
	String channel_name;
	public int getChannel_id() {
		return channel_id;
	}
	public void setChannel_id(int channel_id) {
		this.channel_id = channel_id;
	}
	public String getChannel_name() {
		return channel_name;
	}
	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}	
}
