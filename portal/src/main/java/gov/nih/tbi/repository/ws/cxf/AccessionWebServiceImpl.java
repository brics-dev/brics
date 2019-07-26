
package gov.nih.tbi.repository.ws.cxf;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonSyntaxException;

import gov.nih.tbi.guid.exception.InvalidJwtException;
import gov.nih.tbi.guid.model.GuidJwt;
import gov.nih.tbi.guid.ws.GuidServerAuthenticationUtil;
import gov.nih.tbi.guid.ws.GuidWebserviceProvider;
import gov.nih.tbi.guid.ws.exception.AuthenticationException;
import gov.nih.tbi.repository.ws.AccessionWebService;
import gov.nih.tbi.repository.ws.model.Accession;
import gov.nih.tbi.repository.ws.model.AccessionReturnType;

/**
 * CXF implementation of the Accession (GUID) Web Service
 * 
 * @author Andrew Johnson
 * 
 */
@WebService(endpointInterface = "gov.nih.tbi.repository.ws.AccessionWebService")
public class AccessionWebServiceImpl implements AccessionWebService {

	@Autowired
	private GuidServerAuthenticationUtil guidServerAuthUtil;

    @Autowired
    private GuidWebserviceProvider guidWebserviceProvider;

    
	@Override
	public List<Accession> doAccessionsExist(List<Accession> list) {

		List<String> guidList = new ArrayList<String>();

		if (list != null) {
			for (Accession acc : list) {
				guidList.add(acc.getValue());
				acc.setReturnValue(AccessionReturnType.ERROR);
				acc.setComment("UNTESTED");
			}
		}

		GuidJwt userJwt = null;
		try {
			userJwt = guidServerAuthUtil.getSystemUserJwt();
		} catch (JsonSyntaxException | InvalidJwtException | AuthenticationException e) {
			e.printStackTrace();
		}
		
		if (userJwt != null) {
			String jwt = userJwt.toString();
			List<String> returnList = guidWebserviceProvider.validateGuids(guidList, jwt);

			int i = 0;
			for (String listItem : returnList) {
				// TRUE
				if ("TRUE".equals(listItem)) {
					list.get(i).setReturnValue(AccessionReturnType.VALID);
					list.get(i).setComment("");
				}

				// FALSE
				else if ("FALSE".equals(listItem)) {
					list.get(i).setReturnValue(AccessionReturnType.INVALID);
					list.get(i).setComment("");
				}

				// ERROR
				else if ("ERROR".equals(listItem)) {
					list.get(i).setReturnValue(AccessionReturnType.ERROR);
					list.get(i).setComment("");
				}

				// CONVERTED GUID
				else {
					list.get(i).setReturnValue(AccessionReturnType.PSUEDO_GUID);
					list.get(i).setComment(returnList.get(i));
				}
				i++;
			}
		}

		return list;
	}

}
