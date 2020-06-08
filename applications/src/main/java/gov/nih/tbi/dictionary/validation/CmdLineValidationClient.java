
package gov.nih.tbi.dictionary.validation;

import gov.nih.tbi.account.service.complex.AccountManagerImpl;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.dictionary.validation.model.DataStructureTable;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput;
import gov.nih.tbi.dictionary.validation.view.ValidationClient;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.xml.soap.Node;

public class CmdLineValidationClient
{

    private static final String promptHelpText = "Valid Commands:\n\n"
            + "HELP\t\tDisplays this help message.\n"
            + "LOAD\t\tLoads the directory tree into the tool. Necessary after altering any data files or adding/removing files.\n"
            + "VALIDATE\t\tVerifies data files and identifies associated files. This action can only be taken after an error free build.\n"
            + "BUILD\t\tCreates data file and ticket file for the current submission file. This action can only be taken after successful validation.\n"
            + "SHOW\t\tOutputs the directory tree.\n"
            + "EXIT\t\tExits this tool. All loaded data will be unloaded from memory.\n\n"
            + "*These commands are NOT case sensitive.";

    private static final String usageMessage = "\nCommand Line Validation Tool\n\n"
            + "\tThe Command Line Validation Tool provides a method for users to validate their data for the FITBIR Informatics System in "
            + "environments where a traditional GUI is not available.  This tool does not provide a robust implementation of the validation tool. "
            + "The GUI version should be used in place of the command line tool where possible.\n"
            + "\tWhen run, the tool will attempt to load, validate, and build a submission package for the specified directory. If there are "
            + "errors at any point in the process, the errors will be displayed and the user will be presented with a prompt. From the "
            + "prompt, the user may reload, validate, and build data, as well as exclude/include files in the directory.\n\n"
            + "Windows: CmdLineTool.bat [-d DIRECTORY] [-h]\n"
            + "UNIX/Mac: CmdLineTool.sh [-d DIRECTORY] [-h]\n\n"
            + "-h\tDisplay this usage message.\n"
            + "-d\tUse this flag to specify a root directory. If a directory is not specified, the user will be prompted to enter one after "
            + "running the tool.\n\n" + "USAGE:\n" + promptHelpText;

    private static final String promptMessage = "::> Please enter a command: ";
    private static final String promptBadCmd = "Unrecognized command. Please type 'help' for usage options.";
    private static final String promptUsername = "Please enter a username: ";
    private static final String promptPassword = "Please enter a password: ";
    private static final String promptDirectory = "Please specify a directory: ";

    private static final String LINE_BREAK = "-------------------------------------------------------------------------------";
    private static final String CONNECTING = "Connecting...";
    private static final String CONNECTED = "Connected...";
    private static final String LOADING = "Loading...";
    private static final String VALIDATING = "Validating...";
    private static final String BUILDING = "Building Submission Package...";
    private static final String COMPLETE = "Completed...";
    private static final String EXITING = "Exiting...";

    // Default
    private static final long serialVersionUID = 1L;

    // Constants
    private static final long STARTUP_TIMESTAMP = System.currentTimeMillis();
    private static final int EXIT_OK = 0;
    private static final int EXIT_ERR_ARG = -1;
    private static final int EXIT_ERR_INPUT = -2;
    private static final int EXIT_ERR_LOAD = -3;
    private static final int EXIT_ERR_JAVA_VERSION = -4;
    private static final int EXIT_ERR_VAL = -5;
    private static final int EXIT_ERR_NET = -6;
    private static final int EXIT_ERR_BUILD = -7;
    private static final int EXIT_ERR_UNKNOWN = -8;

    private static final int TASK_START = -1;
    private static final int TASK_DONE = 0;
    private static final int TASK_PROMPT = 1;
    private static final int TASK_LOAD = 2;
    private static final int TASK_VALIDATE = 3;
    private static final int TASK_BUILD = 4;
    private static final int TASK_TREE = 5;
    private static final int TASK_HELP = 6;

