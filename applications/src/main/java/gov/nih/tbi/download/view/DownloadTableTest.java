package gov.nih.tbi.download.view;

public class DownloadTableTest
{

    public static void main(String[] args)
    {

        String serverLocation = "http://pdbp-dev.cit.nih.gov/";
        String username = "wangvg";
        String passHash = "d6c2ac509ec4b4f08c5101ea59bda066ba26df09700e10e9c844c4653dc52d6b";
        String orgEmail = "FITBIR-ops@nih.gov";
        String versionNumber = "2.4.0.2";
                
		new DownloadPackageView(serverLocation, username, passHash, versionNumber, orgEmail);
    }
    

}
