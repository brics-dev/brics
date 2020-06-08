package gov.nih.tbi.repository.rdf;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.semantic.model.StudyRDF;

public class RDFGeneratorManagerTest {

	@Test
	public void addRDFForStudyTest() {
		Model model = ModelFactory.createDefaultModel();
		RDFGeneratorManagerImpl rdfGen = new RDFGeneratorManagerImpl();
		Study testStudy = new Study();
		testStudy.setId(1L);
		testStudy.setTitle("title");
		testStudy.setAbstractText("abstract");
		testStudy.setPrincipalInvestigator("pi");
		testStudy.setPrefixedId("BLA-00001");
		testStudy.setStudyStatus(StudyStatus.PUBLIC);
		rdfGen.addRDFForStudy(model, testStudy);
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.addResultVar(Var.alloc("id"));
		query.addResultVar(Var.alloc("title"));
		query.addResultVar(Var.alloc("abstract"));
		query.addResultVar(Var.alloc("pi"));
		query.addResultVar(Var.alloc("prefixedId"));
		query.addResultVar(Var.alloc("status"));
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		block.addTriple(Triple.create(Var.alloc("study"), StudyRDF.PROPERTY_ID.asNode(), Var.alloc("id")));
		block.addTriple(Triple.create(Var.alloc("study"), StudyRDF.PROPERTY_TITLE.asNode(), Var.alloc("title")));
		block.addTriple(Triple.create(Var.alloc("study"), StudyRDF.PROPERTY_ABSTRACT.asNode(), Var.alloc("abstract")));
		block.addTriple(Triple.create(Var.alloc("study"), StudyRDF.PROPERTY_PI.asNode(), Var.alloc("pi")));
		block.addTriple(Triple.create(Var.alloc("study"), StudyRDF.PROPERTY_PREFIXED_ID.asNode(), Var.alloc("prefixedId")));
		block.addTriple(Triple.create(Var.alloc("study"), StudyRDF.PROPERTY_STATUS.asNode(), Var.alloc("status")));
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet rs = qexec.execSelect();
		
		if(rs.hasNext()) {
			QuerySolution row = rs.next();
			String id = row.get("id").toString();
			assertEquals(id, "1");
			String title = row.get("title").toString();
			assertEquals(title, "title");
			String abstractText = row.get("abstract").toString();
			assertEquals(abstractText, "abstract");
			String pi = row.get("pi").toString();
			assertEquals(pi, "pi");
			String prefixedId = row.get("prefixedId").toString();
			assertEquals(prefixedId, "BLA-00001");
			String status = row.get("status").toString();
			assertEquals(status, StudyStatus.PUBLIC.getName());
		}
	}
}
