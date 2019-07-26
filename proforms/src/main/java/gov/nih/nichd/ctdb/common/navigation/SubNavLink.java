package gov.nih.nichd.ctdb.common.navigation;

import java.util.ArrayList;

/**
 * Represents a link in the left-side navigation element on IBIS pages.
 * Contains an optional list of sub-links of the same type.
 * 
 * Currently only two levels of links are allowed, however we're allowing
 * for more with this method.
 * 
 * @author jpark1
 *
 */
public class SubNavLink {
	private String linkText;
	private String url;
	private ArrayList<SubNavLink> subLinks;
	private String[] permissions;
	private Boolean isDisabled;
	/**
	 * The nickname of the link as defined in LeftNavController
	 */
	private int nickname;
	
	public SubNavLink(String text, String thisUrl, int nickname, String[] perms) {
		setLinkText(text);
		setUrl(thisUrl);
		setPermissions(perms);
		setSubLinks(new ArrayList<SubNavLink>());
		isDisabled = false;
		this.nickname = nickname;
	}
	
	public SubNavLink(String text, String thisUrl, int nickname) {
		setLinkText(text);
		setUrl(thisUrl);
		setPermissions(new String[0]);
		this.nickname = nickname;
		
		subLinks = new ArrayList<SubNavLink>();
	}

	public String getLinkText() {
		return linkText;
	}

	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = urlStripSlash(url);
	}
	
	public void addLink(String text, String url, String[] permissions, int nickname) throws Exception {
		SubNavLink sublink = new SubNavLink(text, url, nickname);
		addLink(sublink);
	}
	
	public void addLink(String text, String url, int nickname) throws Exception {
		SubNavLink sublink = new SubNavLink(text, url, nickname);
		addLink(sublink);
	}
	
	public void addLink(SubNavLink link) throws Exception {
		// check for duplicate
		ArrayList<SubNavLink> currentLinks = getSubLinks();
		for (SubNavLink subLink : currentLinks) {
			if (subLink.equals(link)) {
				// don't need to add a duplicate
				return;
			}
		}
		
		// check for cyclic
		ArrayList<SubNavLink> checkLinks = getAllSubLinks();
		// convert the list to an array so we can iterate happily without a ConcurrentModificationException
		SubNavLink[] arrLinks = new SubNavLink[checkLinks.size()]; 
		arrLinks = checkLinks.toArray(arrLinks); 
		for (SubNavLink subLink : arrLinks) {
			if (subLink.equals(link)) {
				throw new Exception("Tried to add a link that would create an infinite loop in display");
			}
		}
		// no cyclic found
		// check for duplicate
		if (findLink(link) == null) {
			subLinks.add(link);
		}
	}

	public ArrayList<SubNavLink> getSubLinks() {
		return subLinks;
	}

	public void setSubLinks(ArrayList<SubNavLink> subLinks) {
		this.subLinks = subLinks;
	}
	
	/**
	 * Gets all direct and indirect sublinks of this link (the entire tree
	 * rooted at this node but not including this node).
	 * 
	 * @return ArrayList of all sublinks
	 */
	public ArrayList<SubNavLink> getAllSubLinks() {
		ArrayList<SubNavLink> output = new ArrayList<SubNavLink>();
		
		// convert the list to an array so we can iterate happily without a ConcurrentModificationException
		SubNavLink[] arrLinks = new SubNavLink[subLinks.size()]; 
		arrLinks = subLinks.toArray(arrLinks); 
		
		for(SubNavLink link : arrLinks) {
			output.add(link);
			output.addAll(link.getAllSubLinks());
		}
		return output;
	}
	
	public SubNavLink findLink(String linkText) {
		// convert the list to an array so we can iterate happily without a ConcurrentModificationException
		SubNavLink[] arrLinks = new SubNavLink[subLinks.size()]; 
		arrLinks = subLinks.toArray(arrLinks); 
		
		for (SubNavLink link : arrLinks) {
			if (link.getLinkText().equals(linkText)) {
				return link;
			}
		}
		return null;
	}
	
	public SubNavLink findLink(SubNavLink testLink) {
		// convert the list to an array so we can iterate happily without a ConcurrentModificationException
		SubNavLink[] arrLinks = new SubNavLink[subLinks.size()]; 
		arrLinks = subLinks.toArray(arrLinks); 
		
		for (SubNavLink link : arrLinks) {
			if (link.equals(testLink)) {
				return link;
			}
		}
		return null;
	}
	
	public String permissionsString() {
		String output = "";
		if (permissions.length > 0) {
			for (int i=0; i < permissions.length; i++) {
				if (i != 0) {
					output += ","; 
				}
				output += permissions[i];
			}
		}
		return output;
	}
	
	public String[] getPermissions() {
		return permissions;
	}

	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
	}

	public Boolean getIsDisabled() {
		return isDisabled;
	}

	public void setIsDisabled(Boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public int getNickname() {
		return nickname;
	}

	public void setNickname(int nickname) {
		this.nickname = nickname;
	}

	/**
	 * Strips any beginning slash from the url before it is entered into the 
	 * object's storage
	 * 
	 * @param url the url to filter
	 * @return the url without the beginning slash
	 */
	public String urlStripSlash(String url) {
		if (url.startsWith("/")) {
			return url.substring(1);
		}
		return url;
	}
	
	public boolean equals(SubNavLink otherLink) {
		if (otherLink.getUrl().equals(url) && otherLink.getLinkText().equals(linkText)) {
			return true;
		}
		return false;
	}
}
