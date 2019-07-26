
package gov.nih.tbi.dictionary.validation.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.commons.model.exceptions.DataIsolationException;
import gov.nih.tbi.dictionary.validation.ValidationController;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;
import gov.nih.tbi.dictionary.validation.view.FilesRenderer.Field;
import gov.nih.tbi.dictionary.validation.view.OutputsRenderer.Detail;
import gov.nih.tbi.commons.AppConfig;
import gov.nih.tbi.commons.WebstartException;

import org.apache.log4j.Logger;

public class ValidationClient extends JFrame implements MouseWheelListener
{
    static Logger logger = Logger.getLogger(ValidationClient.class);

    // Default
    private static final long serialVersionUID = 1L;

    private static final int BRICS_ARGUMENT_INDEX = 0;
    private static final int DICTIONARY_ARGUMENT_INDEX = 1;
    private static final int VERSION_ARGUMENT_INDEX = 2;
    private static final int USERNAME_ARGUMENT_INDEX = 3;
    private static final int USER_HASH_ARGUMENT_INDEX = 4;
    private static final int PASSWORD_HASH_ARGUMENT_INDEX = 5;

    private ValidationController controller;

    private JMenuBar mainMenu;

    private JMenuItem load, joinFiles, validate, submission, exit, userGuide;

    private JTextField sourceDir;

    private JButton browseButton, loadFilesButton, excludeFilesButton, includeFilesButton, // openStructureButton,
                                                                                           // addStructureButton,
            validateButton, createSubmissionButton, exportButton, reloadButton;

    private DefaultTreeModel fileModel = new DefaultTreeModel(null);

    private ListModel fileDetails;

    private ListSelectionModel fileSelection;

    private DefaultListModel outputModel = new DefaultListModel();

    private JTree fileTree;

	private JList fileTypeList, fileStructuresList, fileStatusList,
			fileResultList,
			fileSummaryList;

    private JList outputTypeList, outputInfoList;
    
    private JCheckBox hideWarningsBox;

    private boolean loaded = false; // True only when data has been loaded without error.
    
    private JScrollBar fileScroll;
    private JScrollBar outputScroll;
    private JScrollBar activeScroll;
    
    private String validationTitle[] = new String[]{
    		"Alcohol Use Disorders Identification Test (AUDIT-C)",
    		"Alcohol Use Disorders Identification Test (AUDIT_FITBIR)",
    		"Balance Error Scoring System (BESS)",
    		"Beck Depression Inventory II (BDI2)",
            "Brief Symptoms Inventory-18 (BSI-18)",
            "Controlled Oral Word Association Test (COWAT)",
            "Dizziness Handicap Inventory (DHI)",
            "FIM Instrument",
            "Glasgow Coma Scale and Pupils (GCS)",
            "Glasgow Outcome Scale - Extended (GOS-E)",
            "Headache Impact Test (HIT-6)",
            "MDS UPDRS (MDSUPDRS)",
            "Montreal Cognitive Assessment (MoCA)",
            "Neurobehavioral Symptom Inventory II (NSI)",
            "Patient Health Questionnaire (PHQ9)",
            "Pittsburgh Sleep Quality Index (PSQI)",
            "PTSD Check List - Civilian Version (PCL-C)",
            "Rivermead Post-Concussion Symptoms Questionnaire (RPQ)",
            "Sport Concussion Assessment Tool (SCAT-3)",
            "Satisfaction with Life Scale (SWLS)",
            "Short Form Health Survey (SF-12)",
            "Short Form Health Survey (SF-36) version 2",
            "TRAIL Making Test (TMT)"};
    private String validationShortName[] = new String[]{
    		"AUDITC",
    		"AUDIT_FITBIR",
    		"BESS",
    		"BDI2",
    		"BSI18",
    		"COWAT",
    		"DHI",
    		"FIM_Instrument",
    		"GCS",
    		"GOSE_Standard",
    		"HIT6",
    		"MDS_UPDRS",
    		"MoCA",
    		"NSI1",
    		"PHQ9",
    		"PSQI",
    		"PCLC_Standard",
    		"Rivermead",
    		"SCAT3",
    		"SWLS_CDISC_FITBIR",
    		"SF12",
    		"SF36v2",
    		"TMT_Standard"};

private int validationNumber = validationTitle.length;
protected HashMap<String, Boolean> extraValidation = 
                  new HashMap<String, Boolean>();
    
    protected Properties props = new Properties();

    // Action Commands
    // public static final String OPEN_SUBMISSION = "open_submission";
    public static final String BROWSE = "browse";
    public static final String LOAD = "load";
    public static final String INCLUDE = "include";
    public static final String EXCLUDE = "exclude";
    public static final String OPEN = "open";
    public static final String ADD = "add";
    public static final String RELOAD = "reload";
    public static final String SAVE_TXT = "save txt";
    public static final String SAVE_CSV = "save csv";
    public static final String EXIT = "exit";

    public static final String JOIN_FILES = "join";
    public static final String VALIDATE = "validate";
    public static final String BUILD_SUBMISSION = "build submission";
    public static final String EXPORT = "export result details";
    
    public static final String HIDE = "hide warnings";

    public static final String SUBMISSION_GUIDE = "submission guide";
    public static final String ABOUT = "about";

    // Fonts
    public static final Font fontArial11 = new Font("Arial", Font.PLAIN, 11);
    public static final Font fontArial11B = new Font("Arial", Font.BOLD, 11);
    public static final Font fontArial12B = new Font("Arial", Font.BOLD, 12);
    public static final Font fontArial12 = new Font("Arial", Font.PLAIN, 12);
    public static final Font fontArial14 = new Font("Arial", Font.PLAIN, 14);
    
