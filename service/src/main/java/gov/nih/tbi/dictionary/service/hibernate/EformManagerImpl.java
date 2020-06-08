package gov.nih.tbi.dictionary.service.hibernate;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.EformManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.dao.eform.BasicEformDao;
import gov.nih.tbi.dictionary.dao.eform.CalculationQuestionDao;
import gov.nih.tbi.dictionary.dao.eform.CountQuestionDao;
import gov.nih.tbi.dictionary.dao.eform.EformDao;
import gov.nih.tbi.dictionary.dao.eform.EmailTriggerDao;
import gov.nih.tbi.dictionary.dao.eform.QuestionAttributeDao;
import gov.nih.tbi.dictionary.dao.eform.QuestionDao;
import gov.nih.tbi.dictionary.dao.eform.QuestionDocumentDao;
import gov.nih.tbi.dictionary.dao.eform.SectionDao;
import gov.nih.tbi.dictionary.dao.eform.SkipRuleQuestionDao;
import gov.nih.tbi.dictionary.model.CalculationRule;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.CountQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.CountQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTrigger;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocumentPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestionPk;
import gov.nih.tbi.repository.dao.UserFileDao;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Service
@Scope("singleton")
public class EformManagerImpl extends BaseManagerImpl implements EformManager {

	private static final long serialVersionUID = -968705923035350582L;
	static Logger logger = Logger.getLogger(EformManagerImpl.class);

	@Autowired
	EformDao eformDao;

	@Autowired
	QuestionDao questionDao;

	@Autowired
	SectionDao sectionDao;

	@Autowired
	BasicEformDao basicEformDao;

	@Autowired
	QuestionAttributeDao questionAttributeDao;

	@Autowired
	QuestionDocumentDao questionDocumentDao;

	@Autowired
	SkipRuleQuestionDao skipRuleQuestionDao;

	@Autowired
	CalculationQuestionDao calculationQuestionDao;
	
	@Autowired
	CountQuestionDao countQuestionDao;

	@Autowired
	RepositoryManager repositoryManager;

	@Autowired
	EmailTriggerDao emailTriggerDao;
	
	@Autowired
	UserFileDao userFileDao;
	
	@Autowired
	ModulesConstants modulesConstants;

	public BasicEform getBasicEform(long basicEformId) {
		return basicEformDao.get(basicEformId);
	}

	public Eform getEformNoLazyLoad(long eformId) {
		return eformDao.getEformNoLazyLoad(eformId);
	}

	public Eform getEformNoLazyLoad(String eformShortName) {
		return eformDao.getEformNoLazyLoad(eformShortName);
	}

	public List<Eform> getEformListNoLazyLoad(Collection<String> eformShortNames) {
		return eformDao.getEformNoLazyLoad(eformShortNames);
	}
	
	public Eform saveEform(Eform eform) {
		return eformDao.save(eform);
	}

	public BasicEform saveBasicEform(BasicEform basicEform) {
		return basicEformDao.save(basicEform);
	}

	public Question saveQuestion(Question question) {
		return questionDao.save(question);
	}

	public Section saveSection(Section section) {
		return sectionDao.save(section);
	}

	public List<BasicEform> getBasicEformSearchList(Set<Long> eformIds) {
		return basicEformDao.list(eformIds);
	}

	public List<BasicEform> getAll() {
		return basicEformDao.getAll();
	}

	public List<BasicEform> searchEformWithFormStructureTitle(List<Long> eformIds) {
		return basicEformDao.searchEformWithFormStructureTitle(eformIds);
	}

	public Question getQuestion(long questionId) {
		return questionDao.get(questionId);
	}

	public ArrayList<Question> getQuestionByQuestionIds(List<Long> questionIds) {
		return questionDao.getByIdList(questionIds);
	}

	public QuestionAttribute saveQuestionAttribute(QuestionAttribute qa) {
		return questionAttributeDao.save(qa);
	}