    private static final String ERR_INPUT = "IO error trying to read input.";
    private static final String ERR_UNKNOWN = "An unknown error has occured, please contact the administrator.";
    private static final String ERR_UNVALIDATED = "User has not validated data. Please validate all data before building.";
    private static final String ERR_UNLOADED = "User had errors on load. User must load error-free data before validating.";

    private static final PrintStream originalStdErr = System.err;
    private static PrintStream detailedLogOutput;

    private static String rootDirectoryPath;
    private static String bricsUrl;
    private static String ddtUrl;
    private static String version;
    private static String username;
    private static String password;
    private static int task = TASK_START;

    private static boolean prompted = false;
    private static boolean valid = false;
    private static boolean loaded = false;

    private static ValidationController controller;
    
    private static final boolean IS_COMING_FROM_PROFORMS = false;

    public static void main(String[] args)
    {

        checkJavaVersion();

        // Read inputs
        try
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equalsIgnoreCase("-h"))
                {
                    printUsage(EXIT_OK);
                }
                else
                    if (args[i].equalsIgnoreCase("-r"))
                    {
                        bricsUrl = args[++i];
                    }
                    else
                        if (args[i].equalsIgnoreCase("-d"))
                        {
                            rootDirectoryPath = args[++i];
                        }
                        else
                        {
                            outputHardErrorLine("Unrecognized option: " + args[i]);
                            printUsage(EXIT_ERR_ARG);
                        }
            }
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            outputHardErrorLine("Error encountered parsing program arguments.");
            printUsage(EXIT_ERR_ARG);
        }

        // User cannot supply a password without a username
        if (username == null && password != null)
        {
            outputHardErrorLine("Cannot provide a password without a username.");
            printUsage(EXIT_ERR_ARG);
        }

        // URL is a required argument
        if (bricsUrl == null)
        {
            outputHardErrorLine("Missing URL");
            System.exit(EXIT_ERR_ARG);
        }

        // If user name is optional then prompt user
        if (username == null)
        {
            System.out.print(promptUsername);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            try
            {
                username = br.readLine();
            }
            catch (IOException e)
            {
                outputHardErrorLine(ERR_INPUT);
                System.exit(EXIT_ERR_INPUT);
            }

        }

        // If password was not provided then prompt
        if (password == null)
        {

            // Get the password
            Console cons;
            char[] passwd;
            // String rawPassword = "Pa$$word";
            String rawPassword = null;
            if (System.console() == null)
            {
                outputHardErrorLine("This tool must be launched from the console.");
                System.exit(EXIT_ERR_UNKNOWN);
            }
            if ((cons = System.console()) != null && (passwd = cons.readPassword("%s", promptPassword)) != null)
            {
                rawPassword = new String(passwd);
                java.util.Arrays.fill(passwd, ' ');
            }

            // Hash and convert the password
            byte[] raw = null;
            try
            {
                raw = MessageDigest.getInstance("SHA-256").digest(rawPassword.getBytes());
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
                System.exit(EXIT_ERR_UNKNOWN);
            }
            byte[] passwordArray = new byte[raw.length];
            for (int i = 0; i < raw.length; i++)
            {
                passwordArray[i] = raw[i];
            }

            password = HashMethods.getServerHash(username, HashMethods.convertFromByte(passwordArray));

        }

        // rootDirectoryPath is optional. If it is missing then prompt for one.
        if (rootDirectoryPath == null)
        {
            System.out.print(promptDirectory);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            try
            {
                rootDirectoryPath = br.readLine();
            }
            catch (IOException e)
            {
                outputHardErrorLine(ERR_INPUT);
                System.exit(EXIT_ERR_INPUT);
            }
        }

        File rootDirectory = new File(rootDirectoryPath);
        if (!rootDirectory.exists() || !rootDirectory.isDirectory())
        {
            outputHardErrorLine("Invalid directory: " + rootDirectoryPath);
            printUsage(EXIT_ERR_ARG);
        }

        outputLine(CONNECTING);
        try
        {
            controller = new ValidationController(null, ddtUrl, bricsUrl, username, password,IS_COMING_FROM_PROFORMS);
        }
        catch (Exception e)
        {
            outputHardErrorLine("Unable to connect to FITBIR. "
                    + "If the problem persists please contact your systems administrator.");
            System.exit(EXIT_ERR_NET);
        }
        outputLine(CONNECTED);

        task = load();
        while (task != TASK_DONE)
        {
            switch (task)
            {
            case TASK_PROMPT:
                task = prompt();
                break;
            case TASK_HELP:
                task = help();
                break;
            case TASK_LOAD:
                task = load();
                break;
            case TASK_VALIDATE:
                task = validate();
                break;
            case TASK_BUILD:
                task = build();
                break;
            case TASK_TREE:
                task = tree();
                break;
            default:
                outputHardErrorLine(ERR_UNKNOWN);
                System.exit(EXIT_ERR_UNKNOWN);
            }
        }

        outputLine(EXITING);
        System.exit(EXIT_OK);
    }

    public static int prompt()
    {

        prompted = true;
        System.out.print(promptMessage);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String input = null;
        try
        {
            input = br.readLine();
        }
        catch (IOException e)
        {
            outputHardErrorLine(ERR_INPUT);
            System.exit(EXIT_ERR_INPUT);
        }
        outputLine("Command: " + input);
        String[] inputs = input.split(" ", 2);
        if (inputs[0].equalsIgnoreCase("load"))
        {
            return TASK_LOAD;
        }
        else
            if (inputs[0].equalsIgnoreCase("validate"))
            {
                return TASK_VALIDATE;
            }
            else
                if (inputs[0].equalsIgnoreCase("build"))
                {
                    return TASK_BUILD;
                }
                else
                    if (inputs[0].equalsIgnoreCase("exit"))
                    {
                        return TASK_DONE;
                    }
                    else
                        if (inputs[0].equalsIgnoreCase("help"))
                        {
                            return TASK_HELP;
                        }
                        else
                            if (inputs[0].equalsIgnoreCase("show"))
                            {
                                return TASK_TREE;
                            }
                            else
                            {
                                System.out.println(promptBadCmd);
                                return TASK_PROMPT;
                            }
    }

    public static int help()
    {

        System.out.println(promptHelpText);
        return TASK_PROMPT;
    }

    public static int tree()
    {

        outputDirTree(controller.getSubmission().getRoot(), 0);
        return TASK_PROMPT;
    }

    public static int load()
    {

        outputLine(LINE_BREAK);
        outputLine(LOADING);
        valid = false;
        loaded = false;

        String errorMessage = controller.load(rootDirectoryPath);
        outputDirTree(controller.getSubmission().getRoot(), 0);
        outputSubmissionErrors(controller.getSubmission(), controller.getSubmission().getRoot(), true);
        if (errorMessage != null)
        {
            outputHardErrorLine(errorMessage);
            outputLine(LINE_BREAK);
            return TASK_PROMPT;

        }

        if (controller.getSubmission().getRoot().getErrorNum() == 0)
        {
            loaded = true;
        }
        else
        {
            outputLine(COMPLETE);
            outputLine(LINE_BREAK);
            return TASK_PROMPT;
        }
        // Only get here if data was valid.
        if (!prompted)
        {
            outputLine(COMPLETE);
            outputLine(LINE_BREAK);
            return TASK_VALIDATE;
        }
        else
        {
            outputLine(COMPLETE);
            outputLine(LINE_BREAK);
            return TASK_PROMPT;
        }
    }

    public static int validate()
    {

        outputLine(LINE_BREAK);
        outputLine(VALIDATING);
        if (!loaded)
        {
            outputHardErrorLine(ERR_UNLOADED);
            outputLine(LINE_BREAK);
            return TASK_PROMPT;
        }
        valid = false;
        // Validation
        String errorMessage = controller.validate();
        DataSubmission submission = controller.getSubmission();
        if (!errorMessage.startsWith("All files are", 0))
        {
            outputHardErrorLine(errorMessage);
        }
        else
        {
            outputLine(errorMessage);
        }
        // There were errors in the submission. Print them and exit
        outputSubmissionErrors(submission, submission.getRoot(), false);
        if (!(submission.getRoot().isValid() && !submission.hasDrafts()))
        {
            outputLine(COMPLETE);
            outputLine(LINE_BREAK);
            return TASK_PROMPT;
        }
        else
        {
            // Were good
            valid = true;
            if (prompted)
            {
                outputLine(COMPLETE);
                outputLine(LINE_BREAK);
                return TASK_PROMPT;
            }
            else
            {
                outputLine(COMPLETE);
                outputLine(LINE_BREAK);
                return TASK_BUILD;
            }
        }
    }

    public static int build()
    {

        outputLine(LINE_BREAK);
        outputLine(BUILDING);
        if (!valid)
        {
            outputHardErrorLine(ERR_UNVALIDATED);
            return TASK_PROMPT;
        }
        valid = false;
        String errorMessage = controller.buildSubmission(rootDirectoryPath,true,null);
        if (!errorMessage.startsWith("A new submission", 0))
        {
            outputHardErrorLine(errorMessage);
            outputLine(LINE_BREAK);
            return TASK_PROMPT;
        }
        else
        {
            outputLine(errorMessage);
        }

        if (prompted)
        {
            outputLine(COMPLETE);
            outputLine(LINE_BREAK);
            return TASK_PROMPT;
        }
        else
        {
            outputLine(COMPLETE);
            outputLine(LINE_BREAK);
            return TASK_DONE;
        }
    }

    private static void outputSubmissionErrors(DataSubmission submission, FileNode root, boolean onLoad)
    {

        outputLine("RESULTS:");
        if (!root.isValid())
        {
            if (onLoad)
            {
                outputLine("\nYour data was loaded with the following ERRORS.");
            }
            else
            {
                outputLine("\nThe following ERRORS were found during validation.");
            }
        }
        else
        {
            if (submission.hasDrafts())
            {
                outputLine("\nNo errors found, please publish all form structures before building your submission.");
            }
            else
            {
                outputLine("\nNo errors found.");
            }
        }
        outputErrors(submission, root, true);

        if (root.getWarnNum() != 0)
        {
            if (onLoad)
            {
                outputLine("\nYour data was loaded with the following WARNINGS.");
            }
            else
            {
                outputLine("\nThe following WARNINGS were found during validation.");
            }
        }
        else
        {
            outputLine("\nNo warnings found.");
        }
        outputErrors(submission, root, false);
    }

    private static void outputErrors(DataSubmission submission, FileNode node, boolean warnings)
    {

        DataStructureTable table = submission.getFileData(node);
        // If there is a data table for this file
        if (table != null)
        {
            TreeSet<ValidationOutput> output;
            if (warnings)
                output = table.getWarnings();
            else
                output = table.getErrors();
            for (ValidationOutput o : output)
            {
                outputLine(o.getTypeString() + ": " + o.toString());
            }
        }
        // Recursively output children's errors
        for (int i = 0; i < node.getChildCount(); i++)
        {
            outputErrors(submission, (FileNode) node.getChildAt(i), warnings);
        }
    }

    private static void checkJavaVersion()
    {

        String javaVersion = System.getProperty("java.version");

        if (javaVersion.equals(""))
        {
            outputHardErrorLine("Java version 1.5 or higher is required for Validation tool. Please install it from http://java.sun.com/");
            // cleanup();
            System.exit(EXIT_ERR_JAVA_VERSION);
        }

        if (!(javaVersion.startsWith("1.5") || javaVersion.startsWith("1.6") || javaVersion.startsWith("1.7")))
        {
            outputHardErrorLine("Java version 1.5 or higher is required for Validation tool. Please install it from http://java.sun.com/");
            // cleanup();
            System.exit(EXIT_ERR_JAVA_VERSION);
        }
    }

    private static void printUsage(int exitValue)
    {

        outputHardErrorLine(usageMessage);
        System.exit(exitValue);
    }

    private static final void outputHardErrorLine(String msg)
    {

        // disableStdErrRedirection();
        System.err.println(msg);
        outputLogLine(msg);
        // enableStdErrRedirection();
    }

    private static final void outputHardErrorLine(String msg, Exception e)
    {

        // disableStdErrRedirection();
        System.err.println(msg);
        outputLogLine(msg);
        e.printStackTrace(System.err);
        // e.printStackTrace(detailedLogOutput);
        // enableStdErrRedirection();
    }

    private static final void outputHardError(Exception e)
    {

        // disableStdErrRedirection();
        e.printStackTrace(System.err);
        // e.printStackTrace(detailedLogOutput);
        // enableStdErrRedirection();
    }

    private static final void disableStdErrRedirection()
    {

        System.setErr(originalStdErr);
    }

    private static final void outputDirTree(FileNode node, int level)
    {

        String output = "";
        if (node.isDirectory())
        {
            for (int i = 0; i < level; i++)
            {
                output = output + "\t";
            }
            if (level == 0)
            {
                output = output + "[ROOT] ";
            }
            else
            {
                output = output + "[DIR] ";
            }
            output = output + node.getName() + "\\";
            // Do not iterate over if this file is excluded
            if (!node.isIncluded())
            {
                output = output + " [EXCLUDED]";
                System.out.println(output);
                return;
            }
            System.out.println(output);

            // Iterate over children
            int associate = 0;
            int unknown = 0;
            int rule = 0;
            for (FileNode n : node.getChildren())
            {
                // recursively calculate child
                outputDirTree(n, level + 1);
                if (n.isDirectory())
                {
                    continue;
                }
                if (FileType.UNKNOWN.equals(n.getType()))
                {
                    unknown++;
                }
                else
                    if (FileType.ASSOCIATED.equals(n.getType()))
                    {
                        associate++;
                    }
                    else
                        if (FileType.TRANSLATION_RULE.equals(n.getType()))
                        {
                            rule++;
                        }
            }
            // Output associate/unknown/rule outputs
            if (associate > 0)
            {
                String out = "";
                for (int i = 0; i < (level + 1); i++)
                {
                    out = out + "\t";
                }
                out = out + "[" + associate + " ASSOCIATED FILES]";
                System.out.println(out);
            }
            if (rule > 0)
            {
                String out = "";
                for (int i = 0; i < (level + 1); i++)
                {
                    out = out + "\t";
                }
                out = out + "[" + rule + " TRANSLATION RULES]";
                System.out.println(out);
            }
            if (unknown > 0)
            {
                String out = "";
                for (int i = 0; i < (level + 1); i++)
                {
                    out = out + "\t";
                }
                out = out + "[" + unknown + " UNKNOWN FILES]";
                System.out.println(out);
            }

        }
        else
            if (FileType.CSV.equals(node.getType()) || FileType.TAB.equals(node.getType())
                    || FileType.XML.equals(node.getType()))
            {
                for (int i = 0; i < level; i++)
                {
                    output = output + "\t";
                }
                output = output + node.getName();
                System.out.println(output);
            }
    }

    private static final void outputLine(String msg)
    {

        System.out.println(msg);
        outputLogLine(msg);
    }

    public static final void outputLogLine(String msg)
    {

        if (detailedLogOutput == null)
        {
            createLogOutputStream();
        }

        detailedLogOutput.println(msg);
    }

    private static final void enableStdErrRedirection()
    {

        if (detailedLogOutput == null)
        {
            createLogOutputStream();
        }

        System.setErr(detailedLogOutput);
    }

    private static final void createLogOutputStream()
    {

        try
        {
            FileOutputStream log;

            boolean validDirectory = true;
            if (rootDirectoryPath == null)
            {
                validDirectory = false;
            }
            else
            {
                File rootDirectory = new File(rootDirectoryPath);
                if (!rootDirectory.exists() || !rootDirectory.isDirectory())
                {
                    validDirectory = false;
                }
            }

            if (rootDirectoryPath == null || !validDirectory)
            {
                log = new FileOutputStream("output_log_" + STARTUP_TIMESTAMP + ".txt");
            }
            else
            {
                log = new FileOutputStream(rootDirectoryPath + File.separator + "output_log_" + STARTUP_TIMESTAMP
                        + ".txt");
            }
            detailedLogOutput = new PrintStream(log);
        }
        catch (IOException e)
        {
            System.err.println("Error creating detailed log output file.");
            e.printStackTrace();
        }
    }

}