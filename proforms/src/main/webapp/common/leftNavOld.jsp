<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.navigation.LeftNavController"%>
<%@ page import="gov.nih.nichd.ctdb.common.navigation.SubNavLink"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%!

/*
<div>
	<ul class="subnavigation">
		<li>
			<a></a>
			<ul>
				<li>
					<a></a>
				</li>
			</ul>
		</li>
		<li>
		</li>
	</ul>
</div>
*/

public String interpretLink(SubNavLink link) {
	String interpretedUrl = link.getUrl();
	if (link.getUrl().equals("")) {
		return "";
	}
	if (link.getUrl().startsWith("/")) {
		link.setUrl(link.getUrl().substring(1));
	}
	if (!interpretedUrl.contains("http") && !link.getUrl().startsWith("javascript:")) {
		interpretedUrl = (String)CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.get("webroot") + "/" + interpretedUrl;
	}
	else {
		interpretedUrl = link.getUrl().replaceAll("::webroot::", (String)CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.get("webroot"));
	}
	return interpretedUrl;
}
%>
<div id="leftNav"><% 
	LeftNavController leftNav = (LeftNavController) request.getAttribute(CtdbConstants.NAVIGATION_LEFTNAV_KEY);
	if (leftNav == null) {
		// if we can't find the leftNav in the normal request (this happens in validation failed cases)
		// try and see if it's in the session
		leftNav = (LeftNavController) request.getSession().getAttribute(CtdbConstants.NAVIGATION_LEFTNAV_KEY);
	}
	if (leftNav != null) {
		for(SubNavLink link : leftNav.getLinks()) {
			String interpretedUrl = interpretLink(link);
			String linkClass = "";
			// by disabling this conditional, we are effectively removing the ability of the accordion to...accordion.
			// the headers will always do an actual redirect to their url page
			//if (link.getSubLinks().size() < 1) {
				linkClass = " disableAccordionLink";
			//}
			
			// if the header doesn't have a url, set its url to the first sublink's url
			if (interpretedUrl.equals("") && link.getSubLinks().size() > 0) {
				interpretedUrl = interpretLink(link.getSubLinks().get(0));
			}
			
			if (link.getPermissions().length > 0) {
			%>
				<security:hasPrivilege privilege="<%=link.permissionsString() %>">
					<h3><a href="<%= interpretedUrl %>" class="leftNavHeader<%= linkClass %>"><%= link.getLinkText() %></a></h3>
					<div>
					<% for (SubNavLink subLink : link.getSubLinks()) {
						interpretedUrl = interpretLink(subLink);
						// permissions
						if (subLink.getPermissions().length > 0) { 
							%>
							<security:hasPrivilege privilege="<%= subLink.permissionsString() %>">
							<a href="<%= interpretedUrl %>"><%= subLink.getLinkText() %></a>
							</security:hasPrivilege>
							<% 
						}
						else {
							%>
							<a href="<%= interpretedUrl %>"><%= subLink.getLinkText() %></a>
							<%
						}
					}
					%>
					</div>
				</security:hasPrivilege>
			<%
			}
			else {
			%>
				<h3><a href="<%= interpretedUrl %>" class="leftNavHeader<%= linkClass %>"><%= link.getLinkText() %></a></h3>
				<div>
				<% for (SubNavLink subLink : link.getSubLinks()) {
					interpretedUrl = interpretLink(subLink);
					// permissions
					if (subLink.getPermissions().length > 0) { 
						%>
						<security:hasPrivilege privilege="<%= subLink.permissionsString() %>">
						<a href="<%= interpretedUrl %>"><%= subLink.getLinkText() %></a>
						</security:hasPrivilege>
						<% 
					}
					else {
						%>
						<a href="<%= interpretedUrl %>"><%= subLink.getLinkText() %></a>
						<%
					}
				}
				%>
				</div>
			<%
			}
		}
	} %>
</div>
<div id="leftNavHandle" class="hide">
&nbsp;
</div>