	public Eform get(Long eformId) {
		return eformDao.get(eformId);
	}

	public List<QuestionDocument> getQuestionDocuments(Question question) {
		return questionDocumentDao.getQuestionDocumentsByQuestionId(question);
	}

	public List<String> getFileNameListByQuestion(Question question) {
		List<QuestionDocument> questionDocuments = getQuestionDocuments(question);
		List<String> documentNames = new ArrayList<String>();

		if (questionDocuments != null) {
			for (QuestionDocument qd : questionDocuments) {
				documentNames.add(qd.getQuestionDocumentPk().getFileName());
			}
		}
		return documentNames;
	}

	public QuestionDocument getQuestionDocument(Question question, String fileName) {
		return questionDocumentDao.getQuestionDocument(new QuestionDocumentPk(question, fileName));
	}

	public void deleteQuestionImages(List<QuestionDocument> documents) {
		questionDocumentDao.deleteQuestionImages(documents);
	}

	public void saveQuestionDocument(List<QuestionDocument> quesDocs) {
		for (QuestionDocument qd : quesDocs) {
			saveQuestionDocument(qd);
		}
	}

	public void saveQuestionDocument(QuestionDocument quesDocs) {
	        
		//userFileDao.save(quesDocs.getUserFile());
	        questionDocumentDao.save(quesDocs);
		
	}

	public List<String> validateEformShortName(String shortName) {
		List<String> returnErrors = new ArrayList<String>();
		String twentySevern = eformShortNameMaxLength(shortName);
		if (twentySevern != null) {
			returnErrors.add(twentySevern);
		}
		if (!isEformShortNameUnique(shortName)) {
			returnErrors.add("Short Name must be unique.");
		}
		String pattern = doesEformShortNameMatchPattern(shortName);
		if (pattern != null) {
			returnErrors.add(pattern);
		}
		return returnErrors;
	}

	/*
	 * This method should only be used for the eform migration
	 */
	public synchronized Eform storeMigratedEform(Eform eform) {
		eform.setShortName(generateVersionedEformShortName(eform.getShortName()));
		return saveEform(eform);
	}

	public String generateVersionedEformShortName(String eformShortName) {
		Integer shortNameVersion = 1;
		boolean isUnique = false;
		while (!isUnique) {
			String uniqueShortName = eformShortName + "_" + shortNameVersion.toString();

			if (isEformShortNameUnique(uniqueShortName)) {
				eformShortName = uniqueShortName;
				isUnique = true;
			}
			shortNameVersion++;
		}
		return eformShortName;
	}

	private String eformShortNameMaxLength(String shortName) {
		if (shortName.length() <= 50) {
			return null;
		} else {
			return "Short Name must be less than or equal to 50 characters.";
		}
	}

	public boolean isEformShortNameUnique(String shortName) {
		return basicEformDao.isEformShortNameUnique(shortName);
	}

	private String doesEformShortNameMatchPattern(String shortName) {
		final Pattern pattern = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

		if (pattern.matcher(shortName).matches()) {
			return null;
		} else {
			return "Short Name must start with an alphabetic character and not contain any special characters or spaces.";
		}
	}

	public void deleteEform(Eform eform) {
		removeCalculationAndSkipQuestions(eform);
		eformDao.deleteEform(eform.getId());
	}

	@Override
	public void deleteEform(Long eFormId) {
		eformDao.deleteEform(eFormId);
	}

