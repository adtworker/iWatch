package com.adtworker.mail.service.thread;

import java.util.Timer;
import java.util.TimerTask;

class Worker extends Timer {

	private WorkerManager workerManager;
	protected TimerTask currenTask = null;
	private boolean isFreeing = true;

	protected Worker(WorkerManager workerManager) {
		this.workerManager = workerManager;

	}

	protected void scheduleTask(WorkerTask task, long delay) {
		isFreeing = false;
		currenTask = new Task(task);
		this.schedule(currenTask, delay);
	}

	protected boolean isFreeing() {
		return isFreeing;
	}

	protected void finishTask() {
		if (isFreeing) {
			return;
		}
		purge();
		currenTask.cancel();
		currenTask = null;
		isFreeing = true;
	}

	protected void destory() {
		cancel();
	}

	class Task extends TimerTask {
		WorkerTask task;

		public Task(WorkerTask task) {
			this.task = task;
		}

		@Override
		public void run() {
			task.onStart();
			task.run();
			task.onFinish();
			finishTask();
			workerManager.changeWorkerStateToFree(Worker.this);
		}
	}
}
