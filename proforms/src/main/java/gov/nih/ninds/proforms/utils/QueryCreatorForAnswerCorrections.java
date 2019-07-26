package gov.nih.ninds.proforms.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
/**
 * This program reads csv files sample data stated below and produces query to correct answer and submitted answer for given data element
 * @author khanaly
 * 
 *GUID,Subject ID,Visit,Visit Type,Video,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSFreeFlowSpeechScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSFacialExprScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSNeckRigidScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSRUERigidScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSLUERigidScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSRLERigidScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSLLERigidScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSFingerTppngRteHndScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSFingerTppngLftHndScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSRteHndScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSLftHndScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSProntSupnRtHndMvmtScr,MDS_UPDRSV1.Part III:MotorExamination.PronatSupinLftHndMvmntScore,MDS_UPDRSV1.Part III:MotorExamination.RteFtToeTppngScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSLftFtToeTppngScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSLegAgiltyRteLegScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSLegAgiltyLftLegScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSArisingFrmChrScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSGaitScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSFreezingGaitScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSPostrlStabltyScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSPostureScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSGlblSpontntyMvmntScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSPostrlTremorRtHndScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSPostrlTremrLftHndScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSKineticTremrRtHndScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSKineticTremrLftHndScr,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSRestTremorAmpRUEScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSRestTremorAmpLUEScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSRestTremorAmpRLEScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSRestTremorAmpLLEScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSRestTremrAmpLipJawScr,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRSConstncyRestTremrScore,MDS_UPDRSV1.Part III:MotorExamination.MDSUPDRS_PartIIIScore
*PDFP184NXJ,001,1,Baseline,12,0,0,0,1,0,0,0,0,0,0,0,1,0,1,1,0,1,0,1,0,0,0,1,1,1,0,1,0,0,0,0,0,0,10
*PDFP184NXJ,001,2,6-month,3,0,0,0,1,0,0,0,1,2,1,0,0,0,1,2,0,1,0,0,0,0,0,1,1,0,0,0,1,0,0,0,0,2,14
 */
public class QueryCreatorForAnswerCorrections {

