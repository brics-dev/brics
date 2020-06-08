
package gov.nih.tbi.dictionary.validation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.commons.AppConfig;
import gov.nih.tbi.commons.WebstartRestProvider;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.validation.engine.ValidationEngine;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.DataTable;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;
import gov.nih.tbi.dictionary.validation.model.FileNodeIterator;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput.OutputType;
import gov.nih.tbi.dictionary.validation.parser.FileParser;
import gov.nih.tbi.dictionary.validation.util.BareBonesBrowserLaunch;
import gov.nih.tbi.dictionary.validation.util.DataFileBuilder;
import gov.nih.tbi.dictionary.validation.util.SubmissionPackageBuilder;
import gov.nih.tbi.dictionary.validation.view.JoinFiles;
import gov.nih.tbi.dictionary.validation.view.ValidationClient;
import gov.nih.tbi.dictionary.validation.view.ValidationUploadManager;
import gov.nih.tbi.repository.UploadManager;
import gov.nih.tbi.repository.UploadManagerController;
import gov.nih.tbi.repository.model.SubmissionDataFile;
import gov.nih.tbi.repository.model.SubmissionPackage;
// import gov.nih.tbi.repository.UploadManagerController;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.ws.AccessionProvider;
import gov.nih.tbi.repository.ws.AccessionWebService;


public class ValidationController implements ActionListener, TreeSelectionListener, ListSelectionListener, PropertyChangeListener {

	static Logger logger = Logger.getLogger(ValidationController.class);

	private static final int WARNING_CUTOFF_NUM = 1000;

	private static final Color LIGHT_RED = new Color(255,184,184);
	// private IUser user;
	// private DataManagerProvider dataClient;
	private WebstartRestProvider ddtClient;
	private static WebstartRestProvider serverClient;
	private AccessionWebService accClient;
	private String message;
	private String bricsUrl;
	private String ddtUrl;
	private boolean isComingFromProforms;
	private String submissionSuffix = "";
	private JComboBox<Study> studySelect;
	private JLabel errorInstructions;


	private ValidationClient view;
	private DataSubmission submission;

	private ValidationEngine engine;
	private FileParser parser;
	private HashMap<FileNode, JTextField> mapFieldToNode;
	private List<JTextField> allFields;
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

	private JDialog working;
	private JProgressBar progressBar;
	private int progressMax = 0; // Keeps track of the max progress bar (console version only)
	private int progress = 0; // Keeps track of the progress bar status (console version only)
	private boolean progressComplete = false; // Tracks when the loading has finished so competing threads dont mess up
												 // the console output

	/**
	 * Keeps track of what is currently selected in the TreeListSelectionModel
	 */
	private List<FileNode> nodeList = new ArrayList<FileNode>();

	// private static String HELP_URL;

	private AppConfig config = AppConfig.getInstance();


	// private static String HELP_URL = new String("http://" + getConnectionConfig().getProperty("TBI_SFTP_URL")
	// + "/portal/study/fileDownloadAction!download.action?fileId=22");


	private List<String> unknownFileWarnings = new ArrayList<String>();
	
	public class JTextFieldLimit extends PlainDocument {
		private int limit;

		JTextFieldLimit(int limit) {
			super();
			this.limit = limit;
		}

		JTextFieldLimit(int limit, boolean upper) {
			super();
			this.limit = limit;
		}

		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			if (str == null)
				return;

