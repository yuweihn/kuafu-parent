package com.yuweix.kuafu.http.conn;


import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class CloseablePoolingHttpClientConnectionManager extends PoolingHttpClientConnectionManager {
	private static final Logger log = LoggerFactory.getLogger(CloseablePoolingHttpClientConnectionManager.class);
	private final Object lock = new Object();
	private volatile ScheduledExecutorService executor;
	private volatile ScheduledFuture<?> future;

	public CloseablePoolingHttpClientConnectionManager(final Registry<ConnectionSocketFactory> socketFactoryRegistry) {
		super(socketFactoryRegistry);
	}

	public void startEvictor(long idleTimeout, long checkInterval) {
		synchronized (lock) {
			if (executor != null) {
				log.error("Evictor has already been started, skipping duplicate initialization.");
				return;
			}
			this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "http-client-idle-evictor");
				t.setDaemon(true);
				return t;
			});
			this.future = this.executor.scheduleWithFixedDelay(() -> {
				try {
					closeExpiredConnections();
					closeIdleConnections(idleTimeout, TimeUnit.MILLISECONDS);
				} catch (Exception ex) {
					log.error("HttpClient connection pool cleanup failed. Error: {}", ex.getMessage(), ex);
				}
			}, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void shutdown() {
		synchronized (lock) {
			if (future != null) {
				future.cancel(false);
				future = null;
			}
			if (executor != null) {
				executor.shutdown();
				try {
					if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
						executor.shutdownNow();
					}
				} catch (InterruptedException e) {
					executor.shutdownNow();
					Thread.currentThread().interrupt();
				}
				executor = null;
			}
		}
		super.shutdown();
	}
}
