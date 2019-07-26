
package gov.nih.tbi.dictionary.validation.view;

import gov.nih.tbi.dictionary.validation.engine.TableValidator;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;

/**
 * This class allows users to identify files and joins them. It defaultly saves it as a tab-delimited file but can also
 * be saved as csv
 * 
 * @author pandyan
 * 
 */
public class JoinFiles extends JFrame implements MouseListener, ActionListener
{

    // Default
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(JoinFiles.class);

    /** path to ndar icon **/
    private static final String FRAME_ICON = "images/ndar_app_icon.gif";

    /** List of current files and folders to work with */
    private Vector<String> fileList;

    /** The JList of files **/
    private JList inputFileList;

    /** panels **/
    private JPanel topPanel, mainPanel, bottomPanel, buttonPanel;

    /** scroll pane **/
    private JScrollPane scrollPane;

    /** label **/
    private JLabel ndarLabel;

    /** JButtons **/
    private JButton addFileButton, removeFileButton, joinFilesButton, cancelButton, upButton, downButton;

    /** font **/
    private Font fontArial12 = new Font("Arial", Font.PLAIN, 12);

    /** font **/
    private final Font fontArial11B = new Font("Arial", Font.BOLD, 11);

    /** current dir **/
    private String currentDir = "";

    /** File chooser **/
    private JFileChooser joinChooser;

    /** path the saved joined file is **/
    private String defaultPath;

    /**
     * constructor
     */
    public JoinFiles()
    {

        fileList = new Vector<String>();
        init();
    }

