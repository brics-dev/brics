package gov.nih.tbi.repository.ws;

import gov.nih.tbi.repository.ws.model.Accession;

import java.util.List;

import javax.jws.WebService;

/**
 * Interface that defines a web service contract
 * 
 * @author Andrew Johnson
 *
 */
@WebService
public interface AccessionWebService
{
	List<Accession> doAccessionsExist( List<Accession> list );
}
