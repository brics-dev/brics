package gov.nih.tbi.commons.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.CalculationRule;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTrigger;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestion;

public interface EformManager extends BaseManager {

	public BasicEform getBasicEform(long eformId);
	
	public List<BasicEform> getBasicEforms(Collection <String> shortNames);

	public Eform getEformNoLazyLoad(long eformId);
	
	public Eform getEformNoLazyLoad(String eformShortName);
	
	public List<Eform> getEformListNoLazyLoad(Collection <String> eformShortName);
	
	public Eform saveEform(Eform eform);
	
	public BasicEform saveBasicEform(BasicEform basicEform);
	
	public Section saveSection(Section section);
	
	public Question saveQuestion(Question question);
	
	public void deleteEform(Eform eform);
	
	public Question getQuestion(long questionId);
	
	public ArrayList<Question> getQuestionByQuestionIds(List<Long> questionIds) ;

	public QuestionAttribute saveQuestionAttribute(QuestionAttribute qa);

	public List<BasicEform> getBasicEformSearchList(Set<Long> eformIds);
	
	public List<BasicEform> getAll();
	
	public List<BasicEform> searchEformWithFormStructureTitle(List<Long> eformIds);
	
	public Eform get(Long eformId);
	
	public List<QuestionDocument> getQuestionDocuments(Question question);
	
	public List<String> getFileNameListByQuestion(Question question);
	
	public QuestionDocument getQuestionDocument(Question question, String fileName);
	
	public void deleteQuestionImages(List<QuestionDocument> documents);
	
	public void deleteEform(Long eFormId);
	
	public void deleteQuestions(List<Question> questionList);
	
	public void saveQuestionDocument(List<QuestionDocument> quesDocs);
	
	public void saveQuestionDocument(QuestionDocument quesDocs);
	
	public void removeCalculationAndSkipQuestions(Eform eform);
	
	public List<String> validateEformShortName(String shortName);
	
	public boolean isEformShortNameUnique(String shortName);
	
	public Eform storeMigratedEform(Eform eform);
	
	public String generateVersionedEformShortName(String eformShortName);

	public Map<Long, Question> copyEformQuestions(Eform eform) throws JSchException;
	
	public Map<Long, Question> copyEformQuestionsWithoutEmailTrigger(Eform eform) throws JSchException;
	
	public Eform copyEform(Eform eformToCopy, Account account) throws JSchException;
	
	public Eform eformImportCopy(Eform eformToCopy, Account account) throws JSchException;
	
	public List<BasicEform> basicEformSearch(Set<Long> eformIds, List<StatusType> eformStatus, String formStructureName, String createdBy,
			Boolean isShared);
	
	public void deleteEmailTrigger(List<EmailTrigger> emailTriggers);
	
	public void deleteQuestionDocumentation(List<QuestionDocument> questionDocuments);
	
	public void removeAllCalculationQuestions(ArrayList<CalculationQuestion> calculationQuestionsToRemove);
	
	public void removeAllSkipRuleQuestions(ArrayList<SkipRuleQuestion> skipRuleQuestionsToRemove);
	
	public void removeAllQuestionDocumentsAssociatedToQuestion(Set<Question> questionSet);
	
	public void saveOrUpdateEform(Eform eform);
	
	public List<BasicEform> searchEform(Account account, boolean Onlyowned, String proxyTicket);
	
	public Set<QuestionAttribute> getQuestionAttributeListByEformShortName(String eformShortName);
	
	/**
	 * Gets a set of QuestionAttribute objects for the given collection of eForm short names. This method can chew up a
	 * lot of execution time, since it will be getting data that would otherwise been lazy loaded. So use this method
	 * with great caution.
	 * 
	 * @param eFormShortNames - A collection of eForm short names that will be used in querying the system for Eform
	 *        objects.
	 * @return A set of QuestionAttribute objects for the given collection of eForm short names.
	 * @throws HibernateException When there is an error while retrieving these QuestionAttribute objects from the
	 *         system.
	 */
	public Set<QuestionAttribute> getQuestionAttributeListByEformShortNameCollection(
			Collection<String> eFormShortNames);

	public List<CalculationRule> getAllCalculationRules();
	
}
