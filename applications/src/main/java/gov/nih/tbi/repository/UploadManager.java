package gov.nih.tbi.repository;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.commons.EulaAgreementWrapper;
import gov.nih.tbi.commons.WebstartRestProvider;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.download.table.FileTableModel;
import gov.nih.tbi.download.workers.DownloadWorker;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.service.io.SftpClientManager;
import gov.nih.tbi.repository.table.ProgressCellRenderer;
import gov.nih.tbi.repository.table.UploadTableModel;
import gov.nih.tbi.repository.view.ReloadDatasetView;
import gov.nih.tbi.repository.ws.RepositoryWebService;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

/**
 * Client side tool for users to upload submission data to the SFTP server for
 * processing.
 * 
 * @author Danny Holloway
 */
public class UploadManager extends JFrame {

	static Logger logger = Logger.getLogger(UploadManager.class);
	private static final long serialVersionUID = 1L;

	// SFTP connection settings
	private static final int URL_ARGUMENT_INDEX = 0;
	private static final int VERSION_ARGUMENT_INDEX = 1;
	private static final int USERNAME_ARGUMENT_INDEX = 2;
	private static final int USER_HASH_ARGUMENT_INDEX = 3;
	private static final int PASSWORD_HASH_ARGUMENT_INDEX = 4;
	private static final int ORG_EMAIL_ARGUMENT_INDEX = 5;
	private static final int ARGS_NUM = 6;

	private static final int MAX_DATASET_NAME_LENGTH = 55;

	// how often the UI is updated (aka tick)
	public static final int UI_UPDATE_INTERVAL = 500;

	private static final int QUEUE_COLUMN_WIDTH = 1;

	// Arguments
	private static String serverLocation;
	private static String userName;
	private static String version;
	private static String userHash;
	private static String password;
	private static String orgEmail;

	private static RepositoryWebService repositoryWebService;
	private static JTextField fileNameField;
	private static JTextField datasetField;
	private static JComboBox<Study> studySelect;
	private static String finalDatasetName;
	private static String finalStudyName;
	private static JXTable table;
	private static UploadTableModel uploadTableModel = new UploadTableModel();
	private static File fileChooserDirectory;
	private static JComboBox<Study> studyChoice = null;
	public static WebstartRestProvider provider;
	private static UploadManager frame;
	public static AtomicBoolean  ISUPLOADING = new AtomicBoolean(false);
	private static final JButton uploadButton = new JButton("Start Submission Upload");
	private static final JButton cancelButton = new JButton("Cancel Submission Upload");
    public static final Font fontArial14 = new Font("Arial", Font.PLAIN, 14);
	public static JDialog systemBusyDialog ;
	
	private Properties props = null;
	// private final static String LAST_TICKET = "last.ticket"; //probably don't
	// need
	// private final static String LAST_DATA_NAME = "last.dataName"; //Probably
	// don't need
	// public final static String LAST_TICKET_DIR = "last.ticketDir";
	public final static String LAST_STUDY = "last.study";

	public static UploadTableModel getUploadTableModel() {
		return uploadTableModel;
	}

	public static void setUploadTableModel(UploadTableModel uploadTableModel) {
		UploadManager.uploadTableModel = uploadTableModel;
	}

	public static String getServerLocation() {

		return serverLocation;
	}

	public static void setServerLocation(String serverLocation) {

		UploadManager.serverLocation = serverLocation;
	}

	public static String getOrgEmail() {

		return orgEmail;
	}

	public static void setOrgEmail(String orgEmail) {

		UploadManager.orgEmail = orgEmail;
	}

	public static String getVersion() {

		return version;
	}

	public static void setVersion(String version) {

		UploadManager.version = version;
	}

	public static String getDatasetName() {

		return datasetField.getText();
	}
	public static String getFinalStudyName() {

		return finalStudyName;
	}
	public static String getFinalDatasetName() {
		return finalDatasetName;
	}

	public static RepositoryWebService getRepositoryWebService() {

		return repositoryWebService;
	}

	public static String getFilePath() {

		return fileNameField.getText();
	}

	public static Study getSelectedStudy() {

		return (Study) studyChoice.getSelectedItem();
	}

	public void setFilePath(String path) {

		fileNameField.setText(path);
		fileChooserDirectory = (new File(path)).getParentFile();
	}

