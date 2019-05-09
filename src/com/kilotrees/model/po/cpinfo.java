package com.kilotrees.model.po;
/*
 * 广告主信息
	CREATE TABLE [dbo].[tb_cpinfo](
	[cpid] [int] NOT NULL,
	[cpname] [varchar](50) NOT NULL
) ON [PRIMARY]

) 
 */
public class cpinfo {
	int cpid;
	String cpname;
	
	public int getCpid() {
		return cpid;
	}
	public void setCpid(int cpid) {
		this.cpid = cpid;
	}
	public String getCpname() {
		return cpname;
	}
	public void setCpname(String cpname) {
		this.cpname = cpname;
	}
	
}
