
package gov.nih.tbi.ordermanager.service.impl;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.ordermanager.dao.BioRepositoryDao;
import gov.nih.tbi.ordermanager.dao.BiospecimenItemDao;
import gov.nih.tbi.ordermanager.dao.BiospecimenItemMappingDao;
import gov.nih.tbi.ordermanager.dao.ItemQueueDao;
import gov.nih.tbi.ordermanager.model.BioRepository;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenItemMapping;
import gov.nih.tbi.ordermanager.model.ItemQueue;
import gov.nih.tbi.ordermanager.service.ItemQueueService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class ItemQueueServiceImpl implements ItemQueueService
{

    private static Logger log = Logger.getLogger(ItemQueueServiceImpl.class);

    @Autowired
    private ItemQueueDao itemQueueDao;

    @Autowired
    private BiospecimenItemDao biospecimenItemDao;

    @Autowired
    private BioRepositoryDao bioRepositoryDao;

    @Autowired
    private BiospecimenItemMappingDao biospecimenItemMappingDao;

    @Autowired
    private ModulesConstants modulesConstants;

    public ItemQueue getItemQueueForUser(User user)
    {

        return itemQueueDao.findByUser(user);
    }

    public Boolean addItemToUserQueue(User user, String data)
    {

        if (data == null || (data.equals("") == true))
        {
            log.error("Unable to add item to user's queue because the data parameter received is null or empty");
            return false;
        }
        if (user == null)
        {
            log.error("Unable to add item to user's queue because the user parameter is null");
            return false;
        }
        
        String[] pieces = data.split("&");
        if (pieces == null)
        {
            log.error("Web service request does not contain parameters needed.");
            return false;
        }

        Map<String, String> itemMap = new HashMap<String, String>();
        for (String piece : pieces)
        {
            String[] keyValue = piece.split("=");
            
            if (keyValue != null && keyValue.length == 2) {
                itemMap.put(keyValue[0], keyValue[1]);
            }
        }
        
        BiospecimenItemMapping mapping = null;
        if (itemMap.containsKey("formName"))
        {
            String formNameVersion = itemMap.get("formName");
            
            int indexOfV = formNameVersion.lastIndexOf("V");
            if (formNameVersion.length() > indexOfV + 1) 
            {
                String version = formNameVersion.substring(indexOfV + 1);
                if (version.matches("[1-9]{1,2}\\.[0-9]{1,2}")) 
                {
                    String formName = formNameVersion.substring(0, indexOfV);
                    mapping = biospecimenItemMappingDao.findByFormName(formName);
                }
            }
            
            
            if (mapping == null) 
            {
                log.error("unable to create a new item to add to queue of user: " + user.getFullName()
                        + " because no mapping was found for the form name: " + formNameVersion);
                return false;
            }
        }
        else
        {
            log.error("unable to create a new item to add to queue of user: " + user.getFullName()
                    + " because the formName parameter is null");
            return false;
        }
        

      
        String biosampleId = itemMap.get(mapping.getBiosampleId());
        if (biosampleId == null)
        {
            log.error("unable to create a new item to add to queue of user: " + user.getFullName()
                    + " because the biosampleId parameter is null");
            return false;
        }
        
        if (!itemMap.containsKey("RepositoryName")) 
        {
            log.error("unable to create a new item to add to queue of user: " + user.getFullName()
                    + " because the repositoryName parameter is null");
            return false;
        }
        
        BioRepository repository = bioRepositoryDao.findByName(itemMap.get("RepositoryName"));
        if (repository == null) 
        {
            log.error("unable to create a new item to add to queue of user: " + user.getFullName()
                    + " because BioRepository is not found for repository " + itemMap.get("RepositoryName"));
            return false;
        }

        if (log.isInfoEnabled())
        {
            log.info("trying to add item with biosampleId: " + biosampleId + " and repository: " + repository.getName()
                    + " to user: " + user.getFullName() + "'s repository");
        }

        // retrieve the item queue object for this user, if one doesn't exist then create one
        ItemQueue queue = itemQueueDao.findByUser(user);

        if (queue == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("queue doesn't exist for user: " + user.getFullName() + ". Creating a new queue");
            }
            
            queue = new ItemQueue();
            queue.setUser(user);
            itemQueueDao.save(queue);
            queue = itemQueueDao.findByUser(user);
        }

        List<BiospecimenItem> items = queue.getItems();

        for (BiospecimenItem item : items)
        {
            if (item.getCoriellId().equals(biosampleId) && item.getBioRepository().getId().equals(repository.getId()))
            {
                // item already exist in this user's queue, don't need to add it again
                if (log.isDebugEnabled())
                {
                    log.debug("an item with biosample id: " + biosampleId + " and repository id: " + repository.getId()
                            + " already exist in the user: " + user.getFullName() + "'s queue. NOT ADDING IT AGAIN");
                }
                return false;
            }
        }
        
       
        String ageYrs = itemMap.get(mapping.getAgeYrs());
        String caseControl = itemMap.get(mapping.getCaseControl());
        String neuroDiagnosis = itemMap.get(mapping.getNeuroDiagnosis());
        
        // create a new item object
        BiospecimenItem item = new BiospecimenItem();
        item.setCoriellId(biosampleId);
        item.setBioRepository(repository);
        item.setItemQueue(queue);
        item.setGuid(itemMap.get(mapping.getGuid()));
        item.setBioreposTubeID(biosampleId);
        item.setVisitTypePDBP(itemMap.get(mapping.getVisitTypePDBP()));
        item.setAgeYrs(ageYrs);
        item.setCaseControl(caseControl);
        item.setNeuroDiagnosis(neuroDiagnosis);
        /*        item.setAgeVal(ageVal);
                item.setBioreposTubeID(bioreposTubeId);
                item.setBiosampleDataOriginator(biosampleDataOriginator);
                item.setConcentrationUoM(concentrationUom);
                item.setRepositoryBiosample(repositoryBiosample);*/
        item.setSampCollType(itemMap.get(mapping.getSampCollType()));
        /*        item.setSampleAliquotMass(sampleAliquotMass);
                item.setSampleAliquotMassUnits(sampleAliquotMassUnits);
                item.setSampleAliquotVol(sampleAliquotVol);
                item.setSampleAliquotVolUnits(sampleAliquotVolUnits);
                item.setSampleAvgHemoglobinVal(sampleAvgHemoglobinVal);*/
        
        item.setInventory(itemMap.get(mapping.getInventory()));
        
        String inventoryDateStr = itemMap.get(mapping.getInventoryDate());
        if (inventoryDateStr != null && !inventoryDateStr.isEmpty()) {
            Date inventoryDate = BRICSTimeDateUtil.stringToDate(inventoryDateStr);
            item.setInventoryDate(inventoryDate);
        }
            
        item.setUnitNumber(itemMap.get(mapping.getUnitNumber()));
        item.setUnitMeasurement(itemMap.get(mapping.getUnitMeasurement()));
        item.setPdbpStudyId(itemMap.get(mapping.getPdbpStudyId()));
        items.add(item);
        queue.setItems(items);
        itemQueueDao.save(queue);
        
        return true;

    }

    public Boolean removeItemFromUserQueue(BiospecimenItem item, User user)
    {

        /*
         * the right way seems to be that I should retireve the user's queue and them remove the corresponding item from it
         */
        ItemQueue userQueue = this.itemQueueDao.findByUser(user);
        if (userQueue == null)
        {
            return Boolean.FALSE;
        }
        List<BiospecimenItem> existingItems = userQueue.getItems();
        for (BiospecimenItem existingItem : existingItems)
        {
            if (existingItem.equals(item) == true)
            {
                // found a match, remove it
                this.biospecimenItemDao.remove(existingItem.getId());
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    public Boolean checkIfItemExistInUserQueue(BiospecimenItem item, User user)
    {

        ItemQueue existingQueue = this.itemQueueDao.findByUser(user);
        if (existingQueue == null)
        {
            return Boolean.FALSE;
        }
        List<BiospecimenItem> existingItems = existingQueue.getItems();
        if ((existingItems == null) || (existingItems.isEmpty() == true))
        {
            return Boolean.FALSE;
        }
        return existingItems.contains(item);
    }

    @Override
    public Boolean removeAllItemsFromUserQueue(User user)
    {

        // Boolean toReturn = Boolean.TRUE;
        ItemQueue existingQueue = this.itemQueueDao.findByUser(user);
        if (existingQueue == null)
        {
            return Boolean.FALSE;
        }
        List<BiospecimenItem> existingItems = existingQueue.getItems();

        if ((existingItems == null) || (existingItems.isEmpty() == true))
        {
            return Boolean.FALSE;
        }
        this.biospecimenItemDao.removeAll(existingItems);
        return Boolean.TRUE;
    }

}