	public void removeCalculationAndSkipQuestions(Eform eform) {
		ArrayList<CalculationQuestion> calculationQuestionToRemove = new ArrayList<CalculationQuestion>();
		ArrayList<SkipRuleQuestion> skipRuleQuestionToRemove = new ArrayList<SkipRuleQuestion>();
		ArrayList<CountQuestion> countQuestionToRemove = new ArrayList<CountQuestion>();
		Set<Question> deleteQuestionDependencies = new HashSet<Question>();

		for (Section section : eform.getSectionList()) {
			for (SectionQuestion sectionQuestion : section.getSectionQuestion()) {
				deleteQuestionDependencies.add(sectionQuestion.getQuestion());
				if (sectionQuestion.getCalculatedQuestion() != null
						&& !sectionQuestion.getCalculatedQuestion().isEmpty()) {
					calculationQuestionToRemove.addAll(sectionQuestion.getCalculatedQuestion());
				}
				if (sectionQuestion.getSkipRuleQuestion() != null && !sectionQuestion.getSkipRuleQuestion().isEmpty()) {
					skipRuleQuestionToRemove.addAll(sectionQuestion.getSkipRuleQuestion());
				}
				if (sectionQuestion.getCountQuestion() != null
						&& !sectionQuestion.getCountQuestion().isEmpty()) {
					countQuestionToRemove.addAll(sectionQuestion.getCountQuestion());
				}
			}
		}

		if (!calculationQuestionToRemove.isEmpty()) {
			removeAllCalculationQuestions(calculationQuestionToRemove);
		}
		
		if (!countQuestionToRemove.isEmpty()) {
			removeAllCountQuestions(countQuestionToRemove);
		}
		
		if (!skipRuleQuestionToRemove.isEmpty()) {
			removeAllSkipRuleQuestions(skipRuleQuestionToRemove);
		}
		// need to make sure all question documents have been removed before deleting the questions
		removeAllQuestionDocumentsAssociatedToQuestion(deleteQuestionDependencies);
	}

	public void removeAllCalculationQuestions(ArrayList<CalculationQuestion> calculationQuestionsToRemove) {
		calculationQuestionDao.removeAll(calculationQuestionsToRemove);
	}
	
	public void removeAllCountQuestions(ArrayList<CountQuestion> countQuestionsToRemove) {
		countQuestionDao.removeAll(countQuestionsToRemove);
	}

	public void removeAllSkipRuleQuestions(ArrayList<SkipRuleQuestion> skipRuleQuestionsToRemove) {
		skipRuleQuestionDao.removeAll(skipRuleQuestionsToRemove);
	}
	

	public void removeAllQuestionDocumentsAssociatedToQuestion(Set<Question> questionSet) {
		if (questionSet != null && !questionSet.isEmpty()) {
			ArrayList<QuestionDocument> questionDocumentsToRemove = new ArrayList<QuestionDocument>();
			for (Question question : questionSet) {
				if (question.getQuestionDocument() != null) {
					for (QuestionDocument qd : question.getQuestionDocument()) {
						questionDocumentsToRemove.add(qd);
					}
				}
			}
			deleteQuestionDocumentation(questionDocumentsToRemove);
		}
	}


	@Override
	public void deleteQuestions(List<Question> questionList) {
		List<QuestionDocument> questionDocumentsToRemove = new ArrayList<QuestionDocument>();

		for (Question question : questionList) {
			if (question.getQuestionDocument() != null && !question.getQuestionDocument().isEmpty()) {
				questionDocumentsToRemove.addAll(question.getQuestionDocument());
			}
		}

		if (!questionDocumentsToRemove.isEmpty()) {
			deleteQuestionImages(questionDocumentsToRemove);
		}
		questionDao.removeAll(questionList);
	}

	public void deleteEmailTrigger(List<EmailTrigger> emailTriggers) {
		emailTriggerDao.removeAll(emailTriggers);
	}

	public void deleteQuestionDocumentation(List<QuestionDocument> questionDocuments) {
		questionDocumentDao.deleteQuestionImages(questionDocuments);
	}

