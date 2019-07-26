package gov.nih.tbi.ws.cxf;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.semantic.model.E_Distinct;
import gov.nih.tbi.service.RDFStoreManager;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.InstancedDataUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVWriter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Path("/recordCount")
public class RecordCountReportService extends QueryBaseRestService {

	private static final Logger log = Logger.getLogger(RecordCountReportService.class);

	private static final String[] COLUMNS = {"Study", "Form", "Dataset", "GUID", "Visit Type", "Count"};

	@Autowired
	RDFStoreManager rdfStoreManager;

	@Autowired
	PermissionModel permissionModel;

	@GET
	@Path("/get")
	@Produces("text/csv")
	public Response downloadRecordCount() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		if (!permissionModel.isQueryAdmin()) {
			String msg = "Warning: Only Query Tool admin user can request for Record Count Report.";
			log.error(msg);
			Response errResponse = Response.status(Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errResponse);
		}

		log.info("Start downloading Record Count Report");

		Query query = constructRecordCountQuery();
		ResultSet result = rdfStoreManager.querySelect(query);
		ByteArrayOutputStream baos = writeResultToOutputStream(result);

		ResponseBuilder response = Response.ok(baos.toByteArray(), "text/csv");

		String fileName = "QT Record Counts - " + BRICSTimeDateUtil.getCurrentReadableTimeString() + ".csv";
		log.info("fileName " + fileName);
		response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		response.header("Content-Type", "text/csv");

		return response.build();
	}


	// Constructs the SPARQL query used to create the record count report
	private Query constructRecordCountQuery() {
		Query query = QueryFactory.make();
		query.setQuerySelectType();

		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.setQueryPattern(body);
		query.addResultVar(QueryToolConstants.STUDY_VAR);
		query.addResultVar("formName");
		query.addResultVar(
				QueryToolConstants.DATASET_IDS_VAR,
				new ExprAggregator(QueryToolConstants.DATASET_IDS_VAR, AggregatorFactory.createGroupConcat(false,
						new E_Distinct(new ExprVar(QueryToolConstants.DATASET_ID_VAR)), ", ", null)));
		query.addResultVar(QueryToolConstants.GUID_VAR);
		query.addResultVar(QueryToolConstants.VISIT_TYPE_VAR);
		query.addResultVar(QueryToolConstants.COUNT_VARIABLE, new ExprAggregator(QueryToolConstants.COUNT_VARIABLE,
				AggregatorFactory.createCountExpr(true, new ExprVar(QueryToolConstants.ROW_VAR))));

		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, RDF.type.asNode(), QueryToolConstants.FORM_VAR));
		block.addTriple(Triple.create(QueryToolConstants.FORM_VAR, QueryToolConstants.FORM_PROPERTY_TITLE,
				Var.alloc("formName")));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_PREFIX,
				QueryToolConstants.DATASET_ID_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_STUDY,
				QueryToolConstants.STUDY_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.ROW_GUID, Var.alloc("guidUri")));
		block.addTriple(Triple.create(Var.alloc("guidUri"), RDFS.label.asNode(), QueryToolConstants.GUID_VAR));
		block.addTriple(Triple.create(QueryToolConstants.ROW_VAR, QueryToolConstants.HAS_REPEATABLE_GROUP_INSTANCE_N,
				QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.INSTANCED_REPEATABLE_GROUP_VARIABLE,
				QueryToolConstants.VISIT_TYPE_URI, QueryToolConstants.VISIT_TYPE_VAR));

		Node accountNode = InstancedDataUtil.getAccountNode(permissionModel.getUserName());
		block =
				InstancedDataUtil.addPermissionTriples(block, accountNode, QueryToolConstants.ROW_VAR,
						QueryToolConstants.DATASET_VAR, QueryToolConstants.STUDY_ID_VAR);

		query.addOrderBy(QueryToolConstants.STUDY_VAR, Query.ORDER_ASCENDING);
		return query;
	}

	// Returns the output stream with the CSV data
	private ByteArrayOutputStream writeResultToOutputStream(ResultSet result) {
		int columnSize = COLUMNS.length;

		CSVWriter csvWriter = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			byteOutputStream = new ByteArrayOutputStream();
			csvWriter = new CSVWriter(new PrintWriter(byteOutputStream));
			csvWriter.writeNext(COLUMNS);

			while (result.hasNext()) {
				String[] row = new String[columnSize];
				QuerySolution resultRow = result.next();

				row[0] = InstancedDataUtil.rdfNodeToString(resultRow.get(QueryToolConstants.STUDY_VAR.getName()));
				row[1] = InstancedDataUtil.rdfNodeToString(resultRow.get("formName"));
				row[2] = InstancedDataUtil.rdfNodeToString(resultRow.get(QueryToolConstants.DATASET_IDS_VAR.getName()));
				row[3] = InstancedDataUtil.rdfNodeToString(resultRow.get(QueryToolConstants.GUID_VAR.getName()));
				row[4] = InstancedDataUtil.rdfNodeToString(resultRow.get(QueryToolConstants.VISIT_TYPE_VAR.getName()));
				row[5] = InstancedDataUtil.rdfNodeToString(resultRow.get(QueryToolConstants.COUNT_VARIABLE.getName()));

				csvWriter.writeNext(row);
			}
		} finally {
			try {
				if (csvWriter != null) {
					csvWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return byteOutputStream;
	}

}
