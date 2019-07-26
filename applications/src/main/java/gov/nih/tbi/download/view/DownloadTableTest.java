package gov.nih.tbi.download.view;

public class DownloadTableTest
{

    public static void main(String[] args)
    {

        String serverLocation = "http://REPLACED/";
        String username = "REPLACED";
        String passHash = "REPLACED";
        String orgEmail = "REPLACED@nih.gov";
        String versionNumber = "2.4.0.2";
                
		new DownloadPackageView(serverLocation, username, passHash, versionNumber, orgEmail);
    }
    

}
