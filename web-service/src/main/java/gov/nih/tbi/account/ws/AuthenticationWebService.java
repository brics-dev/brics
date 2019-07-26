package gov.nih.tbi.account.ws;

import gov.nih.tbi.account.ws.model.UserLogin;

import javax.jws.WebService;

@WebService
public interface AuthenticationWebService
{

	public Boolean authenticate(UserLogin user);
	
}
