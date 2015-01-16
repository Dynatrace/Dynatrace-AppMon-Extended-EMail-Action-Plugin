package com.dynatrace.diagnostics.plugins.extendedmailreport;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugins.extendedmailreport.domain.SharedProperties;
import com.dynatrace.diagnostics.plugins.extendedmailreport.utils.MailPluginConstants;


class CleanupCache implements MailPluginConstants {
	private static final Logger log = Logger.getLogger(CleanupCache.class.getName());
	private static final CleanupCache instance;
	private static ExecutorService executorCleanupCache; 
	
	static {
		try {
			instance = new CleanupCache();
		} catch (Exception e) {
			throw new RuntimeException("CleanupCache class: static block initialization of the CleanupCache object failed");
		}
	}
	
	private volatile boolean alive = false;
	private volatile boolean error = false;
	private volatile boolean shutdown = false;
	private volatile int shutdownCount = 0;
	private volatile boolean service = false;
	private volatile long expireCacheInterval = -1;
	private volatile long cleanupInterval = -1;

	private CleanupCache() {
	}
	
	public static CleanupCache getInstance() {
		return instance;
	}

	public synchronized void startCleanpCache() {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering startCleanpCache method");
		}
		try {
			if (!alive) {
				alive = true;
				shutdown = false;
				shutdownCount = 0;
				if (!service) {
					executorCleanupCache = Executors.newFixedThreadPool(1);
					service = true;
				}
				executorCleanupCache.execute(new Runnable() {
					@Override
					public void run() {
						doWork();
					}
				});
			}
		} catch (Exception e) {
			String msg = "CleanupCache class: doWork method: the following exception was thrown '" + HelperUtils.getExceptionAsString(e) + "'";
			log.severe(msg);
		}	
			
	}

	private void doWork() {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering CleanupCache class doWork method running on thread '" + Thread.currentThread().getName() + "'");
		}
		try {
			while (true) {
				SharedProperties shp;
				String key;
				if (log.isLoggable(Level.FINER)) {
					log.finer("CleanupCache class: doWork method: SHARED_PROPERTIES map hash code is " + MailExecutor.SHARED_PROPERTIES.hashCode() + ", identity hash code is " + System.identityHashCode(MailExecutor.SHARED_PROPERTIES) + ", its content is " + Arrays.toString(MailExecutor.SHARED_PROPERTIES.entrySet().toArray()));
				}
				long currentTime = System.currentTimeMillis();
				Set<Entry<String, SharedProperties>> entries = MailExecutor.SHARED_PROPERTIES.entrySet();
				boolean entriesFound = false;
				for (Entry<String, SharedProperties> entry : entries) {
					entriesFound = true;
					synchronized(shp = MailExecutor.getSharedProperties(entry.getKey())) {
						if (currentTime - shp.getLastRunTime() > expireCacheInterval) {
							// remove element from the SHARED_PROPERTIES map
							if (!MailExecutor.SHARED_PROPERTIES.remove((key = entry.getKey()), shp)) {
								error = true;
								String msg = "CleanupCache class: doWork method: plugin instance id is '" + key + "' points to a wrong SharedProperties object with hashcode " + shp.hashCode();
								log.warning(msg);
							} else {
								if (log.isLoggable(Level.FINER)) {
									String msg = "CleanupCache class: doWork method: entry with plugin instance id '" + key + "' was successfully removed from the SHARED_PROPERTIES map";
									log.finer(msg);
								}
							}
						} else {
							if (log.isLoggable(Level.FINER)) {
								String msg = "CleanupCache class: doWork method: cache has not been expired for the entry with key '" + shp.getId() + "'";
								log.finer(msg);
							}
						}
					}
				}
				if (!entriesFound) {
					if (log.isLoggable(Level.FINER)) {
						String msg = "CleanupCache class: doWork method: no entries found";
						log.finer(msg);
					}
					if (++shutdownCount > 2) {
						break;
					}
				}
				if (shutdown) {
					if (log.isLoggable(Level.FINER)) {
						String msg = "CleanupCache class: doWork method: perform shutdown";
						log.finer(msg);
					}
					break;
				}
				
				if (log.isLoggable(Level.FINER)) {
					String msg = "CleanupCache class: doWork method: thread sleep for " + cleanupInterval + " ms";
					log.finer(msg);
				}
				if (Thread.currentThread().isInterrupted()) {
					shutdown = true;
					break;
				}
				try {
					Thread.sleep(cleanupInterval);
				} catch (InterruptedException e) {
					shutdown = true;
					break;
				}
			}
		} catch (Exception e) {
			error = true;
			String msg = "CleanupCache class: doWork method: the following exception was thrown '" + HelperUtils.getExceptionAsString(e) + "'";
			log.severe(msg);
			throw new RuntimeException(msg);
		} finally {
			alive = false; 
			if (error) {
				// cleanup SHARED_PROPERTIES map
				cleanupMapUnconditionally();
				String msg = "CleanupCache class: doWork method: perform cleanupMapUnconditionally";
				log.severe(msg);
			}
		}
	}
	
	protected synchronized void cleanupMap() {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering CleanupCache class: cleanupMap method");
		}
		if (error) {
			error = false;
			if (log.isLoggable(Level.FINER)) {
				log.finer("CleanupCache class: cleanupMap method: before executing cycle by entries");
			}
			Set<Entry<String, SharedProperties>> entries = MailExecutor.SHARED_PROPERTIES.entrySet();
			String key;
			for (Entry<String, SharedProperties> entry : entries) {
				synchronized(entry.getValue()) {
					MailExecutor.SHARED_PROPERTIES.remove(key = entry.getKey());
				}
				if (log.isLoggable(Level.FINER)) {
					String msg = "CleanupCache class: cleanupMap method: removed key is '" + key + "'";
					log.finer(msg);
				}
			}
		}
		
	}
	
	protected synchronized void cleanupMapUnconditionally() {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering CleanupCache class: cleanupMapUnconditionally method");
		}
		Set<Entry<String, SharedProperties>> entries = MailExecutor.SHARED_PROPERTIES.entrySet();
		for (Entry<String, SharedProperties> entry : entries) {
			String key;
			MailExecutor.SHARED_PROPERTIES.remove(key = entry.getKey());
			if (log.isLoggable(Level.FINER)) {
				String msg = "CleanupCache class: cleanupMapUnconditionally method: removed key is '" + key + "'";
				log.finer(msg);
			}
		}
	}

	protected synchronized void shutdownService() throws InterruptedException {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering CleanupCache class: cleanupMapUnconditionally method");
		}
		
		executorCleanupCache.shutdown(); // no new tasks
		
		try {
			if (!executorCleanupCache.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
				executorCleanupCache.shutdownNow(); // cancel current tasks
				if (!executorCleanupCache.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
					String msg = "CleanupCache class: shutdownService method: the executorCleanupCache executor service did not shutdown.";
					log.severe(msg);
				}
			}
			
		} catch (InterruptedException e) {
			executorCleanupCache.shutdownNow();
			Thread.currentThread().interrupt();
		} finally {
			service = false;
			shutdown = false;
			shutdownCount = 0;
			alive = false;
			error = false;
		}
	}
	
	public boolean isError() {
		return error;
	}
	
	public boolean isAlive() {
		return alive;
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	public long getExpireCacheInterval() {
		return expireCacheInterval;
	}

	public void setExpireCacheInterval(long expireCacheInterval) {
		this.expireCacheInterval = expireCacheInterval;
	}

	public long getCleanupInterval() {
		return cleanupInterval;
	}

	public void setCleanupInterval(long cleanupInterval) {
		this.cleanupInterval = cleanupInterval;
	}
	
	public int getShutdownCount() {
		return shutdownCount;
	}
}