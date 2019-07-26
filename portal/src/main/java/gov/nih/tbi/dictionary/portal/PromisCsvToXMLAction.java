package gov.nih.tbi.dictionary.portal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import au.com.bytecode.opencsv.CSVReader;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.DataStructureExport;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataElementExport;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.MapElementExport;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.RepeatableGroup;
import gov.nih.tbi.repository.model.SubmissionType;

public class PromisCsvToXMLAction extends BaseDictionaryAction {
	
	private static final long serialVersionUID = 2414392716488358241L;	

	private static final Logger logger = Logger.getLogger(PromisCsvToXMLAction.class);
	
	private List<List<String>> importStructureErrors = new ArrayList<List<String>>();
	private List<List<String>> importStructureSuccess = new ArrayList<List<String>>();
	FormStructureExport exportedFs = new FormStructureExport();
	private String uploadContentType;
	private File upload;
	private String contentType;
	private InputStream inputStream;
	private String fileName;

	public String importCSV() {
		return PortalConstants.ACTION_INPUT;
	}
	
	public String exportXML() throws MalformedURLException, UnsupportedEncodingException {
		if (ServiceConstants.CSV_FILE.equals(uploadContentType) || "text/csv".equals(uploadContentType)
				|| ServiceConstants.APPLICATION_CSV_FILE.equals(uploadContentType))  {
			inputStream =  createFsXML();
			fileName = "PROMIS-formStructure.xml";
			HttpServletResponse response = ServletActionContext.getResponse();
			response.setContentType(MediaType.APPLICATION_XML);
		}else {
			// error thrown if the file being uploaded is not an CSV file
			List<String> incorrectFileType = new ArrayList<String>();
			incorrectFileType.add("Invalid file type. If the file is open in another application please close it and try again.");
			setImportStructureErrors(incorrectFileType);
			return PortalConstants.ACTION_INPUT;
		}
		return PortalConstants.ACTION_DOWNLOAD;
	}

	public InputStream createFsXML() {
		InputStream resultStream;
		DataStructureExport export = new DataStructureExport();
		setExportedFormStructure();		
		export.setDataStructure(exportedFs);		
		try {
			JAXBContext jaxContext = JAXBContext.newInstance(DataStructureExport.class, FormStructure.class, RepeatableGroup.class,
													DiseaseStructure.class, DataElement.class, MapElement.class);
			Marshaller marshaller = jaxContext.createMarshaller();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(export, baos);
			resultStream = new ByteArrayInputStream(baos.toByteArray());
		} catch (Exception ex) {
			throw new RuntimeException("Error marshalling to XML", ex);
		}		
		return resultStream;
	}
	
