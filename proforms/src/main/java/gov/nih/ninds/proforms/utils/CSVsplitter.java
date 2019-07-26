package gov.nih.ninds.proforms.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class CSVsplitter {

	public static void main(String[] args) {
		try {
			 String inputfile = "/Users/khanaly/Downloads/TBIVitalSigns_Epo.csv"; //  Source File Name.  
			 double nol = 2500.0; //  No. of lines to be split and saved in each output file.  
			  File file = new File(inputfile);  
			  Scanner scanner = new Scanner(file);  
			  int count = 0;  
			  while (scanner.hasNextLine())   
			  {  
			   scanner.nextLine();  
			   count++;  
			  }  
			  System.out.println("Lines in the file: " + count);     // Displays no. of lines in the input file.  
			  double temp = (count/nol);  
			  int temp1=(int)temp;  
			  int nof=0;  
			  if(temp1==temp)  
			  {  
			   nof=temp1;  
			  }  
			  else  
			  {  
			   nof=temp1+1;  
			  }  
			  System.out.println("No. of files to be generated :"+nof); // Displays no. of files to be generated. 
			 
			// Actual splitting of file into smaller files  

			  FileInputStream fstream = new FileInputStream(inputfile); 
			  DataInputStream in = new DataInputStream(fstream);  

			  BufferedReader br = new BufferedReader(new InputStreamReader(in)); String strLine;  

			  for (int j=1;j<=nof;j++)  
			  {  
			   FileWriter fstream1 = new FileWriter("/Users/khanaly/Downloads/part_"+j+"_TBIVitalSigns_EPO.csv");     // Destination File Location  
			   BufferedWriter out = new BufferedWriter(fstream1);   
			   for (int i=1;i<=nol;i++)  
			   {  
			    strLine = br.readLine();   
			    if (strLine!= null)  
			    {  
			     out.write(strLine);   
			     if(i!=nol)  
			     {  
			      out.newLine();  
			     }  
			    }  
			   }  
			   out.close();  
			  }  

			  in.close();  
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