    public static final Color VALID_COLOR = new Color(114, 247, 101);
    public static final Color ERROR_COLOR = new Color(255, 75, 75);

    

    protected static void checkJavaVersion()
    {

        String javaVersion = System.getProperty("java.version");

        if (javaVersion.equals(""))
        {
            JOptionPane
                    .showMessageDialog(
                            null,
                            "Java version 1.5 or higher is required for Validation tool. Please install it from http://java.sun.com/",
                            "Java Version Error", JOptionPane.ERROR_MESSAGE);
            // cleanup();
            System.exit(0);
        }

        if (!(javaVersion.startsWith("1.5") || javaVersion.startsWith("1.6") || javaVersion.startsWith("1.7") || javaVersion.startsWith("1.8")))
        {
            JOptionPane
                    .showMessageDialog(
                            null,
                            "Java version 1.5 or higher is required for Validation tool. Please upgrade it from http://java.sun.com/",
                            "Java Version Error", JOptionPane.ERROR_MESSAGE);
            // cleanup();
            System.exit(0);
        }
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path, String description)
    {

        java.net.URL imgURL = ValidationClient.class.getClassLoader().getResource(path);
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

    private static void createAndShowGUI()
    {

        UIManager.put("ToolTip.font", fontArial11);

        JFrame frame = new ValidationClient();

        /* Frame location*/
        frame.setIconImage(createImageIcon(ApplicationsConstants.FRAME_ICON, "Frame GIF").getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // TODO: Figure out how to move the focus
        frame.setMinimumSize(new Dimension(900, 650));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private class MyTreeCellRenderer extends DefaultTreeCellRenderer
    {

        // Default
        private static final long serialVersionUID = 1L;

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus)
        {
            setToolTipText(this.getText());
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if(value instanceof FileNode){
                FileNode f = (FileNode) value;
                UIDefaults defaults = javax.swing.UIManager.getDefaults();
                Color defaultSelection = defaults.getColor("Tree.selectionBackground");
                if(f.isIncluded())
                {
                    if(!f.isValidated())
                    {
                        setBackgroundSelectionColor(defaultSelection);
                        setBackgroundNonSelectionColor(defaults.getColor("Tree.nonSelectionBackground"));
                    }
                    else
                        if((f.getErrorNum()) != 0 || (f.getType() == FileType.UNKNOWN &&
                        !f.isImageType()))
                        {
                            //setBackgroundSelectionColor(defaultSelection);
                            setBackgroundSelectionColor(ERROR_COLOR.darker());
                            setBackgroundNonSelectionColor(ERROR_COLOR);
                        }
                        else
                            if (f.isValidated() && f.isValid() )
                            {
                                //setBackgroundSelectionColor(defaultSelection);
                                setBackgroundSelectionColor(VALID_COLOR.darker());
                                setBackgroundNonSelectionColor(VALID_COLOR);
                            }
                            else 
                            {
                                setBackgroundSelectionColor(defaultSelection);
                                setBackgroundNonSelectionColor(defaults.getColor("Tree.nonSelectionBackground"));
                            }
                } 
                else 
                {
                    //If you want to change the selection color back to default
                    //UIDefaults defaults = javax.swing.UIManager.getDefaults();
                    setBackgroundSelectionColor(defaultSelection);
                    //setBackgroundSelectionColor((Color.gray).brighter());
                    setBackgroundNonSelectionColor((Color.gray).brighter());
                }
            }
            
            return this;
        }
    }

    public ValidationClient()
    {

        super(ApplicationsConstants.APP_TITLE);

        addMouseWheelListener(this);
        
        try
        {
            FileInputStream input = new FileInputStream(System.getProperty("user.home", "")
                    + File.separator + "brics.pref");
            props.load(input);
            input.close();
        }
        catch (FileNotFoundException e1)
        {
            //don't need to do anything, just make a new one
        }
        catch (IOException e)
        {
            props = null;
            System.err.println("Could not open preferences file");
        }

        AppConfig config = AppConfig.getInstance();
        
        String bricsUrl = config.getProperty("BRICS_URL");
        String ddtUrl = config.getProperty("DDT_URL");
        //String version = config.getProperty("VERSION");

        // If a username / hash were provided, otherwise use anonymous
        String username = config.getProperty("USERNAME");
        String password = config.getProperty("PASSWD");
        String userHash = config.getProperty("HASH");
        
       try
        {
            this.controller = new ValidationController(this, bricsUrl, ddtUrl, username, password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to connect to FITBIR. "
                    + "If the problem persists please contact your systems administrator.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
		mainMenu = createMenuBar();
        setJMenuBar(mainMenu);

        DefaultTreeCellRenderer cellRenderer = new MyTreeCellRenderer();
        cellRenderer.setFont(fontArial11);

        fileTree = new JTree(fileModel);
        ToolTipManager.sharedInstance().registerComponent(fileTree);
        fileTree.setCellRenderer(cellRenderer);
        fileTree.setRowHeight(20);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        fileTree.setExpandsSelectedPaths(true);
        fileTree.addTreeSelectionListener(controller);

        fileDetails = new TreeListModel(fileTree);
        fileSelection = new TreeListSelectionModel(fileTree);
        fileSelection.addListSelectionListener(controller);

        fileTypeList = new JList(fileDetails);
        fileTypeList.setCellRenderer(new FilesRenderer(Field.TYPE, fileTree, cellRenderer));
        fileTypeList.setFixedCellHeight(fileTree.getRowHeight());
        fileTypeList.setSelectionModel(fileSelection);

        fileStructuresList = new JList(fileDetails);
        fileStructuresList.setCellRenderer(new FilesRenderer(Field.STRUCTURE, fileTree, cellRenderer));
        fileStructuresList.setFixedCellHeight(fileTree.getRowHeight());
        fileStructuresList.setSelectionModel(fileSelection);

		fileStatusList = new JList(fileDetails);
		fileStatusList.setCellRenderer(
				new FilesRenderer(Field.STATUS, fileTree, cellRenderer));
		fileStatusList.setFixedCellHeight(fileTree.getRowHeight());
		fileStatusList.setSelectionModel(fileSelection);

        fileResultList = new JList(fileDetails);
        fileResultList.setCellRenderer(new FilesRenderer(Field.RESULT, fileTree, cellRenderer));
        fileResultList.setFixedCellHeight(fileTree.getRowHeight());
        fileResultList.setSelectionModel(fileSelection);

        fileSummaryList = new JList(fileDetails);
        fileSummaryList.setCellRenderer(new FilesRenderer(Field.SUMMARY, fileTree, cellRenderer));
        fileSummaryList.setFixedCellHeight(fileTree.getRowHeight());
        fileSummaryList.setSelectionModel(fileSelection);

        outputTypeList = new JList(outputModel);
        outputTypeList.setCellRenderer(new OutputsRenderer(Detail.TYPE, cellRenderer));
        outputTypeList.setFixedCellHeight(fileTree.getRowHeight());

        outputInfoList = new JList(outputModel);
        outputInfoList.setCellRenderer(new OutputsRenderer(Detail.INFO, cellRenderer));
        outputInfoList.setFixedCellHeight(fileTree.getRowHeight());

        //setContentPane(createContentPane());
    }
    
    
    private JMenuBar createMenuBar()
    {

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        // JMenuItem open = new JMenuItem("Open Submission...");
        // open.setActionCommand(OPEN_SUBMISSION);
        // open.addActionListener(controller);
        // fileMenu.add(open);

        load = new JMenuItem("Load Files");
        load.setActionCommand(LOAD);
        load.addActionListener(controller);
        fileMenu.add(load);

        // fileMenu.addSeparator();
        //
        // JMenuItem open = new JMenuItem("Open Data Structure");
        // open.setActionCommand(OPEN);
        // open.addActionListener(controller);
        // fileMenu.add(open);
        //
        // JMenuItem add = new JMenuItem("Add Data Structure");
        // add.setActionCommand(ADD);
        // add.addActionListener(controller);
        // fileMenu.add(add);
        //
        // fileMenu.addSeparator();
        //
        // JMenu saveMenu = new JMenu("Save as");
        //
        // JMenuItem saveTxt = new JMenuItem("TXT File");
        // saveTxt.setActionCommand(SAVE_TXT);
        // saveTxt.addActionListener(controller);
        // saveMenu.add(saveTxt);
        //
        // JMenuItem saveCsv = new JMenuItem("CSV File");
        // saveCsv.setActionCommand(SAVE_CSV);
        // saveCsv.addActionListener(controller);
        // saveMenu.add(saveCsv);
        //
        // fileMenu.add(saveMenu);
        //

        // JMenu utilitiesMenu = new JMenu("Utilities");

        joinFiles = new JMenuItem("Join Files");
        joinFiles.setActionCommand(JOIN_FILES);
        joinFiles.addActionListener(controller);
        fileMenu.add(joinFiles);

        validate = new JMenuItem("Validate Files");
        validate.setActionCommand(VALIDATE);
        validate.addActionListener(controller);
        validate.setEnabled(false);
        fileMenu.add(validate);

        fileMenu.addSeparator();

        submission = new JMenuItem("Build Submission Package");
        submission.setActionCommand(BUILD_SUBMISSION);
        submission.addActionListener(controller);
        submission.setEnabled(false);
        fileMenu.add(submission);

        fileMenu.addSeparator();

        exit = new JMenuItem("Exit");
        exit.setActionCommand(EXIT);
        exit.addActionListener(controller);
        fileMenu.add(exit);

        // Uncomment to bring back the File menu
        // menuBar.add(fileMenu);

        // menuBar.add(utilitiesMenu);

        JMenu helpMenu = new JMenu("Help");

        userGuide = new JMenuItem("User Guide");
        userGuide.setActionCommand(SUBMISSION_GUIDE);
        userGuide.addActionListener(controller);
        helpMenu.add(userGuide);

        // JMenuItem about = new JMenuItem("About");
        // about.setActionCommand(ABOUT);
        // about.addActionListener(controller);
        // helpMenu.add(about);

        menuBar.add(helpMenu);

        return menuBar;
    }

    protected JPanel createContentPane()
    {

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(createSourcePane(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTabPane(), createResultDetailsPane());
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.75);
        contentPane.add(splitPane, BorderLayout.CENTER);
        
        InputMap  inputMap = splitPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.CTRL_MASK +  ActionEvent.SHIFT_MASK);
       inputMap.put(key,  "ACTION_TURN_OFF_ON_EXTRA_VALIDATION");
       splitPane.getActionMap().put("ACTION_TURN_OFF_ON_EXTRA_VALIDATION", new ExtraValidationOffOnAction());

        return contentPane;
    }
    
    class ExtraValidationOffOnAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        	 ValidationDialog dialogValidation =  new ValidationDialog(null);
        	 // setLocation and setLocationRelativeTo calls are ignored.  dialogValidation always appears in upper left hand corner
        	 //dialogValidation.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - dialogValidation.getBounds().width/2,
        			 //Toolkit.getDefaultToolkit().getScreenSize().height/2 - dialogValidation.getBounds().height/2);
        	 //dialogValidation.setLocationRelativeTo(null);
        	 if (dialogValidation.isCancelled()) {
        		 return;
        	 }
           
            if (controller != null) {
                controller.setExtraValidation(extraValidation);
            }
        }
    }
    
    private class ValidationDialog extends JDialog implements ActionListener {
    	boolean cancelFlag = false;
    	private JCheckBox cBox[] = new JCheckBox[validationNumber];
    	 /**
         * OK button is used on most dialogs. Defining it in the base allows default actions if the user presses return and
         * the button is in focus.
         */
        protected JButton OKButton;
        protected JButton cancelButton;
        protected JButton allButton;
        protected JButton noneButton;
        /** The default size that all buttons should be. */
        public final Dimension defaultButtonSize = new Dimension(90, 30);
    	public ValidationDialog(Frame parent) {
    		super(parent, true); // force the user to close before going back to the main frame
    		
    		init();
    		setVisible(true);
    	}
    	
    	/**
         * Sets up the GUI (panels, buttons, etc) and displays it on the screen.
         */
        private void init() {
        	int i;
            setForeground(Color.black);

            setTitle("Turn validations on and off");
            getContentPane().setLayout(new BorderLayout());

            JPanel mainPanel;

            mainPanel = new JPanel();
            mainPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            mainPanel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1;
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.fill = GridBagConstraints.BOTH;
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc2.gridwidth = 1;
            gbc2.gridheight = 1;
            gbc2.anchor = GridBagConstraints.WEST;
            gbc2.weightx = 1;
            gbc2.insets = new Insets(3, 3, 3, 3);
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbc2.gridy = -1;
            
            for (i = 0; i < validationNumber; i++) {
                cBox[i] = new JCheckBox(validationTitle[i]);
                cBox[i].setFont(fontArial12);
                gbc2.gridx = 0;
                gbc2.gridy++;
                formPanel.add(cBox[i], gbc2);
            }
           
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = .2;  
            JScrollPane formScroll = new JScrollPane(formPanel);
            formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            mainPanel.add(formScroll, gbc);
            
            getContentPane().add(mainPanel, BorderLayout.CENTER);
            getContentPane().add(buildButtons(), BorderLayout.SOUTH);
            pack();
            setResizable(true);
            setSize(420,300);
            setIconImage(createImageIcon(ApplicationsConstants.FRAME_ICON, "Frame GIF").getImage());

            System.gc();
        }
        
        /**
         * Builds button panel consisting of OK, Cancel and Help buttons.
         *
         * @return  JPanel that has ok, cancel, and help buttons
         */
        protected JPanel buildButtons() {
            JPanel buttonPanel = new JPanel();

            buttonPanel.add(buildAllButton());
            buttonPanel.add(buildNoneButton());
            buttonPanel.add(buildOKButton());
            buttonPanel.add(buildCancelButton());

            return buttonPanel;
        }
        
        protected JButton buildAllButton() {
        	allButton = new JButton("All");
        	allButton.addActionListener(this);
        	allButton.setMinimumSize(defaultButtonSize);
        	allButton.setPreferredSize(defaultButtonSize);
        	allButton.setFont(fontArial12);
        	return allButton;
        }
        
        protected JButton buildNoneButton() {
        	noneButton = new JButton("None");
        	noneButton.addActionListener(this);
        	noneButton.setMinimumSize(defaultButtonSize);
        	noneButton.setPreferredSize(defaultButtonSize);
        	noneButton.setFont(fontArial12);
        	return noneButton;
        }
        
        /**
         * Builds the OK button. Sets it internally as well return the just-built button.
         *
         * @return  JButton ok button
         */
        protected JButton buildOKButton() {
            OKButton = new JButton("OK");
            OKButton.addActionListener(this);

            // OKButton.setToolTipText("Accept values and perform action.");
            OKButton.setMinimumSize(defaultButtonSize);
            OKButton.setPreferredSize(defaultButtonSize);
            OKButton.setFont(fontArial12);

            return OKButton;
        }
        
        /**
         * Builds the cancel button. Sets it internally as well return the just-built button.
         *
         * @return JButton cancel button
         */
        protected JButton buildCancelButton() {
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);

            // cancelButton.setToolTipText("Cancel action.");
            cancelButton.setMinimumSize(defaultButtonSize);
            cancelButton.setPreferredSize(defaultButtonSize);
            cancelButton.setFont(fontArial12);

            return cancelButton;
        }
        
        
        /**
         * Handles the user clicking one of the buttons (OK or Copy) and acts accordingly
         */
        public void actionPerformed(ActionEvent event)
        {
            int i;
            String command = event.getActionCommand();

            if (command.equals("OK"))
            {
            	for (i = 0; i < validationNumber; i++) {
            	    extraValidation.put(validationShortName[i],
            	    		cBox[i].isSelected());
            	}
                dispose();
            }
            else if (command.equals("Cancel")) {
                cancelFlag = true;
                dispose();
            } 
            else if (command.equals("All")) {
            	for (i = 0; i < validationNumber; i++) {
            		cBox[i].setSelected(true);
            	}
            }
            else if (command.equals("None")) {
            	for (i = 0; i < validationNumber; i++) {
            		cBox[i].setSelected(false);
            	}
            }
           
        }
        
        /**
         * Accessor that returns whether or not the dialog has been cancelled.
         *
         * @return  <code>true</code> indicates cancelled, <code>false</code> indicates not cancelled.
         */
        public boolean isCancelled() {
            return cancelFlag;
        }
        
    }