			if ((getLength() + str.length()) <= limit) {
				super.insertString(offset, str, attr);
			}
		}
	}

	public ValidationController(ValidationClient dataEntryClient, String bricsUrl, String ddtUrl, String userName,
			String password, boolean isComingFromProforms) throws Exception { // , String username, String password) {

		// Initiated Connections
		this.bricsUrl = bricsUrl;
		this.ddtUrl = ddtUrl;
		this.isComingFromProforms = isComingFromProforms;
		// this.url = "http://tbi-stage-apps.cit.nih.gov";

		String portalRoot = this.config.getProperty("PORTAL_ROOT");
		String serverLocation = this.config.getProperty("SERVER_LOC");
		try {
			ddtClient = new WebstartRestProvider(this.ddtUrl.trim(), userName, password);
			serverClient = new WebstartRestProvider(serverLocation, userName, password);
			accClient =
					(new AccessionProvider(this.bricsUrl.trim() + "/" + portalRoot + "/ws/accessionWebService?wsdl"))
							.getAccessionWebService();
			logger.debug("Accession connection URL: " + this.bricsUrl.trim() + "/" + portalRoot
					+ "/ws/accessionWebService?wsdl");

		} catch (Exception e) {
			throw e;
		}

		// dataEntryClient will be null if the controller is building build for the command line (no GUI)
		if (dataEntryClient != null) {
			view = dataEntryClient;
		}
		engine = new ValidationEngine(accClient);
		if(!isComingFromProforms) {
			convertStudyList();
		}
	}

	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(ValidationClient.BROWSE)) {
			JFileChooser chooser = new JFileChooser();
			if (!view.getSourceDir().equals("")) {
				chooser.setCurrentDirectory(new File(view.getSourceDir()));
			}
			FileFilter filter = new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory())
						return true;
					String pathStr = pathname.getName();
					if (pathStr.endsWith(".csv"))
						return true;
					return false;
				}

				@Override
				public String getDescription() {
					return "CSVs (.csv)";
				}

			};
			chooser.setFileFilter(filter);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			// chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select submission directory");

			int returnValue = chooser.showOpenDialog(view);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selected = chooser.getSelectedFile();
				String path;
				if (selected.isFile()) {
					path = selected.getParent();
				} else {
					path = selected.getAbsolutePath();
				}
				// view.setSourceDir(String.valueOf(chooser.getSelectedFile().getAbsolutePath()) + File.separatorChar);
				view.setSourceDir(path + File.separatorChar);
				view.enableLoadButton();
			}

		} else if (command.equals(ValidationClient.LOAD)) {
			String errorMessage = load(view.getSourceDir());
			if (errorMessage != null) {
				JOptionPane.showMessageDialog(view, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			}
			// some messages cannot be returned to the string and need to be checked here.
			else if (message != null) {
				JOptionPane.showMessageDialog(view, message, "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if (command.equals(ValidationClient.INCLUDE)) {

			include();

		} else if (command.equals(ValidationClient.EXCLUDE)) {

			exclude();

		} else if (command.equals(ValidationClient.RELOAD)) {

			reload();
			include();

		} else if (command.equals(ValidationClient.VALIDATE)) {

			validate();

		} else if (command.equals(ValidationClient.BUILD_SUBMISSION)) {

			JPanel fullPanel = new JPanel();
			JPanel descPanel = new JPanel();
			JPanel popUpPanel = new JPanel();
			JPanel studyPanel = new JPanel();
			JPanel datasetNamingPanel = new JPanel();
			fullPanel.setLayout(new BoxLayout(fullPanel, BoxLayout.Y_AXIS));
			mapFieldToNode = new HashMap<FileNode, JTextField>();
			allFields = new ArrayList<JTextField>();
			JLabel defaultInfo = new JLabel("Default Dataset Name:");
			JLabel editableInfo = new JLabel("Edit Dataset Name:");
			JLabel studySelectInfo = new JLabel("Select a study to submit to:");
			JLabel datasetNamingInfoLineOne = new JLabel("The dataset name will have the form structure short name appended to it automatically. The max length for the field, including the suffix, is 255 characters.");
			JLabel datasetNamingInfoLineTwo = new JLabel("Example: Dataset1_Demographics");
			studyPanel.add(studySelectInfo);
			editableInfo.setHorizontalAlignment(JLabel.LEFT);
			studySelectInfo.setHorizontalAlignment(JLabel.LEFT);
			datasetNamingPanel.add(datasetNamingInfoLineOne);
			datasetNamingPanel.add(datasetNamingInfoLineTwo);
			datasetNamingInfoLineOne.setHorizontalAlignment(JLabel.LEFT);
			datasetNamingInfoLineTwo.setHorizontalAlignment(JLabel.LEFT);
			datasetNamingPanel.setLayout(new GridLayout(2,1));
			descPanel.add(defaultInfo);
			descPanel.add(editableInfo);
			descPanel.setLayout(new GridLayout(1, 2));
			for (FileNode node : submission.getFileData().keySet()) {
				if(node.isIncluded()) {
					String nameWithoutExt = node.getName().substring(0, node.getName().lastIndexOf("."));
					JTextField anonTextField = new JTextField(20);
					anonTextField.setDocument(new JTextFieldLimit(256));
					JLabel fileLabel = new JLabel(nameWithoutExt);
					JLabel fileShortName = new JLabel("_" + node.getStructureName());
					fileLabel.setHorizontalAlignment(JLabel.LEFT);
					anonTextField.setText(nameWithoutExt);
					allFields.add(anonTextField);
					popUpPanel.add(fileLabel);
					popUpPanel.add(anonTextField);
					popUpPanel.add(fileShortName);
					anonTextField.setCaretPosition(0);
					fileLabel.setToolTipText(nameWithoutExt);
					mapFieldToNode.put(node,anonTextField);
				}
			}
			int panelSize = (allFields.size()) * 35;
			popUpPanel.setPreferredSize(new Dimension(780, panelSize));
			descPanel.setPreferredSize(new Dimension(780,30));
			JScrollPane scrollPane = new JScrollPane(popUpPanel);
			popUpPanel.setLayout(new GridLayout(allFields.size(), 3));
			errorInstructions = new JLabel("\nDataset names highlighted in RED are in error.");
			fullPanel.add(studyPanel);
			fullPanel.add(studySelect);
			fullPanel.add(datasetNamingPanel);
			fullPanel.add(descPanel);
			fullPanel.add(scrollPane);
			fullPanel.add(errorInstructions);
			errorInstructions.setVisible(false);
			if(allFields.size() == 1) {
				fullPanel.setPreferredSize(new Dimension(900,200));
			}else {
				fullPanel.setPreferredSize(new Dimension(900,250));
			}
			if(buildSubmissionPanel(fullPanel) == JOptionPane.OK_OPTION) {
				if (message != null) {
					JOptionPane.showMessageDialog(view, message);
				}
			}
		} else if (command.equals(ValidationClient.EXPORT)) {
			// TODO : Move this file chooser crap to view and get the right fields set from
			// it
			ButtonGroup buttonGroup = new ButtonGroup();
			JRadioButton errorsAndWarningsButton = new JRadioButton("Errors and Warnings");
			buttonGroup.add(errorsAndWarningsButton);
			errorsAndWarningsButton.setSelected(true);
			JRadioButton errorsOnlyButton = new JRadioButton("Errors Only");
			buttonGroup.add(errorsOnlyButton);
			JRadioButton warningsOnlyButton = new JRadioButton("Warnings Only");
			buttonGroup.add(warningsOnlyButton);
			JPanel radioPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 0;
			gbc.gridy = 0;
			radioPanel.add(errorsAndWarningsButton, gbc);
			gbc.gridy = 1;
			radioPanel.add(errorsOnlyButton, gbc);
			gbc.gridy = 2;
			radioPanel.add(warningsOnlyButton, gbc);

			JFileChooser chooser = new JFileChooser();
			File currentDir = new File(submission.getRoot().getConicalPath());
			chooser.setCurrentDirectory(currentDir);

			chooser.setAccessory(radioPanel);
			String path = currentDir + File.separator + "resultDetails";
			String extension = ".txt";
			String finalPath = path + extension;
			File file = new File(finalPath);

			// If the file exists already append a number behind the file name.
			// Number is incremented until the file is found to not exist with that number
			// in the directory.
			int fileNumCounter = 1;

			while (file.exists()) {
				finalPath = path + "(" + fileNumCounter + ")" + extension;
				file = new File(finalPath);
				fileNumCounter++;
			}

			chooser.setSelectedFile(file);

			int returnValue = chooser.showSaveDialog(view);
			String fileName;
			String fullPath;
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				fileName = chooser.getSelectedFile().getName();
				if (!fileName.endsWith(".txt")) {
					fileName = fileName + ".txt";
				}
				String dir = chooser.getCurrentDirectory().toString() + File.separatorChar;
				fullPath = dir + fileName;

				File resultFile = new File(fullPath);

				boolean errorsOnly = errorsOnlyButton.isSelected();
				boolean warningsOnly = warningsOnlyButton.isSelected();

				try {
					FileOutputStream outputStream = new FileOutputStream(resultFile, false);
					PrintStream printStream = new PrintStream(outputStream);
					Iterator<FileNode> iter = new FileNodeIterator(submission.getRoot());
					FileNode node = null;

					while (iter.hasNext()) {
						node = iter.next();

						boolean hasErrors = node.getErrorNum() > 0;
						boolean hasWarnings = node.getWarnNum() > 0;

						if (node.isIncluded() && submission.getDataNodes().contains(node)) {
							printStream.println("\t" + node.getName());
							DataTable dataTable = submission.getFileData(node);
							
							if(node.getWarnNum() >= WARNING_CUTOFF_NUM && !errorsOnly) {
								printStream.println("\t" + ApplicationsConstants.ERR_TOO_MANY_WARNINGS);
							}

							if (!warningsOnly && hasErrors) {
								printStream.println("\t\tERRORS");
								for (ValidationOutput output : dataTable.getErrors()) {
									printStream.println("\t\t\t" + output.toString());
								}
							}

							if (!errorsOnly && hasWarnings) {
								printStream.println("\t\tWARNINGS");
								for (ValidationOutput output : dataTable.getWarnings()) {
									printStream.println("\t\t\t" + output.toString());
								}
							}
						}

					}

					outputStream.close();
					printStream.close();

				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		} else if (command.equals(ValidationClient.JOIN_FILES)) {
			new JoinFiles();
		} else if (command.equals(ValidationClient.SUBMISSION_GUIDE)) {
			String sftpUrl = this.config.getProperty("SFTP_URL");
			String helpUrl =
					new String("http://" + sftpUrl + "/portal/study/fileDownloadAction!download.action?fileId=22");

			// This is the redirect link for Help
			BareBonesBrowserLaunch.openURL(helpUrl);
		} else if (command.equals(ValidationClient.EXIT)) {
			System.exit(0);
		} else if (command.equals(ValidationClient.HIDE)) {
			List<Object> oList = view.getSelected();

			List<FileNode> nodeList = new ArrayList<FileNode>();

			for (Object o : oList) {
				if (o != null && o instanceof FileNode) {
					FileNode node = (FileNode) o;
					// node.include();
					// view.configInclude();

					nodeList.add(node);
				}
			}

			buildDetails(nodeList);
		}

	}
	
	public int buildSubmissionPanel(JPanel fullPanel) {
		int result = JOptionPane.showConfirmDialog(null, fullPanel, "Please confirm Dataset Naming",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			UploadManager.systemBusyDialog.setVisible(true);
			if(!validateDatasetName()) {
				errorInstructions.setVisible(true);
				UploadManager.systemBusyDialog.setVisible(false);
				JOptionPane.showMessageDialog(null, "One or more dataset names in your submission contain a ':'. Please remove this character and try again.", 
						"Dataset name(s) contain illegal character",
						JOptionPane.ERROR_MESSAGE);
				return buildSubmissionPanel(fullPanel);
				
			}
			
			if (!checkPopupDatasetLength()) {
				errorInstructions.setVisible(true);
				UploadManager.systemBusyDialog.setVisible(false);
				JOptionPane.showMessageDialog(null, "One or more dataset names in your submission are over the maximum dataset length of 256 characters. ",
						"Dataset name(s) are too long",
						JOptionPane.ERROR_MESSAGE);
				return buildSubmissionPanel(fullPanel);
			} else {
				if (checkPopupDatasetNames()) {
					UploadManager.systemBusyDialog.setVisible(false);
					buildSubmission(null, false, null);
					return result;
				} else {
					errorInstructions.setVisible(true);
					UploadManager.systemBusyDialog.setVisible(false);
					JOptionPane.showMessageDialog(null, "One or more dataset names in your submission exist within the study",
							"Dataset name(s) already exist",
							JOptionPane.ERROR_MESSAGE);
					return buildSubmissionPanel(fullPanel);
				}
			}
			
		}else {
			return result;
		}
	}
	
	public static List<Study> getStudyList() {

		List<Study> studies = null;
		try {
			studies = new ArrayList<Study>(serverClient.getStudies());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Collections.sort(studies, new Comparator<Study>() {

			@Override
			public int compare(Study o1, Study o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}

		});

		return studies;
	}
	
	private void convertStudyList() {

		studySelect = new JComboBox<Study>(new Vector<Study>(
				getStudyList()));
		// studySelect.setPrototypeDisplayValue(ApplicationsConstants.COMBO_BOX_VALUE);
	}

	private void buildDetails(List<FileNode> selectedList) {

		DefaultListModel model = new DefaultListModel();
		TreeSet<FileNode> nodes = new TreeSet<FileNode>();

		for (FileNode selected : selectedList) {
			for (FileNode node : selected.getSubNodes()) {
				if (node.isIncluded()) {
					nodes.add(node);
				}
			}
		}

		Iterator<FileNode> nodeIter = nodes.iterator();

		boolean addString = (nodes.size() > 1);

		while (nodeIter.hasNext()) {
			FileNode node = nodeIter.next();
			DataTable data = submission.getFileData(node);

			String addBefore = "";

			if (addString && !node.isDirectory()) {
				String name = node.getName();
				int ind = name.lastIndexOf(".");
				if (ind > -1) {
					name = name.substring(0, name.lastIndexOf("."));
				}
				addBefore = name + ": ";
			}

			if (data != null) {
				Iterator<ValidationOutput> outputIter = data.getErrors().iterator();
				while (outputIter.hasNext()) {
					ValidationOutput output = outputIter.next();
					output.setMessagePrefix(addBefore);
					model.addElement(output);
				}
			}
		}

		nodeIter = nodes.iterator();

		if (!view.isHidden()) {

			for (String s : unknownFileWarnings) {
				ValidationOutput output = new ValidationOutput(null, OutputType.WARNING, -1, -1, s);
				model.addElement(output);
			}

			while (nodeIter.hasNext()) {
				FileNode node = nodeIter.next();
				DataTable data = submission.getFileData(node);

				String addBefore = "";

				if (addString && !node.isDirectory()) {
					String name = node.getName();
					int ind = name.lastIndexOf(".");
					if (ind > -1)
						name = name.substring(0, name.lastIndexOf("."));
					addBefore = name + ": ";
				}

				if (data != null) {
					Iterator<ValidationOutput> outputIter = data.getWarnings().iterator();
					while (outputIter.hasNext()) {
						ValidationOutput output = outputIter.next();
						output.setMessagePrefix(addBefore);
						model.addElement(output);
					}
				}
			}
		}

		view.setOutputModel(model);
	}
	
	public boolean checkPopupDatasetNames() {
		Set<Dataset> datasets = null;
		boolean areDatasetsUnique = true;
		try {
			datasets = serverClient.getDatasets(studySelect.getSelectedItem().toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		if (datasets != null && !datasets.isEmpty()) {
			for(JTextField currentField : allFields) {
				for (Dataset dataset : datasets) {
					if (dataset != null && currentField.getText()
							.concat("_" + getNodeFromMap(mapFieldToNode, currentField).getStructureName())
							.equals(dataset.getName())) {
						currentField.setBackground(LIGHT_RED);
						areDatasetsUnique = false;
						break;
					} else {
						currentField.setBackground(Color.WHITE);
					}
				}
			}
		}
		return areDatasetsUnique;
	}
	
	public boolean checkPopupDatasetLength() {

		boolean areDatasetsPermissibleLength = true;

		for (JTextField currentField : allFields) {
			if (currentField.getText().concat("_" + getNodeFromMap(mapFieldToNode, currentField).getStructureName())
					.length() > ApplicationsConstants.MAX_DATASET_NAME_LENGTH) {
				currentField.setBackground(LIGHT_RED);
				areDatasetsPermissibleLength = false;
				break;
			} else {
				currentField.setBackground(Color.WHITE);
			}
		}
		return areDatasetsPermissibleLength;
	}
	
	private boolean validateDatasetName() {
		boolean isValid = true;
		
		for (JTextField currentField : allFields) {
			if (currentField.getText().contains(":")) {
				currentField.setBackground(LIGHT_RED);
				isValid = false;
				break;
			} else {
				currentField.setBackground(Color.WHITE);
			}
		}
		return isValid;
		
	}
	
	private FileNode getNodeFromMap(HashMap<FileNode,JTextField> nodeMap, JTextField field) {
		return nodeMap.entrySet().stream().filter(entry -> field.equals(entry.getValue())).map(Map.Entry::getKey).findFirst()
				.get();
	}

	public String buildSubmission(final String sourceDir, final boolean isNonToolSubmission, String proformsDatasetName) {

		
			message = null;
			// Create the datafile.xml
			if (view != null) {
				working = view.createWorkingPopUp("Building Submission");
				view.configAll(false);
				working.setVisible(true);
			}
			// This thread will allow the waiting dialog to be open while the submission
			// package is built.
			final CountDownLatch latch = new CountDownLatch(1);
			new Thread() {

				public void run() {

					// This value will contain the error message on any exceptions that occur.
					String error = "";

					String directory;
					if (view != null) {
						directory = view.getSourceDir();
					} else {
						directory = sourceDir;
					}

					Date date = new Date();
					try {
						// Build the datafile.xml
						DataFileBuilder dataFileBuilder;
						dataFileBuilder = new DataFileBuilder(submission, directory);
						if (!dataFileBuilder.build(mapFieldToNode, isNonToolSubmission, proformsDatasetName)) {
							error = dataFileBuilder.getErrorMessage();
							logger.error("There was an error building the file: " + error);
							throw new Exception();
						}

						// Create and build the submission ticket
						SubmissionTicket ticket = new SubmissionTicket();

						String version = config.getProperty("VERSION");

						ticket.setVersion(version);
						ticket.setEnvironment(getEnvironment());

						SubmissionPackageBuilder packageBuilder = new SubmissionPackageBuilder(submission,
								dataFileBuilder.getRootPath(), date, submissionSuffix);
						if (packageBuilder.build(mapFieldToNode, isNonToolSubmission, proformsDatasetName)) {
							ticket.setSubmissionPackages(packageBuilder.getSubmissionPackages());
						} else {
							error = packageBuilder.getErrorMessage();
							logger.error("There was an error building the submission package " + error);
							throw new Exception();

						}

						// Write the submision ticket
						File file = new File(directory + File.separator + "submissionTicket-"
								+ packageBuilder.getDate().getTime() + ".xml");
						FileOutputStream stream = null;

						try {
							stream = new FileOutputStream(file);
						} catch (FileNotFoundException e) {
							// This exception is caught, the error message is written and then a new
							// exception is thrown to
							// handle displaying the error message.
							error = "Unable to write ticket to location: " + directory + "submissionTicket-"
									+ packageBuilder.getDate().getTime() + ".xml";
							logger.error(error);
							throw new Exception();
						}
						try {
							JAXB.marshal(ticket, stream);
						} catch (DataBindingException e) {
							error = "JAXB cannot marshal the file.";
							logger.error("There was an error marshalling the file " + error);
							throw new Exception();
						}
						if (view != null) {
							working.dispose();
							view.configAll(true);
						}
						message = "A new submission file has been created in the " + "working directory.";

						if (view instanceof ValidationUploadManager) {

							// Get the name of the first CSV in the data
							// submission to pass to the upload manager
							Set<FileNode> set = submission.getDataNodes();
							String name = "";
							int count = 0;

							for (FileNode f : set) {
								FileType type = f.getType();
								if (type.equals(FileType.CSV) && f.isIncluded()) {
									name = f.getName();
									name = name.substring(0, name.lastIndexOf("."));
									name += "-" + packageBuilder.getDate().getTime();
									count++;
								}
							}
							if (count > 1) {
								name = "";
							}
							ValidationUploadManager client = (ValidationUploadManager) view;
							client.populateSubmissionTicket(file, allFields, (Study)studySelect.getSelectedItem());

						}
					} catch (Exception e) {
						e.printStackTrace();
						if (view != null) {
							working.dispose();
							view.configAll(true);
						}
						message = "An error occurred while building your Submission Package. :\n\n" + error + "\n\n"
								+ "If the problem persists please contact your systems administrator.";
					} finally {
						latch.countDown();
					}
					return;
				}
			}.start();
			if (view == null) {
				try {
					latch.await();
				} catch (InterruptedException e) {
					message = "Error: CountDownLatch Exception! Please report this to an admin.";
					e.printStackTrace();
				}
			}

			if (message == null) {
				message = "Your Submission Package is complete. The Submission Ticket has been created in the working directory "
						+ view.getSourceDir() + ".\n This Ticket will enable you to upload your data to the repository";
			}
		
		return message;
	}

	public void include() {

		List<Object> oList = view.getSelected();

		if (oList.isEmpty())
			return;

		List<FileNode> nodeList = new ArrayList<FileNode>();

		boolean revalidate = false;

		for (Object o : oList) {
			if (o != null && o instanceof FileNode) {
				FileNode node = (FileNode) o;
				if (!node.isIncluded()) {
					node.include();
					revalidate = true;
				}
				if (!node.isValid())
					revalidate = true;
				nodeList.add(node);
			}
		}

		buildDetails(nodeList);

		view.configInclude(submission.getRoot(), revalidate);
	}

	public void exclude() {

		List<Object> oList = view.getSelected();

		if (oList.isEmpty())
			return;

		List<FileNode> nodeList = new ArrayList<FileNode>();

		boolean revalidate = false;

		for (Object o : oList) {
			if (o != null && o instanceof FileNode) {
				FileNode node = (FileNode) o;
				if (node.isIncluded()) {
					node.exclude();
					revalidate = true;
				}
				// view.configInclude();

				nodeList.add(node);
			}
		}

		buildDetails(nodeList);

		view.configExclude(submission.getRoot(), revalidate);
	}

	public void reload() {

		List<Object> oList = view.getSelected();

		final List<FileNode> nodeList = new ArrayList<FileNode>();

		for (Object o : oList) {
			if (o != null && o instanceof FileNode) {
				FileNode node = (FileNode) o;
				nodeList.add(node);
			}
		}
		// Disable the GUI
		parser = new FileParser(ddtClient, submission);
		if (view != null) {
			working = view.createWorkingPopUp("Reloading Files");
			view.configAll(false);
			working.setVisible(true);
		}

		// Run the partial submission
		final CountDownLatch latch = new CountDownLatch(1);
		new Thread() {

			public void run() {

				try {
					submission = parser.buildPartialSubmission(nodeList);
					if (view != null) {
						view.setFileModel(new DefaultTreeModel(submission.getRoot(), true));
						view.configLoad(submission.getRoot());
						working.dispose();
					}
				} catch (WebServiceException e) {
					if (view != null) {
						view.setFileModel(new DefaultTreeModel(null));
						view.configStart();
						working.dispose();
					}
					message = "Unable to connect to FITBIR, " + "please check yout connection and try again. "
							+ "If the problem persists please contact your systems administrator.";
				} finally {
					latch.countDown();
				}
				return;
			}
		}.start();

	}

	/**
	 * Validates the contents of the data tables. If there is a view, then this function disables it while working and
	 * enables it on completion.
	 */
	public String validate() {
		boolean hasFiles = false;
		message = null;
		List<String> fsNamesList = new ArrayList<String>();
		if (view != null) {
			working = view.createWorkingPopUp("Validating Files");
			working.setVisible(true);
			view.configAll(false);
			submission.reset();
		}

		for (FileNode node : submission.getDataNodes()) {
			if (node.isIncluded()) {
				hasFiles = true;
				fsNamesList.add(node.getStructureName());
				System.out.println("Structure Name:\t" + node.getStructureName());

			}
		}
		// check retired DE
		List<StructuralFormStructure> sFS = submission.getDictionary();
		List<String> fsContainingRetiredDE = new ArrayList<String>();
		List<String> archievedFsList = new ArrayList<String>();
		boolean containsRetired = false;
		boolean archievedFs = false;
		for (StructuralFormStructure dataStructure : sFS) {
			if (dataStructure.getStatus().equals(StatusType.ARCHIVED)) {
				for (String fs : fsNamesList) {
					if (fs.toString().equalsIgnoreCase(dataStructure.getShortName())) {
						archievedFs = true;
						archievedFsList.add(dataStructure.getShortName());
					}
				}
			}

			Set<MapElement> me = dataStructure.getDataElements();
			for (MapElement de : me) {
				if (de.getStructuralDataElement().getStatus().equals(DataElementStatus.RETIRED)) {
					for (String fs : fsNamesList) {
						if (fs.toString().equalsIgnoreCase(dataStructure.getShortName())) {
							containsRetired = true;
							fsContainingRetiredDE.add(dataStructure.getShortName());
						}
					}

				}

			}

		}
		if (containsRetired && view != null) {
			JOptionPane.showMessageDialog(null, "One or more of the following Form Structure(s) :\t "
					+ StringUtils.join(fsContainingRetiredDE.toArray(), ',' + " ")
					+ " contains Retired Data Element(s). Retired data elements may not be included in a submission. See below for details.");
			working.setVisible(false);
		}
		if (archievedFs && view != null) {
			JOptionPane.showMessageDialog(null,
					"One or more of the following Form Structure(s):\t "
							+ StringUtils.join(archievedFsList.toArray(), ',' + " ")
							+ " is Archived. Archived Form Structure(s) may not be included in a submission.");
			working.setVisible(false);
		}

		if (hasFiles) {

			final CountDownLatch latch = new CountDownLatch(1);
			new Thread() {

				public void run() {

					try {

						engine.validate(submission,isComingFromProforms);
					} catch (JAXBException e) {
						e.printStackTrace();
						message = "There is an error with your translation rules.";
					} catch (IOException e) {
						e.printStackTrace();
						message = "An error occured while trying to load translation rules.";
					}

					unknownFileWarnings.clear();


					// Logic for displaying the correct message, excluding an files if needed, and
					// setting the submission button if needed.
					if (submission.getRoot().isValidWithUnknowns()) {

						// data is valid. check to see if there are drafts.
						if (submission.hasDrafts()) {
							// There are drafts so we are not going to worry about excluding files
							// and we are not going to check any futher.
							message =
									"The validated file(s) contain Shared Draft or Draft structures. Shared Draft or Draft structures may not be included in a submission.";
						} else {
							// At this point we are going to be submitting data.
							if (view != null) {
								view.enableBuilding(true);
							}
							if (submission.getRoot().isValid()) {
								// The data is fine as it is.
								message = "All files are valid.";
							} else {
								// The data is fine only after we exclude files.
								FileNode root = submission.getRoot();
								unknownFileWarnings = root.excludeUnknowns();
								message =
										"The validated files include unassociated files. These files have been excluded. See warnings for more information.";
							}
						}
					} else {
						// case. Data is not valid, with or without excluding files. Stop here.
						message = "The validated files contain errors.";
					}
					message = message.concat(" Click on a validated file from the list to view its warnings and/or errors.");
					if (view != null) {
						JOptionPane.showMessageDialog(view, message);

						DefaultListModel model = new DefaultListModel();
						for (String s : unknownFileWarnings) {
							ValidationOutput output = new ValidationOutput(null, OutputType.WARNING, -1, -1, s);
							model.addElement(output);
						}
						view.setOutputModel(model);

						view.configValidate(submission.getRoot());

						working.dispose();
					}
					latch.countDown();
					return;
				}
			}.start();

			if (view == null) {
				try {
					latch.await();
				} catch (InterruptedException e) {
					message = "Error: CountDownLatch Exception! Please report this to an admin.";
					e.printStackTrace();
				}
			}
		} else {
			message = "Please select a file to validate.";

			if (view != null) {
				working.setVisible(false);
				JOptionPane.showMessageDialog(view, message);
				view.configLoad(submission.getRoot());
			}

			submission.reset();
			return null;
		}

		return message;
	}

	/**
	 * Loads the contents of the directory into the file tree and loads the raw file data into data tables for
	 * validation.
	 * 
	 * Returns an error message if there was an error or null if there are no errors in load.
	 * 
	 * If the 'view' is non-null, then this function will disable the interface of validation tool while loading and
	 * populate the file tree in the display.
	 * 
	 * @param dir : the source directory to load into the system
	 * @return
	 */
	public String load(String dir) {

		message = null;

		File workingDir = new File(dir);

		if (workingDir.exists() && workingDir.isDirectory()) {

			parser = new FileParser(ddtClient, workingDir);
			if (view != null) {
				working = view.createWorkingPopUp("Loading Files");
				// Retrive reference to progress bar to be able to change data
				Container panel = (Container) working.getContentPane().getComponent(0);
				progressBar = (JProgressBar) panel.getComponent(1);
				view.setSelected(new ArrayList<Integer>());
				view.configAll(false);
				working.setVisible(true);
			}

			// This latch is to prevent race conditions when running with the cmdLineValidator
			final CountDownLatch latch = new CountDownLatch(1);
			SwingWorker task = new SwingWorker() {

				@Override
				public String doInBackground() {

					try {
						parser.setWorker(this);
						submission = parser.buildSubmission();
						if (view != null) {
							view.setFileModel(new DefaultTreeModel(submission.getRoot(), true));
							unknownFileWarnings.clear();
							view.setOutputModel(new DefaultListModel());
							view.configLoad(submission.getRoot());
						}
					} catch (Exception e) {
						if (view != null) {
							view.setFileModel(new DefaultTreeModel(null));
							view.configStart();
						}
						e.printStackTrace();
						message =
								"Unable to retrieve the form structure(s) from the system, please verify the form structure short name is correct. "
										+ "If the problem persists please contact your systems administrator.";
						return message;
					} finally {
						latch.countDown();
					}

					return message;
				}

				@Override
				public void done() {

					if (view != null) {
						working.dispose();
					}
				}
			};
			task.addPropertyChangeListener(this);
			task.execute();
			if (view == null) {
				try {
					latch.await();
				} catch (InterruptedException e) {
					message = "Error: CountDownLatch Exception! Please report this to an admin.";
					e.printStackTrace();
				}
			}

		} else {
			message = "Invalid directory.";
		}
		return message;
	}

	public void valueChanged(TreeSelectionEvent event) {

		TreePath[] paths = event.getPaths();

		for (int i = 0; i < paths.length; i++) {
			if (event.isAddedPath(paths[i])) {
				FileNode selected = (FileNode) paths[i].getLastPathComponent();
				nodeList.add(selected);
			} else {
				nodeList.remove((FileNode) paths[i].getLastPathComponent());
			}
		}

		buildDetails(nodeList);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		ArrayList<Integer> selected = new ArrayList<Integer>();
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (!lsm.isSelectionEmpty()) {
			// Find out which indexes are selected.
			int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();
			for (int i = minIndex; i <= maxIndex; i++) {
				if (lsm.isSelectedIndex(i)) {
					selected.add(i);
				}
			}
		}

		view.setSelected(selected);
	}

	// public static String getVersion()
	// {
	// String deploymentVersion = null;
	//
	// try
	// {
	// InputStream fis = ValidationController.class.getResourceAsStream("/META-INF/MANIFEST.MF");
	//
	// Properties p = new Properties();
	// p.load( fis );
	//
	// deploymentVersion = p.getProperty( "Implementation-Build" );
	//
	// if (deploymentVersion == null || deploymentVersion.equals( "null" ))
	// {
	// deploymentVersion = "development";
	// }
	// }
	// catch (Exception ex)
	// {
	// ex.printStackTrace();
	// deploymentVersion = "Unreadable";
	// }
	//
	// return deploymentVersion;
	//
	// return version;
	// }

	/**
	 * A getter for the data submission object of the controller
	 * 
	 * @return
	 */
	public DataSubmission getSubmission() {

		return submission;
	}

	public String getEnvironment() {

		// Adjusted to return the full URL path in order to identify the correct environment.

		return bricsUrl;

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		// GUI Load
		if (view != null) {
			if (progressBar != null && "progress" == evt.getPropertyName()) {
				progressBar.setValue(progressBar.getValue() + 1);
				progressBar.setStringPainted(true);
			}
			if (progressBar != null && "max" == evt.getPropertyName()) {
				int max = (Integer) evt.getNewValue();
				progressBar.setMinimum(0);
				progressBar.setMaximum(max);
				progressBar.setIndeterminate(false);
				progressBar.setValue(0);
			}
		}
		// Console Load
		else if (System.console() != null) {
			if (!progressComplete && "progress" == evt.getPropertyName()) {
				this.progress++;
				String output = "";
				Console cons = System.console();
				int loadCount = ((this.progress * 100) / this.progressMax);
				if (loadCount > 95) {
					loadCount = 100;
					progressComplete = true;
				}
				output = output + "|";
				for (int i = 0; i < (loadCount / 2); i++) {
					output = output + "=";
				}
				for (int i = 0; i < (50 - (loadCount / 2)); i++) {
					output = output + " ";
				}
				output = output + "| " + loadCount + " %% \r";
				cons.printf(output);
				cons.flush();
				if (progressComplete) {
					cons.printf("\n");
					cons.flush();
				}
			}
			if ("max" == evt.getPropertyName()) {
				progressComplete = false;
				progressMax = (Integer) evt.getNewValue();
			}
		}
	}

	public void setExtraValidation(HashMap<String, Boolean> extraValidation) {
		if (engine != null) {
			engine.setExtraValidation(extraValidation);
		}
	}
}
