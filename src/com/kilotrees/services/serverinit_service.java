package com.kilotrees.services;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.kilotrees.dao.sqlcommon;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.util.DateUtil;

/**
 * 系统启动时初始化服务，保证数据初始化完毕．
 * 
 * @author Administrator
 *
 */
public class serverinit_service {
	private static Logger log = Logger.getLogger(serverinit_service.class);

	private static serverinit_service instance;

	boolean serverInit;

	Timer timer;

	private serverinit_service() {
	}

	public static serverinit_service getInstance() {
		if (instance != null) {
			return instance;
		}
		synchronized (serverinit_service.class) {
			if (instance == null) {
				instance = new serverinit_service();
			}
		}
		return instance;
	}

	/**
	 * 定期初始化留存表
	 */
	void init() {
		newDayBegin(true);
		setTimer();
	}

	/**
	 * @param serverRestart
	 *            服务器是否重启
	 */
	void newDayBegin(boolean serverRestart) {
		serverInit = false;
		// main_service.getInstance().setSystem_ready(false);
		main_service.getInstance().setIsNewDayBegin(!serverRestart);
		if (serverRestart) {
			log.info("服务器初始启动...");
		} else {
			log.info("新一天零点开始...");
		}
		Thread t = new TInitCheckServer();
		t.start();
	}

	public boolean isServerInit() {
		return serverInit;
	}

	public void setServerInit(boolean serverInit) {
		this.serverInit = serverInit;
	}

	class TInitCheckServer extends Thread {
		public void run() {
			log.info("TInitCheckServer thread start id=" + this.getId());
			main_service.getInstance().addNewThread(this);
			sqlcommon.init();
			try {
				int cc = 0;
				while (main_service.getInstance().isThreadWork()) {
					sleep(1);
					ServerConfig.refresh();
					
					Date dDoInitProc = ServerConfig.getRemainjobdoday();
					String s1 = DateUtil.getShortDateString(dDoInitProc);
					String s2 = DateUtil.getShortDateString(new Date());
					if (s1.equals(s2)) {
						// log.info("系统准备完毕,system_ready!");
						serverinit_service.getInstance().setServerInit(true);
						break;
					} else {
						log.info("数所库初始化存储过程完成日期不是今天，继续等！cc=" + cc);
					}
					
					sleep(1000);
					cc++;
					if (cc > 60 * 5) {
						ErrorLog_service.system_errlog(getClass().getName() + "服务器初始化存储过程执行失败,系统执行失败");
						log.error("服务器初始化存储过程执行失败,系统执行失败");
						break;
					}
				}

			} catch (Exception e) {
				ErrorLog_service.system_errlog(e);
				log.error(e.getMessage(), e);
			}
			log.info("XXXXXX---TInitCheckServer thread end threadid=" + this.getId());
			main_service.getInstance().removeThread(this);
		}
	}

	void setTimer() {
		log.info("设置定时器");
		Calendar date = Calendar.getInstance();
		// 设置时间为 xx-xx-xx 00:00:00
		date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
		// 第二天才执行
		date.add(Calendar.DAY_OF_MONTH, 1);
		// 一天的毫秒数
		long daySpan = 24 * 60 * 60 * 1000;
		// 得到定时器实例
		timer = new Timer();
		// 使用匿名内方式进行方法覆盖
		timer.schedule(new TimerTask() {
			public void run() {
				newDayBegin(false);
			}

		}, date.getTime(), daySpan); // daySpan是一天的毫秒数，也是执行间隔

	}

	void stop() {
		if (timer != null) {
			log.info("cancel timer");
			timer.cancel();
		}
	}

}
