package gov.nih.tbi.commons.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

@Service
public class JasperExportServiceImpl implements JasperExportService {
	static Logger logger = Logger.getLogger(JasperExportServiceImpl.class);
	@Override
	public void exportHTML(HttpServletResponse response, String reportTitle, String reportTemplateURL,
			String reportFilename, DataSource dataSource, JRDataSource jrDataSource) {
		logger.info("In exportHTML()");
		ByteArrayOutputStream baos = null;
		try {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("Title", reportTitle);
			params.put("IS_IGNORE_PAGINATION", true);

			InputStream reportStream = this.getClass().getResourceAsStream(reportTemplateURL);

			JasperDesign jd = JRXmlLoader.load(reportStream);

			JasperReport jr = JasperCompileManager.compileReport(jd);

			JasperPrint jp = null;
			if (dataSource != null) {
				jp = JasperFillManager.fillReport(jr, params, dataSource.getConnection());
			} else if (jrDataSource != null) {
				jp = JasperFillManager.fillReport(jr, params, jrDataSource);
			} else {
				throw (new Exception("Either a connection or data source is required."));
			}

			baos = new ByteArrayOutputStream();

			HtmlExporter exporter = new HtmlExporter();
			exporter.setExporterInput(new SimpleExporterInput(jp));
			exporter.setExporterOutput(new SimpleHtmlExporterOutput(baos));
			SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration();
			reportConfig.setWhitePageBackground(true);
			reportConfig.setRemoveEmptySpaceBetweenRows(true);
			SimpleHtmlExporterConfiguration exportConfig = new SimpleHtmlExporterConfiguration();
			exportConfig.setBetweenPagesHtml("");
			exporter.setConfiguration(reportConfig);
			exporter.setConfiguration(exportConfig);
			exporter.exportReport();
			response.setContentType("text/html");
			response.setContentLength(baos.size());
			response.setHeader("Content-Disposition",
					"inline;filename=\"" + getFilenameWithDate(reportFilename) + ".html" + "\"");
			baos.writeTo(response.getOutputStream());
		} catch (Exception e) {
			logger.error("An error occurred while downloading the report.");
			e.printStackTrace();
		} finally {
			if (baos != null) {
				try {
					baos.flush();
					baos.close();
				} catch (Exception e1) {
					logger.error("An error occurred while closing resources.");
					logger.error(e1.getStackTrace());
				}
			}
		}
	}

