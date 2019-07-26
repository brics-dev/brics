package gov.nih.tbi.commons.service;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRDataSource;

public interface JasperExportService {

	public void exportHTML(HttpServletResponse response, String reportTitle, String reportTemplateURL,
			String reportFilename, DataSource dataSource, JRDataSource jrDataSource);

	public void exportCSV(HttpServletResponse response, String reportTitle, String reportTemplateURL,
			String reportFilename, DataSource dataSource, JRDataSource jrDataSource);

	public void exportXLS(HttpServletResponse response, String reportTitle, String reportTemplateURL,
			String reportFilename, DataSource dataSource, JRDataSource jrDataSource);

	public void exportPDF(HttpServletResponse response, String reportTitle, String reportTemplateURL,
			String reportFilename, DataSource dataSource, JRDataSource jrDataSource);

}
