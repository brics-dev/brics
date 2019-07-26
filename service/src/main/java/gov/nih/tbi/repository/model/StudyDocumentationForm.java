
package gov.nih.tbi.repository.model;

import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudySupportingDocumentation;

import java.io.File;
import java.util.Set;

public class StudyDocumentationForm
{

    private File upload;
    private Boolean urlDocumentationRadio;
    protected String supportingDocDescription;
    protected FileType supportingDocType;
    private Set<StudySupportingDocumentation> supportingDocumentationSet;

    public StudyDocumentationForm()
    {

    }

    public StudyDocumentationForm(Study study)
    {

        supportingDocumentationSet = study.getSupportingDocumentationSet();
    }

    /**
     * Read the form fields on the page and set the columns in the study column.
     * 
     */
    @SuppressWarnings("unchecked")
    public void adapt(Study study)
    {

    }
}