    private JPanel createSourcePane()
    {

        JPanel sourcePane = new JPanel();
        sourcePane.setLayout(new GridBagLayout());
        sourcePane.setBorder(new TitledBorder(new EtchedBorder(), "Working Directory", TitledBorder.LEFT,
                TitledBorder.BELOW_TOP, fontArial14));

        browseButton = new JButton("Browse");
        browseButton.setFont(fontArial11B);
        browseButton.setActionCommand(BROWSE);
        browseButton.addActionListener(controller);

        sourceDir = new JTextField(25);
        if(props != null)
            sourceDir.setText(props.getProperty("user.dir", ""));

        loadFilesButton = new JButton("Load Files");
        loadFilesButton.setFont(fontArial11B);
        loadFilesButton.setActionCommand(LOAD);
        loadFilesButton.addActionListener(controller);
        loadFilesButton.setEnabled(sourceDir.getText().length() > 0);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        sourcePane.add(browseButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        sourcePane.add(sourceDir, gbc);

        gbc.gridx = 5;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        sourcePane.add(loadFilesButton, gbc);

        return sourcePane;
    }

    private JTabbedPane createTabPane()
    {

        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setFont(fontArial14);

        tabPane.addTab("Files", createSummaryTab());

        return tabPane;
    }

    private JPanel createSummaryTab()
    {

        JPanel summaryTab = new JPanel();
        summaryTab.setLayout(new BoxLayout(summaryTab, BoxLayout.Y_AXIS));
        summaryTab.add(createFilePanel());
        summaryTab.add(createColorLegend());
        summaryTab.add(createButtonPanel());

        return summaryTab;
    }

    private JPanel createFilePanel()
    {

        MouseListener ml = new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e)
            {

            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                activeScroll = fileScroll;
            }

            @Override
            public void mouseExited(MouseEvent e)
            {

            }
            
        };
        
