
package gov.nih.tbi.download.util;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

// This class renders a JProgressBar in a table cell.
public class ProgressBarRenderer extends JProgressBar implements TableCellRenderer
{

    public JProgressBar progress;
    private static final long serialVersionUID = -5894276848235087923L;

    // Constructor for ProgressRenderer.
    public ProgressBarRenderer()
    {

        super(JProgressBar.HORIZONTAL);
        setValue(0);
        setBorderPainted(false);
        setStringPainted(true);
    }

    /* Returns this JProgressBar as the renderer
       for the given table cell. */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
    {

        if (value instanceof Integer)
        {
            if (isSelected)
                setBackground(table.getSelectionBackground());
            else
                setBackground(table.getBackground());
            setBorderPainted(hasFocus);
            // Ensure that the nonseslected background portion of a
            // progress bar is assigned the same color as the table's
            // background color. The resulting progress bar fits more
            // naturally (from a visual perspective) into the overall
            // table's appearance.
            // setBackground(table.getBackground());
            this.setValue((Integer) value);
        }

        return this;
    }
}