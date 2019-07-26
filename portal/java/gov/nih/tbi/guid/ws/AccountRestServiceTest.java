
package gov.nih.tbi.guid.ws;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.cxf.jaxrs.client.WebClient;
import org.testng.annotations.Test;

public class AccountRestServiceTest extends TestCase
{

    HttpClient client = new HttpClient();
    private static final String URI = "http://localhost:8080/portal/ws/account/account/";

    // @Test
    // public void testGetAccountByIdTest1() throws IOException
    // {
    //
    // Long id = 214L;
    // WebClient client = WebClient.create("http://localhost:8080/portal/ws/account/account/get");
    // Account account = client.path(214).type(MediaType.APPLICATION_XML).accept("text/xml").get(Account.class);
    //
    // // client.reset();
    // }
    //
    // @Test
    // public void testFormPub() throws IOException
    // {
    //
    // WebClient client = WebClient
    // .create("http://fitbir-portal-local.cit.nih.gov:8080/portal/ws/ddt/dictionary/FormStructure/Published/list");
    // // BasicDataStructureList bdsl = client.query("page", "1").query("pageSize", "100").query("ascending", "true")
    // // .query("sort", "null").type("application/xml").accept("text/xml").get(BasicDataStructureList.class);
    // BasicDataStructureList bdsl = client.query("page", "1").accept("text/xml").get(BasicDataStructureList.class);
    // }

    // @Test
    // public void testGetAccount()
    // {
    //
    // WebClient client = WebClient.create("http://fitbir-portal-local.cit.nih.gov:8080/portal/ws/account/account");
    // client.path("role").path(RoleType.ROLE_ADMIN);
    // AccountsWrapper role = client.accept("text/xml").get(AccountsWrapper.class);
    //
    // List<Account> accs = role.getAccountList();
    //
    // }

    // @Test
    // public void testListUserAccess()
    // {
    //
    // LongListWrapper llw;
    // WebClient client = WebClient.create("http://fitbir-portal-local.cit.nih.gov:8080/portal/ws/account/account");
    // client.path("entityMap/").path("list").query("id=" + 2).query("entityType=" + "DATA_STRUCTURE")
    // .query("permissionType=" + "READ").query("isGranted=" + false);
    //
    // WebClient client1 = WebClient
    // .create("http://fitbir-portal-local.cit.nih.gov:8080/portal/ws/account/account/entityMap/list?id=2&entityType=DATA_STRUCTURE&permissionType=READ&isGranted=false");
    // llw = client.accept("text/xml").get(LongListWrapper.class);
    // }

    @Test
    public void testGetAccess()
    {

        EntityType entityType = EntityType.DATA_ELEMENT;
        WebClient client = WebClient.create("http://localhost:8080/portal/ws/account/account/entityMap/getAccess");
        EntityMap em = client.query("entityType", entityType).query("entityId", 201).accept("text/xml")
                .get(EntityMap.class);

    }

    //
    // @Test
    // public void testGetEntityMapOwner()
    // {
    //
    // EntityType entityType = EntityType.DATA_ELEMENT;
    // // WebClient client = WebClient.create("http://localhost:8080/portal/ws/account/account/getOwner/");
    // WebClient client = WebClient
    // .create("http://localhost:8080/portal/ws/account/account/getOwner/DATA_ELEMENT/201");
    // // Account em = client.path("entityType", "DATA_ELEMENT").path("entityId", 201).accept("text/xml")
    // // .get(Account.class);
    // Account em = client.accept("text/xml").get(Account.class);
    //
    // }
    //
    // @Test
    // public void testGetPrivatePermissionGroup()
    // {
    //
    // WebClient client = WebClient.create("http://localhost:8080/portal/ws/account/account/permissionGroups/private");
    // PermissionGroupsWrapper pgw = client.accept("text/xml").get(PermissionGroupsWrapper.class);
    //
    // List<PermissionGroup> l = pgw.getPermissionGroupsList();
    //
    // for (PermissionGroup x : l)
    // }
    //
    // @Test
    // public void testGetByUserName()
    // {
    //
    // WebClient client = WebClient.create("http://localhost:8080/portal/ws/account/account/user");
    // Account account = client.path("mgreen").accept("text/xml").get(Account.class);
    //
    // }
    //
    // @Test
    // public void testListEntityAccess()
    // {
    //
    // EntityType entityType = EntityType.DATA_ELEMENT;
    // PermissionType pertype = PermissionType.READ;
    //
    // WebClient client = WebClient.create("http://localhost:8080/portal/ws/account/account/entityMap/");
    // LongListWrapper llw = client.path("list").query("id", 2).query("entityType", entityType)
    // .query("permissionType", pertype).query("isGranted", true).accept("text/xml")
    // .get(LongListWrapper.class);
    //
    // }
    //
    public void testDeleteMethod() throws HttpException, IOException
    {

        DeleteMethod deleteMethod = new DeleteMethod("http://localhost:8080/portal/ws/account/account/"
                + "entityMap/unregister/201");
        try
        {
            client.executeMethod(deleteMethod);
            assertEquals(200, deleteMethod.getStatusCode());
            // assertEquals("invoked delete:", deleteMethod.getResponseBodyAsString());
        }
        finally
        {
            deleteMethod.releaseConnection();
        }
    }
    //
    // public void testUnregisterEntity() throws HttpException, IOException
    // {
    //
    // DeleteMethod deleteMethod = new DeleteMethod("http://localhost:8080/portal/ws/account/account/"
    // + "unregister/DATA_ELEMENT/200");
    // try
    // {
    // client.executeMethod(deleteMethod);
    // // assertEquals(200, deleteMethod.getStatusCode());
    // // assertEquals("invoked delete:", deleteMethod.getResponseBodyAsString());
    // }
    // finally
    // {
    // deleteMethod.releaseConnection();
    // }
    // }
    //
    // public void testDeleteMethodOverride() throws HttpException, IOException
    // {
    //
    // System.setProperty("com.ibm.ws.jaxrs.httpmethodoverride.enable", "true");
    // HttpClient client = new HttpClient();
    // PostMethod postMethod = null;
    // try
    // {
    // postMethod = new PostMethod("http://localhost:8080/portal/ws/account/account/" + "entityMap/unregister/201");
    // postMethod.setRequestHeader("X-HTTP-Method-Override", "DELETE");
    // client.executeMethod(postMethod);
    // assertEquals(204, postMethod.getStatusCode());
    // assertEquals("invoked delete:hello world", postMethod.getResponseBodyAsString());
    // }
    // finally
    // {
    // postMethod.releaseConnection();
    // }
    //
    // }
    //
    // public void testRegisterEntity() throws HttpException, IOException
    // {
    //
    // PostMethod postMethod = new PostMethod(");
    // try
    // {
    //
    // client.executeMethod(postMethod);
    // // assertEquals(200, postMethod.getStatusCode());
    //
    // }
    // finally
    // {
    // postMethod.releaseConnection();
    // }
    //
    // }
}