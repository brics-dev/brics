package gov.nih.tbi.ordermanager.action;

import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.ordermanagement.OrderManager;
import gov.nih.tbi.ordermanager.dao.BiospecimenOrderDao;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;

public class BiosampleUnblindAction extends OrderManagerBaseAction implements
		SessionAware {

	
	
	private String orderId;
	
	private String fileName;
	
	private int fileSize;
	
	
	
	
	@Autowired
    BiospecimenOrderDao biospecimenOrderDao;
	
	
	
	
	
	public String generateUnblindReport() throws IOException, UserPermissionException{
		if(!getIsOrderManagerAdmin()){
			throw new UserPermissionException("The user does not have admin access to the biospecimen order report.");
		}
		 
		
		long orderIdLong = Long.valueOf(orderId);
		BufferedWriter bufWriter = null;
		OutputStream out = null;
		File csvFile = null;
		FileInputStream fin = null;
		try {
		
			

			BiospecimenOrder order = biospecimenOrderDao.get(orderIdLong);
			Collection<BiospecimenItem> biospecimenItems = order.getRequestedItems();
			
			
			//generate csv file object...create temp file object
			String title = order.getOrderTitle();
			fileName = title + "_" + orderId + ".csv";
			fileName = fileName.replaceAll("\\s+","");
			csvFile = File.createTempFile("temp", ".csv");
			csvFile.deleteOnExit();
			bufWriter = new BufferedWriter(new FileWriter(csvFile));
			
			String headers = "guid,stNumber\n";
			bufWriter.write(headers);
			
			for (BiospecimenItem item : biospecimenItems) {
		         String guid = item.getGuid();
				 String stNumber = item.getStNumber();
				 if(stNumber == null) {
					 stNumber = "";
				 }
				 bufWriter.write(guid + "," + stNumber + "\n");

		    }
			
			if(bufWriter != null) {
				bufWriter.close();
			}

			//write out file to response
			HttpServletResponse res = getResponse();
			res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			res.setHeader("Content-Length", Long.valueOf(csvFile.length()).toString());
			res.setHeader("Content-Type", "application/csv");
			//res.setContentType("application/vnd.ms-excel");
			
			out = res.getOutputStream();
			fin = new FileInputStream(csvFile);
			byte[] buf = new byte[1024];
			int size = 0;
			size = fin.read(buf);
			while(size > 0) {
				out.write(buf, 0, size);
				out.flush();
				size = fin.read(buf);
			}
		}finally {
			
			if(bufWriter != null) {
				bufWriter.close();
			}
			if(out != null) {
				out.close();
			}
			if(fin != null) {
				fin.close();
			}
			if(csvFile != null) {
				csvFile.delete();
			}

		}
		

		return null;
		
	}
	
	
	
	public String getOrderId() {
		return orderId;
	}






	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}






	@Override
	public void setSession(Map<String, Object> arg0) {
		// TODO Auto-generated method stub

	}

}
