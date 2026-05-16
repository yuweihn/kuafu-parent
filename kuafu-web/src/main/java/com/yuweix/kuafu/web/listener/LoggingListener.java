package com.yuweix.kuafu.web.listener;


import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * @author yuwei
 */
public class LoggingListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
	private static final String LOG_DIR = "kuafu.web.log.dir";
	private static final String LOG_FILE_NAME = "kuafu.web.log.filename";


	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment env = event.getEnvironment();
		String logDir = env.getProperty(LOG_DIR);
		if (logDir != null && !"".equals(logDir)) {
			System.setProperty("logDir", logDir);
		}

		String logFileName = env.getProperty(LOG_FILE_NAME);
		if (logFileName != null && !"".equals(logFileName)) {
			System.setProperty("logFileName", logFileName);
		}
	}

	@Override
	public int getOrder() {
		return LoggingApplicationListener.DEFAULT_ORDER - 1;
	}
}
