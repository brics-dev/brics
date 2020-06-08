package gov.nih.tbi.dictionary.validation.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTextField;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;
import gov.nih.tbi.repository.model.SubmissionDataFile;
import gov.nih.tbi.repository.model.SubmissionType;

public class SubmissionPackageBuilderTest {

	@Test
	public void datasetFileListTest() {
		FileNode headNode = new FileNode("/document/file/", "directory", FileType.DIR, null);
		FileNode csvNode = new FileNode("/document/file/", "myCSV.csv", FileType.CSV, headNode);
		FileNode xmlNode1 = new FileNode("/document/file/", "HIT6_20201111_BATCH.xml", FileType.XML, headNode);
		FileNode xmlNode2 =
				new FileNode("/document/file/", "eyeGENE19_20201111_BATCH.xml", FileType.ASSOCIATED, headNode);
		xmlNode2.setAssociation(csvNode);
		csvNode.setValidated(true);
		xmlNode1.setValidated(true);
		xmlNode2.setValidated(true);
		csvNode.setFileSize(0L);
		xmlNode1.setFileSize(0L);
		xmlNode2.setFileSize(0L);
		csvNode.setStructureName("test");
		xmlNode1.setStructureName("test");
		xmlNode2.setStructureName("test");
		headNode.insert(csvNode);
		headNode.insert(xmlNode1);
		headNode.insert(xmlNode2);

		List<SubmissionDataFile> listDatasets = new ArrayList<SubmissionDataFile>();
		ListMultimap<String, SubmissionDataFile> associatedFileMap = ArrayListMultimap.create();
		List<StructuralFormStructure> sfsList = new ArrayList<StructuralFormStructure>();
		HashMap<FileNode, JTextField> dummyMap = new HashMap<FileNode, JTextField>();
		dummyMap.put(headNode, new JTextField());
		dummyMap.put(csvNode, new JTextField());
		dummyMap.put(xmlNode1, new JTextField());
		dummyMap.put(xmlNode2, new JTextField());
		StructuralFormStructure sfs = new StructuralFormStructure();
		sfs.setShortName("test");
		sfs.setFileType(SubmissionType.CLINICAL);
		sfsList.add(sfs);
		DataSubmission submission = new DataSubmission(headNode, null, null, sfsList);
		SubmissionPackageBuilder builder =
				new SubmissionPackageBuilder(submission, "/document/file/", new Date(), "BATCH");

		builder.buildFileLists(headNode, listDatasets, associatedFileMap, dummyMap, false, null);;
		Assert.assertTrue(listDatasets.size() == 2);



	}

	@Test
	public void associatedFileListTest() {
		FileNode headNode = new FileNode("/document/file/", "directory", FileType.DIR, null);
		FileNode csvNode = new FileNode("/document/file/", "myCSV.csv", FileType.CSV, headNode);
		FileNode xmlNode1 = new FileNode("/document/file/", "HIT6_20201111_BATCH.xml", FileType.XML, headNode);
		FileNode xmlNode2 =
				new FileNode("/document/file/", "eyeGENE19_20201111_BATCH.xml", FileType.ASSOCIATED, headNode);
		xmlNode2.setAssociation(csvNode);
		csvNode.setValidated(true);
		xmlNode1.setValidated(true);
		xmlNode2.setValidated(true);
		csvNode.setFileSize(0L);
		xmlNode1.setFileSize(0L);
		xmlNode2.setFileSize(0L);
		csvNode.setStructureName("test");
		xmlNode1.setStructureName("test");
		xmlNode2.setStructureName("test");
		headNode.insert(csvNode);
		headNode.insert(xmlNode1);
		headNode.insert(xmlNode2);

		List<SubmissionDataFile> listDatasets = new ArrayList<SubmissionDataFile>();
		ListMultimap<String, SubmissionDataFile> associatedFileMap = ArrayListMultimap.create();
		List<StructuralFormStructure> sfsList = new ArrayList<StructuralFormStructure>();
		HashMap<FileNode, JTextField> dummyMap = new HashMap<FileNode, JTextField>();
		dummyMap.put(headNode, new JTextField());
		dummyMap.put(csvNode, new JTextField());
		dummyMap.put(xmlNode1, new JTextField());
		dummyMap.put(xmlNode2, new JTextField());
		StructuralFormStructure sfs = new StructuralFormStructure();
		sfs.setShortName("test");
		sfs.setFileType(SubmissionType.CLINICAL);
		sfsList.add(sfs);
		DataSubmission submission = new DataSubmission(headNode, null, null, sfsList);
		SubmissionPackageBuilder builder =
				new SubmissionPackageBuilder(submission, "/document/file/", new Date(), "BATCH");

		builder.buildFileLists(headNode, listDatasets, associatedFileMap, dummyMap, false, null);
		Assert.assertTrue(associatedFileMap.get(listDatasets.get(0).getPath()).size() == 1);



	}
}
