package com.kilotrees.startup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.kilotrees.services.main_service;

@WebListener
public class ServerLifecycleListener implements ServletContextListener {

	public ServerLifecycleListener() {
	}

	public void contextInitialized(ServletContextEvent arg0) {
		main_service.getInstance().start(arg0.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		main_service.getInstance().stop();
	}

}