        JPanel filePanel = new JPanel();
        filePanel.addMouseListener(ml);
        filePanel.setPreferredSize(new Dimension(800, 250));
        filePanel.setLayout(new GridBagLayout());

        JPanel namePane = new JPanel();
        namePane.setLayout(new BoxLayout(namePane, BoxLayout.Y_AXIS));
        namePane.setBorder(BorderFactory.createEtchedBorder());
        namePane.addMouseListener(ml);

        JLabel nameLabel = new JLabel("Name", JLabel.LEFT);
        namePane.add(nameLabel);

        JScrollPane nameScroll = new JScrollPane(fileTree, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        nameScroll.addMouseListener(ml);
        namePane.add(nameScroll);

        JPanel typePane = new JPanel();
        typePane.setLayout(new BoxLayout(typePane, BoxLayout.Y_AXIS));
        typePane.setBorder(BorderFactory.createEtchedBorder());
        typePane.addMouseListener(ml);

        JLabel typeLabel = new JLabel("Type", JLabel.LEFT);
        typePane.add(typeLabel);

        JScrollPane typeScroll = new JScrollPane(fileTypeList, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        typeScroll.getVerticalScrollBar().setModel(nameScroll.getVerticalScrollBar().getModel());
        typeScroll.addMouseListener(ml);
        typePane.add(typeScroll);

        JPanel structuresPane = new JPanel();
        structuresPane.setLayout(new BoxLayout(structuresPane, BoxLayout.Y_AXIS));
        structuresPane.setBorder(BorderFactory.createEtchedBorder());
        structuresPane.addMouseListener(ml);

        JLabel structuresLabel = new JLabel("Structure", JLabel.LEFT);
        structuresPane.add(structuresLabel);

        JScrollPane structuresScroll = new JScrollPane(fileStructuresList, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        structuresScroll.getVerticalScrollBar().setModel(nameScroll.getVerticalScrollBar().getModel());
        structuresScroll.addMouseListener(ml);
        structuresPane.add(structuresScroll);

		JPanel statusPane = new JPanel();
		statusPane.setLayout(new BoxLayout(statusPane, BoxLayout.Y_AXIS));
		statusPane.setBorder(BorderFactory.createEtchedBorder());
		statusPane.addMouseListener(ml);

		JLabel statusLabel = new JLabel("Status", JLabel.LEFT);
		statusPane.add(statusLabel);

		JScrollPane statusScroll = new JScrollPane(fileStatusList,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		statusScroll.getVerticalScrollBar()
				.setModel(nameScroll.getVerticalScrollBar().getModel());
		statusScroll.addMouseListener(ml);
		statusPane.add(statusScroll);

        JPanel resultPane = new JPanel();
        resultPane.setLayout(new BoxLayout(resultPane, BoxLayout.Y_AXIS));
        resultPane.setBorder(BorderFactory.createEtchedBorder());
        resultPane.addMouseListener(ml);

        JLabel resultLabel = new JLabel("Result", JLabel.LEFT);
        resultPane.add(resultLabel);

        JScrollPane resultScroll = new JScrollPane(fileResultList, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultScroll.getVerticalScrollBar().setModel(nameScroll.getVerticalScrollBar().getModel());
        resultScroll.addMouseListener(ml);
        resultPane.add(resultScroll);

        JPanel summaryPane = new JPanel();
        summaryPane.setLayout(new BoxLayout(summaryPane, BoxLayout.Y_AXIS));
        summaryPane.setBorder(BorderFactory.createEtchedBorder());
        summaryPane.addMouseListener(ml);

        JLabel summaryLabel = new JLabel("Summary", JLabel.LEFT);
        summaryPane.add(summaryLabel);

        JScrollPane summaryScroll = new JScrollPane(fileSummaryList, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        summaryScroll.getVerticalScrollBar().setModel(nameScroll.getVerticalScrollBar().getModel());
        summaryScroll.addMouseListener(ml);
        summaryPane.add(summaryScroll);

        fileScroll = new JScrollBar(JScrollBar.VERTICAL);
        fileScroll.setModel(nameScroll.getVerticalScrollBar().getModel());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.gridy = 0;
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.weighty = 1;

        gbc.weightx = 0;
        filePanel.add(fileScroll, gbc);

        gbc.weightx = 0.4;
        filePanel.add(namePane, gbc);

        gbc.weightx = 0.1;
        filePanel.add(typePane, gbc);

        gbc.weightx = 0.2;
        filePanel.add(structuresPane, gbc);

		gbc.weightx = 0.2;
		filePanel.add(statusPane, gbc);

        gbc.weightx = 0.1;
        filePanel.add(resultPane, gbc);

        gbc.weightx = 0.2;
        filePanel.add(summaryPane, gbc);
        
        fileTree.addMouseListener(ml);
        fileTypeList.addMouseListener(ml);
        fileStructuresList.addMouseListener(ml);
		fileStatusList.addMouseListener(ml);
        fileResultList.addMouseListener(ml);
        fileSummaryList.addMouseListener(ml);

        return filePanel;
    }

    private JPanel createButtonPanel()
    {

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        excludeFilesButton = new JButton("Exclude Files");
        excludeFilesButton.setFont(fontArial11B);
        excludeFilesButton.setActionCommand(EXCLUDE);
        excludeFilesButton.addActionListener(controller);
        excludeFilesButton.setEnabled(false);

        includeFilesButton = new JButton("Include Files");
        includeFilesButton.setFont(fontArial11B);
        includeFilesButton.setActionCommand(INCLUDE);
        includeFilesButton.addActionListener(controller);
        includeFilesButton.setEnabled(false);

        reloadButton = new JButton("Reload Selected");
        reloadButton.setFont(fontArial11B);
        reloadButton.setActionCommand(RELOAD);
        reloadButton.addActionListener(controller);
        reloadButton.setEnabled(false);

        // openStructureButton = new JButton("Open Data Structure");
        // openStructureButton.setFont(fontArial11B);
        // openStructureButton.setActionCommand(OPEN);
        // openStructureButton.addActionListener(controller);
        // openStructureButton.setEnabled(false);
        //
        // addStructureButton = new JButton("Add Data Structure");
        // addStructureButton.setFont(fontArial11B);
        // addStructureButton.setActionCommand(ADD);
        // addStructureButton.addActionListener(controller);
        // addStructureButton.setEnabled(false);

        validateButton = new JButton("Validate Files");
        validateButton.setFont(fontArial11B);
        validateButton.setActionCommand(VALIDATE);
        validateButton.addActionListener(controller);
        validateButton.setEnabled(false);

        createSubmissionButton = new JButton("Build Submission Package");
        createSubmissionButton.setFont(fontArial11B);
        createSubmissionButton.setActionCommand(BUILD_SUBMISSION);
        createSubmissionButton.addActionListener(controller);
        createSubmissionButton.setEnabled(false);
        createSubmissionButton
                .setToolTipText("All included files must be valid before the submission package can be built");

        leftPanel.add(excludeFilesButton);
        leftPanel.add(includeFilesButton);
        // leftPanel.add(reloadButton);
        // leftPanel.add(openStructureButton);
        // leftPanel.add(addStructureButton);
        rightPanel.add(validateButton);
        rightPanel.add(createSubmissionButton);

        buttonPanel.add(leftPanel);
        buttonPanel.add(rightPanel);

        return buttonPanel;
    }
    
    private JPanel createColorLegend()
    {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        Dimension prefSize = new Dimension(25, 25);
        
        JLabel legendLabel = new JLabel("Color Legend");
        legendLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        legendPanel.add(legendLabel);
        
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        JPanel processBox = new JPanel();
        processBox.setBackground(Color.white);
        processBox.setBorder(BorderFactory.createLineBorder(Color.black));
        processBox.setMinimumSize(prefSize);
        processBox.setMaximumSize(prefSize);
        colorPanel.add(processBox);
        
        JLabel processLabel = new JLabel("Requires Validation");
        processLabel.setBorder(new EmptyBorder(0, 3, 0, 10));
        colorPanel.add(processLabel);
        
        JPanel validBox = new JPanel();
        validBox.setBackground(VALID_COLOR);
        validBox.setBorder(BorderFactory.createLineBorder(Color.black));
        validBox.setMinimumSize(prefSize);
        validBox.setMaximumSize(prefSize);
        colorPanel.add(validBox);
        
        JLabel validLabel = new JLabel("Valid");
        validLabel.setBorder(new EmptyBorder(0, 3, 0, 10));
        colorPanel.add(validLabel);
        
        JPanel excludedBox = new JPanel();
        excludedBox.setBackground((Color.gray).brighter());
        excludedBox.setBorder(BorderFactory.createLineBorder(Color.black));
        excludedBox.setMinimumSize(prefSize);
        excludedBox.setMaximumSize(prefSize);
        colorPanel.add(excludedBox);
        
        JLabel excludedLabel = new JLabel("Excluded");
        excludedLabel.setBorder(new EmptyBorder(0, 3, 0, 10));
        colorPanel.add(excludedLabel);
        
        JPanel errorBox = new JPanel();
        errorBox.setBackground(ERROR_COLOR);
        errorBox.setBorder(BorderFactory.createLineBorder(Color.black));
        errorBox.setMinimumSize(prefSize);
        errorBox.setMaximumSize(prefSize);
        colorPanel.add(errorBox);
        
        JLabel errorLabel = new JLabel("Error");
        errorLabel.setBorder(new EmptyBorder(0, 3, 0, 10));
        colorPanel.add(errorLabel);
        
        legendPanel.add(colorPanel);
        
        return legendPanel;
    }

    private JPanel createResultDetailsPane()
    {

        MouseListener ml = new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent e)
            {

            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                activeScroll = outputScroll;
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                
            }
            
        };
        
        JPanel resultDetailsPane = new JPanel();
        resultDetailsPane.addMouseListener(ml);
        resultDetailsPane.setLayout(new BoxLayout(resultDetailsPane, BoxLayout.Y_AXIS));
        resultDetailsPane.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(new EtchedBorder(),
                "Result Details", TitledBorder.LEFT, TitledBorder.BELOW_TOP, fontArial14), BorderFactory
                .createEmptyBorder(10, 0, 0, 0)));

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));

