package gov.nih.nichd.ctdb.util.manager;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.ResultControl;
import gov.nih.nichd.ctdb.util.common.LookupResultControl;
import gov.nih.nichd.ctdb.util.common.UnknownLookupException;
import gov.nih.nichd.ctdb.util.dao.LookupManagerDao;
import gov.nih.nichd.ctdb.util.domain.LookupType;

/**
 * LookupManager is a utility business manager which interacts with the LookupManagerDao. The
 * role of the LookupManager is to retrieve lookups (state, institute, status, ...) for the system.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class LookupManager extends CtdbManager {

    private static final Map<Integer, List<CtdbLookup>> cachedLookups = new ConcurrentHashMap<Integer, List<CtdbLookup>>();

    /**
     * Gets all lookup values for the given type.
     *
     * @param lookupType Lookup to retrieve
     * @return List of lookup values for the given type
     * @throws UnknownLookupException thrown if the lookup type trying to access does not exist
     * @throws CtdbException          thrown if any other errors occur while processing
     */
    public List<CtdbLookup> getLookups(LookupType lookupType) throws UnknownLookupException, CtdbException {
    	Integer lookupVal = Integer.valueOf(lookupType.getValue());
    	List<CtdbLookup> lookups = cachedLookups.get(lookupVal);
    	
    	// Check if the lookup list needs to be created.
        if ( lookups == null ) {
            lookups = this.getLookups(lookupType, new LookupResultControl());
            cachedLookups.put(lookupVal, lookups);
        }
        
        return lookups;
    }


    public CtdbLookup getLookup (LookupType type, int id) throws CtdbException {
        List<CtdbLookup> allOfType = getLookups(type);
        
        for ( CtdbLookup lu : allOfType ) {
            if ( lu.getId() == id ) {
                return lu;
            }
        }
        
        throw new CtdbException("Failure finding lookup of type : " + type.getName() + " id : " + id);
    }
    
    /**
     * Gets all lookup values for the given type with sorting.
     *
     * @param lookupType Lookup to retrieve
     * @param rc         ResultControl object to control sorting
     * @return List of lookup values for the given type
     * @throws UnknownLookupException thrown if the lookup type trying to access does not exist
     * @throws CtdbException          thrown if any other errors occur while processing
     */
    public List<CtdbLookup> getLookups(LookupType lookupType, ResultControl rc) throws UnknownLookupException, CtdbException
    {
        List<CtdbLookup> lookups = null;
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            
            switch ( lookupType.getValue() )
            {
            	case 1: // return states lookups
            		lookups = LookupManagerDao.getInstance(conn).getStates(rc);
            		break;
            	case 2: // return institute lookups
            		lookups = LookupManagerDao.getInstance(conn).getInstitutes(rc);
            		break;
            	case 3: // return form status lookups
            		lookups = LookupManagerDao.getInstance(conn).getFormStatuses(rc);
            		break;
            	case 4: // return study status lookups
            		lookups = LookupManagerDao.getInstance(conn).getProtocolStatuses(rc);
            		break;
            	case 5: // return country lookups
            		lookups = LookupManagerDao.getInstance(conn).getCountries(rc);
            		break;
            	case 8: // return question range operator lookups
            		lookups = LookupManagerDao.getInstance(conn).getRangeOperators(rc);
            		break;
            	case 13: // return resolutions lookups
            		lookups = LookupManagerDao.getInstance(conn).getResolutions(rc);
            		break;
            	case 14: // return patient sex lookups
            		lookups = LookupManagerDao.getInstance(conn).getPatientSex(rc);
            		break;
            	case 15: // return patient race lookups
            		lookups = LookupManagerDao.getInstance(conn).getPatientRace(rc);
            		break;
            	case 16: // return patient religion lookups
            		lookups = LookupManagerDao.getInstance(conn).getPatientReligion(rc);
            		break;
            	case 17: // return patient martial status lookups
            		lookups = LookupManagerDao.getInstance(conn).getMaritalStatus(rc);
            		break;
            	case 18: // return LookupType.PATIENT_PREADMIT lookups
            		lookups = LookupManagerDao.getInstance(conn).getPreadmit(rc);
            		break;
            	case 19: // return patient primary study lookups
            		lookups = LookupManagerDao.getInstance(conn).getPrimaryProtocol(rc);
            		break;
            	case 20: // return patient secondary study lookups
            		lookups = LookupManagerDao.getInstance(conn).getSecondaryProtocol(rc);
            		break;
            	case 21: // return patient ethnicity lookups
            		lookups = LookupManagerDao.getInstance(conn).getEthnicity(rc);
            		break;
            	case 22: // return patient education lookups
            		lookups = LookupManagerDao.getInstance(conn).getEducation(rc);
            		break;
            	case 23: // return patient occupation lookups
            		lookups = LookupManagerDao.getInstance(conn).getOccupation(rc);
            		break;
            	case 24: // return attachment type lookups
            		lookups = LookupManagerDao.getInstance(conn).getAttachmentTypes(rc);
            		break;
            	case 29: // return contact types lookups
            		lookups = LookupManagerDao.getInstance(conn).getContactTypes(rc);
            		break;
            	case 32: // return study defaults lookups
            		lookups = LookupManagerDao.getInstance(conn).getProtocolDefaults(rc);
            		break;
            	case 33: // return security questions lookups
            		lookups = LookupManagerDao.getInstance(conn).getSecurityQuestions(rc);
            		break;
            	case 35: // return form types lookups
            		lookups = LookupManagerDao.getInstance(conn).getFromTypes(rc);
            		break;
            	case 38: // return BTRIS access lookups
            		lookups = LookupManagerDao.getInstance(conn).getBtrisAccess(rc);
            		break;
            	case 43: // return IRB status lookups
            		lookups = LookupManagerDao.getInstance(conn).getIrbStatus(rc);
            		break;
            	case 44: // return publication type lookups
            		lookups = LookupManagerDao.getInstance(conn).getPublicationType(rc);
            		break;
            	case 45: // return QA query type lookups
            		lookups = LookupManagerDao.getInstance(conn).getQaQueryType(rc);
            		break;
            	case 46: // return QA query status
            		lookups = LookupManagerDao.getInstance(conn).getQaQueryStatus(rc);
            		break;
            	case 47: // return QA query resolution lookups
            		lookups = LookupManagerDao.getInstance(conn).getQaQueryResolution(rc);
            		break;
            	case 48: // return QA query class lookups
            		lookups = LookupManagerDao.getInstance(conn).getQaQueryClass(rc);
            		break;
            	case 49: // return QA query priority lookups
            		lookups = LookupManagerDao.getInstance(conn).getQaQueryPriority(rc);
            		break;
            	case 51: // return user lookups
            		lookups = LookupManagerDao.getInstance(conn).getUserName(rc);
            		break;
            	case 52: // return visit type lookups
            		lookups = LookupManagerDao.getInstance(conn).getIntervalTypes(rc);
            		break;
            	case 53: // return E-Binder type lookups
            		lookups = LookupManagerDao.getInstance(conn).getEBinderType(rc);
            		break;
            	default:
            		throw new UnknownLookupException("The lookup type trying to be accessed does not exist.");
            }
        }
        finally
        {
            this.close(conn);
        }

        return lookups;
    }
    
    public StringBuffer getAllOccupationOptionsJS() throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return LookupManagerDao.getInstance(conn).getAllOccupationLookupsJS();
        } finally {
            this.close(conn);
        }
    }
}