	public Eform eformImportCopy(Eform eformToCopy, Account account) throws JSchException {
		HashMap<Long, Question> questionCopyMapping = (HashMap<Long, Question>) copyEformQuestions(eformToCopy);
		HashMap<Long, Section> sectionCopyMapping = (HashMap<Long, Section>) copyEformSections(eformToCopy);

		Eform eformCopy = new Eform(eformToCopy);
		eformCopy.setShortName(eformToCopy.getShortName());
		eformCopy.setTitle(eformToCopy.getTitle());
		eformCopy.setCreateBy(account.getUserName());
		eformCopy = eformDao.save(eformCopy);
		HashSet<Section> sectionList = new HashSet<Section>();

		for (Section section : eformToCopy.getSectionList()) {
			sectionList.add(createEformSectionCloneBasedOnNewSectionAndQuestionMapping(section, questionCopyMapping, sectionCopyMapping));
		}

		eformCopy.setSectionList(sectionList);
		
		return eformCopy;
	}

	public Eform copyEform(Eform eformToCopy, Account account) throws JSchException {
		HashMap<Long, Question> questionCopyMapping = (HashMap<Long, Question>) copyEformQuestionsWithoutEmailTrigger(eformToCopy);
		HashMap<Long, Section> sectionCopyMapping = (HashMap<Long, Section>) copyEformSections(eformToCopy);

		Eform eformCopy = new Eform(eformToCopy);
		eformCopy.setCreateBy(account.getUserName());
		HashSet<Section> sectionList = new HashSet<Section>();

		for (Section section : eformToCopy.getSectionList()) {
			sectionList.add(createEformSectionCloneBasedOnNewSectionAndQuestionMapping(section, questionCopyMapping, sectionCopyMapping));
		}

		eformCopy.setSectionList(sectionList);
		return eformCopy;
	}

	private Section createEformSectionCloneBasedOnNewSectionAndQuestionMapping(Section section,
			HashMap<Long, Question> questionCopyMapping, HashMap<Long, Section> sectionCopyMapping) {

		Section sectionCopy = sectionCopyMapping.get(section.getId());

		// replace the old section parent id with the new section parent id
		if (sectionCopy.getRepeatedSectionParent() != null) {
			sectionCopy.setRepeatedSectionParent(sectionCopyMapping.get(sectionCopy.getRepeatedSectionParent()).getId());
		}

		for (SectionQuestion sectionQuestion : section.getSectionQuestion()) {
			Question questionCopy = questionCopyMapping.get(sectionQuestion.getQuestion().getId());
			String newCalculation = "";
			String newCountFormula = "";

			if (sectionQuestion.getCalculation() != null && !sectionQuestion.getCalculation().trim().equals("")) {
				newCalculation = removePsuedoSectionIdsFromCalculationCountFormula(sectionQuestion.getCalculation(),
						sectionCopyMapping, questionCopyMapping);
			}
			
			if (sectionQuestion.getCountFormula() != null && !sectionQuestion.getCountFormula().trim().equals("")) {
				newCountFormula = removePsuedoSectionIdsFromCalculationCountFormula(sectionQuestion.getCountFormula(),
						sectionCopyMapping, questionCopyMapping);
			}

			SectionQuestion sectionQuestionCopy =
					new SectionQuestion(sectionQuestion, sectionCopy, questionCopy, newCalculation,newCountFormula);
			if (sectionQuestion.getCalculatedQuestion() != null && !sectionQuestion.getCalculatedQuestion().isEmpty()) {
				for (CalculationQuestion calculationQuestion : sectionQuestion.getCalculatedQuestion()) {
					CalculationQuestionPk calcQuestionPk = calculationQuestion.getCalculationQuestionCompositePk();
					sectionQuestionCopy.addCalculatedQuestion(new CalculationQuestion(sectionCopy, questionCopy,
							sectionCopyMapping.get(calcQuestionPk.getCalculationSection().getId()),
							questionCopyMapping.get(calcQuestionPk.getCalculationQuestion().getId())));
				}
			}
			if (sectionQuestion.getSkipRuleQuestion() != null && !sectionQuestion.getSkipRuleQuestion().isEmpty()) {
				for (SkipRuleQuestion skipRuleQuestion : sectionQuestion.getSkipRuleQuestion()) {
					SkipRuleQuestionPk skipQuestionPk = skipRuleQuestion.getSkipRuleQuestionCompositePk();
					sectionQuestionCopy.addSkipRuleQuestion(new SkipRuleQuestion(sectionCopy, questionCopy,
							sectionCopyMapping.get(skipQuestionPk.getSkipRuleSection().getId()),
							questionCopyMapping.get(skipQuestionPk.getSkipRuleQuestion().getId())));
				}
			}
			if (sectionQuestion.getCountQuestion() != null && !sectionQuestion.getCountQuestion().isEmpty()) {
				for (CountQuestion countQuestion : sectionQuestion.getCountQuestion()) {
					CountQuestionPk countQuestionPk = countQuestion.getCountQuestionCompositePk();
					sectionQuestionCopy.addCountQuestion(new CountQuestion(sectionCopy, questionCopy,
							sectionCopyMapping.get(countQuestionPk.getCountSection().getId()),
							questionCopyMapping.get(countQuestionPk.getCountQuestion().getId())));
				}
			}
			sectionCopy.addToSectionQuestion(sectionQuestionCopy);
		}
		return sectionCopy;
	}

