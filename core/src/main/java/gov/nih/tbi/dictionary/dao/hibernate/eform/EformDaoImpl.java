package gov.nih.tbi.dictionary.dao.hibernate.eform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.eform.EformDao;
import gov.nih.tbi.dictionary.model.CalculationRule;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTrigger;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;

@Transactional("dictionaryTransactionManager")
@Repository
public class EformDaoImpl extends GenericDictDaoImpl<Eform, Long> implements EformDao {

	@Autowired
	public EformDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {
		super(Eform.class, sessionFactory);
	}

	public Eform getEformNoLazyLoad(long eformId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Eform> query = cb.createQuery(Eform.class);
		Root<Eform> root = query.from(Eform.class);

		query.where(cb.equal(root.get("id"), eformId)).distinct(true);

		// join on the lazy loaded fields
		Fetch<Eform, Section> sectionFetch = root.fetch("sectionList", JoinType.LEFT);
		Fetch<Section, SectionQuestion> sqFetch = sectionFetch.fetch("sectionQuestion", JoinType.LEFT);
		//sqFetch.fetch("calculatedQuestion", JoinType.LEFT);
		//sqFetch.fetch("skipRuleQuestion", JoinType.LEFT);
		sqFetch.fetch("section", JoinType.LEFT);
		Fetch<SectionQuestion, Question> qFetch = sqFetch.fetch("question", JoinType.LEFT);
		qFetch.fetch("visualScale", JoinType.LEFT);
		qFetch.fetch("questionDocument", JoinType.LEFT);
		qFetch.fetch("questionAnswerOption", JoinType.LEFT);
		Fetch<Question, QuestionAttribute> qaFetch = qFetch.fetch("questionAttribute", JoinType.LEFT);
		Fetch<QuestionAttribute, EmailTrigger> etFetch = qaFetch.fetch("emailTrigger", JoinType.LEFT);
		etFetch.fetch("triggerValues", JoinType.LEFT);

		Eform eform = getUniqueResult(query);
		selectFetchSectionQuestions(eform);
		
		return eform;
	}

	public Eform getEformNoLazyLoad(String eformShortName) {
		// null check id
		if (!StringUtils.isBlank(eformShortName)) {

			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<Eform> query = cb.createQuery(Eform.class);
			Root<Eform> root = query.from(Eform.class);

			query.where(cb.like(cb.upper(root.get("shortName")), eformShortName.trim().toUpperCase())).distinct(true);

			// join on the lazy loaded fields
			Fetch<Eform, Section> sectionFetch = root.fetch("sectionList", JoinType.LEFT);
			Fetch<Section, SectionQuestion> sqFetch = sectionFetch.fetch("sectionQuestion", JoinType.LEFT);
			sqFetch.fetch("section", JoinType.LEFT);
			Fetch<SectionQuestion, Question> qFetch = sqFetch.fetch("question", JoinType.LEFT);
			Fetch<Question, QuestionAttribute> qaFetch = qFetch.fetch("questionAttribute", JoinType.LEFT);
			qFetch.fetch("visualScale", JoinType.LEFT);
			qFetch.fetch("questionDocument", JoinType.LEFT);
			qFetch.fetch("questionAnswerOption", JoinType.LEFT);
			Fetch<QuestionAttribute, EmailTrigger> etFetch = qaFetch.fetch("emailTrigger", JoinType.LEFT);
			etFetch.fetch("triggerValues", JoinType.LEFT);

			Eform result = getUniqueResult(query);
			selectFetchSectionQuestions(result);

			return result;

		} else {
			return new Eform();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Eform> getEformNoLazyLoad(Collection<String> eFormShortNames) {
		// Validate the given short name collection.
		if ((eFormShortNames == null) || eFormShortNames.isEmpty()) {
			return new ArrayList<Eform>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Eform> query = cb.createQuery(Eform.class);
		Root<Eform> root = query.from(Eform.class);

		query.where(root.get("shortName").in(eFormShortNames)).distinct(true);

		// Force fetch data that would otherwise be lazy loaded.
		Fetch<Eform, Section> sectionFetch = root.fetch("sectionList", JoinType.LEFT);
		Fetch<Section, SectionQuestion> sqFetch = sectionFetch.fetch("sectionQuestion", JoinType.LEFT);
		sqFetch.fetch("section", JoinType.LEFT);
		Fetch<SectionQuestion, Question> qFetch = sqFetch.fetch("question", JoinType.LEFT);
		Fetch<Question, QuestionAttribute> qaFetch = qFetch.fetch("questionAttribute", JoinType.LEFT);
		qFetch.fetch("visualScale", JoinType.LEFT);
		qFetch.fetch("questionDocument", JoinType.LEFT);
		qFetch.fetch("questionAnswerOption", JoinType.LEFT);
		Fetch<QuestionAttribute, EmailTrigger> etFetch = qaFetch.fetch("emailTrigger", JoinType.LEFT);
		etFetch.fetch("triggerValues", JoinType.LEFT);

		List<Eform> eForms = createQuery(query).getResultList();
		for (Eform eform : eForms) {
			selectFetchSectionQuestions(eform);
		}

		return eForms;
	}

	
	// This approach mocks FetchMode.SELECT to avoid Hibernate MultipleBagFetchException
	private void selectFetchSectionQuestions(Eform eform) {
		if (eform != null) {
			for (Section section : eform.getSectionList()) {
				for (SectionQuestion sq : section.getSectionQuestion()) {
					sq.getCalculatedQuestion().size();
					sq.getSkipRuleQuestion().size();
				}
			}
		}
	}
	
	@Override
	public void deleteEform(long eFormId) {
		this.remove(eFormId);
	}

	public void saveOrUpdate(Eform eform) {
		getSession().merge(eform);
	}

	public List<CalculationRule> getAllCalculationRules() {

		String baseQuery =
				"select e.short_name as eform_name, s.name as section_name, q.name as question_name, sq.calculation"
						+ " from eform e join section s on e.id = s.eform_id" 
						+ " join section_question sq on sq.section_id = s.id" 
						+ " join question q on sq.question_id = q.id"
						+ " where sq.calculation != '' and s.repeated_section_parent_id isNull ";

		Query query = getSession().createNativeQuery(baseQuery);

		List<Object[]> results = query.getResultList();
		List<CalculationRule> rules = new ArrayList<CalculationRule>();

		for (Object[] obj : results) {
			String eformName = String.valueOf(obj[0]);
			String sectionName = String.valueOf(obj[1]);
			String questionName = String.valueOf(obj[2]);
			String calculation = String.valueOf(obj[3]);

			CalculationRule calculationRule = new CalculationRule(eformName, sectionName, questionName, calculation);
			rules.add(calculationRule);
		}

		return rules;
	}

}
