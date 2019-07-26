
package gov.nih.tbi.dictionary.validation.view;

import gov.nih.tbi.dictionary.validation.model.ValidationOutput;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

public class OutputsRenderer implements ListCellRenderer
{

    enum Detail
    {
        TYPE, INFO
    }

    private Detail detail;
    private DefaultTreeCellRenderer renderer;
    private JLabel label;

    public OutputsRenderer(Detail detail, DefaultTreeCellRenderer renderer)
    {

        this.detail = detail;
        this.renderer = renderer;
        label = new JLabel();
        label.setFont(renderer.getFont());
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean hasFocus)
    {

        if (value instanceof ValidationOutput)
        {
            ValidationOutput output = (ValidationOutput) value;

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

            if (hasFocus)
            {
                label.setBorder(BorderFactory.createLineBorder(renderer.getBorderSelectionColor()));
            }
            else
            {
                label.setBorder(BorderFactory.createEmptyBorder());
            }

            switch (detail)
            {
            case TYPE:
                label.setText(output.getTypeString());
                break;
            case INFO:
                label.setText(output.getMessage());
                label.setToolTipText(label.getText());
                break;
            default:
                label.setText("");
            }

        }

        label.setVisible(true);
        return label;
    }

}
