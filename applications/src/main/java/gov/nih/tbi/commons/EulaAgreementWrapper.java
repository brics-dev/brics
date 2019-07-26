
package gov.nih.tbi.commons;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.repository.UploadManagerController;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
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
    
    /**
     * To differentiate between EULA for standalone upload manager
     * and the combined validation and upload tool
     */
    private boolean exitOnDecline;

    public static void main(String[] args)
    {

        String orgEmail = "REPLACED@nih.gov";

        EulaAgreementWrapper test = new EulaAgreementWrapper(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent arg0)
            {

                // uploadPanel();
            }
        }, orgEmail);

    }

    public EulaAgreementWrapper(ActionListener acceptListener, final String orgEmail)
    {

    	this(acceptListener, null, orgEmail);
        
        exitOnDecline = true;
    }
    
    /**
     * For the ValidationUploadManager so that you can add in the ability to 
     * decline the EULA but still access the Validation Client portion of the
     * merged application
     * 
     */
    public EulaAgreementWrapper(ActionListener acceptListener, ActionListener declineListener, final String orgEmail)
    {
    	
    	JLabel agreementText = new JLabel(ApplicationsConstants.AGREEMENT_TEXT_1 + orgEmail
                + ApplicationsConstants.AGREEMENT_TEXT_2);
    	
    	String note = "<html><b><i>NOTE: Declining this license disables upload functionality. "
    			+ "Only the Validation Tool will display.<br><br></i></b></html>";
    	JLabel noteText = new JLabel(note);

        JButton agreeButton = new JButton("Accept");
        Font newButtonFont = new Font(agreeButton.getFont().getName(), agreeButton.getFont().getStyle(), 20);
        agreeButton.setFont(newButtonFont);
        agreeButton.setActionCommand(ACCEPT_ACTION);
        agreeButton.addActionListener(acceptListener);
        agreeButton.addActionListener(this);

        JButton declineButton = new JButton("Decline");
        declineButton.setFont(newButtonFont);
        declineButton.setActionCommand(DECLINE_ACTION);
        if(declineListener != null)
        	declineButton.addActionListener(declineListener);
        declineButton.addActionListener(this);

        // set group layout
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        // layout options
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        int verticalSize = 400;
        // set horizontal grouping
        
        ParallelGroup pGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER)
        		.addComponent(agreementText);
        SequentialGroup sGroup = layout
                .createSequentialGroup()
                .addComponent(agreementText);
        if(declineListener != null){
        	pGroup.addComponent(noteText);
        	sGroup.addComponent(noteText);
        	verticalSize = 425;
        }
        pGroup.addGroup(
                layout.createSequentialGroup().addComponent(agreeButton)
                .addComponent(declineButton));
        layout.setHorizontalGroup(pGroup);
        sGroup.addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(agreeButton)
                .addComponent(declineButton));
        layout.setVerticalGroup(sGroup);
        
        /*if(declineListener != null){
	        layout.setHorizontalGroup(layout.createSequentialGroup()
	                .addGroup(
	                        layout.createParallelGroup(GroupLayout.Alignment.CENTER)
	                                .addComponent(agreementText)
	                                .addComponent(noteText)
	                                .addGroup(
	                                        layout.createSequentialGroup().addComponent(agreeButton)
	                                                .addComponent(declineButton))));
	
	        // set vertical grouping
	        layout.setVerticalGroup(layout
	                .createSequentialGroup()
	                .addComponent(agreementText)
	                .addComponent(noteText)
	                .addGroup(
	                        layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(agreeButton)
	                                .addComponent(declineButton)));
	        verticalSize = 425;
        } else {
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
	        verticalSize = 400;
        }*/

        layout.linkSize(SwingConstants.HORIZONTAL, agreeButton, declineButton);
        layout.linkSize(SwingConstants.VERTICAL, agreeButton, declineButton);
        pack();

        this.setTitle("EULA Agreement");
        this.setSize(new Dimension(650, verticalSize));

        this.setIconImage(UploadManagerController.createImageIcon(ApplicationsConstants.FRAME_ICON, "Frame GIF")
                .getImage());

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
        
        exitOnDecline = false;
    }

    @Override
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
                if(exitOnDecline){
                	System.exit(0);
                } else {
                	this.setVisible(false);
                }
            }

    }

}
