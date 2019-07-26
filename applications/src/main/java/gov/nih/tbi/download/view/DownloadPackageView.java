package gov.nih.tbi.download.view;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.commons.EulaAgreementWrapper;
import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.download.table.DownloadableRow;
import gov.nih.tbi.download.table.PackageRow;
import gov.nih.tbi.download.table.PackageTableModel;
import gov.nih.tbi.download.util.ByteComparator;
import gov.nih.tbi.download.util.CheckBoxHeader;
import gov.nih.tbi.download.util.FileCountComparator;
import gov.nih.tbi.download.util.ProgressBarRenderer;
import gov.nih.tbi.download.util.ProgressComparator;
import gov.nih.tbi.download.workers.DeleteWorker;
import gov.nih.tbi.download.workers.DownloadWorker;
import gov.nih.tbi.download.workers.RefreshWorker;
import gov.nih.tbi.download.workers.StopWorker;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.io.SftpClientManager;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTable;

/**
 * This class is used to display a macro-micro view of the download queue. This is achieved by having two different
 * tables containing information. The top portion of the frame contains the macro-view, which consists of information on
 * the broader download packages. The bottom portion contains the micro-view (see <code>DownloadFileView
 * </code>). This class mostly only contains UI elements and offloads work to threads and a controller class.
 * 
 * @author wangvg
 * 
 */
public class DownloadPackageView extends JFrame {

	private static final long serialVersionUID = -817352689678650717L;

	private JXTable packageTable;

	private PackageTableModel packageModel;

	private DownloadFileView microView;

	private JTextField fileNameField;

	private DownloadController control;

	private Properties props = new Properties();

	private int displayedRow;

	private JTextField selectedNumLabel;

	private JTextField totalNumLabel;

	private JTextField downloadSizeLabel;

	private JLabel downloadByteLabel;

	private JTextField downloadNumLabel;

	private JDialog systemBusyDialog;

	public static final long UI_UPDATE_INTERVAL = 500;

	public static final long UI_DOWNLOAD_COOL_DOWN = 1000; // wait time stopping a download so the user
															// does not overload the sftp connection

	private static final int URL_ARGUMENT_INDEX = 0;

	private static final int VERSION_ARGUMENT_INDEX = 1;

	private static final int USERNAME_ARGUMENT_INDEX = 2;

	private static final int USER_HASH_ARGUMENT_INDEX = 3;

	private static final int PASSWORD_HASH_ARGUMENT_INDEX = 4;

	private static final int ORGANIZATION_EMAIL_INDEX = 5;

	private static final int ARGS_LENGTH = 6;

	private final JButton downloadButton = new JButton("Start Download(s)");
	private final JButton stopButton = new JButton("Cancel Download(s)");
	private final JButton removeButton = new JButton("Remove Package");
	private final JButton refreshButton = new JButton("Refresh Queue");
	private final JButton selectFileButton = new JButton("Browse");

	public static final String PROPERTY_CONFIG_FILE_NAME = System.getProperty("user.home", "") + File.separator
			+ "brics.pref";
	public static final String DOWNLOAD_HOME_PROPERTY_NAME = "download.dir";

