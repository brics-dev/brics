
package gov.nih.tbi;

import gov.nih.tbi.commons.dao.UserDao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonsDaoComponent extends DaoComponent
{

    static Logger logger = Logger.getLogger(CommonsDaoComponent.class);

    /**********************************************************************************/

    @Autowired
    UserDao userDao;

    /**********************************************************************************/

}
