package gov.nih.tbi.ordermanagement.util;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class OrderManagerCSVUtil{
	
	// Order ID, Order Title, Submitted on Date, Submitter Name, Sample ID, repository, Sample Type, GUID, Visit type, Inventory, Inventory Date, Quantity, Unit Number, Unit of Measure
	public static ByteArrayOutputStream biospecimenOrderToCsv(List<BiospecimenOrder> biospecimenOrdersList) throws IOException{

		//list of biospecimen orders should not be null
		if (biospecimenOrdersList == null) {
			throw new IllegalArgumentException("There are no biospecimen orders in the list.");
		}
		
		// create new writer
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos));

		// write headers into the csv
		writer.writeNext(toPrimitive(ServiceConstants.EXPORT_BIOSPECIMEN_CSV_HEADERS));

		//loop through all the items in each order and print them to the csv
		List<String> currentRow = new ArrayList<String>();
		for (BiospecimenOrder biospecimenOrder : biospecimenOrdersList) {			
				for(BiospecimenItem biospecimenItem : biospecimenOrder.getRequestedItems()){
					for(String column : ServiceConstants.EXPORT_BIOSPECIMEN_CSV_HEADERS){
						
						switch(column){
						case ServiceConstants.ORDER_ID:
							currentRow.add(biospecimenOrder.getId() != null ? Long.toString(biospecimenOrder.getId()) : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.ORDER_TITLE:
							currentRow.add(biospecimenOrder.getOrderTitle() != null ? biospecimenOrder.getOrderTitle() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.SUBMITTED_DATE:
							currentRow.add(biospecimenOrder.getDateSubmitted() != null ? BRICSTimeDateUtil.formatDate(biospecimenOrder.getDateSubmitted()) : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.SUBMITTER_NAME:
							currentRow.add(biospecimenOrder.getUser().getFullName() != null ? biospecimenOrder.getUser().getFullName() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.SAMPLE_ID:
							currentRow.add(biospecimenItem.getBioreposTubeID() != null ? biospecimenItem.getBioreposTubeID() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.REPOSITORY:
							currentRow.add(biospecimenItem.getBioRepository().getName() != null ? biospecimenItem.getBioRepository().getName() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.SAMPLE_TYPE:
							currentRow.add(biospecimenItem.getSampCollType() != null ? biospecimenItem.getSampCollType() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.GUID:
							currentRow.add(biospecimenItem.getGuid() != null ? biospecimenItem.getGuid() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.VISIT_TYPE:
							currentRow.add(biospecimenItem.getVisitTypePDBP() != null ? biospecimenItem.getVisitTypePDBP() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.INVENTORY:
							currentRow.add(biospecimenItem.getInventory() != null ? biospecimenItem.getInventory() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.INVENTORY_DATE:
							currentRow.add(biospecimenItem.getInventoryDate() != null ? BRICSTimeDateUtil.formatDate(biospecimenItem.getInventoryDate()) : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.QUANTITY:
							currentRow.add(biospecimenItem.getNumberOfAliquots() != null ? Integer.toString(biospecimenItem.getNumberOfAliquots()) : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.UNIT_NUMBER:
							currentRow.add(biospecimenItem.getUnitNumber() != null ? biospecimenItem.getUnitNumber() : ServiceConstants.EMPTY_STRING);
							break;
						case ServiceConstants.UNIT_OF_MEASURE:
							currentRow.add(biospecimenItem.getUnitMeasurement() != null ? biospecimenItem.getUnitMeasurement() : ServiceConstants.EMPTY_STRING);
							break;
						}
					}
					
					//print row to the csv
					String[] out = new String[currentRow.size()];
					out = currentRow.toArray(out);

					writer.writeNext(out);
					currentRow.clear();
				}

		}

		writer.close();
		return baos;
	}
	
	private static String[] toPrimitive(List<String> list) {

		if (list == null) {
			return new String[0];
		}

		String[] primitive = new String[list.size()];

		Iterator<String> iterator = list.iterator();

		for (int i = 0; i < primitive.length; i++) {
			primitive[i] = iterator.next();
		}

		return primitive;
	}
}