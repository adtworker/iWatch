package com.adtworker.mail.service.thread;

import java.util.ArrayList;
import java.util.List;

class PlaningTaskManager {
	List<WorkerTask> planingTaskList = new ArrayList<WorkerTask>();

	protected PlaningTaskManager(ThreadProvider threadProvider) {

	}

	void addPlaningTask(WorkerTask task) {
		planingTaskList.add(task);
	}

	boolean havePlaningTask() {
		if (planingTaskList.size() > 0) {
			return true;
		}
		return false;
	}

	boolean havePlaningTask(WorkerTask task) {
		if ((planingTaskList.size() > 0) && planingTaskList.contains(task)) {
			return true;
		}
		return false;
	}

	WorkerTask getATask() {
		if (planingTaskList.size() > 0) {
			return planingTaskList.remove(0);
		} else {
			return null;
		}
	}

	boolean removePlaningTask(WorkerTask task) {
		return planingTaskList.remove(task);
	}

	void removeAllTasks() {
		for (int i = 0; i < planingTaskList.size(); i++) {
			planingTaskList.remove(i);
		}

	}
}
