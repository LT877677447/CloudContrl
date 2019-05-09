package com.kilotrees.services;

import java.util.Date;
/**
 * 程序某些地方中要使用连续递增的不同值标明不同的请求，象任务中autoid和短信验证码的序列号．由一个数据表多个字段记录，每次增加时更新表字段值
 * //2019-1-5 改成用时间擢，不用操作数据库了
 */
public class autoid_service {
	//static maxautoid mid;
	//static maxautoid seqid;
	//2019-1-5 改成用时间擢，不用操作数据库了，取当前时间毫秒数，并且在后面加三位随机数
	public synchronized static long getMaxAutoid() //throws SQLException
	{
//		if(mid == null)
//		{
//			mid = maxautoiddao.getMaxAutoid();
//		}
//		mid.setMaxid(mid.getMaxid() + 1);
//		if(maxautoiddao.updateMaxid(mid) == false)
//			throw new SQLException("error to execute maxautoiddao.updateMaxid(mid).....");
//		return mid.getMaxid();
		long autoid = new Date().getTime();
		int rand = new java.util.Random().nextInt(1000);
		autoid *= 1000;
		autoid += rand;
		//String s = "" + autoid;
		//去掉前面2位
		//s = s.substring(2);
		//补上随机数三位
//		if(rand >= 100)
//			s += rand;
//		else if(rand >= 10)
//			s += "0" + rand;
//		else
//			s += "00" + rand;
//		autoid = Long.parseLong(s);
		return autoid;
	}
	
	/**返回16位随机long
	 * @return
	 */
	public synchronized static long getMaxSeqid()// throws SQLException
	{
		return getMaxAutoid();
//		if(seqid == null)
//		{
//			seqid = maxautoiddao.getMaxSeqid();
//		}
//		seqid.setMaxid(seqid.getMaxid() + 1);
//		if(maxautoiddao.updateSeqid(seqid) == false)
//			throw new SQLException("getMaxSeqid");
//		return seqid.getMaxid();
	}
	
	public static void main(String[] argv)
	{
		System.out.println(getMaxAutoid());
		
		try {
			Thread.sleep(1000* 3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(new Date().getTime());
	}
}
