package com.kilotrees.model.po;
/**
 * 

CREATE TABLE [dbo].[tb_maxautoid](
	[maxid] [bigint] NOT NULL,
	[maxseqid] [bigint] NULL
) ON [PRIMARY]

 * @author Administrator
 *
 */
public class maxautoid {
	public static final String tablename = "tb_maxautoid";
	long maxid;
	//long seqid;

	public long getMaxid() {
		return maxid;
	}

	public void setMaxid(long maxid) {
		this.maxid = maxid;
	}
//
//	public long getSeqid() {
//		return seqid;
//	}
//
//	public void setSeqid(long seqid) {
//		this.seqid = seqid;
//	}
	
}
