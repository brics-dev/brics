package gov.nih.tbi.dictionary.validation.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;

public class FileParserTest {
	
	@Test
	public void testExclusions() {
		FileNode xmlNode1 = new FileNode("/document/file/", "HIT6_20201111_BATCH.xml", FileType.XML, null);
		FileNode xmlNode2 = new FileNode("/document/file/", "eyeGENE19_20201111_BATCH.xml", FileType.XML, null);
		FileNode xmlNode3 = new FileNode("/document/file/", "myFile.xml", FileType.XML, null);
		FileNode ticketNode = new FileNode("/document/file/", "submissionTicket-2929283001133.xml", FileType.XML, null);
		FileNode csvNode = new FileNode("/document/file/", "myCSV.csv", FileType.CSV, null);
		FileNode outputNode = new FileNode("document/file", "output_log_2929283001133.txt", FileType.UNKNOWN, null);
		
		FileParser.excludeUnrecognizableFiles(xmlNode1.getName(),xmlNode1);
		FileParser.excludeUnrecognizableFiles(xmlNode2.getName(),xmlNode2);
		FileParser.excludeUnrecognizableFiles(xmlNode3.getName(),xmlNode3);
		FileParser.excludeUnrecognizableFiles(ticketNode.getName(),ticketNode);
		FileParser.excludeUnrecognizableFiles(csvNode.getName(),csvNode);
		FileParser.excludeUnrecognizableFiles(outputNode.getName(),outputNode);
		
		Assert.assertEquals(xmlNode1.isIncluded(), false);
		Assert.assertEquals(xmlNode2.isIncluded(), false);
		Assert.assertEquals(xmlNode3.isIncluded(), true);
		Assert.assertEquals(ticketNode.isIncluded(), false);
		Assert.assertEquals(csvNode.isIncluded(), true);
		Assert.assertEquals(outputNode.isIncluded(), false);
	}
}