package gov.nih.ninds.proforms.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
/**
 * This program reads file with guid in first column interval on fourth column and form id=201(MDS-UPDRS) and give admin id and create script to update 
 * administrator table xsubmissonid to 1 means not sumbitted so mirth can pick and do other process
 * @author khanaly
 *PDFP184NXJ,1,1,14,12,0,0,0,1,0,0,0,0,0,0,0,1,0,1,1,0,1,0,1,0,0,0,1,1,1,0,1,0,0,0,0,0,0,10
 *PDFP184NXJ,1,2,15,3,0,0,0,1,0,0,0,1,2,1,0,0,0,1,2,0,1,0,0,0,0,0,1,1,0,0,0,1,0,0,0,0,2,14
 *PDFP184NXJ,1,3,16,29,0,0,1,2,2,0,0,2,1,1,0,1,0,1,2,0,1,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,17
 */
public class AdminIdQyeryCreator {
	public static void main(String[] args) {
		final String updateClasue = "update administeredform set xsubmissionstatusid=1 where administeredformid =(";
		final String querySting1 = "select administeredformid from administeredform where formid=201 and "
				+ "intervalid=(select intervalid from interval where intervalid=";
		final String querySting2=" and protocolid=2) and patientid=(select patientid from patient where guid='";
		final String querySting3="'));";

		//maintains insertion order
		
		
		String csvFilePath ="/Users/khanaly/Downloads/a_v.csv";
		 String splitBy = ",";
		 String line ="";
		LinkedList<String> guidList = new LinkedList<String>();
		LinkedList<String> vtList = new LinkedList<String>();
		BufferedReader br = null;
		int lineNo = 0 ;
		try {
			br = new BufferedReader(new FileReader(csvFilePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			
			while ((line = br.readLine()) != null) {
			    // this is to extra a particular row of data from csv
				
			    String[] cols = line.split(splitBy);
			    guidList.add(cols[0]);
			    vtList.add(cols[3]);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		 ListIterator<String> guidIterator = guidList.listIterator();
		 ListIterator<String> vtIterator = vtList.listIterator();
	        while (guidIterator.hasNext()&&vtIterator.hasNext()) {
	        	
	            System.out.println(updateClasue+querySting1+vtIterator.next()+querySting2+guidIterator.next()+querySting3);
	        }
	        
	  

	}

}
