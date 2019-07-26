package gov.nih.tbi.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.service.cache.InstancedDataCache;

import java.util.Collection;
import java.util.List;

import org.jasig.cas.client.validation.Assertion;

public interface DownloadManager {

	public String downloadDataTable(String packageName, InstancedDataTable instancedDataTable,
			List<FormResult> selectedForms, CodeMapping codeMapping, String username, boolean isNormalCSV,
			InstancedDataCache cache) throws CSVGenerationException;

	public String downloadDataCart(String packageName, Collection<FormResult> forms, String userName,
			boolean isNormalCSV);

	public String downloadToMetaStudy(String fileName, InstancedDataTable instancedDataTable,
			List<FormResult> selectedForms, CodeMapping codeMapping, Account userAccount, long metaStudyId,
			String description, Assertion casAssertion, InstancedDataCache cache);

	public InstancedDataTable generateInstancedDataTable(List<FormResult> forms, String username);

}
