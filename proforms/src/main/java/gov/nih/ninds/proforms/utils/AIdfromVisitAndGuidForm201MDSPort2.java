package gov.nih.ninds.proforms.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

public class AIdfromVisitAndGuidForm201MDSPort2 {

	public static void main(String[] args) {
		final String querypart1 = "select sum(answer::int) as MDSUPDRS_TotalScore from administeredform af, "
				+ " response r,patientresponse pr,questionattributes qa,form f,section s,sectionquestion sq "
				+ " where af.administeredformid = ( ";
		final String querypart3 = " and af.administeredformid = r.administeredformid "
				+ " and af.formid= f.formid and f.formid=201 and f.formid= s.formid and sq.sectionid=s.sectionid "
				+ " and sq.questionattributesid= qa.questionattributesid and qa.questionid = r.questionid "
				+ " and pr.responseid = r.responseid and qa.data_element_name "
				+ "in('MDSUPDRS_PartIScore','MDSUPDRS_PartIIScore','MDSUPDRS_PartIIIScore','MDSUPDRS_PartIVScore');";
		try {
			CSVReader reader = new CSVReader(new FileReader("/Users/khanaly/Downloads/aid.csv"));
			
			try {
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {

					System.out.println(querypart1 + "select administeredformid from administeredform where formid=201 and "
							+ "intervalid=" + nextLine[0] + " and  patientid=(select patientid from patient where guid='"
							+ nextLine[1] + "'))" + querypart3);
				}
			}
			finally {
				reader.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
