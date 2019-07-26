package gov.nih.tbi.repository.table;

import gov.nih.tbi.download.util.CheckBoxHeader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

public class DatasetTableColumnFactory extends ColumnFactory {

	@Override
	public void configureTableColumn(final TableModel model, TableColumnExt columnExt) {
		super.configureTableColumn(model, columnExt);

		int modelIndex = columnExt.getModelIndex();
		
		if (DatasetTableColumn.CHECK_BOX.getColumnIndex() == modelIndex) {
			columnExt.setHeaderRenderer(new CheckBoxHeader(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() instanceof JCheckBox) {
						JCheckBox box = (JCheckBox) e.getSource();
						boolean checked = box.isSelected();
						for (int x = 0; x < model.getRowCount(); x++) {
							model.setValueAt(checked, x, DatasetTableColumn.CHECK_BOX.getColumnIndex());
						}
					}
				}
			}));
		}
	}

	@Override
	public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
		super.configureColumnWidths(table, columnExt);

		int modelIndex = columnExt.getModelIndex();

		DatasetTableColumn column = DatasetTableColumn.getByColumnIndex(modelIndex);

		switch (column) {
			case CHECK_BOX:
				columnExt.setMaxWidth(35);
				columnExt.setMinWidth(35);
				break;
			case NAME:
				columnExt.setPreferredWidth(240);
				break; // use default width
			case STUDY_TITLE:
				columnExt.setPreferredWidth(240);
				break; // use default width
			case PROGRESS:
				columnExt.setPreferredWidth(60);
				break;
			default:
				break;
		}
	}
}