        JPanel descriptPane = new JPanel();
        descriptPane.setBorder(BorderFactory.createEtchedBorder());
        descriptPane.setLayout(new BoxLayout(descriptPane, BoxLayout.Y_AXIS));

        JLabel descriptLabel = new JLabel("Description", JLabel.LEFT);
        descriptPane.add(descriptLabel);

        JScrollPane descriptScroll = new JScrollPane(outputInfoList, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptScroll.addMouseListener(ml);
        descriptPane.add(descriptScroll);

        JPanel typePane = new JPanel();
        typePane.setLayout(new BoxLayout(typePane, BoxLayout.Y_AXIS));
        typePane.setBorder(BorderFactory.createEtchedBorder());

        JLabel typeLabel = new JLabel("Type", JLabel.LEFT);
        typePane.add(typeLabel);

        JScrollPane typeScroll = new JScrollPane(outputTypeList, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        typeScroll.getVerticalScrollBar().setModel(descriptScroll.getVerticalScrollBar().getModel());
        typeScroll.addMouseListener(ml);
        typePane.add(typeScroll);

        typePane.setMinimumSize(new Dimension(125, 75));
        typePane.setPreferredSize(new Dimension(125, 75));
        typePane.setMaximumSize(new Dimension(125, Integer.MAX_VALUE));

        outputScroll = new JScrollBar(JScrollBar.VERTICAL);
        outputScroll.setModel(typeScroll.getVerticalScrollBar().getModel());

        messagePanel.add(outputScroll);
        messagePanel.add(typePane);
        messagePanel.add(descriptPane);

        JPanel buttonPanel = new JPanel(new GridLayout(1,3));
        
        hideWarningsBox = new JCheckBox("Hide warnings");
        hideWarningsBox.setActionCommand(HIDE);
        hideWarningsBox.addActionListener(controller);
        
        buttonPanel.add(hideWarningsBox);
        
        exportButton = new JButton("Export Result Details");
        exportButton.setFont(fontArial11B);
        exportButton.setActionCommand(EXPORT);
        exportButton.addActionListener(controller);
        exportButton.setEnabled(false);

        JPanel panel2 = new JPanel();
        panel2.setBorder(null);
        panel2.add(exportButton);
        
        buttonPanel.add(panel2);
        
        
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        JLabel emptyLabel = new JLabel(" ");
        emptyPanel.add(emptyLabel);
        buttonPanel.add(emptyLabel);

        resultDetailsPane.add(messagePanel);
        resultDetailsPane.add(buttonPanel);
        
        outputTypeList.addMouseListener(ml);
        outputInfoList.addMouseListener(ml);

        return resultDetailsPane;
    }

    public JDialog createWorkingPopUp(String title)
    {

        JLabel label = new JLabel("Please Wait... ");
        label.setFont(fontArial14);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setSize(275, 50);

        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(label);
        panel.add(progressBar);

        final JDialog dialog = new JDialog(this, title, false);
        dialog.getContentPane().add(panel);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);

        return dialog;
    }