	public static void main(String[] args) {
		String queryWithParms = "";
		final String querySting1 = "update patientresponse set answer =";
		final String querySting2 = ",";
		final String querySting3 = "submitanswer=";
		final String querySting4=" where patientresponseid = (select patientresponseid from administeredform af, "
				+ "response r,patientresponse pr,questionattributes qa,form f,section s,sectionquestion sq "
				+ "where af.administeredformid = xxx and af.administeredformid = r.administeredformid "
				+ "and af.formid= f.formid and f.formid=xxx and f.formid= s.formid and sq.sectionid=s.sectionid "
				+ "and sq.questionattributesid= qa.questionattributesid and qa.questionid = r.questionid "
				+ "and pr.responseid = r.responseid and qa.data_element_name ='";
		final String querySting5 = "');";
		//maintains insertion order
		LinkedList<String> dataElementList = new LinkedList<String>();
		dataElementList.add("MDSUPDRSFreeFlowSpeechScore");//1
		dataElementList.add("MDSUPDRSFacialExprScore");//2
		dataElementList.add("MDSUPDRSNeckRigidScore");//3
		dataElementList.add("MDSUPDRSRUERigidScore");//4
		dataElementList.add("MDSUPDRSLUERigidScore");//5
		dataElementList.add("MDSUPDRSRLERigidScore");//6
		dataElementList.add("MDSUPDRSLLERigidScore");//7
		dataElementList.add("MDSUPDRSFingerTppngRteHndScore");//8
		dataElementList.add("MDSUPDRSFingerTppngLftHndScore");//9
		dataElementList.add("MDSUPDRSRteHndScore");//10
		dataElementList.add("MDSUPDRSLftHndScore");//11
		dataElementList.add("MDSUPDRSProntSupnRtHndMvmtScr");//12
		dataElementList.add("PronatSupinLftHndMvmntScore");//13
		dataElementList.add("RteFtToeTppngScore");//14
		dataElementList.add("MDSUPDRSLftFtToeTppngScore");//15
		dataElementList.add("MDSUPDRSLegAgiltyRteLegScore");//16
		dataElementList.add("MDSUPDRSLegAgiltyLftLegScore");//17
		dataElementList.add("MDSUPDRSArisingFrmChrScore");//18
		dataElementList.add("MDSUPDRSGaitScore");//19
		dataElementList.add("MDSUPDRSFreezingGaitScore");//20
		dataElementList.add("MDSUPDRSPostrlStabltyScore");//21
		dataElementList.add("MDSUPDRSPostureScore");//22
		dataElementList.add("MDSUPDRSGlblSpontntyMvmntScore");//23
		dataElementList.add("MDSUPDRSPostrlTremorRtHndScore");//24
		dataElementList.add("MDSUPDRSPostrlTremrLftHndScore");//25
		dataElementList.add("MDSUPDRSKineticTremrRtHndScore");//26
		dataElementList.add("MDSUPDRSKineticTremrLftHndScr");//27
		dataElementList.add("MDSUPDRSRestTremorAmpRUEScore");//28
		dataElementList.add("MDSUPDRSRestTremorAmpLUEScore");//29
		dataElementList.add("MDSUPDRSRestTremorAmpRLEScore");//30
		dataElementList.add("MDSUPDRSRestTremorAmpLLEScore");//31
		dataElementList.add("MDSUPDRSRestTremrAmpLipJawScr");//32
		dataElementList.add("MDSUPDRSConstncyRestTremrScore");//33
		dataElementList.add("MDSUPDRS_PartIIIScore");//34
		
		String csvFilePath ="/Users/khanaly/Downloads/y.csv";
		 String splitBy = ",";
		 String line ="";
		LinkedList<String> admin103List = new LinkedList<String>();
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
				lineNo++;
			    // this is to extra a particular row of data from csv
				if (lineNo == 2) {
			    String[] cols = line.split(splitBy);
			    admin103List.add(cols[5]);
			    admin103List.add(cols[6]);
			    admin103List.add(cols[7]);
			    admin103List.add(cols[8]);
			    admin103List.add(cols[9]);
			    admin103List.add(cols[10]);
			    admin103List.add(cols[11]);
			    admin103List.add(cols[12]);
			    admin103List.add(cols[13]);
			    admin103List.add(cols[14]);
			    admin103List.add(cols[15]);
			    admin103List.add(cols[16]);
			    admin103List.add(cols[17]);
			    admin103List.add(cols[18]);
			    admin103List.add(cols[19]);
			    admin103List.add(cols[20]);
			    admin103List.add(cols[21]);
			    admin103List.add(cols[22]);
			    admin103List.add(cols[23]); 
			    admin103List.add(cols[24]);
			    admin103List.add(cols[25]); 
			    admin103List.add(cols[26]);
			    admin103List.add(cols[27]);
			    admin103List.add(cols[28]);
			    admin103List.add(cols[29]);
			    admin103List.add(cols[30]);
			    admin103List.add(cols[31]);
			    admin103List.add(cols[32]);
			    admin103List.add(cols[33]);
			    admin103List.add(cols[34]);
			    admin103List.add(cols[35]);
			    admin103List.add(cols[36]);
			    admin103List.add(cols[37]);
			    admin103List.add(cols[38]);
				}
				else{
					continue;
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 ListIterator<String> listIterator = dataElementList.listIterator();
		 ListIterator<String> csvIterator = admin103List.listIterator();
	        while (csvIterator.hasNext() && listIterator.hasNext() ) {
	        	String answer = csvIterator.next();
	            System.out.println(querySting1+answer+querySting2+querySting3+answer+querySting4+listIterator.next()+querySting5);
	        }
	        
	  

	}

}
