package gov.nih.tbi.service;

import java.util.Collection;
import java.util.List;

import org.jasig.cas.client.validation.Assertion;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.service.cache.InstancedDataCache;

public interface DownloadManager {

	public String downloadDataTable(String packageName, InstancedDataTable instancedDataTable,
			List<FormResult> selectedForms, CodeMapping codeMapping, String username, boolean isNormalCSV,
			InstancedDataCache cache, String booleanExpression, boolean showAgeRange)
			throws CSVGenerationException, FilterEvaluatorException;

	public String downloadDataCart(String packageName, Collection<FormResult> forms, String userName,
			boolean isNormalCSV, boolean showAgeRange) throws FilterEvaluatorException;

	public String downloadToMetaStudy(String fileName, InstancedDataTable instancedDataTable,
			List<FormResult> selectedForms, CodeMapping codeMapping, Account userAccount, long metaStudyId,
			String description, Assertion casAssertion, InstancedDataCache cache, String booleanExpression,
			boolean showAgeRange) throws FilterEvaluatorException;

	public InstancedDataTable generateInstancedDataTable(List<FormResult> forms, String username,
			String booleanExpression) throws FilterEvaluatorException;

}