	private static String removePsuedoSectionIdsFromCalculationCountFormula(String rule,
			HashMap<Long, Section> sectionMap, HashMap<Long, Question> questionMap) {
		if (rule != null & !rule.isEmpty()) {
			int sectionIndex = rule.indexOf("S_");
			while (sectionIndex != -1) {
				Integer sectionStart = sectionIndex + 2;
				Integer sectionEnd = rule.indexOf("_Q", sectionStart);
				String sectionIdToReplace = rule.substring(sectionStart, sectionEnd);
				Long newSectionId = sectionMap.get(Long.parseLong(sectionIdToReplace)).getId();
				rule = rule.substring(0, sectionStart) + String.valueOf(newSectionId)
						+ rule.substring(sectionEnd, rule.length());
				sectionIndex = rule.indexOf("S_", sectionIndex + 1);
			}

			int indexQuestion = rule.indexOf("Q_");
			while (indexQuestion != -1) {
				Integer questionStart = indexQuestion + 2;
				Integer questionEnd = rule.indexOf("]", questionStart);
				String questionIdToReplace = rule.substring(questionStart, questionEnd);
				String newQuestionId = String.valueOf(questionMap.get(Long.parseLong(questionIdToReplace)).getId());
				rule = rule.substring(0, questionStart) + newQuestionId
						+ rule.substring(questionEnd, rule.length());
				indexQuestion = rule.indexOf("Q_", indexQuestion + 1);
			}
		}
		return rule;
	}

	public Map<Long, Question> copyEformQuestionsWithoutEmailTrigger(Eform eform) throws JSchException {
		Map<Long, Question> uniqueQuestions = new HashMap<Long, Question>();

		for (Section section : eform.getSectionList()) {
			for (SectionQuestion sectionQuestion : section.getSectionQuestion()) {
				Question question = sectionQuestion.getQuestion();
				long questionMappingId = question.getId();

				if (!uniqueQuestions.containsKey(questionMappingId)) {
					Question questionCopy = saveQuestion(removeAllEmailTriggersFromQuestion(new Question(question)));

					if (question.getQuestionDocument() != null && !question.getQuestionDocument().isEmpty()) {
						for (QuestionDocument questionDocument : question.getQuestionDocument()) {
							copyQuestionDocument(questionCopy, questionDocument);
						}
					}
					uniqueQuestions.put(questionMappingId, questionCopy);
				}
			}
		}
		return uniqueQuestions;
	}

	private Question removeAllEmailTriggersFromQuestion(Question question) {
		question.getQuestionAttribute().setEmailTrigger(null);
		return question;
	}

