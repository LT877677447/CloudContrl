package com.kilotrees.services;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.kilotrees.dao.connectionmgr;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.serverbean.IPGetter;

public class main_service {
	private static Logger log = Logger.getLogger(main_service.class);

	private ArrayList<Thread> systemThreads = new ArrayList<Thread>();
	
	private static main_service instance;

	/**
	 * 项目初始化前为false，初始化完成后设为true
	 */
	private boolean system_ready = false;

	/**
	 * 项目启动为false
	 */
	private boolean isNewDayBegin = false;

	private Date start_time;
	
	/**
	 * 项目停止设为false
	 */
	boolean threadWork = true;

	private main_service() {
		start_time = new Date();
	}

	public static main_service getInstance() {
		if (instance != null) {
			return instance;
		}
		synchronized (main_service.class) {
			if (instance == null) {
				instance = new main_service();
			}
		}
		return instance;
	}

	public void start(ServletContext ctx) {
		log.info("服务器启动");
		
		ServerConfig.contextRealPath = ctx.getRealPath("/");
		ServerConfig.refresh();
		
		serverinit_service.getInstance().init();
		
		// 2019-4-23 每日凌晨12点抓取mofang后台数据(放在serverinit_service.init()后面)
//		FetchMoFangData_Service.getInstance().init();
		Thread thread = new TRefresh();
		thread.setName("Thread-Core");
		thread.start();
	}

	public void stop() {
		log.info("服务器停止");
		
		serverinit_service.getInstance().stop();

		threadWork = false;

		for (Thread t : systemThreads) {
			t.interrupt();
		}
		systemThreads.clear();
		// 放在最后
		connectionmgr.getInstance().webStop();
	}

	public void addNewThread(Thread t) {
		synchronized (systemThreads) {
			systemThreads.add(t);
		}
	}

	public void removeThread(Thread t) {
		synchronized (systemThreads) {
			systemThreads.remove(t);
		}
	}

	public long startTimeout() {
		Date now = new Date();
		return (now.getTime() - start_time.getTime()) / 1000;
	}

	public boolean isThreadWork() {
		return threadWork;
	}
	
	/**刷新完各种service就true
	 * @return
	 */
	public boolean isSystem_ready() {
		return system_ready;
	}

	public void setSystem_ready(boolean system_ready) {
		this.system_ready = system_ready;
	}

	public void setIsNewDayBegin(boolean isNewDay) {
		this.isNewDayBegin = isNewDay;
		if (isNewDay) {
			this.system_ready = false;
		}
	}

	class TRefresh extends Thread {
		boolean firstStart = true;

		public void run() {
			log.info("TRefresh 刷新线程启动,thread id=" + this.getId());
			main_service.getInstance().addNewThread(this);
			while (threadWork) {
				try {
					if (!serverinit_service.getInstance().isServerInit()) {
						sleep(10);
						continue;
					}
					
					if (firstStart) {
						log.info("TRefresh 等待 ServerInit 完成");
						firstStart = false;
					}
					
					ServerConfig.refresh();
					phonetype_service.getInstance().refresh();
					timeline_service.getInstance().refresh();
					advnewtask_service.getInstance().refresh();

					actdeviceinfo_service.getInstance().refresh();
					advremaintask_service.getInstance().refresh();
					advgroup_service.getInstance().refresh();
					advtodayresult_service.getInstance().refresh();
					smscheckcode_service.getInstance().refresh();
					
					if (isNewDayBegin) {
						// 线程正在工作期间，新的一天到来，立即再次初始化所有配置
						isNewDayBegin = false;
						log.warn("新一天开始，重新刷新所有配置");
						continue;
					}
					advsalloc_service.getInstance().refresh();
					IPGetter.service_refresh();

					if (main_service.getInstance().isSystem_ready() == false) {
						main_service.getInstance().setSystem_ready(true);
					}
					sleep(1000 * ServerConfig.getRefreshstep());
				} catch (Exception e) {
					if (threadWork == false) {
						break;
					}
					ErrorLog_service.system_errlog(e);
					log.error(e.getMessage(), e);
					try {
						sleep(1000);
					} catch (Exception e1) {
						ErrorLog_service.system_errlog(e1);
						log.error(e1.getMessage(), e1);
					}
				}
			}
			log.info("XXXXXX--main_service TRefresh end thread:" + this.getId());
		}
	}

}
