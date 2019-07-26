package gov.nih.tbi.repository.service.hibernate;

import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.metastudy.service.hibernate.MetaStudyManagerTest;
import gov.nih.tbi.repository.model.hibernate.Study;

@ContextConfiguration({"/context.xml"})
public class RepositoryManagerTest extends AbstractTestNGSpringContextTests {
	private static final Logger logger = Logger.getLogger(MetaStudyManagerTest.class);

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	protected AccountManager accountManager;

	@Autowired
	protected RepositoryManager respositoryManager;

	@Test
	public void doiCreateTest() {
		if (!modulesConstants.isDoiEnabled()) {
			logger.info("DOI is disabled for this instance. Skipping create DOI test.");
			return;
		}

		Study study = null;

		// Get example study object from the database.
		try {
			study = getAStudy();

			Assert.assertNotNull(study, "Was unable to get a study object from the database.");

			if (study == null) {
				return;
			}

			// Initialize the meta study object.
			study.setDoi(null);
			study.setOstiId(null);
		} catch (Exception e) {
			Assert.fail("Got an error while getting an example study object from the database.", e);
			return;
		}

		// Create a DOI off of the example study.
		try {
			respositoryManager.createDoiForStudy(study);

			Assert.assertFalse((study.getDoi() == null) || study.getDoi().isEmpty(), "No DOI detected.");
			Assert.assertTrue((study.getOstiId() != null) && (study.getOstiId() > 0), "No OSIT ID detected.");
		} catch (IllegalStateException ise) {
			Assert.fail("Couldn't convert the study to DOI record.", ise);
		} catch (WebApplicationException wae) {
			Assert.fail("Got an error from the IAD web service.", wae);
		} catch (DoiWsValidationException dwve) {
			Assert.fail("Got a validation error from the IAD web service.", dwve);
		} catch (Exception e) {
			Assert.fail("Got an error while creating a DOI for the \"" + study.getTitle() + "\" study.", e);
		}
	}

	private Study getAStudy() {
		Study cs = null;
		Account adminAcc = accountManager.getAccountByUserName("administrator");
		List<Study> studies = respositoryManager.listStudies(adminAcc, null, PermissionType.READ);

		// Find the first study with an owner associated with it.
		for (Study s : studies) {
			List<EntityMap> entities = accountManager.listEntityAccess(s.getId(), EntityType.STUDY);
			Account owner = null;

			for (EntityMap em : entities) {
				if (em.getPermission() == PermissionType.OWNER) {
					owner = em.getAccount();
					break;
				}
			}

			// Check if an owner was found.
			if (owner != null) {
				cs = s;
				break;
			}
		}

		return cs;
	}

}
