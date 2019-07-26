package gov.nih.tbi.download.util;

import gov.nih.tbi.download.table.FileTableModel;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Subclass to implement the GUI's check all box.
 * 
 * This class has since been moved to its own class so that it is usable
 * from both the macro view and the micro view. -Victor Wang
 * 
 * @author mgree1
 * 
 */
public class CheckBoxHeader extends JCheckBox implements TableCellRenderer, MouseListener
{

    /**
     * 
     */
    private static final long serialVersionUID = 9184687141171199513L;
    protected CheckBoxHeader rendererComponent;
    protected int column;
    protected boolean mousePressed = false;

    public CheckBoxHeader(ActionListener listener)
    {
        setSelected(true);
        rendererComponent = this;
        rendererComponent.addActionListener(listener);
        
        rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
    {

        if (table != null)
        {
            JTableHeader header = table.getTableHeader();
            if (header != null)
            {
                rendererComponent.setForeground(header.getForeground());
                rendererComponent.setBackground(header.getBackground());
                rendererComponent.setFont(header.getFont());
                header.addMouseListener(rendererComponent);
            }
        }
        setColumn(column);
        
		if (allSelected(table)) {
       		setSelected(true);
        }else{
       		setSelected(false);
      	}
        
        return rendererComponent;
    }

    protected void setColumn(int column)
    {

        this.column = column;
    }

    public int getColumn()
    {

        return column;
    }

    protected void handleClickEvent(MouseEvent e)
    {

        if (mousePressed)
        {
            mousePressed = false;
            JTableHeader header = (JTableHeader) (e.getSource());
            JTable tableView = header.getTable();
            TableColumnModel columnModel = tableView.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = tableView.convertColumnIndexToModel(viewColumn);

            if (viewColumn == this.column && e.getClickCount() == 1 && column != -1)
            {
                doClick();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {

        handleClickEvent(e);
        ((JTableHeader) e.getSource()).repaint();

    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e){}

    @Override
    public void mouseEntered(MouseEvent e){}

    @Override
    public void mouseExited(MouseEvent e){}

	public Boolean allSelected(JTable table) {

		int n = table.getRowCount();
		if (n == 0)
			return false;

		for (int row = 0; row < n; row++) {
			if (((Boolean) table.getModel().getValueAt(table.convertRowIndexToModel(row),
					FileTableModel.CHECKBOX_COLUMN)) == false) {
				return false;
			}
		}
		return true;
	}
}
