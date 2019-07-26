package gov.nih.nichd.ctdb.response.util;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.ws.HashMethods;

public class BricsAccountWsClient {

	public static void main(String[] args) {
		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(BricsAccountWsClient.getBaseURI());
		Account acc = wt.request(MediaType.TEXT_XML).get(Account.class);

		//See more in cas pothAuth filter
		String pass = HashMethods.convertFromByte(acc.getPassword()) ;
		System.out.println("acc.getPassword()"+pass);
		System.out.println("acc.getPassword()"+acc.getAddress1());
		System.out.println("acc.getPassword()"+acc.getCity());
		System.out.println("acc.getPassword()"+acc.getUser().getFirstName());
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://cnrm-stage.cit.nih.gov/portal/ws/account/account/get/").build();
	}
}
