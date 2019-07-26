package gov.nih.tbi.download.util;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

/**
 * Program to create 1000 files by copying and pasting in same directory with different name
 * 
 * @author khanaly
 *
 */
public class FileCreator {

	public static void main(String[] args) throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			File source = new File("/Users/khanaly/Downloads/All/all.csv");
		Calendar calendar = Calendar.getInstance();
			File destination = new File("/Users/khanaly/Downloads/All/all" + calendar.getTimeInMillis() + ".csv");
		try {

				FileUtils.copyFile(source, destination);
				Thread.sleep(100);
				System.out.println("i" + i);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

	}
	
	

}