    public void configAll(boolean enable)
    {

        load.setEnabled(enable);
        joinFiles.setEnabled(enable);
        validate.setEnabled(enable);
        submission.setEnabled(enable);
        exit.setEnabled(enable);
        userGuide.setEnabled(enable);

        sourceDir.setEnabled(enable);

        browseButton.setEnabled(enable);
        loadFilesButton.setEnabled(enable);
        excludeFilesButton.setEnabled(enable);
        includeFilesButton.setEnabled(enable);
        reloadButton.setEnabled(enable);
        validateButton.setEnabled(enable);
        createSubmissionButton.setEnabled(enable);
        exportButton.setEnabled(enable);
    }

    public void configStart()
    {

        load.setEnabled(true);
        joinFiles.setEnabled(true);
        exit.setEnabled(true);
        userGuide.setEnabled(true);

        sourceDir.setEnabled(true);

        browseButton.setEnabled(true);
        loadFilesButton.setEnabled(true);

        fileTree.clearSelection();
        outputModel.clear();
    }

    public void configLoad(FileNode root)
    {

        configStart();

        if(props != null)
        {
            props.setProperty("user.dir", sourceDir.getText());
            try
            {
                FileOutputStream writer = new FileOutputStream(System.getProperty("user.home", "") 
                        + File.separator + "brics.pref");
                props.store(writer, "--No Comment--");
                writer.close();
            }
            catch (FileNotFoundException e)
            {
                System.err.println("Cannot make new properties file");
            }
            catch (IOException e)
            {
                System.err.println("Cannot write to properties file");
            }
        }
        
        // Stay on after load
        excludeFilesButton.setEnabled(true);
        includeFilesButton.setEnabled(true);
        reloadButton.setEnabled(true);
        exportButton.setEnabled(true);
        createSubmissionButton.setEnabled(false);
        
        //Exclude unknowns automatically
        //Code taken from configTree as steps are pretty much similar
        /*Stack<FileNode> stack = new Stack<FileNode>();
        stack.push(root);
        while(!stack.isEmpty()){
            FileNode node = stack.pop();
            if (node.getType() != FileType.DIR && node.getType() == FileType.UNKNOWN)
            {
                node.exclude();
            }
            else
            {
                ArrayList<FileNode> children = node.getChildren();
                Collections.sort(children); // the comparison matches the displayed order
                // and I have to insert into the stack reverse order for DFS anyway
                for (int i = children.size() - 1; i >= 0; i--)
                {
                    stack.push(children.get(i));
                }
            }
        }*/

		if (root.hasValidFormStructure())
        {
            loaded = true;
            validateButton.setEnabled(true);
        }
        else
        {
            loaded = false;
            validateButton.setEnabled(false);
        }

        repaint();
    }