	public Map<Long, Question> copyEformQuestions(Eform eform) throws JSchException {

        Map<Long, Question> uniqueQuestions = new HashMap<Long, Question>();
		long questionCopyStartTime = System.currentTimeMillis();
		//Long i = 0l;

		for (Section section : eform.getSectionList()) {
			for (SectionQuestion sectionQuestion : section.getSectionQuestion()) {
				Question question = sectionQuestion.getQuestion();
				long questionMappingId = question.getId();

				if (!uniqueQuestions.containsKey(questionMappingId)) {
					Question questionCopy = saveQuestion(new Question(question));
					
					if(question.getQuestionDocument() != null && !question.getQuestionDocument().isEmpty())
					{ 
					    Set<QuestionDocument> qdSet = new LinkedHashSet<>();
					    
					    for(QuestionDocument qd: question.getQuestionDocument()){
					    	
					    	QuestionDocument questionDocumentCopy = new QuestionDocument();
					    	questionDocumentCopy.setQuestionDocumentPk(new QuestionDocumentPk(questionCopy, qd.getQuestionDocumentPk().getFileName()));
							
							UserFile userFileCopy = new UserFile(qd.getUserFile());
							userFileCopy.setPath(ServiceConstants.DDT_EFORM_FILE_PATH + questionCopy.getId() + ServiceConstants.FILE_SEPARATER);
							userFileCopy.setUploadedDate(new Date());
							userFileCopy.setId(null);

							questionDocumentCopy.setUserFile(userFileCopy);
							repositoryManager.copyFileDDT(qd.getUserFile(), userFileCopy);
							saveQuestionDocument(questionDocumentCopy);
							qdSet.add(questionDocumentCopy);
										        
					    }
					    
					    questionCopy.setQuestionDocument(qdSet);
					}
					uniqueQuestions.put(questionMappingId, questionCopy);
					//i++;
				}
			}
		}
		//ArrayList<BigInteger> uniqueIds = questionDao.getNextKey(i);
		long questionCopyEndTime = System.currentTimeMillis();
		logger.error("This is how long it took to copy and save questions " + (questionCopyEndTime - questionCopyStartTime) + " milliseconds");
		//updateQuestionMappingWithGeneratedId(uniqueIds, uniqueQuestions);
		return uniqueQuestions;
	}

	/*private void updateQuestionMappingWithGeneratedId(ArrayList<BigInteger> uniqueIds,
			Map<Long, Question> uniqueQuestions) {
		long questionIdAddition = System.currentTimeMillis();
		int uniqueIdPlace = 0;
		List<Question> questionSaveList = new ArrayList<Question>();

		for (Map.Entry<Long, Question> entry : uniqueQuestions.entrySet()) {
			Question question = entry.getValue();
			question.setId(uniqueIds.get(uniqueIdPlace).longValue());
			
			if(question.getQuestionDocument() != null && !question.getQuestionDocument().isEmpty()) {
				Set<QuestionDocument> qdSet = new LinkedHashSet<>();
				
				for(QuestionDocument qd: question.getQuestionDocument()){
					qd.setQuestionDocumentPk(new QuestionDocumentPk(question, qd.getQuestionDocumentPk().getFileName()));
					
					UserFile userFileCopy = new UserFile(qd.getUserFile());
					userFileCopy.setPath(ServiceConstants.DDT_EFORM_FILE_PATH + question.getId() + ServiceConstants.FILE_SEPARATER);
					userFileCopy.setUploadedDate(new Date());
					userFileCopy.setId(null);

					qd.setUserFile(userFileCopy);
					qdSet.add(qd);
				}
				question.setQuestionDocument(qdSet);
			}
			uniqueIdPlace++;
			questionSaveList.add(question);
		}
		long questionIdAdditionEnd = System.currentTimeMillis();
		logger.error("This is how long it took to add the ids to the map "
				+ (questionIdAdditionEnd - questionIdAddition) + " milliseconds");
		saveBulkQuestionList(questionSaveList);
	}

	private void saveBulkQuestionList(List<Question> questionList) {
		long questionBatchInsertStart = System.currentTimeMillis();
		questionDao.batchInsetQuestions(questionList);
		long questionbatchInstertEnd = System.currentTimeMillis();
		logger.error("This is how long it took to batch insert questions "
				+ (questionbatchInstertEnd - questionBatchInsertStart) + " milliseconds");
	}*/