	public void setExportedFormStructure() {
		CSVReader reader;
		String[] infoHead = {"Title","Short Name","Description","Organization",
						"Standardization","Form Type","is Required Program Form","is CopyRight", "is CAT", "CAT OID", "Measurement Type"};
		String[] associatedDiseaseHead = {"Assosiated Diseases"};
		String[] groupHead = {"GROUP-","Group Name", "Repeatable Type", "Threshold",	"Position Number"};
		String[] elementHead = {"ELEMENTS-","Element Name","Required","Position Number"};
		ArrayList<Integer> anchors = new ArrayList<Integer>();
		ArrayList<String[]> allLines = new ArrayList<String[]>();
		try {
			reader = new CSVReader(new FileReader(upload));			
			String [] nextLine;
			int inx = 0;
			while((nextLine = reader.readNext()) != null) {
				nextLine = removeTailEmpty(nextLine);
				allLines.add(nextLine);
				if(Arrays.equals(nextLine, infoHead)) {
					anchors.add(inx);
				}else if(Arrays.equals(nextLine, associatedDiseaseHead)) {
					anchors.add(inx);
				}else if(Arrays.equals(nextLine, groupHead)) {
					anchors.add(inx);
				}else if(Arrays.equals(nextLine, elementHead)) {
					anchors.add(inx);
				}else {
					inx++;
					continue;					
				}				
				inx++;
			}
			// set structure info
			List<String[]> subLines;
			int endPoint = allLines.size();
			Set<RepeatableGroup> rgSet = new HashSet<RepeatableGroup>();
			RepeatableGroup currentRg = null;
			for(int i=0; i<anchors.size();i++) {
				if(i+1!=anchors.size()) {
					subLines = allLines.subList(anchors.get(i), anchors.get(i+1));
					if(Arrays.equals(subLines.get(0), infoHead)) {
						setExportedFsInfo(subLines.get(1), exportedFs);
					}else if(Arrays.equals(subLines.get(0), associatedDiseaseHead)){
						setExportedFsDiseases(subLines.subList(1, subLines.size()), exportedFs);
					}else if(Arrays.equals(subLines.get(0), groupHead)) {
						currentRg = initiateRepeatableGroup(subLines.get(1));					
					}else if(Arrays.equals(subLines.get(0), elementHead)) {
						currentRg = setMapElement(subLines.subList(1, subLines.size()), currentRg);											
					}else {
						continue;
					}
				}else {
					subLines = allLines.subList(anchors.get(i), endPoint);
					if(Arrays.equals(subLines.get(0), elementHead)) {
						currentRg = setMapElement(subLines.subList(1, subLines.size()), currentRg);											
					}
				}
				if(currentRg != null) {
					rgSet.add(currentRg);
				}
			}
			exportedFs.setRepeatableGroups(rgSet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setExportedFsInfo(String [] line, FormStructureExport exportedFs) {
		exportedFs.setTitle(line[0]);
		exportedFs.setShortName(line[1]);
		exportedFs.setDescription(line[2]);
		exportedFs.setOrganization(line[3]);
		exportedFs.setStandardization(line[4]);
		String a = line[5];
		exportedFs.setFileType(SubmissionType.getObject(line[5]));
		exportedFs.setRequired(false);
		exportedFs.setIsCopyrighted(true);
		exportedFs.setCAT(true);
		exportedFs.setCatOid(line[9]);
		exportedFs.setMeasurementType(line[10]);
	}
	
	public void setExportedFsDiseases(List<String[]> associatedDiseaseNames, FormStructureExport exportedFs) {
		Set<DiseaseStructure> dsset = new HashSet<DiseaseStructure>();
		for(String[] adName:associatedDiseaseNames) {
			DiseaseStructure ds = new DiseaseStructure();
			Disease d = new Disease();
			d.setIsActive(true);
			d.setIsMajor(true);
			d.setName(adName[0]);
			ds.setDisease(d);
			dsset.add(ds);
		}		
		exportedFs.setDiseaseList(dsset);
	}
	
	public RepeatableGroup initiateRepeatableGroup(String[] group){
		RepeatableGroup rg = new RepeatableGroup();		
		rg.setName(group[1]);
		rg.setType(RepeatableType.valueOf(group[2]));
		rg.setThreshold(Integer.valueOf(group[3]));
		rg.setPosition(Integer.valueOf(group[4]));
		return rg;
	}
	
	public RepeatableGroup setMapElement(List<String[]> elements, RepeatableGroup rg) {
		Set<MapElementExport> mes = new LinkedHashSet<MapElementExport>();
		for(int i=0; i<elements.size(); i++) {
			String[] element = elements.get(i);
			MapElementExport me = new MapElementExport();
			DataElementExport sde = new DataElementExport();
			sde.setName(element[1]);
			me.setRepeatableGroup(rg.getName());
			me.setDataElement(sde);
			me.setPosition(i+1);
			me.setRequiredType(RequiredType.RECOMMENDED);			
			mes.add(me);
		}
		rg.setMapElements(mes);
		return rg;
	}
	
	public String[] removeTailEmpty(String[] arr) {
		LinkedList<String> temp = new LinkedList<String>();
		for(int i=0; i<arr.length;i++) { 
			if(i==0) { // we just skip the first element
				temp.add(arr[i]);
			}else {
				if(arr[i] != null)
					if(arr[i].length()>0)
						temp.add(arr[i]);
			}
		}			
	    return temp.toArray(new String[temp.size()]);
	}
	
	public List<List<String>> getImportStructureErrors() {
		return importStructureErrors;
	}

	public void setImportStructureErrors(List<String> list) {
		this.importStructureErrors.add(list);
	}

	public List<List<String>> getImportStructureSuccess() {
		return importStructureSuccess;
	}

	public void setImportStructureSuccess(List<String> list) {
		this.importStructureSuccess.add(list);
	}
	
	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}
	
	public String getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
