package gov.nih.cit.brics.file.data.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Represents a User's access token in a java-readable format
 *
 */
public class UserToken {
	private Long id;
	private String username;
	private String fullName;
	private String tenant;
	private Long orgId;
	private List<GrantedAuthority> authorities;
	private LocalDateTime tokenExpiration;

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

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
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

	public LocalDateTime getTokenExpiration() {
		return tokenExpiration;
	}

	public void setTokenExpiration(LocalDateTime tokenExpiration) {
		this.tokenExpiration = tokenExpiration;
	}

	@Override
	public int hashCode() {
		return Objects.hash(authorities, fullName, id, orgId, tenant, tokenExpiration, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UserToken)) {
			return false;
		}
		UserToken other = (UserToken) obj;
		return Objects.equals(authorities, other.authorities) && Objects.equals(fullName, other.fullName)
				&& Objects.equals(id, other.id) && Objects.equals(orgId, other.orgId)
				&& Objects.equals(tenant, other.tenant) && Objects.equals(tokenExpiration, other.tokenExpiration)
				&& Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserToken [id=");
		builder.append(id);
		builder.append(", username=");
		builder.append(username);
		builder.append(", fullName=");
		builder.append(fullName);
		builder.append(", tenant=");
		builder.append(tenant);
		builder.append(", orgId=");
		builder.append(orgId);
		builder.append(", authorities=");
		builder.append(authorities);
		builder.append(", tokenExpiration=");
		builder.append(tokenExpiration);
		builder.append("]");
		return builder.toString();
	}
}