	public void configInclude(FileNode root, boolean revalidate)
    {
		if (revalidate) {
        	createSubmissionButton.setEnabled(false);
        	submission.setEnabled(false);
        }
		// newly added files/folders may come with errors, which will disable the validation button
		//if (root.hasValidFormStructure() && root.getErrorNum() == 0) {
		if (root.hasValidFormStructure()) { // as long as valid FS name in confirmed from CSV, the button should be
												// enabled.
			validateButton.setEnabled(true);
		} else {
			validateButton.setEnabled(false);
		}

		outputModel.clear();
        repaint();
    }

    public void configExclude(FileNode root, boolean revalidate)
    {

        excludeFilesButton.setEnabled(true);
        includeFilesButton.setEnabled(true);
        reloadButton.setEnabled(true);
        exportButton.setEnabled(true);
        //createSubmissionButton.setEnabled(false);

        //
		// if (loaded || root.getErrorNum() == 0)
		// if (root.hasValidFormStructureName() && root.getErrorNum() == 0)
		if (root.hasValidFormStructure())
        {
            validateButton.setEnabled(true);
        }
        else
        {
            validateButton.setEnabled(false);
        }
        if(revalidate){
        	createSubmissionButton.setEnabled(false);
        	submission.setEnabled(false);
        }
        
        outputModel.clear();
        repaint();
    }

