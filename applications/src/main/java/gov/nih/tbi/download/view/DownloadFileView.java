package gov.nih.tbi.download.view;

import gov.nih.tbi.download.table.DownloadableRow;
import gov.nih.tbi.download.table.FileTableModel;
import gov.nih.tbi.download.table.PackageRow;
import gov.nih.tbi.download.util.ByteComparator;
import gov.nih.tbi.download.util.CheckBoxHeader;
import gov.nih.tbi.download.util.ProgressBarRenderer;
import gov.nih.tbi.download.util.ProgressComparator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.RowFilter;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTable;

/**
 * This class is used to create a panel that contains information regarding a highlighted download package. It also acts
 * as a psuedo-control component to allow users to manipulate the download queue at the file level.
 * 
 * @author wangvg
 * 
 */
public class DownloadFileView extends JPanel {

	private static final long serialVersionUID = -2649071271955583424L;

	private PackageRow displayedPackage;

	private FileTableModel fileModel;

	private JTextField packageField;

	private DownloadPackageView parentView;

	private JTextField selectedNumLabel;

	private JXTable fileTable;

	private JTextField totalNumLabel;
	private JTextField filterText;
	TableRowSorter<FileTableModel> tableRowSorter;
	//RowFilter<FileTableModel, Object>  //, textFilter;
	RowFilter<FileTableModel, Integer> typeFilter, textFilter;
	List<RowFilter<FileTableModel, Integer>> filters;

	public DownloadFileView(DownloadPackageView macroView) {
		super();
		parentView = macroView;
		fileModel = new FileTableModel();
		// DownloadPackageView.threadMessage("fileViewConstructor()");

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(buildTopPanel());
		add(buildTablePane());
		add(buildfilterPanel());
	}

	public void clearTable() {
		// parentView.threadMessage("fileViewClearTable()");
		fileModel.clearAll();
		if (displayedPackage != null)
			displayedPackage.setChangeListener(null);
		displayedPackage = null;

		refreshNumLabels();
		fileTable.getTableHeader().repaint();
	}

	public PackageRow getDisplayedPackage() {
		return displayedPackage;
	}

	public FileTableModel getModel() {
		return fileModel;
	}

	public JTable getTable() {
		return fileTable;
	}

