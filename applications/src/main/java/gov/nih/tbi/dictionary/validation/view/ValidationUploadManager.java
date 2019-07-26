package gov.nih.tbi.dictionary.validation.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import gov.nih.tbi.commons.AppConfig;
import gov.nih.tbi.commons.WebstartException;
import gov.nih.tbi.commons.model.exceptions.DataIsolationException;
import org.apache.log4j.Logger;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.commons.EulaAgreementWrapper;
import gov.nih.tbi.repository.UploadManager;
import gov.nih.tbi.repository.service.io.SftpClientManager;

/**
 * A combination tool for both the validation client and the upload manager.
 * Using the methods that generate the UIs from the respective classes
 * (which were modified to allow me to get the panels), the two panels are
 * placed in separate tabs. Once a submission ticket is built in the validation
 * client, the corresponding field in the upload manager is automatically
 * populated and the upload manager is displayed. 
 * 
 * @author wangvg
 *
 */
public class ValidationUploadManager extends ValidationClient 
{

    /**
     * 
     */
    private static final long serialVersionUID = -2005866238515572856L;

    static Logger logger = Logger.getLogger(ValidationUploadManager.class);


    private static final int BRICS_ARGUMENT_INDEX = 0;
    private static final int DICTIONARY_ARGUMENT_INDEX = 1;
    private static final int URL_ARGUMENT_INDEX = 2;
    private static final int VERSION_ARGUMENT_INDEX = 3;
    private static final int USERNAME_ARGUMENT_INDEX = 4;
    private static final int USER_HASH_ARGUMENT_INDEX = 5;
    private static final int PASSWORD_HASH_ARGUMENT_INDEX = 6;
    private static final int ORG_EMAIL_ARGUMENT_INDEX = 7;

    private static final int SFTP_NAME_INDEX = 8;
    private static final int SFTP_BASEDIR_INDEX = 9;
    private static final int SFTP_PORT_INDEX = 10;
    private static final int SFTP_URL_INDEX = 11;
    private static final int SFTP_USER_INDEX = 12;
    private static final int SFTP_PASSWD_INDEX = 13;
    private static final int PORTAL_ROOT_INDEX = 14;

    private static final int ARGS_NUM = 15;

    // Tab indicies
    private static final int VALIDATION_TAB = 0;
    private static final int UPLOAD_TAB = 1;

    private static String bricsUrl;
    private static String ddtUrl;
    private static String serverLocation;
    private static String version;
    private static String username;
    private static String userHash;
    private static String password;
    private static String orgEmail;

    private static String sftpName;
    private static String sftpBasedir;
    private static String sftpPort;
    private static String sftpUrl;
    private static String sftpUser;
    private static String sftpPasswd;
    private static String portalRoot;

    private JTabbedPane tabPanel;

    private UploadManager uploader;

