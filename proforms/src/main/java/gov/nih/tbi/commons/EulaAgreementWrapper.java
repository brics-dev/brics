
package gov.nih.tbi.commons;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

public class EulaAgreementWrapper extends JFrame implements ActionListener
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1137705420682278137L;

    private static final String ACCEPT_ACTION = "ACCEPT";
    private static final String DECLINE_ACTION = "DECLINE";

    public static String FRAME_ICON = "images/tools.jpg";
    public static String POLICY_EMAIL = "ORG-ops@mail.nih.gov";

    public static final String AGREEMENT_TEXT_1 = "<html><b>Data Privacy</b><br /> This system is a collaborative environment with privacy rules that pertain to the collection and display of imaging data. Before accessing and using this system, please ensure you familiarize yourself with our privacy rules available through the Data Access Request and supporting documentation.<br /><br /> Collection of this information is authorized under 42 U.S.C. 241, 242, 248, 281(a)(b)(1)(P) and 44 U.S.C. 3101. The primary use of this information is to facilitate medical research. This information may be disclosed to researchers for research purposes, and to system administrators for evaluation and data normalization.<br /><br /> Rules governing submission of this information are based on the data sharing rules defined in the Notice of Grant Award (NOGA). If you do not have a grant defining data sharing requirements, data submission is voluntary. Data entered into the system will be used solely for scientific and research purposes and is designed to further the understanding of the disease. Modification of information may be addressed by contacting your system administrator at ";
    public static final String AGREEMENT_TEXT_2 = ". Significant system update information may be posted on the site as required.<br /><br /></html>";

    public EulaAgreementWrapper(ActionListener acceptListener, String orgEmail)
    {

        String AGREEMENT_TEXT = AGREEMENT_TEXT_1 + orgEmail + AGREEMENT_TEXT_2;

        JLabel agreementText = new JLabel(AGREEMENT_TEXT);
        JButton agreeButton = new JButton("Accept");
        Font newButtonFont = new Font(agreeButton.getFont().getName(), agreeButton.getFont().getStyle(), 20);
        agreeButton.setFont(newButtonFont);
        agreeButton.setActionCommand(ACCEPT_ACTION);
        agreeButton.addActionListener(acceptListener);
        agreeButton.addActionListener(this);

        JButton declineButton = new JButton("Decline");
        declineButton.setFont(newButtonFont);
        declineButton.setActionCommand(DECLINE_ACTION);
        declineButton.addActionListener(this);

        // set group layout
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        // layout options
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        // set horizontal grouping
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(agreementText)
                                .addGroup(
                                        layout.createSequentialGroup().addComponent(agreeButton)
                                                .addComponent(declineButton))));

        // set vertical grouping
        layout.setVerticalGroup(layout
                .createSequentialGroup()
                .addComponent(agreementText)
                .addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(agreeButton)
                                .addComponent(declineButton)));

        layout.linkSize(SwingConstants.HORIZONTAL, agreeButton, declineButton);
        layout.linkSize(SwingConstants.VERTICAL, agreeButton, declineButton);
        pack();

        this.setTitle("EULA Agreement");
        this.setSize(new Dimension(650, 400));

        // if (org.equals("FITBIR"))
        // {
        // FRAME_ICON = "images/TBI-Favicon-orange.jpg";
        // }
        // else
        // if (org.equals("PDBP"))
        // {
        // FRAME_ICON = "images/PDBPFavicon.jpg";
        // }
        //
        this.setIconImage(createImageIcon(FRAME_ICON, "Frame GIF").getImage());

        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // confirm quit dialog
        this.addWindowListener(new WindowAdapter()
        {

            public void windowClosing(WindowEvent e)
            {

                // Display confirm dialog
                int confirmed = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Confirm Quit",
                        JOptionPane.YES_NO_OPTION);

                // Close if user confirmed
                if (confirmed == JOptionPane.YES_OPTION)
                {
                    // Close frame and clean up
                    System.exit(0);
                }
            }
        });
    }

    public void actionPerformed(ActionEvent e)
    {

        String command = e.getActionCommand();

        if (ACCEPT_ACTION.equals(command))
        {
            this.setVisible(false);
        }
        else
            if (DECLINE_ACTION.equals(command))
            {
                System.exit(0);
            }

    }

    /**
     * Loads the icon to go on the frame
     * 
     * @param path
     * @param description
     * @return
     */
    public static ImageIcon createImageIcon(String path, String description)
    {

        java.net.URL imgURL = EulaAgreementWrapper.class.getClassLoader().getResource(path);
        if (imgURL != null)
        {
            ImageIcon imageIcon = new ImageIcon(imgURL, description);
            return imageIcon;
        }
        else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

}
