package gov.nih.brics.downloadtool.data.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Represents a User's access token in a java-readable format
 *
 */
public class UserToken {
	Long id;
	String username;
	String fullName;
	String tenant;
	Long orgId;
	List<GrantedAuthority> authorities;
	Date tokenExpiration;
	
	public UserToken() {
		this.authorities = new ArrayList<>();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	public List<GrantedAuthority> getAuthorities() {
		return this.authorities;
	}
	public String[] getAuthoritiesArray() {
		String[] output = new String[authorities.size()];
		for (int i = 0; i < authorities.size(); i++) {
			output[i] = authorities.get(i).getAuthority();
		}
		return output;
	}
	public String getAuthoritiesString() {
		return String.join(",", getAuthoritiesArray());
	}
	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}
	public void setAuthorities(String authorities) {
		String[] auths = authorities.split(",");
		List<String> authsList = Arrays.asList(auths);
		for (String authority : authsList) {
			this.authorities.add(new SimpleGrantedAuthority(authority));
		}
	}
	public Date getTokenExpiration() {
		return tokenExpiration;
	}
	public void setTokenExpiration(Date tokenExpiration) {
		this.tokenExpiration = tokenExpiration;
	}
	
	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
}
