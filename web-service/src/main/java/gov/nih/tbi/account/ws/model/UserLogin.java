package gov.nih.tbi.account.ws.model;

public class UserLogin
{
	private String userName;
	private String hash1;
	private String hash2;
	
	public UserLogin()
	{
		
	}
	
	public UserLogin(String userName, String hash1, String hash2)
	{
		this.userName = userName;
		this.hash1 = hash1;
		this.hash2 = hash2;
	}
	
	public String getUserName()
	{
		return userName;
	}
	public void setUserName( String userName )
	{
		this.userName = userName;
	}
	public String getHash1()
	{
		return hash1;
	}
	public void setHash1( String hash1 )
	{
		this.hash1 = hash1;
	}
	public String getHash2()
	{
		return hash2;
	}
	public void setHash2( String hash2 )
	{
		this.hash2 = hash2;
	}
	
}
