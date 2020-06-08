package gov.nih.tbi.pojo;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class QueryResult {
	private List<String> resultVars;
	private List<QuerySolution> queryData;

	public QueryResult(ResultSet rs) {
		queryData = new ArrayList<>();

		this.resultVars = new ArrayList<>(rs.getResultVars());

		while (rs.hasNext()) {
			queryData.add(rs.next());
		}
	}

	public boolean hasData() {
		return queryData != null && !queryData.isEmpty();
	}

	public List<String> getResultVars() {
		return resultVars;
	}

	public void setResultVars(List<String> resultVars) {
		this.resultVars = resultVars;
	}

	public List<QuerySolution> getQueryData() {
		return queryData;
	}

	public void setQueryData(List<QuerySolution> queryData) {
		this.queryData = queryData;
	}
}
