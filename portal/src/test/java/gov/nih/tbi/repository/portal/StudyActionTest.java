package gov.nih.tbi.repository.portal;

import gov.nih.tbi.commons.model.DatasetStatus;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StudyActionTest {

	@Test
	public void validateStatusRequestTest() {
		// Private
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.PRIVATE, DatasetStatus.PRIVATE));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.PRIVATE, DatasetStatus.ERROR));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.PRIVATE, DatasetStatus.LOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.PRIVATE, DatasetStatus.UPLOADING));
		Assert.assertTrue(StudyDatasetAction.validateStatusRequest(DatasetStatus.PRIVATE, DatasetStatus.SHARED));
		Assert.assertTrue(StudyDatasetAction.validateStatusRequest(DatasetStatus.PRIVATE, DatasetStatus.ARCHIVED));
		Assert.assertTrue(StudyDatasetAction.validateStatusRequest(DatasetStatus.PRIVATE, DatasetStatus.DELETED));
		// Shared
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.SHARED, DatasetStatus.PRIVATE));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.SHARED, DatasetStatus.ERROR));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.SHARED, DatasetStatus.LOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.SHARED, DatasetStatus.UPLOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.SHARED, DatasetStatus.SHARED));
		Assert.assertTrue(StudyDatasetAction.validateStatusRequest(DatasetStatus.SHARED, DatasetStatus.ARCHIVED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.SHARED, DatasetStatus.DELETED));
		// Archived
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ARCHIVED, DatasetStatus.PRIVATE));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ARCHIVED, DatasetStatus.ERROR));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ARCHIVED, DatasetStatus.LOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ARCHIVED, DatasetStatus.UPLOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ARCHIVED, DatasetStatus.SHARED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ARCHIVED, DatasetStatus.ARCHIVED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ARCHIVED, DatasetStatus.DELETED));
		// Loading
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.LOADING, DatasetStatus.PRIVATE));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.LOADING, DatasetStatus.ERROR));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.LOADING, DatasetStatus.LOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.LOADING, DatasetStatus.UPLOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.LOADING, DatasetStatus.SHARED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.LOADING, DatasetStatus.ARCHIVED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.LOADING, DatasetStatus.DELETED));
		// Error
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ERROR, DatasetStatus.PRIVATE));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ERROR, DatasetStatus.ERROR));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ERROR, DatasetStatus.LOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ERROR, DatasetStatus.UPLOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ERROR, DatasetStatus.SHARED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ERROR, DatasetStatus.ARCHIVED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.ERROR, DatasetStatus.DELETED));
		// Uploading
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.UPLOADING, DatasetStatus.PRIVATE));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.UPLOADING, DatasetStatus.ERROR));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.UPLOADING, DatasetStatus.LOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.UPLOADING, DatasetStatus.UPLOADING));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.UPLOADING, DatasetStatus.SHARED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.UPLOADING, DatasetStatus.ARCHIVED));
		Assert.assertFalse(StudyDatasetAction.validateStatusRequest(DatasetStatus.UPLOADING, DatasetStatus.DELETED));
	}
}
