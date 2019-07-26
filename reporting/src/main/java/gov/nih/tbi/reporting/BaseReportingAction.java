package gov.nih.tbi.reporting;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.commons.reporting.BaseAction;
import gov.nih.tbi.commons.service.JasperExportService;
import gov.nih.tbi.commons.service.ReportingManager;
import gov.nih.tbi.constants.ReportingModulesConstants;
import gov.nih.tbi.repository.model.hibernate.ReportType;
import gov.nih.tbi.util.ReportingUtils;

public class BaseReportingAction extends BaseAction implements ServletResponseAware {

	private static final long serialVersionUID = -3847123251438277367L;
	private static final Logger logger = Logger.getLogger(BaseReportingAction.class);

	@Autowired
	protected ReportingManager reportingManager;
	
	@Autowired
	protected JasperExportService jasperExportService;

	@Autowired
	protected ReportingModulesConstants modulesConstants;
	
	@Autowired
	ReportingUtils reportUtil;
	
	protected HttpServletResponse response;
	
	public HttpServletResponse getServletResponse() {
		return response;
	}

	@Override
	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
		
	}
	
	public List<ReportType> getReportTypes() {
		return reportingManager.listAllReports();
	}
}