    /**
     * init
     */
    public void init()
    {

        this.setIconImage(createImageIcon(FRAME_ICON, "Frame GIF").getImage());
        this.setTitle("Join Files");
        this.setSize(880, 550);
        this.setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();

        topPanel = new JPanel();
        mainPanel = new JPanel();
        bottomPanel = new JPanel();
        buttonPanel = new JPanel(new GridBagLayout());

        ndarLabel = new JLabel();
        ndarLabel.setFont(fontArial12);
        ndarLabel
                .setText("<html> Specify the order in which you would like the files to be joined. The file containing the leading <br> set of colums should be at the top of this list and the file containing the last set of columns <br> should be at the bottom. All files must have the same number of rows.  For more details <br> surrounding this functionality, please see the <a href=\"http://ndar.nih.gov/ndarpublicweb/Documents/NDAR_DataValidationSubmissionlUserGuide.pdf\">NDAR Data Submission User's Guide</a>.</html>");
        ndarLabel.addMouseListener(this);
        topPanel.add(ndarLabel);

        inputFileList = new JList(fileList);
        inputFileList.setVisibleRowCount(4);
        inputFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inputFileList.setMinimumSize(new Dimension(500, 150));
        inputFileList.setMaximumSize(new Dimension(500, 500));
        inputFileList.addMouseListener(this);
        scrollPane = new JScrollPane(inputFileList);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setMinimumSize(new Dimension(500, 150));
        scrollPane.setPreferredSize(new Dimension(500, 150));
        mainPanel.add(scrollPane);

        addFileButton = new JButton("Add File");
        addFileButton.addActionListener(this);
        addFileButton.setFont(fontArial11B);
        addFileButton.setActionCommand("addFile");
        bottomPanel.add(addFileButton);

        removeFileButton = new JButton("Remove File");
        removeFileButton.addActionListener(this);
        removeFileButton.setEnabled(false);
        removeFileButton.setFont(fontArial11B);
        removeFileButton.setActionCommand("removeFile");
        bottomPanel.add(removeFileButton);

        joinFilesButton = new JButton("Join Files");
        joinFilesButton.addActionListener(this);
        joinFilesButton.setFont(fontArial11B);
        joinFilesButton.setEnabled(false);
        joinFilesButton.setActionCommand("joinFiles");
        bottomPanel.add(joinFilesButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setFont(fontArial11B);
        cancelButton.setActionCommand("cancel");
        bottomPanel.add(cancelButton);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 25, 5);
        upButton = new JButton("   Up   ");
        upButton.addActionListener(this);
        upButton.setEnabled(false);
        upButton.setFont(fontArial11B);
        upButton.setActionCommand("up");
        upButton.setToolTipText("Move selected file up the list");

        buttonPanel.add(upButton, gbc);

        gbc.gridy = 1;
        downButton = new JButton("Down");
        downButton.addActionListener(this);
        downButton.setFont(fontArial11B);
        downButton.setEnabled(false);
        downButton.setActionCommand("down");
        downButton.setToolTipText("move selected file down the list");
        buttonPanel.add(downButton, gbc);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        this.pack();
        this.setVisible(true);
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     **/
    protected static ImageIcon createImageIcon(String path, String description)
    {

        java.net.URL imgURL = ValidationClient.class.getClassLoader().getResource(path);
        if (imgURL != null)
        {
            return new ImageIcon(imgURL, description);
        }
        else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * mouse clicked
     */
    public void mouseClicked(MouseEvent e)
    {

        Object source = e.getSource();

        if (source == ndarLabel)
        {
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                openURL("http://ndar.nih.gov/ndarpublicweb/Documents/NDAR_DataValidationSubmissionlUserGuide.pdf");
            }
        }
        else
            if (source == inputFileList)
            {
                if (fileList.size() == 0 || fileList.size() == 1)
                {
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                }
                else
                {
                    int index = inputFileList.getSelectedIndex();
                    if (index == 0)
                    {
                        upButton.setEnabled(false);
                        downButton.setEnabled(true);
                    }
                    else
                        if (index == fileList.size() - 1)
                        {
                            upButton.setEnabled(true);
                            downButton.setEnabled(false);
                        }
                        else
                        {
                            upButton.setEnabled(true);
                            downButton.setEnabled(true);
                        }
                }

            }
    }

    /**
     * moves file up list
     */
    private void up()
    {

        int index = inputFileList.getSelectedIndex();
        int newIndex = index - 1;
        String value = fileList.remove(index);
        fileList.insertElementAt(value, newIndex);
        inputFileList.setSelectedIndex(newIndex);
        inputFileList.updateUI();
        if (newIndex == 0)
        {
            upButton.setEnabled(false);
        }
        else
        {
            upButton.setEnabled(true);
        }
        downButton.setEnabled(true);
    }

    /**
     * moves file down list
     */
    private void down()
    {

        int index = inputFileList.getSelectedIndex();
        int newIndex = index + 1;
        String value = fileList.remove(index);
        fileList.insertElementAt(value, newIndex);
        inputFileList.setSelectedIndex(newIndex);
        inputFileList.updateUI();
        if (newIndex == fileList.size() - 1)
        {
            downButton.setEnabled(false);
        }
        else
        {
            downButton.setEnabled(true);
        }
        upButton.setEnabled(true);

    }

    /**
     * mouse entered
     */
    public void mouseEntered(MouseEvent e)
    {

        Object source = e.getSource();

        if (source == ndarLabel)
        {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

    }

    /**
     * mouse exited
     */
    public void mouseExited(MouseEvent e)
    {

        Object source = e.getSource();

        if (source == ndarLabel)
        {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

    }

    /**
     * mouse pressed
     */
    public void mousePressed(MouseEvent e)
    {

        // TODO Auto-generated method stub

    }

    /**
     * mouse released
     */
    public void mouseReleased(MouseEvent e)
    {

        // TODO Auto-generated method stub

    }

    /**
     * action performed
     */
    public void actionPerformed(ActionEvent e)
    {

        String command = e.getActionCommand();
        if (command.equalsIgnoreCase("cancel"))
        {
            dispose();
        }
        else
            if (command.equals("addFile"))
            {
                JFileChooser chooser = new JFileChooser();
                if (!currentDir.equals(""))
                {
                    chooser.setCurrentDirectory(new File(currentDir));
                }
                chooser.setMultiSelectionEnabled(false);
                chooser.setDialogTitle("Select file");
                chooser.setFileFilter(new MyFilter());

                int returnValue = chooser.showDialog(this, "OK");
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    fileList.add(path);
                    inputFileList.updateUI();
                    currentDir = chooser.getSelectedFile().getAbsolutePath();
                    if (fileList.size() >= 2)
                    {
                        joinFilesButton.setEnabled(true);
                        upButton.setEnabled(true);
                        downButton.setEnabled(false);
                    }
                    inputFileList.setSelectedIndex(fileList.size() - 1);
                    removeFileButton.setEnabled(true);

                }
            }
            else
                if (command.equals("removeFile"))
                {
                    int index = inputFileList.getSelectedIndex();
                    fileList.remove(inputFileList.getSelectedValue());
                    inputFileList.updateUI();
                    if (fileList.size() < 2)
                    {
                        joinFilesButton.setEnabled(false);
                        upButton.setEnabled(false);
                        downButton.setEnabled(false);
                    }
                    if (fileList.size() > 0)
                    {
                        if (index < fileList.size())
                        {
                            inputFileList.setSelectedIndex(index);
                            if (index == 0)
                            {
                                upButton.setEnabled(false);
                            }
                            else
                            {
                                upButton.setEnabled(true);
                            }
                            if (index == fileList.size() - 1)
                            {
                                downButton.setEnabled(false);
                            }
                            else
                            {
                                downButton.setEnabled(true);
                            }
                        }
                        else
                        {
                            inputFileList.setSelectedIndex(fileList.size() - 1);
                            if (fileList.size() == 1)
                            {
                                upButton.setEnabled(false);
                            }
                            else
                            {
                                upButton.setEnabled(true);
                            }
                            downButton.setEnabled(false);
                        }

                    }
                    else
                    {
                        removeFileButton.setEnabled(false);
                    }
                }
                else
                    if (command.equals("up"))
                    {
                        up();
                    }
                    else
                        if (command.equals("down"))
                        {
                            down();
                        }
                        else
                            if (command.equals("joinFiles"))
                            {
                                // first we will check if all files have same num rows and all have same delimiter
                                boolean[] areCommaDelimited = new boolean[fileList.size()];
                                int[] numRows = new int[fileList.size()];
                                ArrayList<String> invalidFiles = new ArrayList<String>();
                                for (int i = 0; i < fileList.size(); i++)
                                {
                                    String filePath = fileList.get(i);
                                    try
                                    {
                                        FileInputStream inputStream = new FileInputStream(filePath);
                                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                                        int counter = 0;
                                        String line;
                                        boolean isCommaDelimited = false;
                                        while ((line = br.readLine()) != null && (!(line.trim().equals(""))))
                                        {
                                            if (counter == 0)
                                            {
                                                // check if its tab delimited or comma delimited
                                                line = line.replace(",", " , ");
                                                String[] fields = line.split(",");
                                                if (fields.length == 1)
                                                {
                                                    // must be tab-delimted
                                                    line = line.replace("\t", " \t ");
                                                    fields = line.split("\t");
                                                    if (fields.length == 1)
                                                    {
                                                        // INVALID FILE...not comma delimited and not tab delimited
                                                        invalidFiles.add(filePath);
                                                    }
                                                    else
                                                    {
                                                        isCommaDelimited = false;
                                                    }
                                                }
                                                else
                                                {
                                                    isCommaDelimited = true;
                                                }
                                            }
                                            counter++;
                                        }
                                        areCommaDelimited[i] = isCommaDelimited;
                                        numRows[i] = counter;
                                        br.close();
                                    }
                                    catch (Exception ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                }
                                // display invalid files and return
                                if (invalidFiles.size() > 0)
                                {
                                    String line = "";
                                    for (int i = 0; i < invalidFiles.size(); i++)
                                    {
                                        line = line + "- " + invalidFiles.get(i) + "\n";
                                    }

                                    JOptionPane.showMessageDialog(this,
                                            "The following files are not in comma or tab delimited format: \n" + line,
                                            "Error in Joining", JOptionPane.INFORMATION_MESSAGE);
                                    return;
                                }

                                // now go through the areCommaDelimited and numRows arrays to make sure they are all the
                                // same
                                boolean testVal = false;
                                boolean success = true;
                                for (int i = 0; i < areCommaDelimited.length; i++)
                                {
                                    if (i == 0)
                                    {
                                        testVal = areCommaDelimited[i];
                                    }
                                    else
                                    {
                                        boolean val = areCommaDelimited[i];
                                        if (val != testVal)
                                        {
                                            success = false;
                                            break;
                                        }
                                    }
                                }
                                if (!success)
                                {
                                    // DISPLAY ERROR INDICATING THAT FILES DO NOT SEEM TO HAVE SAME DELIMITER
                                    JOptionPane
                                            .showMessageDialog(
                                                    this,
                                                    "NDAR cannot join these files because each file does not have the same delimiter. \n Please ensure that all files you are attempting to join have the same delimiter",
                                                    "Error in Joining", JOptionPane.INFORMATION_MESSAGE);
                                    return;
                                }

                                int testInt = 0;
                                success = true;
                                for (int i = 0; i < numRows.length; i++)
                                {
                                    if (i == 0)
                                    {
                                        testInt = numRows[i];
                                    }
                                    else
                                    {
                                        int val = numRows[i];
                                        if (val != testInt)
                                        {
                                            success = false;
                                            break;
                                        }
                                    }
                                }

                                if (!success)
                                {
                                    // DISPLAY ERROR INDICATING THAT FILES DO NOT SEEM TO HAVE SAME NUMBER OF ROWS
                                    JOptionPane
                                            .showMessageDialog(
                                                    this,
                                                    "NDAR cannot join these files because each file does not have the same number of rows. \n Please ensure that all files you are attempting to join have the exact same number of rows",
                                                    "Error in Joining", JOptionPane.INFORMATION_MESSAGE);
                                    return;
                                }

                                // now we can join files
                                joinFiles(testVal);
                            }
                            else
                                if (command.equals("saveAsTab"))
                                {
                                    defaultPath = defaultPath.replace(".csv", ".txt");
                                    File file = new File(defaultPath);
                                    joinChooser.setCurrentDirectory(file);
                                    joinChooser.setSelectedFile(file);
                                    joinChooser.validate();
                                    joinChooser.repaint();
                                }
                                else
                                    if (command.equals("saveAsComma"))
                                    {
                                        defaultPath = defaultPath.replace(".txt", ".csv");
                                        File file = new File(defaultPath);
                                        joinChooser.setCurrentDirectory(file);
                                        joinChooser.setSelectedFile(file);
                                        joinChooser.validate();
                                        joinChooser.repaint();
                                    }

    }

    /**
     * joins the files
     */
    private void joinFiles(boolean isCommaDelimited)
    {

        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton saveAsTabButton = new JRadioButton("Save as tab delimited");
        saveAsTabButton.addActionListener(this);
        saveAsTabButton.setActionCommand("saveAsTab");
        buttonGroup.add(saveAsTabButton);
        JRadioButton saveAsCommaButton = new JRadioButton("Save as comma delimited");
        saveAsCommaButton.addActionListener(this);
        saveAsCommaButton.setActionCommand("saveAsComma");
        buttonGroup.add(saveAsCommaButton);
        if (isCommaDelimited)
        {
            saveAsCommaButton.setSelected(true);
        }
        else
        {
            saveAsTabButton.setSelected(true);
        }
        JPanel radioPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        radioPanel.add(saveAsTabButton, gbc);
        gbc.gridy = 1;
        radioPanel.add(saveAsCommaButton, gbc);

        joinChooser = new JFileChooser();
        joinChooser.setFileFilter(new MyFilter());
        joinChooser.setAccessory(radioPanel);
        if (!currentDir.equals(""))
        {
            joinChooser.setCurrentDirectory(new File(currentDir));
        }

        String s = fileList.get(0);
        String name = s.substring(s.lastIndexOf(File.separator) + 1, s.lastIndexOf("."));
        String extension = s.substring(s.lastIndexOf("."), s.length());
        String defaultName = name + "_concat";
        defaultPath = s.substring(0, s.lastIndexOf(File.separator)) + File.separator + defaultName + extension;
        File file = new File(defaultPath);
        joinChooser.setSelectedFile(file);
        File outputFile;
        joinChooser.setDialogTitle("Save concatenated file");

        int returnValue = joinChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            outputFile = joinChooser.getSelectedFile();

            // now we can begin the concatenation
            try
            {
                // boolean isCommaDelimited = testVal;

                FileOutputStream outputStream = new FileOutputStream(outputFile);
                PrintStream printStream = new PrintStream(outputStream);

                FileInputStream[] inputStreams = new FileInputStream[fileList.size()];
                BufferedReader[] readers = new BufferedReader[fileList.size()];

                for (int i = 0; i < fileList.size(); i++)
                {
                    inputStreams[i] = new FileInputStream(fileList.get(i));
                    readers[i] = new BufferedReader(new InputStreamReader(inputStreams[i]));
                }

                String line;
                int counter = 0;
                while ((line = readers[0].readLine()) != null && (!(line.trim().equals(""))))
                {

                    if (counter != 0)
                    {
                        printStream.print("\n");
                    }
                    if (isCommaDelimited && saveAsTabButton.isSelected())
                    {
                        line = line.replace(",", "\t");
                        printStream.print(line);
                    }
                    else
                        if (!isCommaDelimited && saveAsCommaButton.isSelected())
                        {
                            line = line.replace("\t", ",");
                            printStream.print(line);
                        }
                        else
                        {
                            printStream.print(line);
                        }
                    for (int i = 1; i < readers.length; i++)
                    {
                        line = readers[i].readLine();
                        if (isCommaDelimited)
                        {
                            if (line.startsWith(","))
                            {
                                // printStream.print(line);
                                if (isCommaDelimited && saveAsTabButton.isSelected())
                                {
                                    line = line.replace(",", "\t");
                                    printStream.print(line);
                                }
                                else
                                {
                                    printStream.print(line);
                                }
                            }
                            else
                            {
                                line = "," + line;
                                // printStream.print(line);
                                if (isCommaDelimited && saveAsTabButton.isSelected())
                                {
                                    line = line.replace(",", "\t");
                                    printStream.print(line);
                                }
                                else
                                {
                                    printStream.print(line);
                                }
                            }
                        }
                        else
                        {
                            if (line.startsWith("\t"))
                            {
                                // printStream.print(line);
                                if (!isCommaDelimited && saveAsCommaButton.isSelected())
                                {
                                    line = line.replace("\t", ",");
                                    printStream.print(line);
                                }
                                else
                                {
                                    printStream.print(line);
                                }
                            }
                            else
                            {
                                line = "\t" + line;
                                // printStream.print(line);
                                if (!isCommaDelimited && saveAsCommaButton.isSelected())
                                {
                                    line = line.replace("\t", ",");
                                    printStream.print(line);
                                }
                                else
                                {
                                    printStream.print(line);
                                }
                            }
                        }
                    }

                    counter++;

                }

                this.dispose();

                for (int i = 0; i < readers.length; i++)
                {
                    readers[i].close();
                }
                printStream.close();
            }
            catch (Exception e)
            {

            }

        }
    }

    /**
     * Launches browser...code obtained from: Bare Bones Browser Launch by Dem Pilafian Web Page Copyright (c) 2007
     * Center Key Software Source Code and Javadoc are Public Domain http://www.centerkey.com/java/browser
     * 
     * @param url
     */
    private void openURL(String url)
    {

        String osName = System.getProperty("os.name");
        try
        {
            if (osName.startsWith("Mac OS"))
            {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
                openURL.invoke(null, new Object[] { url });
            }
            else
                if (osName.startsWith("Windows"))
                {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                }
                else
                { // assume Unix or Linux
                    String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                    String browser = null;
                    for (int count = 0; count < browsers.length && browser == null; count++)
                    {
                        if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
                        {
                            browser = browsers[count];
                        }
                    }
                    if (browser == null)
                    {
                    	logger.error("Can not find web browser");
                    }
                    else
                    {
                        Runtime.getRuntime().exec(new String[] { browser, url });
                    }
                }
        }
        catch (Exception e)
        {
        	logger.error("Can not find web browser");
        }
    }

    // --------------------------------INNER CLASS--------------------------------------------------------
    class MyFilter extends javax.swing.filechooser.FileFilter
    {

        public boolean accept(File file)
        {

            if (file.isDirectory())
            {
                return true;
            }
            String filename = file.getName();
            return (filename.endsWith(".txt") || filename.endsWith(".csv"));
        }

        public String getDescription()
        {

            return "*.txt,*.csv";
        }
    }

}
