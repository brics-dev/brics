package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.dictionary.model.InstanceRequiredFor;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataElementExport;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.MapElementExport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class DataStructureXmlExportAction extends BaseDictionaryAction {

	private static final long serialVersionUID = 1L;

	private InputStream inputStream;

	private String fileName;

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

	/**
	 * Creates a JAXB marshaller and gets the DataStructure from session to marshall it to XML.
	 * 
	 * @return
	 */
	public String export() {

		gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataStructureExport export =
				new gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataStructureExport();
		gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport exportStructure =
				changeDataElement(getSessionDataStructure().getDataStructure());

		export.setDataStructure(exportStructure);

		try {
			JAXBContext jaxContext =
					JAXBContext.newInstance(
							gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataStructureExport.class,
							gov.nih.tbi.dictionary.model.hibernate.FormStructure.class,
							gov.nih.tbi.dictionary.model.hibernate.formstructure.export.RepeatableGroup.class,
							DiseaseStructure.class, DataElement.class, MapElement.class);

			Marshaller marshaller = jaxContext.createMarshaller();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(export, baos);

			inputStream = new ByteArrayInputStream(baos.toByteArray());
		} catch (Exception ex) {
			throw new RuntimeException("Error marshalling to XML", ex);
		}

		// Set filename for use later
		fileName = export.getDataStructure().getShortName().toString() + ".xml";

		return PortalConstants.ACTION_EXPORT;
	}

	/**
	 * This method converts the new form structure to the old version for export compatibility It also removes all data
	 * from the data element except the data element name
	 * 
	 * @param workingStructure
	 * @return
	 */
	private gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport changeDataElement(
			gov.nih.tbi.dictionary.model.hibernate.FormStructure workingStructure) {

		gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport returnStructure =
				new gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport();

		// copy shell contents
		returnStructure.setDescription(workingStructure.getDescription());
		returnStructure.setDiseaseList(workingStructure.getDiseaseList());
		returnStructure.setFileType(workingStructure.getFileType());
		returnStructure.setId(workingStructure.getId());
		returnStructure.setIsCopyrighted(workingStructure.getIsCopyrighted());
		returnStructure.setStandardization(workingStructure.getStandardization().getDisplay());
		returnStructure.setRequired(workingStructure.getInstancesRequiredFor().contains(
				new InstanceRequiredFor(getOrgName())));

		// returnStructure.setModifiedDate(workingStructure.getModifiedDate());
		// returnStructure.setModifiedUserId(workingStructure.getModifiedUserId());
		returnStructure.setOrganization(workingStructure.getOrganization());
		returnStructure.setPublicationDate(workingStructure.getPublicationDate());
		// repeatable groups translated below
		returnStructure.setShortName(workingStructure.getShortName());
		returnStructure.setStatus(workingStructure.getStatus());
		returnStructure.setTitle(workingStructure.getTitle());
		// returnStructure.setValidatable(workingStructure.getValidatable());
		returnStructure.setVersion(workingStructure.getVersion());
		// added by Ching-Heng
		returnStructure.setCAT(workingStructure.isCAT());
		returnStructure.setCatOid(workingStructure.getCatOid());
		returnStructure.setMeasurementType(workingStructure.getMeasurementType());

		for (gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup workingRG : workingStructure.getRepeatableGroups()) {

			gov.nih.tbi.dictionary.model.hibernate.formstructure.export.RepeatableGroup newRepeatableGroup =
					new gov.nih.tbi.dictionary.model.hibernate.formstructure.export.RepeatableGroup();

			for (gov.nih.tbi.dictionary.model.hibernate.MapElement workingME : workingRG.getMapElements()) {

				MapElementExport newMapElement = new MapElementExport();
				newMapElement.setId(workingME.getId());
				newMapElement.setRequiredType(workingME.getRequiredType());
				newMapElement.setPosition(workingME.getPosition());

				newMapElement.setRepeatableGroup(workingME.getRepeatableGroup().getName());

				// get the old DE Name and assign it to the new DE
				DataElementExport newDE = new DataElementExport();
				newDE.setName(workingME.getStructuralDataElement().getName());
				// replace old DE with new DE in the ME
				newMapElement.setDataElement(newDE);

				// add in the ME to the new version of RG
				newRepeatableGroup.getMapElements().add(newMapElement);

			}

			// add in the new version of RG
			newRepeatableGroup.setId(workingRG.getId());
			newRepeatableGroup.setName(workingRG.getName());
			newRepeatableGroup.setPosition(workingRG.getPosition());
			newRepeatableGroup.setThreshold(workingRG.getThreshold());
			newRepeatableGroup.setType(workingRG.getType());

			returnStructure.getRepeatableGroups().add(newRepeatableGroup);
		}

		return returnStructure;
	}

	// public static void main(String[] args) throws Exception {
	//
	//
	//
	// JAXBContext jaxContext = JAXBContext.newInstance(
	// DataStructureExport.class,
	// DataStructure.class,
	// RepeatableGroup.class,
	// DiseaseStructure.class,
	// AbstractDataStructure.class,
	// BasicDataElement.class,
	// DataElement.class,
	// MapElement.class);
	//
	// Unmarshaller um = jaxContext.createUnmarshaller();
	// DataStructure myDSFromFile = null;
	//
	//
	//
	//
	// DataStructureExport myDSEFromFile =
	// (gov.nih.tbi.dictionary.model.hibernate.DataStructureExport)
	// um.unmarshal(new File("C:/BRICS/0_FixImport/work/MedicalHistory.xml"));
	//
	//
	// gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataStructureExport export =
	// new gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataStructureExport();
	// gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataStructure exportStructure =
	// changeDataElement((DataStructure) myDSFromFile);
	//
	// export.setDataStructure(exportStructure);
	//
	// try
	// {
	// JAXBContext jaxContext2 = JAXBContext.newInstance(
	// gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataStructureExport.class,
	// gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataStructure.class,
	// gov.nih.tbi.dictionary.model.hibernate.formstructure.export.RepeatableGroup.class,
	// DiseaseStructure.class,
	// AbstractDataStructure.class,
	// BasicDataElement.class,
	// DataElement.class,
	// MapElement.class);
	//
	// Marshaller marshaller2 = jaxContext2.createMarshaller();
	//
	// marshaller2.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	// marshaller2.marshal(export, new File("C:/BRICS/0_FixImport/work/MedicalHistory2.xml"));
	//
	// }
	// catch (Exception ex)
	// {
	// throw new RuntimeException("Error marshalling to XML", ex);
	// }
	// }
}
