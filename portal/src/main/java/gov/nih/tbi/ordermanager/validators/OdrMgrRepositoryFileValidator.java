
package gov.nih.tbi.ordermanager.validators;

import gov.nih.tbi.ordermanager.dao.BioRepositoryDao;
import gov.nih.tbi.ordermanager.model.BioRepository;
import gov.nih.tbi.ordermanager.model.BioRepositoryFileType;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class OdrMgrRepositoryFileValidator extends FieldValidatorSupport
{

    @Autowired
    private BioRepositoryDao bioRepositoryDao;

    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String ufmFieldName = this.getFieldName();
        // TODO Auto-generated method stub
        // uploadedFilesMap = (Map<UserFile, byte[]>) this.getFieldValue(this.getFieldName(), object);

        ArrayList<UserFile> attachedFiles = (ArrayList<UserFile>) this.getFieldValue("filesAttached", object);

        BiospecimenOrder order = (BiospecimenOrder) this.getFieldValue("orderBean", object);

        HashMap<String, BioRepositoryFileType> filesTypeMap = new HashMap<String, BioRepositoryFileType>();
        if (order != null)
        {
            for (BiospecimenItem item : order.getRequestedItems())
            {
                BioRepository br = item.getBioRepository();
                for (BioRepositoryFileType fileType : br.getRequiredFileTypes())
                {
                    filesTypeMap.put(fileType.getName(), fileType);
                }
            }

            if (attachedFiles != null)
            {
                for (UserFile attachedFile : attachedFiles)
                {
                    String attachedFileType = attachedFile.getDescription();
                    if (attachedFileType != null && !attachedFileType.equals(""))
                    {
                        if (filesTypeMap.containsKey(attachedFileType))
                        {
                            filesTypeMap.remove(attachedFileType);
                        }
                    }
                }
                // We expect that the map should be empty when the for loop completes. If the map is not empty then
                // there
                // was a file that was omitted.
                if (!filesTypeMap.isEmpty())
                {
                    // Not sure why, but this message does not ever appear. Instead the failure message from struts xml
                    // file
                    // is displayed. However a field error must be added to indicate that something has failed.
                    addFieldError(ufmFieldName, "Upload File Name is required");
                }
            }
            else
            {
                addFieldError(ufmFieldName, "Upload File Name is required");
            }
        }
        else
        {
            addFieldError(ufmFieldName, "Upload File Name is required");
        }

    }
}
