
package gov.nih.tbi.dictionary.validation.engine;

import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.Translations;
import gov.nih.tbi.dictionary.validation.model.DataStructureTable;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;
import gov.nih.tbi.dictionary.validation.parser.FileParser;
import gov.nih.tbi.repository.ws.AccessionWebService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

public class ValidationEngine
{

    // private DataDictionaryProvider ddtClient;
    private AccessionWebService accClient;

    ExecutorService executorService;

    int N_CPU = Runtime.getRuntime().availableProcessors() - 1;
    float U_CPU = (float) 0.75;

    public ValidationEngine(AccessionWebService accClient)
    {

        this.accClient = accClient;
        executorService = Executors.newFixedThreadPool(getThreadCount());
    }

    private int getThreadCount()
    {

        int count = Math.round(N_CPU * U_CPU);
        if (count <= 0)
        {
            return 1;
        }
        else
        {
            return count;
        }
    }

    public void validate(DataSubmission submission) throws IOException, JAXBException
    {

        submission.setDrafts(false);

        Collection<Callable<FileNode>> tasks = new ArrayList<Callable<FileNode>>();
        TableValidator.setTranslationRule(null);

        for (FileNode node : submission.getDataNodes())
        {
            if (node.isIncluded())
            {
                if (FileType.TRANSLATION_RULE.equals(node.getType()))
                {
                    Translations translationRule = FileParser.buildTranslations(node.getConicalPath());
                    if (TableValidator.getTranslationRule() != null)
                    {
                        JOptionPane.showMessageDialog(null, "There can only be one translation rule per submission",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    DataStructureTable table = submission.getFileData(node);
                    tasks.add(new TableValidator(submission.getDictionary(), table,
                            submission.getReferencedData(table), node, accClient));
                    TableValidator.setTranslationRule(translationRule);
                }
                else
                {
                    DataStructureTable table = submission.getFileData(node);
                    tasks.add(new TableValidator(submission.getDictionary(), table,
                            submission.getReferencedData(table), node, accClient));
                    if (!table.getStructure().getStatus().equals(StatusType.PUBLISHED))
                    {
                        submission.setDrafts(true);
                    }
                }
            }
        }

        try
        {
            for (Future<FileNode> f : executorService.invokeAll(tasks))
            {
                FileNode node = f.get();
                node.setValidated(true);
            }
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // Add error messages to all file nodes with type unknown.
        // addUnknownErrors(submission.getRoot());

    }

    public void addUnknownErrors(FileNode node)
    {

        if (FileNode.FileType.DIR.equals(node.getType()))
        {
            for (FileNode n : node.getChildren())
            {
                addUnknownErrors(n);
            }
        }
        else
            if (FileNode.FileType.UNKNOWN.equals(node.getType()))
            {
                node.setErrorNum(node.getErrorNum() + 1);
            }
    }
    
    public void setExtraValidation(HashMap<String, Boolean> extraValidation) {
    	TableValidator.setExtraValidation(extraValidation);
    }

}
