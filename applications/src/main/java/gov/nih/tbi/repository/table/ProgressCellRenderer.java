package gov.nih.tbi.repository.table;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {

	private static final long serialVersionUID = 534708483548854448L;

	public ProgressCellRenderer() {

		// Initialize the progress bar renderer to use a horizontal
		// progress bar.
		super(JProgressBar.HORIZONTAL);
		// Ensure that the progress bar border is not painted. (The
		// result is ugly when it appears in a table cell.)
		setBorderPainted(false);
		// Ensure that percentage text is painted on the progress bar.
		setStringPainted(true);
	}

	public JComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {

		if (value instanceof Integer) {
			// Ensure that the nonseslected background portion of a
			// progress bar is assigned the same color as the table's
			// background color. The resulting progress bar fits more
			// naturally (from a visual perspective) into the overall
			// table's appearance.
			setBackground(table.getBackground());
			this.setValue((Integer) value);
		}

		return this;
	}
}