	public void copyQuestionDocument(Question questionCopy, QuestionDocument questionDocument) throws JSchException {
		QuestionDocument questionDocumentCopy = new QuestionDocument();
		questionDocumentCopy.setQuestionDocumentPk(
				new QuestionDocumentPk(questionCopy, questionDocument.getQuestionDocumentPk().getFileName()));

		UserFile userFileCopy = new UserFile(questionDocument.getUserFile());
		userFileCopy.setPath(ServiceConstants.DDT_EFORM_FILE_PATH + questionCopy.getId() + ServiceConstants.FILE_SEPARATER);
		userFileCopy.setUploadedDate(new Date());
		userFileCopy.setId(null);
		
		questionDocumentCopy.setUserFile(userFileCopy);

		repositoryManager.copyFileDDT(questionDocument.getUserFile(), userFileCopy);
		saveQuestionDocument(questionDocumentCopy);
	}

	public Map<Long, Section> copyEformSections(Eform eform) {
		Map<Long, Section> uniqueSections = new HashMap<Long, Section>();
		long sectionCopyStartTime = System.currentTimeMillis();
		for (Section section : eform.getSectionList()) {
			long sectionMappingId = section.getId();

			if (!uniqueSections.containsKey(sectionMappingId)) {
				Section newCopy = saveSection(new Section(section));
				uniqueSections.put(sectionMappingId, newCopy);
			}
		}
		long sectionCopyEndTime = System.currentTimeMillis();
		logger.error("This is how long it took to copy and save sections " + (sectionCopyEndTime - sectionCopyStartTime)
				+ " milliseconds");
		return uniqueSections;
	}

	public List<BasicEform> basicEformSearch(Set<Long> eformIds, List<StatusType> eformStatus, String formStructureName,
			String createdBy, Boolean isShared) {
		return basicEformDao.basicEformSearch(eformIds, eformStatus, formStructureName, createdBy, isShared);
	}

	public void saveOrUpdateEform(Eform eform) {
		eformDao.saveOrUpdate(eform);
	}

	@Override
	public List<BasicEform> searchEform(Account account, boolean Onlyowned, String proxyTicket) {
		
		Set <Long> accessIds = buildEformPermissionFacet(account,Onlyowned,proxyTicket);
		
		List<Long> accessIdsList = new ArrayList<Long>();
		accessIdsList.addAll(accessIds);
		
		List<BasicEform> basicEformList = new ArrayList<BasicEform>();
		if(accessIdsList.size()!=0){
			basicEformList = searchEformWithFormStructureTitle(accessIdsList);
		}
		
		return basicEformList;
	}

	private Set<Long> buildEformPermissionFacet(Account account, boolean onlyOwned, String proxyTicket) {
		
		PermissionType permissionType = null;
		
        if (!onlyOwned)
        {
            permissionType = PermissionType.READ;
        }
        else
        {
            permissionType = PermissionType.OWNER;
        }

        // Make a web service call that gets the ids the user has the chosen access to. (Possible results for the
        // search).
        Set<Long> accessIds = null;
        RestAccountProvider restProvider = new RestAccountProvider(modulesConstants.getModulesAccountURL(Long
                .valueOf(account.getDiseaseKey())), proxyTicket);
        try
        {
            accessIds = restProvider.listUserAccess(account.getId(), EntityType.EFORM, permissionType,
                    false);
        }
        catch (UnsupportedEncodingException e)
        {
			logger.error("Could not build eform permission facet.", e);
        }
        return accessIds;
        
	}
	