	public static void main(String[] args) throws MalformedURLException {

		if (args.length != ARGS_LENGTH) {
			JOptionPane.showMessageDialog(null,
					"This Download Manager Tool has an invalid number of arguments.  Please check "
							+ "for any errors and try again.", "Tool error", JOptionPane.ERROR_MESSAGE);
			System.exit(ERROR);
		}

		final String serverLocation = args[URL_ARGUMENT_INDEX];
		final String versionNumber = args[VERSION_ARGUMENT_INDEX];
		final String username = args[USERNAME_ARGUMENT_INDEX];
		String userHash = args[USER_HASH_ARGUMENT_INDEX];
		final String password = args[PASSWORD_HASH_ARGUMENT_INDEX];
		final String orgEmail = args[ORGANIZATION_EMAIL_INDEX];

		if (!HashMethods.validateServerHash(userHash, username)) {
			JOptionPane.showMessageDialog(null,
					"This Upload Manager Tool is corrupt.  Please relaunch the tool and try again.", "Tool error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(ERROR);
		}

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new DownloadPackageView(serverLocation, username, password, versionNumber, orgEmail);
			}
		});
	}


	public DownloadPackageView(String serverLocation, String userName, String password, String versionNumber,
			String orgEmail) {
		super();

		new EulaAgreementWrapper(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// threadMessage("EulaAgreementWrapper()");
				initializeUI();
				setVisible(true);
			}
		}, orgEmail);


		setTitle(ApplicationsConstants.APPLICATION_HEADER + " [" + versionNumber + "]");

		setIconImage(createImageIcon(ApplicationsConstants.FRAME_ICON, "Frame GIF").getImage());
		setVisible(false);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		packageModel = new PackageTableModel();
		packageModel.addTableModelListener(new TableModelListener() {
			// TODO: should avoid using table Change event
			@Override
			public void tableChanged(TableModelEvent e) {
				PackageTableModel model = (PackageTableModel) e.getSource();
				if (!model.isCheckboxEditable()) {
					return;
				}
				// -1 refers to the automated update other than user clicks.
				// if (e.getColumn() == PackageTableModel.CHECKBOX_COLUMN || e.getType() != TableModelEvent.UPDATE) {
				if (e.getColumn() == PackageTableModel.CHECKBOX_COLUMN || e.getColumn() < 0) {
					refreshButtonsAndStatistics();
					setButtonsStatus(model.getSelectedNumber() > 0);
				}
			}
		});

		// Override the tooltip text method so that it will only display for
		// file name
		packageTable = new JXTable(packageModel) {

			private static final long serialVersionUID = 5000520070751947618L;

			public String getToolTipText(MouseEvent e) {
				int column = columnAtPoint(e.getPoint());
				if (column == PackageTableModel.NAME_COLUMN) {
					int row = rowAtPoint(e.getPoint());
					if (row > -1 && row < getRowCount()) {
						int modelRow = convertRowIndexToModel(row);
						return (String) packageModel.getValueAt(modelRow, column);
					}
				}
				return "";
			}
		};
		packageTable.setAutoCreateColumnsFromModel(false);

		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				// Display confirm dialog
				int confirmed =
						JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Confirm Quit",
								JOptionPane.YES_NO_OPTION);

				// Close if user confirmed
				if (confirmed == JOptionPane.YES_OPTION) {
					// Close dt and clean up
					dispose();
					SftpClientManager.closeAll();
					System.exit(0);
				}
			}
		});
		control = new DownloadController(serverLocation, userName, password);
	}

	/**
	 * Method to go through all the files in the download queue to check if they are already in the provided download
	 * location. Any files already downloaded previously will be marked as complete and unselected by default. The
	 * progress bars will be set to 100% for futher visual information. Also, any package that has already been fully
	 * downloaded will also be marked as complete and progress bar set to 100%.
	 */

	public void checkHostForFiles() {
		String baseDir = fileNameField.getText();

		for (PackageRow pkg : packageModel.getRows()) {
			// package is a keyword, so use pkg instead
			String packageName = pkg.getPackage().getName();
			String dirStr = baseDir + File.separator + packageName + File.separator;


			boolean allSelected = true;
			for (DownloadableRow download : pkg.getFiles()) {
				UserFile file = download.getDownloadable().getUserFile();
				File hostFile = new File(dirStr + file.getName());
				if (hostFile.exists()) {
					long fileSize = file.getSize();
					long hostFileSize = hostFile.length();
					// File must be present AND be the correct size to be
					// auto-excluded. Easier than checksum, but maybe less
					// accurate
					if (fileSize == hostFileSize) {
						download.setCompleted(true);
						download.setSelected(false);
					} else {
						download.setCompleted(false);
						allSelected = false;
					}
				} else {
					download.setCompleted(false);
					download.setProgress(0);
					allSelected = false;
				}
			}

			// If the entire package has already been downloaded,
			// change the package status to COMPLETED to visually
			// indicate this
			if (allSelected) {
				pkg.setStatus(DownloadStatus.COMPLETED);
			}
		}

		packageModel.fireTableDataChanged();
		microView.getModel().fireTableDataChanged();
		packageTable.getTableHeader().repaint();
	}

	public int getDisplayedRowIndex() {
		return displayedRow;
	}

	public void setDisplayedRowIndex(int index) {
		displayedRow = index;
	}

	public DownloadFileView getMicroView() {
		return microView;
	}

	public JDialog getLoadingDialog() {
		return systemBusyDialog;
	}

	public PackageTableModel getModel() {
		return packageModel;
	}

	public JXTable getTable() {
		return packageTable;
	}

	/**
	 * Used to display the currently highlighted package in the micro view
	 * 
	 * @param row
	 */
	public void openEditor(int row) {
		displayedRow = row;
		microView.clearTable();
		microView.populateTable(packageModel.get(row));
	}

	/**
	 * Recalculates the summary statistics and button visibility in the table for the display.
	 */
	public void refreshButtonsAndStatistics() {
		int count = 0;
		for (PackageRow pkg : packageModel.getRows()) {
			count += pkg.getSelectedNumber();
		}

		selectedNumLabel.setText(String.valueOf(packageModel.getSelectedNumber()));
		totalNumLabel.setText(String.valueOf(packageModel.getRows().size()));

		downloadNumLabel.setText(String.valueOf(count));

		String sizeStr = packageModel.getSelectedSize();
		String[] split = sizeStr.split(" ");
		if (split.length > 1) {
			downloadSizeLabel.setText(split[0]);
			downloadByteLabel.setText(split[1]);
		} else {
			downloadSizeLabel.setText("0");
			downloadByteLabel.setText("B");
		}
	}

	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 */
	private ImageIcon createImageIcon(String path, String description) {

		java.net.URL imgURL = DownloadPackageView.class.getClassLoader().getResource(path);
		if (imgURL != null) {
			ImageIcon imageIcon = new ImageIcon(imgURL, description);
			return imageIcon;
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	@SuppressWarnings("serial")
	private void initializeUI() {

		// Moved some application initialization stuff here to get it
		// out of the class instantiation and into the action after the
		// EULA is accepted

		displayedRow = -1;

		microView = new DownloadFileView(this);


		// Load the preferences file if available
		readProperties();

		// We are going to use a split pane to show both the macro and micro views
		// in one frame. Top pane is for macro and bottom pane is for micro
		JPanel topSplit = new JPanel();

		topSplit.setLayout(new BoxLayout(topSplit, BoxLayout.Y_AXIS));

		// Top panel contains the directory chooser, labels
		// showing how many packages are selected, and how
		// many packages there are in total
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(null);

		JPanel directoryPanel = new JPanel();
		topPanel.add(directoryPanel, BorderLayout.WEST);

		directoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel selectFileLabel = new JLabel("Save To: ");
		selectFileLabel.setHorizontalAlignment(JLabel.RIGHT);
		selectFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// threadMessage("SaveToDirCall()");
				selectFileButton.setEnabled(false);
				selectDirectory();
				selectFileButton.setEnabled(true);
			}
		});

		fileNameField = new JTextField(30);
		fileNameField.setEditable(false);

		if (props != null)
			fileNameField.setText(props.getProperty(DOWNLOAD_HOME_PROPERTY_NAME,
					System.getProperty("user.home", System.getProperty("user.dir"))));

		directoryPanel.add(selectFileLabel);
		directoryPanel.add(fileNameField);
		directoryPanel.add(selectFileButton);

		// The panel that contains the information keeping track
		// of how many packages are selected, and how many packages
		// there are in total
		JPanel selectedPanel = new JPanel(new FlowLayout());
		JLabel selectedLabel = new JLabel("Packages Selected: ");
		selectedNumLabel = new JTextField() {
			@Override
			public void setBorder(Border border) {};
		};
		selectedNumLabel.setColumns(4);
		selectedNumLabel.setText("0");
		selectedNumLabel.setEditable(false);
		selectedNumLabel.setHorizontalAlignment(JTextField.RIGHT);
		selectedNumLabel.setFont(selectedLabel.getFont());

		int buttonHeight = selectFileButton.getPreferredSize().height;
		int labelWidth = selectedNumLabel.getPreferredSize().width;
		selectedNumLabel.setPreferredSize(new Dimension(labelWidth, buttonHeight));

		JLabel ofLabel = new JLabel("of");
		totalNumLabel = new JTextField() {
			@Override
			public void setBorder(Border border) {};
		};
		totalNumLabel.setColumns(4);
		totalNumLabel.setText("0");
		totalNumLabel.setEditable(false);
		totalNumLabel.setHorizontalAlignment(JTextField.LEFT);
		totalNumLabel.setFont(ofLabel.getFont());

		selectedPanel.add(selectedLabel);
		selectedPanel.add(selectedNumLabel);
		selectedPanel.add(ofLabel);
		selectedPanel.add(totalNumLabel);

		topPanel.add(selectedPanel, BorderLayout.EAST);

		// Start initializing the table containing
		// all the packages

		// Clicking on a row will display the selected package's
		// contents in the micro view
		packageTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// threadMessage("packageTableMouseReleased()");

				int row = packageTable.getSelectedRow();
				int column = packageTable.columnAtPoint(e.getPoint());
				if (column == 0) {
					int modelRow = packageTable.convertRowIndexToModel(row);
					PackageRow pkg = packageModel.get(modelRow);
					pkg.selectAll(pkg.isSelected());
					packageTable.getTableHeader().repaint();
					refreshButtonsAndStatistics();
				}
				if (row > -1 && row < packageModel.getRowCount()) {
					int modelRow = packageTable.convertRowIndexToModel(row);
					openEditor(modelRow);
				}
			}
		});
		packageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableRowSorter<PackageTableModel> tableRowSorter = new TableRowSorter<PackageTableModel>(packageModel);
		tableRowSorter.setSortable(PackageTableModel.CHECKBOX_COLUMN, false);
		tableRowSorter.setComparator(PackageTableModel.FILES_COLUMN, new FileCountComparator());
		tableRowSorter.setComparator(PackageTableModel.SIZE_COLUMN, new ByteComparator());
		tableRowSorter.setComparator(PackageTableModel.PROGRESS_COLUMN, new ProgressComparator());

		packageTable.setRowSorter(tableRowSorter);

		TableColumnModel columnModel = packageTable.getColumnModel();
		columnModel.getColumn(PackageTableModel.PROGRESS_COLUMN).setCellRenderer(new ProgressBarRenderer());

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		columnModel.getColumn(PackageTableModel.DATE_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(PackageTableModel.STATUS_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(PackageTableModel.FILES_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(PackageTableModel.SIZE_COLUMN).setCellRenderer(centerRenderer);

		packageTable.getTableHeader().setReorderingAllowed(false);
		packageTable.setFillsViewportHeight(true);

		for (int i = 0; i < packageModel.getColumnCount(); i++) {
			TableColumn column = packageTable.getColumnModel().getColumn(i);
			if (i == PackageTableModel.NAME_COLUMN) {
				column.setPreferredWidth(240);
			} else if (i == PackageTableModel.PROGRESS_COLUMN) {
				column.setPreferredWidth(50);
			} else if (i == PackageTableModel.CHECKBOX_COLUMN) {
				column.setMaxWidth(35);
				column.setMinWidth(35);
			} else if (i == PackageTableModel.FILES_COLUMN || i == PackageTableModel.SIZE_COLUMN) {
				column.setPreferredWidth(30);
			} else if (i == PackageTableModel.STATUS_COLUMN) {
				column.setPreferredWidth(60);
			} else {
				column.setPreferredWidth(60);
			}
		}

		final DownloadPackageView view = this;
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		stopButton.setEnabled(false);
		downloadButton.setEnabled(false);
		removeButton.setEnabled(false);

		TableColumn tc = packageTable.getColumnModel().getColumn(PackageTableModel.CHECKBOX_COLUMN);
		tc.setCellEditor(packageTable.getCellEditor());
		tc.setCellRenderer(packageTable.getDefaultRenderer(Boolean.class));
		tc.setHeaderRenderer(new CheckBoxHeader(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// threadMessage("checkboxTopHeaderActionP()");

				if (e.getSource() instanceof JCheckBox) {
					JCheckBox box = (JCheckBox) e.getSource();
					Boolean checked = box.isSelected();
					for (int x = 0; x < packageTable.getRowCount(); x++) {
						packageTable.setValueAt(new Boolean(checked), x, PackageTableModel.CHECKBOX_COLUMN);
						int modelRow = packageTable.convertRowIndexToModel(x);
						packageModel.get(modelRow).selectAll(checked);
					}
					microView.getModel().fireTableDataChanged();
					microView.getTable().getTableHeader().repaint();
					view.refreshButtonsAndStatistics();
					packageModel.fireTableDataChanged();
					packageTable.getTableHeader().repaint();
				}
			}
		}));

		JPanel tablePanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(packageTable);
		scrollPane.setBorder(new TitledBorder("Download Packages"));

		tablePanel.add(scrollPane, BorderLayout.CENTER);


		JPanel downloadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel sizeLabel = new JLabel("Total Selected: ");
		// This contains the size suffix (B, KB, MB, GB, etc)
		downloadByteLabel = new JLabel("B");

		downloadSizeLabel = new JTextField() {
			@Override
			public void setBorder(Border border) {};
		};

		downloadNumLabel = new JTextField() {
			@Override
			public void setBorder(Border border) {};
		};

		downloadNumLabel.setText("0");
		downloadNumLabel.setColumns(4);
		downloadNumLabel.setHorizontalAlignment(JTextField.RIGHT);
		downloadNumLabel.setEditable(false);
		downloadNumLabel.setFont(downloadByteLabel.getFont());

		JLabel fileLabel = new JLabel("Files");
		fileLabel.setBorder(new EmptyBorder(0, 0, 0, 5));

		downloadSizeLabel.setText("0");
		downloadSizeLabel.setColumns(4);
		downloadSizeLabel.setHorizontalAlignment(JTextField.RIGHT);
		downloadSizeLabel.setEditable(false);
		downloadSizeLabel.setFont(downloadByteLabel.getFont());

		downloadPanel.add(sizeLabel);
		downloadPanel.add(downloadNumLabel);
		downloadPanel.add(fileLabel);
		downloadPanel.add(downloadSizeLabel);
		downloadPanel.add(downloadByteLabel);


		downloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// threadMessage("downloadButtonActionP()");
				List<Integer> packageRowIndices;
				if (!preDownloadCheck()) {
					return;
				}
				// Only download if there are actually files selected
				if (packageModel.getSelectedNumber() > 0) {
					packageRowIndices = setupDownloadRowStatus(packageTable, packageModel);
					setButtonsStatusRunning(true);
					new DownloadWorker(control, view, fileNameField.getText()).execute();

				}

			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// threadMessage("stopButtonActionP()");
				view.getLoadingDialog().setVisible(true);
				new StopWorker(packageModel, view).execute();
			}
		});

		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// threadMessage("RemoveButtonActionP()");
				ArrayList<Long> ids = new ArrayList<Long>();
				Iterator<PackageRow> iterator = packageModel.getRows().iterator();
				while (iterator.hasNext()) {
					PackageRow pkg = iterator.next();
					if (pkg.isSelected()) {
						for (DownloadableRow row : pkg.getFiles()) {
							ids.add(row.getDownloadable().getId());
						}
					}
				}
				if (ids.size() > 0) {
					int response =
							JOptionPane.showConfirmDialog(rootPane,
									"Are you sure you want to delete selected package(s)?", "Delete Package(s)",
									JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.YES_OPTION) {
						// reset iterator
						iterator = packageModel.getRows().iterator();
						while (iterator.hasNext()) {
							PackageRow pkg = iterator.next();
							if (pkg.isSelected()) {
								if (pkg == microView.getDisplayedPackage()) {
									microView.clearTable();
									view.setDisplayedRowIndex(-1);
								}
								iterator.remove();
							}
						}
						new DeleteWorker(control, view, ids).execute();

						packageModel.fireTableDataChanged();
						refreshButtonsAndStatistics();
					}
				}
			}
		});

		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				refreshButton.setEnabled(false);
				view.getLoadingDialog().setVisible(true);
				// threadMessage("refreshButtonActionP()");
				new RefreshWorker(control, view).execute();
			}
		});

		Dimension buttonDim = stopButton.getPreferredSize();

		downloadButton.setPreferredSize(buttonDim);
		stopButton.setPreferredSize(buttonDim);
		removeButton.setPreferredSize(buttonDim);
		refreshButton.setPreferredSize(buttonDim);

		buttonPanel.add(downloadButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(refreshButton);

		topSplit.add(topPanel);
		topSplit.add(tablePanel);
		topSplit.add(downloadPanel);
		topSplit.add(buttonPanel);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(topSplit);

		splitPane.setBottomComponent(microView);

		getContentPane().add(splitPane);

		// refreshNumLabels();
		refreshButtonsAndStatistics();

		// To make sure that the size of the application doesn't
		// start too large for smaller screens
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		int height = Math.min(screenDim.height - 50, 700);
		int width = Math.min(screenDim.width - 50, 1000);
		setSize(new Dimension(width, height));
		setVisible(true);
		splitPane.setDividerLocation(0.4);
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
		new RefreshWorker(control, view).execute();
	}

	/**
	 * This is functionality to prompt the user in the event that the user selects files that have already been
	 * completed. If he or she does not wish to download the files again then the rows will be deselected.
	 */
	private boolean preDownloadCheck() {

		boolean check = false;
		// threadMessage("PredownloadCheck()");

		for (PackageRow pkg : packageModel.getRows()) {
			if (pkg.isSelected()) {
				for (DownloadableRow row : pkg.getFiles()) {
					if (row.isSelected() && row.isCompleted()) {
						check = true;
						break;
					}
				}
			}
		}

		if (check) {
			int response =
					JOptionPane.showConfirmDialog(rootPane,
							"Some files have already been downloaded. Would you like to download them again?",
							"Download", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION) {
				for (PackageRow pkg : packageModel.getRows()) {
					if (pkg.isSelected()) {
						for (DownloadableRow row : pkg.getFiles()) {
							if (row.isCompleted()) {
								// Deselect the row so that it won't be downloaded again
								row.setSelected(false);
							}
						}
						microView.getTable().getTableHeader().repaint();
					}
					if (pkg.getSelectedNumber() == 0) {
						pkg.setSelected(false);
						// packageModel.fireTableCellUpdated(packageModel.indexOf(pkg),
						// PackageTableModel.CHECKBOX_COLUMN);
						// several columns need to be updated.
						packageModel.fireTableRowsUpdated(packageModel.indexOf(pkg), packageModel.indexOf(pkg));
					}
				}
				microView.refreshNumLabels();
			} else if (response == JOptionPane.CANCEL_OPTION) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates a JFileChooser which is used to set the 'Save To: ' field
	 */
	private void selectDirectory() {

		JFileChooser chooser = new JFileChooser(fileNameField.getText());

		chooser.setDialogTitle("Select a directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnVal = chooser.showOpenDialog(getParent());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String selection = chooser.getSelectedFile().getAbsolutePath();
			fileNameField.setText(selection);
			if (props != null) {
				props.setProperty(DOWNLOAD_HOME_PROPERTY_NAME, selection);
				FileOutputStream writer = null;
				try {
					writer = new FileOutputStream(PROPERTY_CONFIG_FILE_NAME);
					props.store(writer, "--No Comment--");
					writer.close();
				} catch (FileNotFoundException e) {
					System.err.println("Cannot make new properties file");
				} catch (IOException e) {
					System.err.println("Cannot write to properties file");
				} finally {
					try {
						writer.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						System.err.println("Could not close file stream");
					}
				}
			}
			checkHostForFiles();
		}
	}

	public void readProperties() {
		FileInputStream input = null;
		try {
			File propertyConfigFile = new File(PROPERTY_CONFIG_FILE_NAME);
			if (propertyConfigFile.exists()) {
				input = new FileInputStream(propertyConfigFile);
				props.load(input);
				input.close();
			}
		} catch (FileNotFoundException e1) {// used during initialization
			if (props.isEmpty()) {
				props.setProperty(PROPERTY_CONFIG_FILE_NAME, System.getProperty("user.home", ""));
				writeOutDefaultProperties();
			} else {
				System.err.println("Could not find preferences file");
			}
			e1.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not open preferences file");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("error: " + e.getLocalizedMessage());
		}
 finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e1) {
					System.err.println("Could not close file stream");
					e1.printStackTrace();
				}
			}
		}
	}

	private void writeOutDefaultProperties() {
		try {
			FileOutputStream writer = new FileOutputStream(PROPERTY_CONFIG_FILE_NAME);
			props.store(writer, "--No Comment--");
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("Cannot make new properties file");
		} catch (IOException e) {
			System.err.println("Cannot write to properties file");
		}
	}

	/*
	 * Function to set button status when actions running in multi threads: downloading, removing, refreshing
	 */
	public void setButtonsStatusRunning(boolean isDownloading) {
		// threadMessage("setButtonStatusRunning()");
		downloadButton.setEnabled(false);
		removeButton.setEnabled(false);
		refreshButton.setEnabled(false);
		selectFileButton.setEnabled(false);
		// set true when downloading is in progress, otherwise set it false
		stopButton.setEnabled(isDownloading);
		packageModel.setCheckboxEditable(!isDownloading);
		this.getMicroView().getFileTableModel().setCheckboxEditable(!isDownloading);
	}

	/*
	 * Function to set button status according to if any packages are selected
	 */
	public void setButtonsStatus(boolean hasSelection) {
		// threadMessage("setButtonStatus()");
		downloadButton.setEnabled(hasSelection);
		removeButton.setEnabled(hasSelection);
		stopButton.setEnabled(false);
		refreshButton.setEnabled(true);
		selectFileButton.setEnabled(true);
	}

	public List<Integer> setupDownloadRowStatus(JTable table, PackageTableModel downloadModel) {
		List<Integer> indices = new ArrayList<Integer>();
		// threadMessage("setupDownloadRowStatus()");
		for (int i = 0; i < table.getRowCount(); i++) {
			int index = table.convertRowIndexToModel(i);
			PackageRow pkg = downloadModel.get(index);
			if (pkg.isSelected()) {
				DownloadStatus status = pkg.getStatus();

				if (status != DownloadStatus.PENDING || status != DownloadStatus.IN_PROGRESS
						|| status != DownloadStatus.ERROR) {
					indices.add(index);
					downloadModel.get(index).setStatus(DownloadStatus.PENDING);
					downloadModel.fireTableCellUpdated(index, PackageTableModel.STATUS_COLUMN);
				}
			}
		}
		return indices;
	}

	public static void threadMessage(String msg) {

		if (EventQueue.isDispatchThread()) {
			System.out.println(msg + " is EDT thread:  " + Thread.currentThread().getId());
		} else {
			System.out.println("~~~~ NON-EDT thread:" + msg + " with name " + Thread.currentThread().getName());
		}
	}

	private void preparePackages(PackageTableModel model, List<Integer> indices) {
		for (final int index : indices) {
			final PackageRow pkg = model.get(index);
			// prepare pkg and marked files for download
			pkg.clearDownloads();
			// we're starting download, so change current row to 'in progress'
			DownloadController.changeRowStatus(model, index, DownloadStatus.IN_PROGRESS);
			for (DownloadableRow row : pkg.getFiles()) {
				if (row.isSelected()) {
					pkg.setDownloadableStatus(row, DownloadStatus.PENDING);
					pkg.addDownload(row);
					row.setCompleted(false);
				}
			}
		}
	}

	public void enableRefreshButton(boolean status) {
		this.refreshButton.setEnabled(status);
	}
}