	@Override
	public void exportCSV(HttpServletResponse response, String reportTitle, String reportTemplateURL,
			String reportFilename, DataSource dataSource, JRDataSource jrDataSource) {
		logger.info("In exportCSV()");
		ByteArrayOutputStream baos = null;
		try {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("Title", reportTitle);
			params.put("IS_IGNORE_PAGINATION", true);

			InputStream reportStream = this.getClass().getResourceAsStream(reportTemplateURL);

			JasperDesign jd = JRXmlLoader.load(reportStream);

			JasperReport jr = JasperCompileManager.compileReport(jd);

			JasperPrint jp = null;
			if (dataSource != null) {
				jp = JasperFillManager.fillReport(jr, params, dataSource.getConnection());
			} else if (jrDataSource != null) {
				jp = JasperFillManager.fillReport(jr, params, jrDataSource);
			} else {
				logger.warn("A data source is required to export a report.");
			}

			baos = new ByteArrayOutputStream();

			JRCsvExporter exporter = new JRCsvExporter();
			exporter.setExporterInput(new SimpleExporterInput(jp));
			exporter.setExporterOutput(new SimpleWriterExporterOutput(baos));
			exporter.exportReport();
			response.setContentType("text/csv");
			response.setContentLength(baos.size());
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + getFilenameWithDate(reportFilename) + ".csv" + "\"");
			baos.writeTo(response.getOutputStream());
		} catch (Exception e) {
			logger.error("An error occurred while exporting the report.");
			e.printStackTrace();
		} finally {
			if (baos != null) {
				try {
					baos.flush();
					baos.close();
				} catch (Exception e1) {
					logger.error("An error occurred while closing resources.");
					logger.error(e1.getStackTrace());
				}
			}
		}
	}

	@Override
	public void exportXLS(HttpServletResponse response, String reportTitle, String reportTemplateURL,
			String reportFilename, DataSource dataSource, JRDataSource jrDataSource) {
		logger.info("In exportXLS()");
		ByteArrayOutputStream baos = null;
		try {

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("Title", reportTitle);
			params.put("IS_IGNORE_PAGINATION", true);

			InputStream reportStream = this.getClass().getResourceAsStream(reportTemplateURL);

			JasperDesign jd = JRXmlLoader.load(reportStream);

			JasperReport jr = JasperCompileManager.compileReport(jd);

			JasperPrint jp = null;
			if (dataSource != null) {
				jp = JasperFillManager.fillReport(jr, params, dataSource.getConnection());
			} else if (jrDataSource != null) {
				jp = JasperFillManager.fillReport(jr, params, jrDataSource);
			} else {
				logger.warn("A data source is required to export a report.");
			}

			baos = new ByteArrayOutputStream();

			JRXlsExporter exporter = new JRXlsExporter();
			exporter.setExporterInput(new SimpleExporterInput(jp));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
			SimpleXlsReportConfiguration reportConfig = new SimpleXlsReportConfiguration();
			reportConfig.setRemoveEmptySpaceBetweenColumns(true);
			reportConfig.setRemoveEmptySpaceBetweenRows(true);
			reportConfig.setDetectCellType(true);
			exporter.setConfiguration(reportConfig);
			exporter.exportReport();
			response.setContentType("application/vnd.ms-excel");
			response.setContentLength(baos.size());
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + getFilenameWithDate(reportFilename) + ".xls" + "\"");
			baos.writeTo(response.getOutputStream());
		} catch (Exception e) {
			logger.error("An error occurred while exporting the report.");
			e.printStackTrace();
		} finally {
			if (baos != null) {
				try {
					baos.flush();
					baos.close();
				} catch (Exception e1) {
					logger.error("An error occurred while closing resources.");
					logger.error(e1.getStackTrace());
				}
			}
		}

	}

	@Override
	public void exportPDF(HttpServletResponse response, String reportTitle, String reportTemplateURL,
			String reportFilename, DataSource dataSource, JRDataSource jrDataSource) {
		logger.info("In exportPDF()");
		ByteArrayOutputStream baos = null;
		try {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("Title", reportTitle);
			params.put("IS_IGNORE_PAGINATION", false);

			InputStream reportStream = this.getClass().getResourceAsStream(reportTemplateURL);

			JasperDesign jd = JRXmlLoader.load(reportStream);

			JasperReport jr = JasperCompileManager.compileReport(jd);

			JasperPrint jp = null;
			if (dataSource != null) {
				jp = JasperFillManager.fillReport(jr, params, dataSource.getConnection());
			} else if (jrDataSource != null) {
				jp = JasperFillManager.fillReport(jr, params, jrDataSource);
			} else {
				logger.warn("A data source is required to export a report.");
			}

			baos = new ByteArrayOutputStream();

			JRPdfExporter exporter = new JRPdfExporter();
			exporter.setExporterInput(new SimpleExporterInput(jp));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
			SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
			reportConfig.setSizePageToContent(true);
			reportConfig.setForceLineBreakPolicy(false);
			SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
			exportConfig.setMetadataAuthor("NTI");
			exportConfig.setEncrypted(true);
			exportConfig.setOwnerPassword("NTI");
			exportConfig.setAllowedPermissionsHint("PRINTING");
			exporter.setConfiguration(reportConfig);
			exporter.setConfiguration(exportConfig);
			exporter.exportReport();
			response.setContentType("application/pdf");
			response.setContentLength(baos.size());
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + getFilenameWithDate(reportFilename) + ".pdf" + "\"");
			baos.writeTo(response.getOutputStream());
		} catch (Exception e) {
			logger.error("An error occurred while exporting the report.");
			e.printStackTrace();
		} finally {
			if (baos != null) {
				try {
					baos.flush();
					baos.close();
				} catch (Exception e1) {
					logger.error("An error occurred while closing resources.");
					logger.error(e1.getStackTrace());
				}
			}
		}
	}

	private String getFilenameWithDate(String reportFilename) {
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy_hhmmss");
		Date currentDate = new Date();
		return reportFilename + "_" + dateFormat.format(currentDate);
	}

}