    public void configValidate(FileNode root)
    {

        configStart();

        // Stay on after validate
        excludeFilesButton.setEnabled(true);
        includeFilesButton.setEnabled(true);
        reloadButton.setEnabled(true);
        exportButton.setEnabled(true);
        validateButton.setEnabled(true);

        if (root.getErrorNum() != 0)
        {
            createSubmissionButton.setEnabled(false);
        }
		// disable the button when none of valid FS are detected
		if (!root.hasValidFormStructure()) {
			validateButton.setEnabled(false);
		}

        configTree(root);
        repaint();
    }

    private void configTree(FileNode root)
    {

        Stack<FileNode> nodeStack = new Stack<FileNode>();
        nodeStack.push(root);
        while (!nodeStack.isEmpty())
        {
            FileNode node = nodeStack.pop();
            if (node.getType() != FileType.DIR && node.getErrorNum() > 0)
            {
                FileNode parent = (FileNode) node.getParent();
                fileTree.expandPath(parent.getTreePath());
                int row = fileTree.getRowForPath(node.getTreePath());
                List<Integer> selected = new ArrayList<Integer>();
                selected.add(row);
                setSelected(selected);
                break;
            }
            else
            {
                ArrayList<FileNode> children = node.getChildren();
                Collections.sort(children); // the comparison matches the displayed order
                // and I have to insert into the stack reverse order for DFS anyway
                for (int i = children.size() - 1; i >= 0; i--)
                {
                    nodeStack.push(children.get(i));
                }
            }
        }
    }

    public void repaint()
    {

        this.getContentPane().repaint();
    }

    public String getSourceDir()
    {

        return sourceDir.getText();
    }

    public void setSourceDir(String s)
    {

        sourceDir.setText(s);
    }

    public TreeModel getFileModel()
    {

        return fileModel;
    }

    public void setFileModel(DefaultTreeModel fileModel)
    {

        this.fileModel = fileModel;
        fileTree.setModel(fileModel);

        fileDetails = new TreeListModel(fileTree);
        fileTypeList.setModel(fileDetails);
        fileStructuresList.setModel(fileDetails);
		fileStatusList.setModel(fileDetails);
        fileResultList.setModel(fileDetails);
        fileSummaryList.setModel(fileDetails);
        repaint();
    }

    public ListModel getOutputModel()
    {

        return outputModel;
    }

    public void setOutputModel(DefaultListModel listModel)
    {

        outputTypeList.setModel(listModel);
        outputInfoList.setModel(listModel);
    }

    public List<Object> getSelected()
    {

        TreePath[] paths = fileTree.getSelectionPaths();
        List<Object> componentList = new ArrayList<Object>();

        for (int i = 0; i < fileTree.getSelectionCount(); i++)
        {
            componentList.add(paths[i].getLastPathComponent());
        }

        return componentList;
    }

    public void setSelected(List<Integer> selected)
    {

        int[] nums = new int[selected.size()];

        for (int i = 0; i < selected.size(); i++)
        {
            nums[i] = selected.get(i);
        }

        fileTree.setSelectionRows(nums);
        fileTree.requestFocusInWindow();
    }

    public void enableBuilding(boolean enabled)
    {

        createSubmissionButton.setEnabled(enabled);
        submission.setEnabled(enabled);
    }
    
    public void enableLoadButton(){
        loadFilesButton.setEnabled(true);
    }
    public boolean isHidden(){
        return hideWarningsBox.isSelected();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    
    {
        if(activeScroll != null)
        {
            int num = e.getWheelRotation();
            int res;
            int scale;
            if(activeScroll == fileScroll)
            {
                res = fileTree.getRowHeight();
                scale = fileTree.getRowCount();
            }
            else
            {
                res = fileTypeList.getFixedCellHeight();
                scale = outputInfoList.getModel().getSize();
            }
            //int res = e.getUnitsToScroll();
            int min = activeScroll.getMinimum();
            int max = activeScroll.getMaximum();
            scale = (int)Math.round(Math.log(scale)/Math.log(5));
            if(scale < 1) scale = 1;
            int move = num*res*scale;
            int currentValue = activeScroll.getValue();
            int newValue = Math.max(min, Math.min(max, currentValue + move));
            activeScroll.setValue(newValue);
        }
    }
}
