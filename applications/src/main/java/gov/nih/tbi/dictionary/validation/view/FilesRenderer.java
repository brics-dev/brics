
package gov.nih.tbi.dictionary.validation.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

import gov.nih.tbi.dictionary.validation.model.FileNode;

public class FilesRenderer implements ListCellRenderer
{

    enum Field
    {
		TYPE, STRUCTURE, STATUS, RESULT, SUMMARY
    }

    private Field field;
    private DefaultTreeCellRenderer renderer;
    private JLabel label;
    private JTree tree;

    public FilesRenderer(Field field, JTree tree, DefaultTreeCellRenderer renderer)
    {

        this.field = field;
        this.tree = tree;
        this.renderer = renderer;
        label = new JLabel();
        label.setFont(renderer.getFont());
        label.setOpaque(true);

        // these are being ignored
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setHorizontalTextPosition(JLabel.CENTER);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean hasFocus)
    {

        if (value instanceof FileNode)
        {
            FileNode file = (FileNode) value;

            if (file.isIncluded())
            {

                renderer.getTreeCellRendererComponent(tree, value, isSelected, tree.isExpanded(index), 
                        tree.getModel().isLeaf(value), index, hasFocus);
                
                if (isSelected)
                {
                    label.setBackground(renderer.getBackgroundSelectionColor());
                    label.setForeground(renderer.getTextSelectionColor());
                }
                else
                {
                    label.setBackground(renderer.getBackgroundNonSelectionColor());
                    label.setForeground(renderer.getTextNonSelectionColor());
                }
                
                /*if ((file.getErrorNum()!=0) || (file.getType() == FileType.UNKNOWN))
                {
                    label.setBackground(ValidationClient.red);
                    label.setForeground(renderer.getTextSelectionColor());
                }
                else 
                    if (isSelected)
                {
                    label.setBackground(renderer.getBackgroundSelectionColor());
                    label.setForeground(renderer.getTextSelectionColor());
                }
                else
                {
                    label.setBackground(renderer.getBackgroundNonSelectionColor());
                    label.setForeground(renderer.getTextNonSelectionColor());
                }*/

                switch (field)
                {
                case TYPE:
                    label.setText(file.getTypeDisplay());
                    break;
                case STRUCTURE:
                    label.setText(file.getStructureDisplay());
                    break;
					case STATUS :
						label.setText(file.getFsStatus());
						break;
                case RESULT:
                    label.setText(file.getResultDisplay());
                    break;
                case SUMMARY:
                    label.setText(file.getSummaryDisplay());
                    break;
                default:
                    label.setText("");
                }
            }
            else
            {
                label.setBackground((Color.GRAY).brighter());
                label.setText("");
            }
        }

        label.setVisible(true);

        return label;
    }

}
