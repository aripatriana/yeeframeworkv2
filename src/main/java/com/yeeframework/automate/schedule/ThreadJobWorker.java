package com.yeeframework.automate.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.RunTestWorkflow;

public class ThreadJobWorker extends Thread {
	
	private final Logger log = LoggerFactory.getLogger(ThreadJobWorker.class);
	static ReentrantLock lock = new ReentrantLock();
	static ConcurrentHashMap<JobKey, ThreadJobRunnable> poolThread = new ConcurrentHashMap<JobKey, ThreadJobRunnable>(); 
	int thread;
	
	public ThreadJobWorker(int thread) {
		this.thread = thread;
	}
	
	public static void newThreadJobRunnable(RunTestWorkflow runTestWorkflow, JobExecutionContext context) {
		lock.lock();
		if (!poolThread.contains(context.getJobDetail().getKey())) {
			poolThread.put(context.getJobDetail().getKey(), new ThreadJobRunnable(runTestWorkflow, context));
		}
		lock.unlock();
	}
	
	@Override
	public void run() {
		while(true) {
			if (!poolThread.isEmpty()) {
				ExecutorService executor = Executors.newFixedThreadPool(thread);
				
				while (!poolThread.isEmpty()) {
					lock.lock();
					List<JobKey> removed = new ArrayList<JobKey>(); 
					poolThread.forEach((jobKey , threadJobRunnable) -> {
						executor.execute(threadJobRunnable);
						removed.add(jobKey);
					});
					
					removed.forEach(jobKey -> {
						poolThread.remove(jobKey);						
					});
					lock.unlock();
				}
				executor.shutdown();
				try {
					executor.awaitTermination(Long.MAX_VALUE,TimeUnit.MILLISECONDS);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}	
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();		
			}
		}
	}
	
	@Override
	public synchronized void start() {
		log.info("Start {}", this);
		super.start();
	}

	@Override
	public String toString() {
		return "ThreadJobWorker [thread=" + thread + "]";
	}

}
