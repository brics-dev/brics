
package gov.nih.tbi.guid.ws;

import java.io.IOException;
import java.net.URLEncoder;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.testng.annotations.Test;

public class RepositoryRestServiceTest extends TestCase
{

    HttpClient client = new HttpClient();
    private static final String URI = "http://localhost:8080/portal/ws/repository/repository/";

    /*
     * For some reason the en 
     */
    /*   @Test
       public void testSetDataStoreInfoArchive() throws IOException
       {

           // URL: ("DataStore/DataStructure/{dataStructureId}/archive")

           int dsID = 369;
           HttpClient client = new HttpClient();

           PostMethod postMethod = null;
           try
           {
               postMethod = new PostMethod(URLEncoder.encode(URI + "DataStore/DataStructure/" + dsID
                       + "/archive?isArchived=false", "UTF-8"));
               postMethod = new PostMethod(URI + "DataStore/DataStructure/" + dsID + "/archive?isArchived=false");
               postMethod.setRequestHeader("X-HTTP-Method-Override", "POST");
               client.executeMethod(postMethod);
           }
           finally
           {
               postMethod.releaseConnection();
           }

       }*/

    /*  @Test
      public void testDeleteFromDownloadQueueRWS() throws IOException
      {

          // String url = URLEncoder.encode(URI + "Download/deleteFromDownloadQueue?id=241", "UTF-8");
          DeleteMethod deleteMethod = new DeleteMethod(URI + "Download/deleteFromDownloadQueue");
          try
          {
              client.executeMethod(deleteMethod);
              assertEquals(204, deleteMethod.getStatusCode());
          }
          finally
          {
              deleteMethod.releaseConnection();

          }
      }*/

    @Test
    public void testDeleteDatasetByName() throws HttpException, IOException
    {

        String dataset = URLEncoder.encode("RestServicesDELETEMETEST", "UTF-8"); // RestServicesDELETEMETEST
                                                                                 // "Rest Service DELETE ME TEST"
        String studyName = URLEncoder.encode("Repo Rest Services", "UTF-8"); // RRSFS
        DeleteMethod deleteMethod = new DeleteMethod(URI + "Study/" + studyName + "/deleteByName?dataset=" + dataset);
        try
        {
            client.executeMethod(deleteMethod);
            assertEquals(200, deleteMethod.getStatusCode());
        }
        finally
        {
            deleteMethod.releaseConnection();

        }
    }
    
    @Test
    public void testCreateStudies() throws HttpException, IOException {

    	PostMethod method = new PostMethod(URI + "Study/createStudies");

        try {
        	int statusCode = client.executeMethod(method);

        	if (statusCode != HttpStatus.SC_OK) {
        		System.err.println("Method failed: " + method.getStatusLine());
        	}

        	byte[] responseBody = method.getResponseBody();

        	System.out.println(new String(responseBody));

        } catch (HttpException e) {
        	System.err.println("Fatal protocol violation: " + e.getMessage());
        	e.printStackTrace();
        } catch (IOException e) {
        	System.err.println("Fatal transport error: " + e.getMessage());
        	e.printStackTrace();
        } finally {
        	method.releaseConnection();
        }  

    }
    
    @Test
    public void testDeleteStudies() throws HttpException, IOException {

        DeleteMethod deleteMethod = new DeleteMethod(URI + "Study/deleteDupStudies");
        try{
            client.executeMethod(deleteMethod);
            assertEquals(200, deleteMethod.getStatusCode());
        }  finally {
            deleteMethod.releaseConnection();

        }
    }

}