	/**
	 * Populates the micro view based on the given package. Also adds a change listener to the displayed package so that
	 * changes are properly propagated by the UI from the controller, which is naive to the micro view.
	 * 
	 * @param row
	 */
	public void populateTable(PackageRow pkg) {
		// parentView.threadMessage("fileViewPolulateTable()");
		displayedPackage = pkg;
		ArrayList<DownloadableRow> files = pkg.getFiles();
		String name = pkg.getPackage().getName();
		packageField.setText(name);
		fileModel.setRows(files);
		fileModel.fireTableDataChanged();
		fileTable.getTableHeader().repaint();

		pkg.setChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// DownloadPackageView.threadMessage("fileViewPackageRowChangeLister()");
				Object src = e.getSource();
				if (src instanceof DownloadableRow) {
					DownloadableRow row = (DownloadableRow) src;
					int rowNum = fileModel.indexOf(row);
					fileModel.fireTableRowsUpdated(rowNum, rowNum);
            	}else if(src instanceof PackageRow){
					newTypeFilter();
            		fileModel.fireTableDataChanged();
            		refreshNumLabels();
            	}
			}
		});

		refreshNumLabels();
	}

	private JPanel buildTablePane() {
		// Override the tooltip text method so that it will only display for
		// file name and study name
		fileTable = new JXTable(fileModel) {

			private static final long serialVersionUID = 5000520070751947618L;

			public String getToolTipText(MouseEvent e) {
				int column = columnAtPoint(e.getPoint());

				if (column == FileTableModel.NAME_COLUMN || column == FileTableModel.STUDY_COLUMN) {
					int row = rowAtPoint(e.getPoint());
					if (row > -1 && row < getRowCount()) {
						int modelRow = convertRowIndexToModel(row);
						return (String) fileModel.getValueAt(modelRow, column);
					}
				}
				return "";
			}
		};

		// In this version, when you shift-click in the checkbox column, any items
		// highlighted by the shift-click will be selected. However, if all the items
		// highlighted are already selected, the items will instead become deselected.
		fileTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int column = fileTable.columnAtPoint(e.getPoint());
				// parentView.threadMessage("fileViewTableMouseAdaptorReleaseListener()");

				if (column == FileTableModel.CHECKBOX_COLUMN && fileModel.isCheckboxEditable()) {
					fileTable.getTableHeader().repaint();
					refreshNumLabels();

					if (displayedPackage.getSelectedNumber() > 0) {
						displayedPackage.setSelected(true);
					} else {
						displayedPackage.setSelected(false);
					}
					parentView.getModel().fireTableRowsUpdated(parentView.getDisplayedRowIndex(),
							parentView.getDisplayedRowIndex());
					// parentView.getModel().fireTableCellUpdated(parentView.getDisplayedRowIndex(),
					// PackageTableModel.CHECKBOX_COLUMN);
					// parentView.refreshButtonsAndStatistics();
				}
			}
		});
        fileTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
				// parentView.threadMessage("fileViewAnotherMouseEventListener()");
                    int column = fileTable.columnAtPoint(e.getPoint());

				/*
				 * if(column == FileTableModel.CHECKBOX_COLUMN && e.isShiftDown()){ //int row = table.getSelectedRow();
				 * int[] selected = fileTable.getSelectedRows(); int row = fileTable.rowAtPoint(e.getPoint());
				 * 
				 * boolean allSelected = true; for(int i=0;i<selected.length;i++){ int index =
				 * fileTable.convertRowIndexToModel(selected[i]); if(selected[i] == row){
				 * fileModel.get(index).setSelected(!fileModel.get(index).isSelected()); }
				 * if(!fileModel.get(index).isSelected()){ allSelected = false; break; } } for(int
				 * i=0;i<selected.length;i++){ int index = fileTable.convertRowIndexToModel(selected[i]);
				 * fileModel.get(index).setSelected(!allSelected); } refreshNumLabels(); }
				 */
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
    });

		tableRowSorter = new TableRowSorter<FileTableModel>(fileModel);
		tableRowSorter.setSortable(FileTableModel.CHECKBOX_COLUMN, false);
		tableRowSorter.setComparator(FileTableModel.SIZE_COLUMN, new ByteComparator());
		tableRowSorter.setComparator(FileTableModel.PROGRESS_COLUMN, new ProgressComparator());

		fileTable.setRowSorter(tableRowSorter);

		TableColumnModel columnModel = fileTable.getColumnModel();
		columnModel.getColumn(FileTableModel.PROGRESS_COLUMN).setCellRenderer(new ProgressBarRenderer());

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		columnModel.getColumn(FileTableModel.TYPE_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(FileTableModel.STATUS_COLUMN).setCellRenderer(centerRenderer);
		columnModel.getColumn(FileTableModel.SIZE_COLUMN).setCellRenderer(centerRenderer);

		fileTable.getTableHeader().setReorderingAllowed(false);
		fileTable.setFillsViewportHeight(true);

		TableColumn column = null;
		for (int i = 0; i < fileModel.getColumnCount(); i++) {
			column = fileTable.getColumnModel().getColumn(i);
			if (i == FileTableModel.NAME_COLUMN) {
				column.setPreferredWidth(250);
			} else if (i == FileTableModel.STUDY_COLUMN) {
				column.setPreferredWidth(250);
			} else if (i == FileTableModel.CHECKBOX_COLUMN) {
				column.setMaxWidth(35);
				column.setMinWidth(35);
			} else if (i == FileTableModel.TYPE_COLUMN) {
				column.setPreferredWidth(120);
			} else if (i == FileTableModel.PROGRESS_COLUMN) {
				column.setPreferredWidth(70);
			} else if (i == FileTableModel.SIZE_COLUMN) {
				column.setPreferredWidth(50);
			} else {
				column.setPreferredWidth(100);
			}
		}

		final DownloadFileView view = this;
		
        fileModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
				// parentView.threadMessage("fileViewTableModelLister()");
				if (e.getColumn() == FileTableModel.CHECKBOX_COLUMN || e.getColumn() < 0) {
                            fileTable.getTableHeader().repaint();
                            refreshNumLabels();
                    }

            }

        });

		TableColumn tc = fileTable.getColumnModel().getColumn(FileTableModel.CHECKBOX_COLUMN);
		tc.setCellEditor(fileTable.getCellEditor());
		tc.setCellRenderer(fileTable.getDefaultRenderer(Boolean.class));
		tc.setHeaderRenderer(new CheckBoxHeader(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// parentView.threadMessage("fileViewTableHeaderCheckboxActionP()");
				if (!fileModel.isCheckboxEditable())
					return;

				if (displayedPackage != null && e.getSource() instanceof JCheckBox) {
					JCheckBox box = (JCheckBox) e.getSource();

					Boolean checked = box.isSelected();
					for (int x = 0; x < fileTable.getRowCount(); x++) {
						fileTable.setValueAt(new Boolean(checked), x, FileTableModel.CHECKBOX_COLUMN);
					}
					displayedPackage.setSelected(displayedPackage.getSelectedNumber() > 0);
					int displayedRow = parentView.getDisplayedRowIndex();
					parentView.getModel().fireTableRowsUpdated(displayedRow, displayedRow);
					parentView.refreshButtonsAndStatistics();
					// parentView.getTable().getTableHeader().repaint();
					view.refreshNumLabels();
				}
			}
		}));

		JPanel tablePanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(fileTable);
		scrollPane.setBorder(new TitledBorder("Package Contents"));

		tablePanel.add(scrollPane, BorderLayout.CENTER);

		return tablePanel;
	}

	/**
	 * Builds the top panel, which contains the package name the number of selected packages, and total number of
	 * packages
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	private JPanel buildTopPanel() {
		JPanel topPanel = new JPanel(new BorderLayout());

		JPanel packagePanel = new JPanel();
		packagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel fieldLabel = new JLabel("Package Name: ");
		packagePanel.add(fieldLabel);

		packageField = new JTextField() {
			@Override
			public void setBorder(Border border) {};
		};
		packageField.setColumns(50);
		packageField.setText("(Select a package)");
		packageField.setEditable(false);
		packageField.setHorizontalAlignment(JTextField.LEFT);
		packagePanel.add(packageField);

		topPanel.add(packagePanel, BorderLayout.WEST);

		JPanel selectedPanel = new JPanel();
		JLabel selectedLabel = new JLabel("Files Selected: ");

		selectedNumLabel = new JTextField() {
			@Override
			public void setBorder(Border border) {};
		};
		selectedNumLabel.setColumns(4);
		selectedNumLabel.setText("0");
		selectedNumLabel.setEditable(false);
		selectedNumLabel.setHorizontalAlignment(JTextField.RIGHT);
		selectedNumLabel.setFont(selectedLabel.getFont());

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

		return topPanel;
	}

	private JPanel buildfilterPanel(){
		// JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// JPanel filterPanel = new JPanel(new BorderLayout());
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
		filterPanel.setBorder(BorderFactory.createTitledBorder("Package File Filter"));
		// TODO use setLayout(new FlowLayout(FlowLayout.LEFT));
		// This is the button that shows the dropdown menu
		// to filter out rows
		//JButton popupButton = new PopupButton("Files", parentView, null);
		final JButton fileButton = new JButton("File Types");

		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JLabel textLabel = new JLabel("Filter Text: ", SwingConstants.TRAILING);

		filterText = new JTextField();
		filterText.setColumns(20);
		filterText.setHorizontalAlignment(JTextField.LEFT);

		filterText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				newTextFilter();
			}

			public void insertUpdate(DocumentEvent e) {
				newTextFilter();
			}

			public void removeUpdate(DocumentEvent e) {
				newTextFilter();
			}
		});
		textLabel.setLabelFor(filterText);
		textPanel.add(textLabel);
		textPanel.add(filterText);
		Dimension dim = fileButton.getPreferredSize();
		dim.width *= 2;
		fileButton.setPreferredSize(dim);
		filterPanel.add(fileButton);
		filterPanel.add(textPanel);

		fileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                        if(displayedPackage != null)
                                buildFileFilterPopup(fileButton);
                }
        });

		return filterPanel;
	}
    private void buildFileFilterPopup(final Component parent) {

        ScrollablePanel mainPanel = new ScrollablePanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JCheckBox[] fileTypes = displayedPackage.getFileTypes();

        for (int i = 0; i < fileTypes.length; i++) {
                mainPanel.add(fileTypes[i]);
        }
        
        if (fileTypes.length > 0)
                mainPanel.setIncrement(fileTypes[0].getPreferredSize().height);

        final JScrollPane pane = new JScrollPane(mainPanel);

        final Popup popup =
                        PopupFactory.getSharedInstance().getPopup(parent, pane, parent.getLocationOnScreen().x,
                                        parent.getLocationOnScreen().y + parent.getPreferredSize().height);

        parent.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                        popup.show();
                }

                @Override
                public void focusLost(FocusEvent e) {
				popup.hide();
                    parent.removeFocusListener(this);
                }
        });

        popup.show();
    }

	public void refreshNumLabels() {
		// parentView.threadMessage("fileViewRefreshNumLabels()");
		if (displayedPackage == null) {
			selectedNumLabel.setText("0");
			totalNumLabel.setText("0");
			filterText.setText("");
			packageField.setText("(Select a package)");
		} else {
			selectedNumLabel.setText(String.valueOf(displayedPackage.getSelectedNumber()));
			totalNumLabel.setText(String.valueOf(displayedPackage.getFiles().size()));
		}
	}

	/**
	 * Text-based filtering
	 */
	private void newTextFilter() {
		// If current expression doesn't parse, don't update.
		// parentView.threadMessage("fileViewNewTextFilter()");
		try {
			textFilter = RowFilter.regexFilter("(?i)" + filterText.getText());
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		tableRowSorter.setRowFilter(combinedFilters());
	}

	// keying off the file extensions provided in the package
	public void newTypeFilter() {
		// parentView.threadMessage("fileViewNewTypeFilter()");

		typeFilter = new RowFilter<FileTableModel, Integer>() {
			@Override
			public boolean include(RowFilter.Entry<? extends FileTableModel, ? extends Integer> entry) {
				DownloadableRow row = entry.getModel().get(entry.getIdentifier());
				JCheckBox[] boxes = displayedPackage.getFileTypes();
				for (int i = 0; i < boxes.length; i++) {
					JCheckBox box = boxes[i];
					String boxExt = box.getText();
					if (box.isSelected() && boxExt.equalsIgnoreCase(row.getFileType()))
						return true;
				}
				return false;
			}
		};
		tableRowSorter.setRowFilter(combinedFilters());
	}

	// combine two type of filters with AND logic
	public RowFilter<FileTableModel, Integer> combinedFilters() {
		if (typeFilter != null) {
			if (textFilter != null) {
				filters = new ArrayList<RowFilter<FileTableModel, Integer>>();
				filters.add(typeFilter);
				filters.add(textFilter);
				return RowFilter.andFilter(filters);
			} else {
				return typeFilter;
			}
		}
		else{
			return textFilter;
		}
	}


	static class ScrollablePanel extends JPanel implements Scrollable {

        private static final long serialVersionUID = 6510963657039024598L;

        private int increment = 5;

        @Override
        public Dimension getPreferredScrollableViewportSize() {
                int width = getPreferredSize().width;
                int height = getPreferredSize().height;
                return new Dimension(width, height < 100 ? height : 100);
        }

        public void setIncrement(int newIncrement) {
                increment = newIncrement;
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
                return increment;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
                return increment;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
                return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
                return false;
        }

	}

	public FileTableModel getFileTableModel() {
		return fileModel;
	}
}