	public Set<QuestionAttribute> getQuestionAttributeListByEformShortName(String eformShortName) {
		Set<QuestionAttribute> questionAttributeSet = new HashSet<QuestionAttribute>();
		Eform eform = eformDao.getEformNoLazyLoad(eformShortName);

		for (Section section : eform.getSectionList()) {
			for (SectionQuestion sq : section.getSectionQuestion()) {
				questionAttributeSet.add(sq.getQuestion().getQuestionAttribute());
		}
		}
		
		return questionAttributeSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<QuestionAttribute> getQuestionAttributeListByEformShortNameCollection(
			Collection<String> eFormShortNames) {
		List<Eform> eFormList = eformDao.getEformNoLazyLoad(eFormShortNames);
		Set<QuestionAttribute> questionAttributeSet = new HashSet<QuestionAttribute>();

		if (eFormList != null) {
			for (Eform eform : eFormList) {
				for (Section section : eform.getSectionList()) {
					for (SectionQuestion sq : section.getSectionQuestion()) {
						questionAttributeSet.add(sq.getQuestion().getQuestionAttribute());
					}
				}
			}
		}

		return questionAttributeSet;
	}

	@Override
	public List<CalculationRule> getAllCalculationRules() {
		
		List<CalculationRule> calculationRules = eformDao.getAllCalculationRules();	
		
		Map<Long,String> sectionMap = new HashMap<Long, String>();	
		List<Section> allSection = sectionDao.getAll();			
		for(Section section:allSection){
			sectionMap.put(section.getId(), section.getName());
		}
		
		Map<Long,String> questionMap = new HashMap<Long,String>();
		List<Question> allQuestion = questionDao.getAll();		
		for(Question question: allQuestion){
			questionMap.put(question.getId(),question.getName());
		}

			
		for(CalculationRule calculationRule : calculationRules){
			calculationRule.setCalculationRule(replaceIdsWithEntityNameInCalculation(calculationRule.getCalculationRule(),sectionMap,questionMap));
		}
		
	    return calculationRules;		
	}
	
	
	private String replaceIdsWithEntityNameInCalculation(String calculationRule,Map<Long,String> sectionMap,Map<Long,String> questionMap) {
		if (calculationRule != null & !calculationRule.isEmpty()) {
			int sectionIndex = calculationRule.indexOf("[S_");
			while (sectionIndex != -1) {
				Integer sectionStart = sectionIndex + 3;
				Integer sectionEnd = calculationRule.indexOf("_Q", sectionStart);
				String sectionIdToReplace = calculationRule.substring(sectionStart, sectionEnd);
				Long newSectionId = Long.parseLong(sectionIdToReplace);
				String sectionName = sectionMap.get(newSectionId);
				
				//insert sectionName instead of the quotes if we want to show sectionName
				//with the calculation rule
				calculationRule = calculationRule.substring(0, sectionStart)+""+
						calculationRule.substring(sectionEnd, calculationRule.length());
				sectionIndex = calculationRule.indexOf("[S_", sectionIndex + 1);
			}

			int indexQuestion = calculationRule.indexOf("Q_");
			while (indexQuestion != -1) {
				Integer questionStart = indexQuestion + 2;
				Integer questionEnd = calculationRule.indexOf("]", questionStart);
				String questionIdToReplace = calculationRule.substring(questionStart, questionEnd);
				long newQuestionId = Long.parseLong(questionIdToReplace);
				String questionName = questionMap.get(newQuestionId);
				calculationRule = calculationRule.substring(0, questionStart) + "{"+ questionName +"}"
						+ calculationRule.substring(questionEnd, calculationRule.length());
				int index = calculationRule.lastIndexOf("}");
				indexQuestion = calculationRule.indexOf("Q_", index);
			}			
		}
		
		String newCalculationRule = calculationRule.replace("[S__Q_{", "");
		String finalCalculationRule = newCalculationRule.replace("}]", "");
		
		return finalCalculationRule;
	}

	@Override
	public List<BasicEform> getBasicEforms(Collection<String> shortNames) {
		return basicEformDao.getBasicEForms(shortNames);
	} 

}
