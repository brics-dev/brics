package gov.nih.tbi.ordermanager.action;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.ordermanagement.OrderManager;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderExportAction extends OrderManagerBaseAction implements SessionAware {

	private static Logger logger = Logger.getLogger(OrderExportAction.class);
	private static final long serialVersionUID = -6038836094087148914L;

	public String submitDateFrom;
	public String submitDateTo;
	public InputStream inputStream;
	public byte[] emptyReturn = new byte[0];
	private String fileName;

	@Autowired
	private OrderManager orderManager;

	@Override
	public void setSession(Map<String, Object> arg0) {
		// TODO Auto-generated method stub

	}

	public StreamResult validateShippedForm() throws IOException, UserPermissionException {
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}
	
	public String download() throws IOException, UserPermissionException {
		
		if(!getIsOrderManagerAdmin()){
			throw new UserPermissionException("The user does not have admin access to the biospecimen order report.");
		}

		//right now we only order on shipped status
		List<OrderStatus> reportStatuses = new ArrayList<OrderStatus>();
		reportStatuses.add(OrderStatus.SHIPPED);

		Date fromDate = BRICSTimeDateUtil.stringToDate(submitDateFrom);
		Date toDate = BRICSTimeDateUtil.stringToDate(submitDateTo);
		
		Calendar c = Calendar.getInstance(); 
		c.setTime(toDate); 
		c.add(Calendar.DATE, 1);
		toDate = c.getTime();
		
		List<BiospecimenOrder> shippedList = orderManager.searchBioSpecimenOrder(fromDate, toDate, reportStatuses);
		
		//append date to the file name
		Date todayDate = new Date();
		String date = BRICSTimeDateUtil.formatDate(todayDate);
		
		this.fileName = "biospecimentShippedReport_" + date + ".csv";

		if(shippedList != null && !shippedList.isEmpty()){
			inputStream = new ByteArrayInputStream(orderManager.generateBiospecimenOrderCSV(shippedList).toByteArray());
		} else {
			inputStream = new ByteArrayInputStream(emptyReturn);
		}

		return "export";
	}
	
    public InputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }

	public String getSubmitDateFrom() {
		return submitDateFrom;
	}

	public void setSubmitDateFrom(String submitDateFrom) {
		this.submitDateFrom = submitDateFrom;
	}

	public String getSubmitDateTo() {
		return submitDateTo;
	}

	public void setSubmitDateTo(String submitDateTo) {
		this.submitDateTo = submitDateTo;
	}
	
	public String getFileName(){
		return this.fileName;
	}
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
}
