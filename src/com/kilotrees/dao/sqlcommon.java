package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advnewreglog;
import com.kilotrees.model.po.advremainlog;
import com.kilotrees.services.ErrorLog_service;
import com.kilotrees.services.advremaintask_service;

public class sqlcommon {
	private static Logger log = Logger.getLogger(sqlcommon.class);
	/**
	 * @param tablename_pre 表名
	 * @param month 月份  1-12
	 * @return tablename_pre_month
	 */
	public static String getMonthTableName(String tablename_pre,int month)
	{		
		String smonth= "" + month;
		if(month < 10)
			smonth = "0" + month;
		return tablename_pre + "_" + smonth;
	}
	/**
	 * 系统第一次启动时建立所有月表或日表
	 */
	public static void createTableIfnotExit(String tablename,String createSql)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try{
		 ResultSet rs = con.getMetaData().getTables(null, null, tablename, null);  
         if (rs.next()) { 
        	 //System.out.println("createTableIfnotExit table " + tablename + " exist,return");
        	 rs.close();
        	 return;
         }
         System.out.println("createTableIfnotExit table " + tablename + " not exist,create!");
         ps = con.prepareStatement(createSql);
         ps.execute();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(createSql);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	/**
	 * 系统启动时调用初始化存储过程proc_init
	 * 
drop proc [dbo].[proc_init]
go
CREATE proc [dbo].[proc_init]
as
/**
	存储过程：系统在0点或初始启动时检查tb_advtaskinfo和tb_advremaintask表，重新整理当天任务，
	完成后设置tb_serverconfig表remainjobdoday
	每天执行一次
///
begin
	declare @doday datetime;
	declare @dayshort varchar(32)
	select @doday = remainjobdoday from tb_serverconfig
	if(DATEDIFF(dd,@doday,getdate()) = 0)
	begin
	
		print '每天执行一次,remainjododay is set today:' + convert(varchar(100),@doday,120)
		return
	end
	--set proc_ini is working
	set @dayshort = CONVERT(varchar(10),GETDATE(),120)
	print @dayshort
	update tb_serverconfig set remainjobprocwork=1
	--把要删除的autoid加入tb_delautoid表中
	--print '1'将最后留存日期小于当天的autoid放到tb_delautoid表中dotoday > -1控制着只执行一次
	insert into tb_delautoid select autoid,0 from tb_advremaintask  where lastremaintime < GETDATE() and  dotoday > -1	and autoid not in(select del_autoid from tb_delautoid)
	--print '2'将已经停掉的留存autoid放到tb_delautoid表中
	insert into tb_delautoid select autoid,0 from tb_advremaintask  where adv_id in(select adv_id from tb_advtaskinfo where onlineflag = 2) and  dotoday > -1 and autoid not in(select del_autoid from tb_delautoid)
	--print '3'
	update tb_advremaintask set dotoday = -1 where lastremaintime < GETDATE() and dotoday > -1
	update tb_advremaintask set dotoday = -2 where adv_id in(select adv_id from tb_advtaskinfo where onlineflag = 2)  and dotoday > -1
	--update tb_advremaintask set dayopencount = 0 where dotoday > -1
	
	update tb_advremaintask set dotoday = dayopencount where dotoday > -1 and substring(remaininfo,DATEDIFF(dd,firstremaintime,GETDATE())+1,1) = '1'
	update tb_advremaintask set dotoday = 0 where  dotoday > -1 and substring(remaininfo,DATEDIFF(dd,firstremaintime,GETDATE())+1,1) <> '1'
	
	update tb_serverconfig set remainjobdoday=GETDATE()
	update tb_serverconfig set remainjobprocwork=0
end

GO
*/

	/**
	 * 初始化[tb_advremaintask]留存表的todayopencount和dotoday
	 */
	public static void init()
	{
		//防止maxautoid表为空
		maxautoiddao.checkTable();
		for(int i = 1; i <= 12; i++)
		{
			int month = i;
			String tableName = advnewreglog.getMonthTableName(month);
			createTableIfnotExit(tableName,advnewreglog.getCreateTableSql(month));
			
			tableName = advremainlog.getMonthTableName(month);
			createTableIfnotExit(tableName,advremainlog.getCreateTableSql(month));
		}
		advremaintask_service.getInstance().systemInit();
		//取消存储过程
//		Connection con = connectionmgr.getInstance().getConnection();
//		String proStr = "{call proc_init}";  
//		
//		try{		
//			CallableStatement cs = con.prepareCall(proStr);
//         	cs.execute();
//         	cs.close();
//		}catch(Exception e)
//		{			
//			log.error(e.getMessage(),e);
//		}
//		finally
//		{
//			connectionmgr.getInstance().closeConnection(con,null);
//		}
	}
}