	public void setDataName(String name) {

		datasetField.setText(name);
	}

	public void setFileChooserDirectory(String path) {

		fileChooserDirectory = new File(path);
	}

	public void setProperties(Properties properties) {

		props = properties;
	}

	public void setSelectedStudy(String study) {

		studySelect.setSelectedItem(study);
	}

	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException, MalformedURLException {

		// Display an error and exit if the argument length is wrong
		if (args.length != ARGS_NUM) {
			JOptionPane
					.showMessageDialog(
							null,
							"Application has been initiated "
									+ "with a number improper arguments please contact your systems administrator",
							"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		frame = new UploadManager(args[URL_ARGUMENT_INDEX],
				args[VERSION_ARGUMENT_INDEX], args[USERNAME_ARGUMENT_INDEX],
				args[USER_HASH_ARGUMENT_INDEX],
				args[PASSWORD_HASH_ARGUMENT_INDEX],
				args[ORG_EMAIL_ARGUMENT_INDEX]);

		EulaAgreementWrapper wrapper = new EulaAgreementWrapper(
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {

						frame.uploadPanel();
						frame.setVisible(true);
					}
				}, orgEmail);

		frame.setTitle(ApplicationsConstants.APPLICATION_TITLE);
		frame.setSize(new Dimension(650, 400));

		frame.setIconImage(UploadManagerController.createImageIcon(
				ApplicationsConstants.FRAME_ICON, "Frame GIF").getImage());

		frame.setVisible(false);
		frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// confirm quit dialog
		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				int confirmed = JOptionPane.showConfirmDialog(frame,
						"Are you sure you want to quit?", "Confirm Quit",
						JOptionPane.YES_NO_OPTION);
				// Close if user confirmed
				if (confirmed == JOptionPane.YES_OPTION) {
					// Close frame and clean up
					frame.dispose();
					SftpClientManager.closeAll();
					System.exit(0);
				}
			}
		});
	}

	/**
	 * Consume the WSDL for the webservice and initialize the UI.
	 * 
	 * @param serverName
	 * @param args2
	 * @param args
	 * @throws MalformedURLException
	 */
	public UploadManager(String serverName, String version, String userName,
			String userHash, String password, String orgEmail)
			throws MalformedURLException {

		if (!HashMethods.validateServerHash(userHash, userName)) {
			JOptionPane
					.showMessageDialog(
							rootPane,
							"This Upload Manager Tool is corrupt.  Please relaunch the tool and try again.",
							"Tool error", JOptionPane.ERROR_MESSAGE);
			System.exit(ERROR);
		} else {
			UploadManager.serverLocation = serverName;
			UploadManager.version = version;
			UploadManager.userName = userName;
			UploadManager.userHash = userHash;
			UploadManager.password = password;
			UploadManager.setOrgEmail(orgEmail);
			provider = new WebstartRestProvider(serverLocation, userName, password);
		}
	}

	/**
	 * Creates and displays a FileDialog so that the user can select the file to upload.
	 */
	private void selectFile() {

		JFileChooser uploadFile = null;

		if (UploadManager.fileChooserDirectory == null) {
			uploadFile = new JFileChooser();
		} else {
			uploadFile = new JFileChooser(UploadManager.fileChooserDirectory);
		}

		FileNameExtensionFilter filter = new FileNameExtensionFilter("Submission Ticket (.xml)", "xml");
		uploadFile.setFileFilter(filter);
		uploadFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int retVal = uploadFile.showOpenDialog(rootPane);
		if (JFileChooser.APPROVE_OPTION == retVal) {
			fileNameField.setText(uploadFile.getSelectedFile().getAbsolutePath());
			fileChooserDirectory = uploadFile.getCurrentDirectory();

			SubmissionTicket submissionTicket =
					UploadManagerController.unmarshalSubmissionTicket(uploadFile.getSelectedFile()
							.getAbsolutePath());
			if (submissionTicket != null) {
				String name = submissionTicket.getSubmissionPackage()
						.getDatasets().get(0).getName();
				name = name.replace(".csv", "");
				String file = uploadFile.getSelectedFile().getName();
				file = file.replace("submissionTicket", "");
				file = file.replace(".xml", "");
				setDataName(name + file);
			}

		}
	}

	/**
	 * Helper function for any validation required before upload.
	 * 
	 * @return
	 */
	private boolean isUploadFileNameValid() {
		if (fileNameField.getText() == null
				|| ServiceConstants.EMPTY_STRING
						.equals(fileNameField.getText())) {
			return false;
		} 
		return true;
	}

	private boolean isSubmissionTicketValid(String fileName,
			SubmissionTicket submissionTicket) throws Exception {
		if (!fileName.isEmpty() && !fileName.contains("submissionTicket")) {
			JOptionPane
					.showMessageDialog(
							rootPane,
							"A Submission Ticket was not detected. Please upload the Submission Ticket.",
							"Invalid Ticket", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (submissionTicket == null) {
			JOptionPane.showMessageDialog(rootPane,
					"Invalid Submission Ticket", "Invalid Ticket",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (!UploadManagerController.validateTicketCrc(submissionTicket)) {
			JOptionPane.showMessageDialog(rootPane,
					"Submission Package has been illegally modified",
					"Illegal Modification Detected", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// Check if the environment and version are consistant with those of the
		// ticket
		if (submissionTicket.getEnvironment().indexOf(serverLocation) > 0) {
			JOptionPane
					.showMessageDialog(
							rootPane,
							"The environment this ticket was created in does not match the upload manager's environment.",
							"Environment mismatch", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (!version.equalsIgnoreCase(submissionTicket.getVersion())) {
			System.out.println(version + " " + submissionTicket.getVersion());
			JOptionPane
					.showMessageDialog(
							rootPane,
							"The deployment version this ticket was created in does not match the upload manager's version.",
							"Version mismatch", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;

	}

	/**
	 * Connects to the SFTP server to upload the file selected and starts
	 * updating the progress bar.
	 * 
	 * @throws Exception
	 */
	private void upload() throws Exception {

		String fileName = fileNameField.getText();
		setProperty(LAST_STUDY, getSelectedStudy().getTitle());
		// unmarshal the submission package into SubmissionPackage Object

		SubmissionTicket submissionTicket = UploadManagerController
				.unmarshalSubmissionTicket(fileName);
		if (!isSubmissionTicketValid(fileName, submissionTicket)) {
			return;
		}
		// verify the user uploaded a file with the string submissionTicket in the name
				if (!fileName.isEmpty() && !fileName.contains("submissionTicket")) {
					JOptionPane.showMessageDialog(rootPane,
							"A Submission Ticket was not detected. Please upload the Submission Ticket.", "Invalid Ticket",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (submissionTicket == null) {
					JOptionPane.showMessageDialog(rootPane, "Invalid Submission Ticket", "Invalid Ticket",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (!UploadManagerController.validateTicketCrc(submissionTicket)) {
					JOptionPane.showMessageDialog(rootPane, "Submission Package has been illegally modified",
							"Illegal Modification Detected", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Check if the environment and version are consistant with those of the ticket
				if (submissionTicket.getEnvironment().indexOf(serverLocation) > 0) {
					JOptionPane.showMessageDialog(rootPane,
							"The environment this ticket was created in does not match the upload manager's environment.",
							"Environment mismatch", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (!version.equalsIgnoreCase(submissionTicket.getVersion())) {
					System.out.println(version + " " + submissionTicket.getVersion());
					JOptionPane.showMessageDialog(rootPane,
							"The deployment version this ticket was created in does not match the upload manager's version.",
							"Version mismatch", JOptionPane.ERROR_MESSAGE);
					return;
				}
		Dataset dataset = UploadManagerController
				.processSubmissionTicket(submissionTicket);
		UploadManagerController.loadUploadQueue(dataset);

		System.out.println("In UploadManager, current uploadToken="
				+ UploadManagerController.getUploadToken());

		// clear the fields when all child thread done with the upload: only on
		// the successful condition
		if (UploadManagerController.getUploadToken() == ApplicationsConstants.MAXIMUM_CONCURRENT_UPLOAD) {
			datasetField.setText(ServiceConstants.EMPTY_STRING);
			fileNameField.setText(ServiceConstants.EMPTY_STRING);
		}
	}

	private boolean isUploadConfirmed() {
		Boolean isConfirmed = false;
		if (!isUploadFileNameValid()) {
			JOptionPane.showMessageDialog(rootPane,
					"You must select a file to upload", "Upload Error",
					JOptionPane.ERROR_MESSAGE);
		} else if (datasetField == null
				|| datasetField.getText().equals(ServiceConstants.EMPTY_STRING)) {
			JOptionPane.showMessageDialog(rootPane,
					"You must enter a name for the Dataset", "Dataset Error",
					JOptionPane.ERROR_MESSAGE);
		} else if (datasetField.getText().length() > MAX_DATASET_NAME_LENGTH) {
			JOptionPane.showMessageDialog(rootPane,
					"Dataset name must not exceed " + MAX_DATASET_NAME_LENGTH
							+ " characters", "Dataset Error",
					JOptionPane.ERROR_MESSAGE);
		} else if (studySelect.getSelectedItem() == null) {
			JOptionPane
					.showMessageDialog(
							rootPane,
							"A study must be selected, if no studies appear in the drop down menu, "
									+ "please return to the organization website and create one.",
							"Dataset Error", JOptionPane.ERROR_MESSAGE);
		} else {
			if (!UploadManagerController.isDatasetUnique()) {
				JOptionPane.showMessageDialog(rootPane,
						"Dataset name must be unique", "Dataset Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				// Display confirm dialog
				int confirmed = JOptionPane.showConfirmDialog(frame,
						"Your submission will be added to the study: '"
								+ studySelect.getSelectedItem()
								+ "', please click OK to continue.",
						"Confirm Submission", JOptionPane.OK_CANCEL_OPTION);

				isConfirmed  = (confirmed == JOptionPane.OK_OPTION);
			}
		}
		return isConfirmed;
	}

	/**
	 * Build a Choice to select a study name from the list returned by the web
	 * service.
	 * 
	 * @return Choice to select study name.
	 */
	private JComboBox<Study> getStudyList() {

		studySelect = new JComboBox<Study>(new Vector<Study>(
				UploadManagerController.getStudyList()));
		// studySelect.setPrototypeDisplayValue(ApplicationsConstants.COMBO_BOX_VALUE);
		return studySelect;
	}

	/**
	 * Refreshes the list of studies.
	 */
	public void refreshStudy() {

		// Have to copy over the items, creating a new instance of combobox will
		// NOT work
		studyChoice.removeAllItems();

		for (Study study : UploadManagerController.getStudyList()) {
			studyChoice.addItem(study);
		}

		studyChoice.setSelectedIndex(0);
	}

	/**
	 * Build a right-mouse popup menu for the table
	 * 
	 * @return
	 */
	private JPopupMenu getTableMenu(final int rowIndex) {

		JPopupMenu menu = new JPopupMenu();
		JMenuItem cancel = new JMenuItem("Cancel");
		JMenuItem retry = new JMenuItem("Retry");

		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					((UploadTableModel) table.getModel())
							.cancelSingleItemUpload(rowIndex);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(rootPane,
							"An error has occured while cancelling",
							"Cancel Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		retry.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				UploadManagerController.retryUpload(rowIndex);
			}
		});

		if (UploadManagerController.getUploadItem(rowIndex) != null) {
			if (UploadStatus.CANCELLED.equals(UploadManagerController
					.getUploadItem(rowIndex).getUploadStatus())) {
				menu.add(retry);
			} else if (!UploadStatus.COMPLETED.equals(UploadManagerController
					.getUploadItem(rowIndex).getUploadStatus())) {
				menu.add(cancel);
			}
		}

		return menu;
	}

	private void uploadPanel() {

		JPanel contentPanel = createUploadPanel();
		getContentPane().add(contentPanel);
		pack();

		if (props != null) {
			try {
				FileInputStream input = new FileInputStream(System.getProperty(
						"user.home", "") + File.separator + "brics.pref");
				props.load(input);
				input.close();
				setFileChooserDirectory(props.getProperty("user.dir", ""));
				setSelectedStudy(props.getProperty("last.study", ""));
			} catch (FileNotFoundException e1) {
				// don't need to do anything, just make a new one
			} catch (IOException e) {
				props = null;// should probably not do anything instead of
								// making new one
				System.err.println("Could not open preferences file");
			}
		}
	}

	private void setProperty(String property, String value) {

		if (props == null)
			return;

		props.setProperty(property, value);
		try {
			FileOutputStream writer = new FileOutputStream(System.getProperty(
					"user.home", "") + File.separator + "brics.pref");
			props.store(writer, "--No Comment--");
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("Cannot make new properties file");
		} catch (IOException e) {
			System.err.println("Cannot write to properties file");
		}
	}

	/**
	 * Build a panel with the upload UI components.
	 * 
	 * @return Panel
	 */
	public JPanel createUploadPanel() {

		getContentPane().removeAll();
		getContentPane().setSize(new Dimension(1080, 550));
		// study selection
		JLabel studyInstruction = new JLabel(
				"1. Please select a study from the list below.  You may only submit data to the studies that you have upload permission rights.");
		JLabel studyLabel = new JLabel("Study Name: ");
		studyChoice = getStudyList();
		final JButton studyRefreshButton = new JButton("Refresh");
		final UploadManager view  = this;
		studyRefreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (!UploadManagerController.getStudyList().isEmpty()) {
					studyRefreshButton.setEnabled(false);
					refreshStudy();
					studyRefreshButton.setEnabled(true);
				}
			}
		});

		// file select components
		JLabel selectFileInstruction = new JLabel(
				"<html>2. Please select a valid submission ticket.  The submission ticket contains an inventory of the data to be uploaded to the system.<br />  To create a valid submission ticket, please use the Validation Tool.</html>");
		JLabel selectFileLabel = new JLabel("Submission Ticket (XML): ");
		selectFileLabel.setHorizontalAlignment(JLabel.RIGHT);

		JButton selectFileButton = new JButton("Browse");
		selectFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				selectFile();
			}
		});

		fileNameField = new JTextField(30);
		fileNameField.setEditable(false);

		// dataset display
		JLabel datasetInstruction = new JLabel(
				"3. Enter a name for your dataset submission.  The dataset name must be unique for the selected study.");
		JLabel datasetLabel = new JLabel("Dataset Name: ");
		datasetField = new JTextField(30);
		datasetField.setEditable(true);

		// upload button

		uploadButton.addActionListener(new StartUploadActionListener());
		cancelButton.addActionListener(new CancelUploadActionListener());
		//cancelButton.setEnabled(false);
		
		JPanel upperActionPanel = new JPanel(new FlowLayout(FlowLayout.LEADING,
				10, 20));
		upperActionPanel.add(uploadButton);
		upperActionPanel.add(cancelButton);
		// queue table
		table = new JXTable(uploadTableModel);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(UploadTableModel.UPLOAD_PROGRESS_COLUMN)
				.setCellRenderer(new ProgressCellRenderer());
		table.getColumn(UploadTableModel.QUEUE_COLUMN).setPreferredWidth(
				QUEUE_COLUMN_WIDTH);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		columnModel.getColumn(UploadTableModel.QUEUE_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(UploadTableModel.FILE_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(UploadTableModel.STUDY_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(UploadTableModel.DATASET_COLUMN ).setCellRenderer(centerRenderer);
		columnModel.getColumn(UploadTableModel.UPLOAD_SPEED_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(UploadTableModel.TIME_REMAINING_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(UploadTableModel.STATUS_COLUMN).setCellRenderer(centerRenderer);
	
		table.setFillsViewportHeight(true);
		table.setSortable(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		uploadTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
				if (e.getColumn() <=0) {
					table.getTableHeader().repaint();
				}
            }
        });
	
		JScrollPane queueTable = new JScrollPane(table);

		// queue table border
		TitledBorder queueBorder = new TitledBorder("Upload Queue");
		queueTable.setBorder(queueBorder);

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {

				int r = table.rowAtPoint(e.getPoint());
				if (r >= 0 && r < table.getRowCount()) {
					table.setRowSelectionInterval(r, r);
				} else {
					table.clearSelection();
				}

				int rowindex = table.getSelectedRow();
				if (rowindex < 0)
					return;
				if (e.getButton() == MouseEvent.BUTTON3 && e.isPopupTrigger()
						&& e.getComponent() instanceof JTable) {
					JPopupMenu popup = getTableMenu(r);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
				// unused double click listener
				// if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()
				// == 2) {
				// openDetailedWindow(r);
				// }
			}
		});

		// Action panel
		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
		JButton clearCompletedButton = new JButton(
				"Clear Completed Submissions");
		clearCompletedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					UploadManagerController
							.clearByStatus(UploadStatus.COMPLETED);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(rootPane,
							"An error has occured during submission clearing",
							"Clearing Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JButton clearCancelledButton = new JButton(
				"Clear Cancelled Submissions");
		clearCancelledButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					UploadManagerController
							.clearByStatus(UploadStatus.CANCELLED);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(rootPane,
							"An error has occured during submission clearing",
							"Clearing Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JButton loadPendingButton = new JButton("Load Pending Submissions");
		loadPendingButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				List<Dataset> pendingDatasets = UploadManagerController
						.getUploadingDataset();
				ReloadDatasetView reloadDatasetView = new ReloadDatasetView(
						frame, pendingDatasets);
				reloadDatasetView.setVisible(true);
			}
		});


		actionPanel.add(clearCompletedButton);
		actionPanel.add(clearCancelledButton);
		//actionPanel.add(loadPendingButton);
		// set group layout
		JPanel contentPanel = new JPanel();
		GroupLayout layout = new GroupLayout(contentPanel);
		contentPanel.setLayout(layout);

		// layout options
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		// set horizontal grouping
		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(upperActionPanel)
								.addComponent(queueTable)
								.addComponent(actionPanel)
								.addGroup(
										layout.createSequentialGroup()
												.addGroup(
														layout.createParallelGroup(
																GroupLayout.Alignment.LEADING)
																.addComponent(
																		studyInstruction)
																.addComponent(
																		selectFileInstruction)
																.addComponent(
																		datasetInstruction)
																.addGroup(
																		layout.createSequentialGroup()
																				.addGroup(
																						layout.createParallelGroup(
																								GroupLayout.Alignment.LEADING)
																								.addComponent(
																										studyLabel)
																								.addComponent(
																										selectFileLabel)
																								.addComponent(
																										datasetLabel))
																				// .addComponent(uploadFileButton))
																				.addGroup(
																						layout.createParallelGroup(
																								GroupLayout.Alignment.LEADING)
																								.addComponent(
																										studyChoice)
																								.addComponent(
																										fileNameField)
																								.addComponent(
																										datasetField))
																				.addGroup(
																						layout.createParallelGroup(
																								GroupLayout.Alignment.LEADING)
																								.addComponent(
																										studyRefreshButton)
																								.addComponent(
																										selectFileButton)))))));

		// set vertical grouping
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addComponent(studyInstruction)
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(studyLabel)
								.addComponent(studyChoice)
								.addComponent(studyRefreshButton))
				.addGap(20)
				.addComponent(selectFileInstruction)
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(selectFileLabel)
								.addComponent(fileNameField)
								.addComponent(selectFileButton))
				.addGap(20)
				.addComponent(datasetInstruction)
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(datasetLabel)
								.addComponent(datasetField)).addGap(20)
				.addComponent(upperActionPanel).addComponent(queueTable)
				.addComponent(actionPanel));

		// link the size of buttons/fields
		/*
		layout.linkSize(SwingConstants.HORIZONTAL, studyChoice, fileNameField,
				datasetField);
		layout.linkSize(SwingConstants.VERTICAL, studyChoice, fileNameField,
				datasetField);
		layout.linkSize(SwingConstants.HORIZONTAL, selectFileButton,
				studyRefreshButton);
		JLabel label = new JLabel("Please Wait... ");
		label.setFont(fontArial14);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setSize(275, 50);

		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(label);
		panel.add(progressBar);

		
		systemBusyDialog = new JDialog(this, "In process", false);
		systemBusyDialog.getContentPane().add(panel);
		systemBusyDialog.setSize(300, 100);
		systemBusyDialog.setLocationRelativeTo(this);
		systemBusyDialog.pack();
		systemBusyDialog.setVisible(false);
*/
		
		systemBusyDialog = new JDialog(this, "In Progress", ModalityType.MODELESS);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.add(progressBar, BorderLayout.CENTER);
		panel.add(new JLabel("System is processing the request, please wait......"),
				BorderLayout.PAGE_START);
		panel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
		systemBusyDialog.add(panel);
		systemBusyDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		systemBusyDialog.setResizable(false);
		systemBusyDialog.pack();
		systemBusyDialog.setLocationRelativeTo(this);
		systemBusyDialog.setVisible(true);
		return contentPanel;
	}
	private class StartUploadActionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			//verify input informaiton & confirm the submission
			int confirmed = JOptionPane.showConfirmDialog(frame,
					"<html>While data is being uploaded, please do not edit the study and dataset status, or close the upload tool. <br>Wait until you receive email notification that the submission is complete.<br><br></html>",
					"Warning: Submission times may vary.", JOptionPane.OK_CANCEL_OPTION);
			if(confirmed != JOptionPane.OK_OPTION){
				return;
			}
			//Step 1: Preparing Dataset submission: 
			try{
				//systemBusyDialog.setVisible(true); 
				logger.debug("Start the uploading");
				setUploadingMode(true);
				SubmissionQCStatus ticketStatus = UploadManagerController.getSubmissionQCStatus(fileNameField.getText(), studySelect.getSelectedItem().toString(), datasetField.getText(), version, serverLocation); 
				//stop if data is invalid
				if (ticketStatus != SubmissionQCStatus.PASSED ){
					JOptionPane.showMessageDialog(rootPane, ticketStatus.getMessage(),
							ticketStatus.getCategory(), JOptionPane.ERROR_MESSAGE);
					setUploadingMode(false);
					//systemBusyDialog.setVisible(false); 
					return;
				}
			}
			// handle run-time unexpected error, 
			catch(Exception qce){ 
				//qce.printStackTrace();
				logger.error("Submission Preperation Failed: " + qce.getMessage());
				JOptionPane.showMessageDialog(rootPane,
						"An error has occured during validating submission ticket",
						"Submission Ticket Error", JOptionPane.ERROR_MESSAGE);
				setUploadingMode(false);
				//systemBusyDialog.setVisible(false); 
				return;
			}

			//Step 2: proceed to submit
			setProperty(LAST_STUDY, getSelectedStudy().getTitle());
			//Processing submission ticket with XML format to get valid dataset for upload 
			UploadManager.finalStudyName = new String( getSelectedStudy().getTitle());
			UploadManager.finalDatasetName = new String(getDatasetName());
			//systemBusyDialog.setVisible(true); 
			setUploadingMode(true);
			new UploadSwingWorker().execute();
		}
	}

	private class CancelUploadActionListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			Object[] options = { "Yes, Cancel Submission",
			"No, Continue Submission please" };
			int confirmed = JOptionPane
					.showOptionDialog(
							null,
							"You are about to cancel the submission of this dataset. The dataset will not be uploaded into \n"
									+ "the system. Are you sure you would like to proceed with cancelling the submission?\n\n",
									"Cancel Submission", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null, options,
									options[0]);
			if (confirmed == JOptionPane.OK_OPTION) {
				try {
					systemBusyDialog.setVisible(true); 
					logger.debug("Start the Cancelling the submission");
					setUploadingMode(false);
					cancelButton.setEnabled(false);
					new CancelUploadWorker(frame).execute();
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane
					.showMessageDialog(
							rootPane,
							"An error has occured during cancelling submission",
							"Submission Cancellation Error",
							JOptionPane.ERROR_MESSAGE);
					setUploadingMode(false);
				}
			}
		}
	}



	public static boolean isUploading(){
		return ISUPLOADING.get();
	}
	
	public static void setUploadingMode(boolean isUploading){
		uploadButton.setEnabled(!isUploading);
		cancelButton.setEnabled(isUploading);
		ISUPLOADING.set(isUploading);
	}

	
	public JDialog createWorkingPopUp(String title)
	{

		JLabel label = new JLabel("Please Wait... ");
		label.setFont(fontArial14);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setSize(275, 50);

		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(label);
		panel.add(progressBar);

		final JDialog dialog = new JDialog(this, title, false);
		dialog.getContentPane().add(panel);
		dialog.setSize(300, 100);
		dialog.setLocationRelativeTo(this);

		return dialog;
	}
	public static UploadTableModel getModel() {
		return (UploadTableModel) table.getModel();
		
	}


	public static JXTable getTable() {
		return table;
	}

}
