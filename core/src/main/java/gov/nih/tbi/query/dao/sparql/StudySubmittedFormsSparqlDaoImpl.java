package gov.nih.tbi.query.dao.sparql;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import gov.nih.tbi.MetadataStore;
import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.query.dao.StudySubmittedFormsSparqlDao;
import gov.nih.tbi.repository.model.PublicSubmittedForm;
import gov.nih.tbi.repository.model.StudySubmittedForm;

@Repository
public class StudySubmittedFormsSparqlDaoImpl extends
GenericSparqlDaoImpl <List<StudySubmittedForm>> implements StudySubmittedFormsSparqlDao{

	@Override
	public List<List<StudySubmittedForm>> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StudySubmittedForm> get(String studyTitle) {
		
		List <StudySubmittedForm> studySubmittedForms = new ArrayList<StudySubmittedForm>();
		
		StringBuffer qb =  new StringBuffer();
		qb.append("select ?studyId ?studyTitle ?fsName ?fsShortName ?datasetStatus (str(count(?row)) as ?count) {");
		qb.append("?row a ?form .");
		qb.append("?form <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/shortName> ?fsShortName .");
		qb.append("?form <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/title> ?fsName .");
		qb.append("?form rdfs:subClassOf <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure> .");
		qb.append("?row fs:dataset ?dataset .");
		qb.append("?dataset dataset:status ?datasetStatus .");
		qb.append("?dataset study:facetedStudy ?study .");
		qb.append("?study study:title ?studyTitle .");
		qb.append("?study study:studyId ?studyId .");
		qb.append("FILTER (str(?studyTitle) = \""+studyTitle+"\")");
		qb.append("}");
		qb.append(" group by ?fsName ?fsShortName ?studyId ?studyTitle ?datasetStatus");
		qb.append(" order by asc(?fsName)");
		
		String query = qb.toString();
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		
		String studyIdVar = resultset.getResultVars().get(0);
		String studyTitleVar = resultset.getResultVars().get(1);
        String fSNameVar = resultset.getResultVars().get(2);
        String fSShortNameVar = resultset.getResultVars().get(3);
        String datasetStatusVar = resultset.getResultVars().get(4);
        String countVar = resultset.getResultVars().get(5);
        
        
        while (resultset.hasNext()) {
            QuerySolution row = resultset.next();

            if (!row.contains(studyTitleVar)) {
                continue;
            }

            Long studyId = row.getLiteral(studyIdVar).getLong();
            String title = row.getLiteral(studyTitleVar).toString();
            String fSTitle = row.getLiteral(fSNameVar).toString();
            String fSShortName = row.getLiteral(fSShortNameVar).toString();
            String numberOfRecords = row.getLiteral(countVar).toString();
            String datasetStatus = row.getLiteral(datasetStatusVar).toString();
            
            StudySubmittedForm studySubmittedForm = new StudySubmittedForm(studyId,title,fSTitle,fSShortName,numberOfRecords,datasetStatus);
            studySubmittedForms.add(studySubmittedForm);
            
        }
				
		return studySubmittedForms;
			
	}

	@Override
	public List<StudySubmittedForm> save(List<StudySubmittedForm> object) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Multimap<Long, StudySubmittedForm> getAllStudySubmittedForms() {
			
		Multimap<Long, StudySubmittedForm> studySubmittedForms = ArrayListMultimap.create();
		
		StringBuffer qb =  new StringBuffer();
		qb.append("select ?studyId ?studyTitle ?fsName ?fsShortName ?datasetStatus  (str(count(?row)) as ?count) {");
		qb.append("?row a ?form .");
		qb.append("?form <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/shortName> ?fsShortName .");
		qb.append("?form <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/title> ?fsName .");
		qb.append("?form rdfs:subClassOf <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure> .");
		qb.append("?row fs:dataset ?dataset .");
		qb.append("?dataset dataset:status ?datasetStatus .");
		qb.append("?dataset study:facetedStudy ?study .");
		qb.append("?study study:title ?studyTitle .");
		qb.append("?study study:studyId ?studyId .");
		qb.append("?study study:status ?studyStatus .");
		qb.append("FILTER (str(?studyStatus ) = \"Public\") ");
		
		qb.append("}");
		qb.append(" group by ?fsName ?fsShortName ?studyId ?studyTitle ?datasetStatus");
		qb.append(" order by asc(?fsName)");
		
		String query = qb.toString();
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		
		String studyIdVar = resultset.getResultVars().get(0);
		String studyTitleVar = resultset.getResultVars().get(1);
        String fSNameVar = resultset.getResultVars().get(2);
        String fSShortNameVar = resultset.getResultVars().get(3);
        String datasetStatusVar = resultset.getResultVars().get(4);
        String countVar = resultset.getResultVars().get(5);
        
        
        while (resultset.hasNext()) {
            QuerySolution row = resultset.next();

            if (!row.contains(studyTitleVar)) {
                continue;
            }
            Long studyId = row.getLiteral(studyIdVar).getLong();
            String title = row.getLiteral(studyTitleVar).toString();
            String fSTitle = row.getLiteral(fSNameVar).toString();
            String fSShortName = row.getLiteral(fSShortNameVar).toString();
            String numberOfRecords = row.getLiteral(countVar).toString();
            String datasetStatus = row.getLiteral(datasetStatusVar).toString();
            
            StudySubmittedForm studySubmittedForm = new StudySubmittedForm(studyId,title,fSTitle,fSShortName,numberOfRecords,datasetStatus);
            studySubmittedForms.put(studyId, studySubmittedForm);
            
        }
        			
		return studySubmittedForms;
		
	}
	
	@Override
	public List<PublicSubmittedForm> getAllPublicSubmittedForms(){
		List<PublicSubmittedForm> publicSubmittedFormList = new ArrayList<PublicSubmittedForm>();
		
		StringBuffer qb =  new StringBuffer();
		qb.append("select  ?fsShortName ?fsName (str(count(?row)) as ?rowCount) ");
		qb.append("where { ");
		qb.append("?row a ?form . ");
		qb.append("?form rdfs:subClassOf <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure> . ");
		qb.append("?form <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/shortName> ?fsShortName . ");
		qb.append("?form <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/title> ?fsName . ");		
		qb.append("?row fs:dataset ?dataset . ");
		qb.append("?dataset study:facetedStudy ?study . ");
		qb.append("?study study:status ?studyStatus . ");
		qb.append("FILTER  (str(?studyStatus) = 'Public') ");
		qb.append("} ");
		qb.append(" group by ?fsName ?fsShortName");
		qb.append(" order by asc(?fsName)");
		
		String query = qb.toString();
//		System.out.println("StudySubmittedFormsSparqlDaoImpl.java ->getAllPublicSubmittedForms() query: "+query);
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		
		String fsShortNameVar = resultset.getResultVars().get(0);
        String fSNameVar = resultset.getResultVars().get(1);        
        String countVar = resultset.getResultVars().get(2);
		
        while (resultset.hasNext()) {
            QuerySolution row = resultset.next();

            if (!row.contains(fsShortNameVar)) {
                continue;
            }

            String fsShortName = row.getLiteral(fsShortNameVar).toString();
            String fSTitle = row.getLiteral(fSNameVar).toString();            
            String numberOfRecords = row.getLiteral(countVar).toString();            
            List<String> studyTitleList = getStudyTitleListByFSShortName(fsShortName);
            
            PublicSubmittedForm studySubmittedForm = new PublicSubmittedForm(fSTitle,fsShortName,studyTitleList,numberOfRecords);
            publicSubmittedFormList.add(studySubmittedForm);           
        }
		return publicSubmittedFormList;
	}
	
	public List<String> getStudyTitleListByFSShortName(String fsShortName){
		List<String> studyTitleList = new ArrayList<String>();
		
		StringBuffer qb =  new StringBuffer();
		qb.append("select ?studyTitle ?studyId (str(count(?row)) as ?rowCount) ");
		qb.append("where { ");
		qb.append("?row a ?form . ");
		qb.append("?form rdfs:subClassOf <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure> . ");
		qb.append("?form <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/shortName> ?fsShortName . ");		
		qb.append("?form study:facetedStudy ?study . ");
		qb.append("?row fs:dataset ?dataset . ");
		qb.append("?dataset study:facetedStudy ?study . ");
		qb.append("?study study:title ?studyTitle . ");
		qb.append("?study study:studyId ?studyId . ");
		qb.append("?study study:status ?studyStatus . ");
		qb.append("FILTER (str(?fsShortName ) = \""+fsShortName+"\") ");
		qb.append("FILTER  (str(?studyStatus) = 'Public') ");
		qb.append("} ");
		qb.append("group by ?fsShortName ?studyTitle ?studyId ");
		qb.append("order by asc(?studyTitle) ");
		
		String query = qb.toString();
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		String studyTitleVar = resultset.getResultVars().get(0);
		String studyIdVar = resultset.getResultVars().get(1);
		String countVar = resultset.getResultVars().get(2);
		JsonObject publicationJson = new JsonObject();
		while (resultset.hasNext()) {
			QuerySolution row = resultset.next();
			if(!row.contains(studyTitleVar)){
				continue;
			}
			String studyTitle = row.getLiteral(studyTitleVar).toString();
			String studyId = row.getLiteral(studyIdVar).toString();
			String numberOfRecords = row.getLiteral(countVar).toString();
			publicationJson.addProperty("studyTitle", studyTitle);
			publicationJson.addProperty("studyId", studyId);
			publicationJson.addProperty("numberOfRecords", numberOfRecords);
			String publications = publicationJson.toString(); 
			studyTitleList.add(publications);
		}		
		return studyTitleList;
	}
	
	public Integer getRowCountByStudy(String studyId){
		Integer numOfRows = 0;

		StringBuffer qb =  new StringBuffer();
		qb.append("select (str(count(distinct ?row)) as ?rowCount)  where {");
		qb.append("     ?row fs:dataset ?dataset . ");		
		qb.append("     ?dataset study:facetedStudy ?study . ");
		qb.append("     ?study study:studyId ?studyId . ");
		qb.append("		FILTER (str(?studyId ) = \""+studyId+"\") ");
		qb.append("} ");
		
		String query = qb.toString(); //System.out.println("StudySubmittedFormsSparqlDaoImpl.getSubjectCountByStudy() query: "+query);
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		String rowCount = resultset.getResultVars().get(0);
		while (resultset.hasNext()) {
			QuerySolution row = resultset.next();
			if(!row.contains(rowCount)){
				continue;
			}
			numOfRows = Integer.valueOf(row.getLiteral(rowCount).toString());
			
		}		
		return numOfRows;
	}
	
	public Integer getSubjectCountByStudy(String studyId){
		Integer subjectCount = 0;

		StringBuffer qb =  new StringBuffer();
		qb.append("select (str(count(distinct ?guid)) as ?guidCount)  where {");
		qb.append("     ?row fs:dataset ?dataset . ");		
		qb.append("     ?dataset study:facetedStudy ?study . ");
		qb.append("     ?study study:studyId ?studyId . ");
		qb.append("		?row <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/guid> ?guidUri .  ");	
		qb.append("		?guidUri ?p ?guid .  ");
		qb.append("		FILTER (str(?studyId ) = \""+studyId+"\") ");
		qb.append("} ");
		
		String query = qb.toString(); //System.out.println("StudySubmittedFormsSparqlDaoImpl.getSubjectCountByStudy() query: "+query);
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		String guidCount = resultset.getResultVars().get(0);
		while (resultset.hasNext()) {
			QuerySolution row = resultset.next();
			if(!row.contains(guidCount)){
				continue;
			}
			subjectCount = Integer.valueOf(row.getLiteral(guidCount).toString());
			
		}		
		return subjectCount;
	}
	
	public Integer getFormCountByStudy(String studyId){
		Integer numOfForms = 0;

		StringBuffer qb =  new StringBuffer();
		qb.append("select (str(count(distinct ?form)) as ?formCount) { ?study rdfs:subClassOf <http://ninds.nih.gov/repository/fitbir/1.0/Study>. "
				+ "?study <http://ninds.nih.gov/repository/fitbir/1.0/Study/studyId> \""+studyId+"\" ." + 
				"?study <http://ninds.nih.gov/repository/fitbir/1.0/Study/facetedForm> ?form ." + 
				"}");
		String query = qb.toString(); //System.out.println("StudySubmittedFormsSparqlDaoImpl.getSubjectCountByStudy() query: "+query);
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		String formCount = resultset.getResultVars().get(0);
		while (resultset.hasNext()) {
			QuerySolution row = resultset.next();
			if(!row.contains(formCount)){
				continue;
			}
			numOfForms = Integer.valueOf(row.getLiteral(formCount).toString());
			
		}		
		return numOfForms;
	}
	
	public String getLastSubmitDateByStudy(String studyId){
		String lastSubmitDate = "";

		StringBuffer qb =  new StringBuffer();
		qb.append("select (str(max(?submitDate)) as ?maxSubmitDate)  where {");
		qb.append("     ?row fs:dataset ?dataset . ");		
		qb.append("     ?dataset study:facetedStudy ?study . ");
		qb.append("     ?dataset dataset:submitDate ?submitDate . ");
		qb.append("     ?study study:studyId ?studyId . ");
		qb.append("		FILTER (str(?studyId ) = \""+studyId+"\") ");
		qb.append("} ");
		
		String query = qb.toString(); //System.out.println("StudySubmittedFormsSparqlDaoImpl.getSubjectCountByStudy() query: "+query);
		ResultSet resultset = virtuosoStore.querySelect(query, MetadataStore.REASONING);
		String maxSubmitDate = resultset.getResultVars().get(0);
		while (resultset.hasNext()) {
			QuerySolution row = resultset.next();
			if(!row.contains(maxSubmitDate)){
				continue;
			}
			lastSubmitDate = row.getLiteral(maxSubmitDate).toString();
			
		}		
		return lastSubmitDate;
	}
	
}
