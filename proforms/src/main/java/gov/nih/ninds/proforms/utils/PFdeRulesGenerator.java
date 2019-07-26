package gov.nih.ninds.proforms.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * This class creates the report with DE contains required,skip and calculation rules
 * @author khanaly
 * https://dcb-confluence.cit.nih.gov/display/~khanaly/Proforms+Rules
 */
public class PFdeRulesGenerator {
	public static final String SINGLE_QUOTE = "\'";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> formsNamesList = new LinkedList<String>();
		formsNamesList.add("Adverse Events");
		formsNamesList.add("Behavioral History");
		formsNamesList.add("CSF Collection Data Form");
		formsNamesList.add("CSF Collection Follow Up Phone Call");
		formsNamesList.add("Demographics");
		formsNamesList.add("Early Termination Questionnaire");
		formsNamesList.add("Epworth Sleepiness Scale");
		formsNamesList.add("Family History");
		formsNamesList.add("Hamilton Anxiety Rating Scale (HAM-A)");
		formsNamesList.add("Hamilton Depression Rating Scale (HDRS)");
		formsNamesList.add("Informed Consent and Enrollment");
		formsNamesList.add("Laboratory Tests and Tracking");
		formsNamesList.add("MDS-UPDRS");
		formsNamesList.add("Modified Schwab and England Scale");
		formsNamesList.add("Montreal Cognitive Assessment (MoCA)");
		formsNamesList.add("Neurological Examination");
		formsNamesList.add("PDBP Inclusion and Exclusion Criteria");
		formsNamesList.add("PDQ-39");
		formsNamesList.add("Prior and Concomitant Medications");
		formsNamesList.add("Protocol Deviations");
		formsNamesList.add("Rapid Eye Movement Behavior Disorder Questionnaire");
		formsNamesList.add("University of Pennsylvania Smell Identification");
		formsNamesList.add("Vital Signs");
		
		for(String s:formsNamesList){
		System.out.println("--Form Name:\t"+s.toString());	
		
		//Required DE
//		System.out.println("select distinct qa.data_element_name from questionattributes qa,form f,section s,sectionquestion sq where ");	
//		System.out.println("f.formid= s.formid and sq.sectionid=s.sectionid and sq.questionattributesid= qa.questionattributesid ");	
//		System.out.println("and f.name ="+SINGLE_QUOTE+s.toString()+SINGLE_QUOTE+" and requiredflag = 't' and protocolid =8 order by qa.data_element_name;");
		
		//Calculation Rules
//		System.out.println("select qa.data_element_name,qa.calculation from questionattributes qa,form f,section s,sectionquestion sq where ");	
//		System.out.println("f.formid= s.formid and sq.sectionid=s.sectionid and sq.questionattributesid= qa.questionattributesid ");	
//		System.out.println("and f.name="+SINGLE_QUOTE+s.toString()+SINGLE_QUOTE+" and calculatedflag='t' and f.protocolid=8 ");
		
		//Skip Rules
		System.out.println(" create or replace function findSkipRules() returns text as ");
		System.out.println(" $BODY$ ");
		System.out.println(" declare ");
		System.out.println(" qa_row record; ");
		System.out.println(" sk_row skiprulequestion%rowtype; ");
		System.out.println(" elem text; ");
		System.out.println(" result text := ''; ");
		System.out.println(" begin ");
		System.out.println(" select s.sectionid, qa.questionid into qa_row from questionattributes qa,form f, section s,sectionquestion sq where  ");
		System.out.println(" f.formid = s.formid and sq.sectionid = s.sectionid and sq.questionattributesid = qa.questionattributesid  ");
		System.out.println(" and f.name = "+SINGLE_QUOTE+s.toString()+SINGLE_QUOTE+" and skipruleflag = 't' and f.protocolid = 8; ");
		System.out.println(" for sk_row in select * from skiprulequestion where sectionid = qa_row.sectionid and questionid = qa_row.questionid ");
		System.out.println(" loop ");
		System.out.println(" execute 'select qa.data_element_name from questionattributes qa join sectionquestion sq on qa.questionattributesid = sq.questionattributesid where sq.sectionid = $1 and sq.questionid = $2' ");
		System.out.println(" into elem using sk_row.skipsectionid, sk_row.skipquestionid; ");
		System.out.println(" if elem is not null and length(elem) > 0 and position(elem in result) = 0  ");
		System.out.println(" then ");
		System.out.println(" result := result || elem || ',';  ");
		System.out.println(" end if;  ");
		System.out.println(" end loop;  ");
		System.out.println(" return left(result, -1);  ");
		System.out.println(" end; ");
		System.out.println(" $BODY$ language plpgsql; ");
		//Prepopulation Rules
//		System.out.println(" select qa.data_element_name,qa.prepopulationvalue from questionattributes qa,form f,section s,sectionquestion sq where "); 
//		System.out.println(" f.formid= s.formid and sq.sectionid=s.sectionid and sq.questionattributesid= qa.questionattributesid ");
//		System.out.println(" and f.name="+SINGLE_QUOTE+s.toString()+SINGLE_QUOTE+" and prepopulation='t' and f.protocolid=1 ");
		}
		
	}

}
