package com.adtworker.mail.service.thread;

public class ThreadProvider {

	private static ThreadProvider threadProvider = null;
	private PlaningTaskManager planingTaskManager = null;
	private WorkerManager workerManager = null;

	public static ThreadProvider getInstance() {
		if (threadProvider == null) {
			threadProvider = new ThreadProvider();
		}
		return threadProvider;
	}

	private ThreadProvider() {
		planingTaskManager = new PlaningTaskManager(this);
		workerManager = new WorkerManager(this);
	}

	public void scheduleTask(WorkerTask task) {
		workerManager.scheduleTask(task, 0);
	}

	public void scheduleTask(WorkerTask task, long delay) {
		workerManager.scheduleTask(task, delay);
	}

	public void addTask(WorkerTask task) {
		planingTaskManager.addPlaningTask(task);
		workPlangingTask();
	}

	public void removeTask(WorkerTask task) {
		if (planingTaskManager.havePlaningTask(task)) {
			planingTaskManager.removePlaningTask(task);
		}
		workerManager.stopTask(task);
	}

	public void removeAllTask() {
		planingTaskManager.removeAllTasks();
	}

	protected void workPlangingTask() {
		if (planingTaskManager.havePlaningTask()) {
			WorkerTask tempTask = planingTaskManager.getATask();
			if (tempTask != null) {
				scheduleTask(tempTask);
			}
		}
	}

	public int getFreeWorkerCount() {
		return workerManager.getFreeWorkerCount();
	}

	public int getBusyWorkerCount() {
		return workerManager.getBusyWorkerCount();
	}
}
