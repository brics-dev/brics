
package gov.nih.tbi.commons.dao.sparql;

import java.util.Calendar;
import java.util.Date;

import gov.nih.tbi.MetadataStore;
import gov.nih.tbi.VirtuosoStore;
import gov.nih.tbi.commons.dao.GenericSparqlDao;
import gov.nih.tbi.commons.util.QueryConstructionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * This class implements the generic dao for sparql
 * 
 * @author Francis Chen
 * 
 * @param <T>
 */
@Transactional
public abstract class GenericSparqlDaoImpl<T> implements GenericSparqlDao<T> {

	@Autowired
	protected VirtuosoStore virtuosoStore;

	public GenericSparqlDaoImpl() {

	}

	protected ResultSet querySelect(Query query) {

		return virtuosoStore.querySelect(query, MetadataStore.REASONING, false);
	}

	protected ResultSet queryLargeResultSelect(Query query) {

		return virtuosoStore.querySelect(query, MetadataStore.REASONING, true);
	}


	protected void update(String sparqlUpdate) {

		virtuosoStore.update(sparqlUpdate);
	}

	protected boolean rdfNodeToBoolean(RDFNode booleanNode) {
		return Boolean.valueOf(rdfNodeToString(booleanNode));
	}

	protected Date rdfNodeToDate(RDFNode dateNode) {

		if (dateNode == null) {
			return null;
		}

		// if (dateString != null)
		// {
		// // remove unneeded millisecond precision from dates that come back
		// dateString = dateString.substring(0, dateString.length() - 2);
		// }
		Calendar cal = null;
		if (!dateNode.toString().isEmpty()) {
			cal = ((XSDDateTime) dateNode.asLiteral().getValue()).asCalendar();
			cal.setTimeZone(Calendar.getInstance().getTimeZone());
			cal.add(Calendar.HOUR, -4);
		}

		return cal != null ? cal.getTime() : null;
	}

	/**
	 * Convenience methods to null check the rdf node before parsing it to a string
	 * 
	 * @param node
	 * @return
	 */
	protected String rdfNodeToString(RDFNode node) {
		if (node == null) {
			return null;
		} else {
			String str = node.toString();
			// remove the escaping backslash in rdf node by replacing \" with "
			if (str.indexOf("\\\"") >= 0) {
				str = str.replaceAll("\\\\\"", "\"");
			}
			return QueryConstructionUtil.trimRdfType(str);
		}
	}

	protected void storeObject(OntModel model) {

		virtuosoStore.storeModel(model);
	}
}
