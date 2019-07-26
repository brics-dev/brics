package gov.nih.tbi.metastudy.service.hibernate;

import java.util.Date;
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
import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;

@ContextConfiguration({"/context.xml"})
public class MetaStudyManagerTest extends AbstractTestNGSpringContextTests {
	private static final Logger logger = Logger.getLogger(MetaStudyManagerTest.class);

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	protected MetaStudyManager metaStudyManager;

	@Autowired
	protected AccountManager accountManager;

	@Test
	public void doiCreateTest() {

		if (!modulesConstants.isDoiEnabled()) {
			logger.info("DOI is disabled for this instance. Skipping create DOI test.");
			return;
		}

		MetaStudy ms = null;

		// Get an example meta study object from the database.
		try {
			ms = getAMetaStudy();

			Assert.assertNotNull(ms, "Was unable to get a meta study object from the database.");

			if (ms == null) {
				return;
			}

			// Initialize the meta study object.
			ms.setDoi("");
			ms.setOstiId(null);
			ms.setPublishedDate(new Date());
		} catch (Exception e) {
			Assert.fail("Got an error while getting an example meta study object from the database.", e);
			return;
		}

		// Create a DOI off of the example meta study.
		try {
			metaStudyManager.createDoiForMetaStudy(ms);

			Assert.assertFalse((ms.getDoi() == null) || ms.getDoi().isEmpty(), "No DOI detected.");
			Assert.assertTrue((ms.getOstiId() != null) && (ms.getOstiId() > 0), "No OSIT ID detected.");
		} catch (IllegalStateException ise) {
			Assert.fail("Couldn't convert the meta study to DOI record.", ise);
		} catch (WebApplicationException wae) {
			Assert.fail("Got an error from the IAD web service.", wae);
		} catch (DoiWsValidationException dwve) {
			Assert.fail("Got a validation error from the IAD web service.", dwve);
		} catch (Exception e) {
			Assert.fail("Got an error while creating a DOI for the \"" + ms.getTitle() + "\" meta study.", e);
		}
	}

	private MetaStudy getAMetaStudy() {
		MetaStudy cms = null;

		for (MetaStudy ms : metaStudyManager.getAllMetaStudies()) {
			// Make sure this meta study has an owner.
			List<EntityMap> entities = accountManager.listEntityAccess(ms.getId(), EntityType.META_STUDY);
			Account owner = null;

			for (EntityMap em : entities) {
				if (em.getPermission() == PermissionType.OWNER) {
					owner = em.getAccount();
					break;
				}
			}

			if (owner != null) {
				cms = ms;
				break;
			}
		}

		return cms;
	}

}