    public static void main(String[] args)
    {
        logger.info("You are in ValidationUploadManager");

        checkJavaVersion();

        if (args.length != ARGS_NUM)
        {
            JOptionPane.showMessageDialog(null, "Application has been initiated "
                    + "with a number improper arguments please contact your systems administrator", "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        bricsUrl = args[BRICS_ARGUMENT_INDEX];
        ddtUrl = args[DICTIONARY_ARGUMENT_INDEX];
        serverLocation = args[URL_ARGUMENT_INDEX];
        version = args[VERSION_ARGUMENT_INDEX];
        username = args[USERNAME_ARGUMENT_INDEX];
        userHash = args[USER_HASH_ARGUMENT_INDEX];
        password = args[PASSWORD_HASH_ARGUMENT_INDEX];
        orgEmail = args[ORG_EMAIL_ARGUMENT_INDEX];

        sftpName = args[SFTP_NAME_INDEX];
        sftpBasedir = args[SFTP_BASEDIR_INDEX];
        sftpPort = args[SFTP_PORT_INDEX];
        sftpUrl = args[SFTP_URL_INDEX];
        sftpUser = args[SFTP_USER_INDEX];
        sftpPasswd = args[SFTP_PASSWD_INDEX];
        portalRoot = args[PORTAL_ROOT_INDEX];

        try {

            AppConfig config = AppConfig.getInstance();
            String confSecurKey = config.init();

            config.setProperty("BRICS_URL", bricsUrl, confSecurKey);
            config.setProperty("DDT_URL", ddtUrl, confSecurKey);
            config.setProperty("VERSION", version, confSecurKey);
            config.setProperty("USERNAME", username, confSecurKey);
            config.setProperty("PASSWD", password, confSecurKey);
            config.setProperty("HASH", userHash, confSecurKey);

            config.setProperty("SFTP_NAME", sftpName, confSecurKey);
            config.setProperty("SFTP_BASEDIR", sftpBasedir, confSecurKey);
            config.setProperty("SFTP_PORT", sftpPort, confSecurKey);
            config.setProperty("SFTP_URL", sftpUrl, confSecurKey);
            config.setProperty("SFTP_USER", sftpUser, confSecurKey);
            config.setProperty("SFTP_PASSWORD", sftpPasswd, confSecurKey);
            config.setProperty("PORTAL_ROOT", portalRoot, confSecurKey);

            config.commit(confSecurKey);

        } catch (DataIsolationException isolExc) {

            throw new WebstartException("Unable to initiate config");
        }

        SwingUtilities.invokeLater(new Runnable()
        {
        	public void run()
        	{
        		ActionListener acceptListener = new ActionListener()
        		{
        			@Override
        			public void actionPerformed(ActionEvent arg0)
        			{

        				new ValidationUploadManager(true);
        			}
        		};
        		//We want to still show the validation client in the case
        		//that the EULA was declined
        		ActionListener declineListener = new ActionListener()
        		{
        			@Override
        			public void actionPerformed(ActionEvent arg0)
        			{

        				new ValidationUploadManager(false);
        			}
        		};
        		new EulaAgreementWrapper(acceptListener, declineListener, orgEmail);
        		
        	}

        });
    }
    /**
     * Constructor for the combined validation client and upload manager. 
     * Uses a modified constructor from the validation client to build
     * the user interface. The tabs are created and the UIs from the
     * validation client and upload manager are added to each tab and
     * displayed in the same frame.
     * @param acceptedEula whether or not the user accepted the license.
     * In the event that the license is declined, only the validation 
     * client tab will be used.
     */
    public ValidationUploadManager(boolean acceptedEula)
    {
        super();

        logger.info("Eula agreement accepted");

        setTitle("Submission Tools [" + version + "]");

        tabPanel = new JTabbedPane();

        final ValidationUploadManager  frame = this;

        //taken from the Validation Client
        setIconImage(createImageIcon(ApplicationsConstants.FRAME_ICON, "Frame GIF").getImage());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        tabPanel.insertTab(ApplicationsConstants.APP_TITLE, null, createContentPane() ,
                ApplicationsConstants.APP_TITLE, VALIDATION_TAB);

        if(acceptedEula){
            try {
                uploader = new UploadManager(serverLocation, version, username, userHash, password, orgEmail);
                uploader.setFileChooserDirectory(props.getProperty("user.dir", ""));
                uploader.setProperties(props);
                
            } catch (MalformedURLException e1) {
                JOptionPane.showMessageDialog(null, "Error in contacting BRICS servers. Check server "
                        + "parameters if issues persist.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
                System.exit(0);
            }

            tabPanel.insertTab(ApplicationsConstants.APPLICATION_TITLE, 
                    null, uploader.createUploadPanel(), ApplicationsConstants.APPLICATION_TITLE, UPLOAD_TAB);
            uploader.setSelectedStudy(props.getProperty(UploadManager.LAST_STUDY, ""));
        }

        getContentPane().add(tabPanel);

        setVisible(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // confirm quit dialog
        addWindowListener(new WindowAdapter()
        {

            public void windowClosing(WindowEvent e)
            {
				int confirmed = -1; 
				// Display confirm dialog
				if (UploadManager.isUploading()) {
					confirmed = JOptionPane.showConfirmDialog(null,
							"<html>" + "You are about to exit the submission tool before all pending uploads<br>"
									 + "have been completed If your submission has been pending for an <br>"
									 + "extended period of time, please contact your system administrator.<br><br>"
									 + "</html>",
							"WAIT!", JOptionPane.YES_NO_OPTION);
				}
				else{
					confirmed = JOptionPane.showConfirmDialog(frame,
						"Are you sure you want to quit?", "Confirm Quit",
						JOptionPane.YES_NO_OPTION);
				}
				// Close if user confirmed
				if (confirmed == JOptionPane.YES_OPTION) {
					// Close frame and clean up
					frame.dispose();
					SftpClientManager.closeAll();
					System.exit(0);
				}
			}
        });

        getContentPane().add(tabPanel, BorderLayout.CENTER);

        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

        pack();
        setVisible(true);
    }

    /**
     * When both tabs are shown, once the submission ticket is built
     * the field in the upload manager UI should be populated with
     * that ticket.
     * 
     * UPDATED 10/10/14: Also gets the name of the first file in the
     * submission and uses that to pre-populate the "Dataset Name"
     * field in the Upload Manager.
     * 
     * @param file the submission ticket to populate the field with. 
     * Should be taken from the Validation Controller class which
     * returns the file.
     * 
     * @param name the name of the first file in the submission,
     * passed in by the Validation controller
     */
    public void populateSubmissionTicket(File file, String name)
    {

        if(tabPanel.getTabCount() > 1)
        {
            
            String fileStr = file.getAbsolutePath();
            uploader.setFilePath(fileStr);
            uploader.setDataName(name);
            tabPanel.setSelectedIndex(UPLOAD_TAB);
        }
    }
}
