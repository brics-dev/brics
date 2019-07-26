package gov.nih.tbi.repository.view;

import gov.nih.tbi.repository.UploadManagerController;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.table.DatasetTableColumn;
import gov.nih.tbi.repository.table.DatasetTableColumnFactory;
import gov.nih.tbi.repository.table.DatasetTableModel;
import gov.nih.tbi.repository.table.DatasetUploadProgressComparator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTable;

public class ReloadDatasetView extends JDialog {

	private static final long serialVersionUID = 8094802847607368378L;

	private static final String TITLE = "Load Pending Submissions";
	private static final String INSTRUCTION = "Please select the submissions you would like to upload";

	private DatasetTableModel tableModel;

	public ReloadDatasetView(Frame owner, List<Dataset> datasets) {
		super(owner, true);
		this.setTitle(TITLE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(screenSize.width * 1 / 3, screenSize.height * 1 / 3));
		
		JButton uploadButton = new JButton("Upload Selected Submissions");
		tableModel = new DatasetTableModel(datasets, uploadButton);
		final JXTable table = new JXTable();
		table.getTableHeader().setReorderingAllowed(false);
		table.setColumnFactory(new DatasetTableColumnFactory());
		table.packAll();
		table.setModel(tableModel);
		table.setFillsViewportHeight(true);

		// turn off sorting on checkbox column
		TableRowSorter<DatasetTableModel> tableRowSorter = new TableRowSorter<DatasetTableModel>(tableModel);
		tableRowSorter.setSortable(DatasetTableColumn.CHECK_BOX.getColumnIndex(), false);
		tableRowSorter.setComparator(DatasetTableColumn.PROGRESS.getColumnIndex(),
				new DatasetUploadProgressComparator());
		table.setRowSorter(tableRowSorter);


		JPanel wrapperPanel = new JPanel(new BorderLayout(10, 10));
		this.add(wrapperPanel);
		wrapperPanel.add(new JLabel(INSTRUCTION), BorderLayout.NORTH);
		wrapperPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wrapperPanel.add(buttonPanel, BorderLayout.SOUTH);
		uploadButton.setEnabled(false);
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(uploadButton);
		buttonPanel.add(cancelButton);

		uploadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
				List<Dataset> selectedDatasets = tableModel.getSelectedDatasetIds();
				UploadManagerController.addToQueue(selectedDatasets);
				UploadManagerController.doUpload();
			}
		});

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
	}
}
