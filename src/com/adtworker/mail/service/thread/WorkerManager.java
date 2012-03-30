package com.adtworker.mail.service.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

class WorkerManager {

	ThreadProvider context;

	private List<Worker> busyWorkerList = new ArrayList<Worker>();
	private List<Worker> freeWorkersList = new ArrayList<Worker>();

	WorkerManager(ThreadProvider context) {
		this.context = context;
	}

	protected void scheduleTask(WorkerTask task) {
		getWorker().scheduleTask(task, 0);
	}

	protected void scheduleTask(WorkerTask task, long delay) {
		getWorker().scheduleTask(task, delay);
	}

	protected int getFreeWorkerCount() {
		return freeWorkersList.size();
	}

	protected int getBusyWorkerCount() {
		return busyWorkerList.size();
	}

	protected void askOutAllWorkers() {
		stopAllTask();
		for (int i = 0; i < freeWorkersList.size(); i++) {
			Worker temWorker = freeWorkersList.remove(i);
			temWorker.destory();
		}
	}

	protected void stopAllTask() {
		for (int i = 0; i < getBusyWorkerCount(); i++) {
			Worker temWorker = busyWorkerList.remove(i);
			temWorker.finishTask();
			changeWorkerStateToFree(temWorker);
		}
	}

	protected boolean stopTask(WorkerTask task) {
		Worker tempWorker = getWorkerByTask(task);
		if (tempWorker != null) {
			tempWorker.finishTask();
			changeWorkerStateToFree(tempWorker);
			return true;
		}
		return false;
	}

	protected void changeWorkerStateToFree(Worker worker) {
		context.workPlangingTask();
		if (worker.isFreeing()) {
			busyWorkerList.remove(worker);
			freeWorkersList.add(worker);
		}
		// Log.e("changeWorkerStateToFree", "free: " + getFreeWorkerCount() + " busy: " + getBusyWorkerCount());
	}

	private Worker getWorkerByTask(TimerTask task) {
		Worker tempWorker = null;
		for (int i = 0; i < busyWorkerList.size(); i++) {
			tempWorker = busyWorkerList.get(i);
			if (tempWorker.currenTask == task) {
				return tempWorker;
			}
		}
		return null;
	}

	private Worker getWorker() {
		Worker tempWorker = null;
		if (getFreeWorkerCount() == 0) {
			tempWorker = new Worker(this);
			busyWorkerList.add(tempWorker);
		} else {
			tempWorker = freeWorkersList.remove(0);
			busyWorkerList.add(tempWorker);
			return tempWorker;
		}
		// Log.e("getWorker", "free: " + getFreeWorkerCount() + " busy: " + getBusyWorkerCount());
		return tempWorker;
	}
}
