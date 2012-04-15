package com.adtworker.mail;

import java.io.File;

public class Utils {

	public static void delFolder(String folderPath) {
		try {
			delAllFileinFolder(folderPath);
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete();

		} catch (Exception e) {
			System.out.println("Error in deleting fold " + folderPath);
			e.printStackTrace();

		}
	}

	public static void delAllFileinFolder(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFileinFolder(path + "/" + tempList[i]);
				delFolder(path + "/" + tempList[i]);
			}
		}
	}

}
