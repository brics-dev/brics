<%@ page
	import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User,
                 gov.nih.tbi.account.model.hibernate.Account,
                 gov.nih.tbi.account.model.hibernate.AccountRole,
                 gov.nih.nichd.ctdb.security.util.SecuritySessionUtil,
                 java.util.Set,
                 java.util.Collections,
                 gov.nih.tbi.commons.model.RoleType,
                 gov.nih.tbi.commons.model.RoleStatus,
                 java.util.Iterator"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<s:set var="usingGUID"
	value="#systemPreferences.get('guid_with_non_pii')" />
<div id="navigation">
	<div class="nav-wrapper clear-both">
		<ul id="primary-nav">
			<li id="homeModuleLink"><a
				href="<s:property value="#systemPreferences.get('brics.modules.home.url')"/>">
					<span id="homeModuleLinkContainer"> <s:text
							name="brics.modules.home.label" />
				</span>
			</a></li>
			<li id="workspaceModuleLink"><a
				href="<s:property value="#systemPreferences.get('brics.modules.workspace.url')"/>">
					<s:text name="brics.modules.workspace.label" />
			</a></li>
			<li id="proformsModuleLink"><a
				href="<s:property value="#webRoot"/>" class="current"> <s:text
						name="application.name.nonPii" />
			</a></li>

			<%	if ((Boolean)request.getSession().getAttribute(SecuritySessionUtil.BRICS_PERMISSION_GUID)) { %>
			<li id="guidModuleLink"><a
				href="<s:property value="#systemPreferences.get('brics.modules.guid.url')"/>">
					<s:text name="brics.modules.guid.label" />
			</a></li>
			<% 	} else { %>
			<li id="guidModuleLink">
				<div class="missingPermission">
					<s:text name="brics.modules.guid.label" />
				</div>
			</li>
			<%	} 
					
						if ((Boolean)request.getSession().getAttribute(SecuritySessionUtil.BRICS_PERMISSION_DDT)) { %>
			<li id="dataDictionaryModuleLink"><a
				href="<s:property value="#systemPreferences.get('brics.modules.ddt.url')"/>">
					<s:text name="brics.modules.ddt.label" />
			</a></li>
			<% } else { %>
			<li id="dataDictionaryModuleLink">
				<div class="missingPermission">
					<s:text name="brics.modules.ddt.label" />
				</div>
			</li>
			<%	} 
						
						if ((Boolean)request.getSession().getAttribute(SecuritySessionUtil.BRICS_PERMISSION_REPO)) { %>
			<li id="dataRepositoryModuleLink"><a
				href="<s:property value="#systemPreferences.get('brics.modules.repo.url')"/>">
					<s:text name="brics.modules.repo.label" />
			</a></li>
			<%	} else { %>
			<li id="dataRepositoryModuleLink">
				<div class="missingPermission">
					<s:text name="brics.modules.repo.label" />
				</div>
			</li>
			<%	} %>
			<% if ((Boolean)request.getSession().getAttribute(SecuritySessionUtil.BRICS_PERMISSION_QUERY)) { %>
			<li id="queryModuleLink"><a
				href="<s:property value="#systemPreferences.get('brics.modules.query.url')"/>">
					<s:text name="brics.modules.query.label" />
			</a></li>
			<% } else { %>
			<li id="queryModuleLink">
				<div class="missingPermission">
					<s:text name="brics.modules.query.label" />
				</div>
			</li>
			<% } %>
			<% if ((Boolean)request.getSession().getAttribute(SecuritySessionUtil.BRICS_PERMISSION_METASTUDY)) { %>
			<li id="metaStudyModuleLink"><a
				href="<s:property value="#systemPreferences.get('brics.modules.metastudy.url')"/>">
					<s:text name="brics.modules.metastudy.label" />
			</a></li>
			<% } else { %>
			<li id="metaStudyModuleLink">
				<div class="missingPermission">
					<s:text name="brics.modules.metastudy.label" />
				</div>
			</li>
			<% } %>
			<li id="userManagementModuleLink"><a
				href="<s:property value="#systemPreferences.get('brics.modules.account.url')"/>">
					<s:text name="brics.modules.account.label" />
			</a></li>
			<s:if test="#systemPreferences.get('enable.report') == 'true'">
				<s:if
					test="#systemPreferences.get('brics.modules.reporting.url') != ''">
					<%
						if ((Boolean) request.getSession().getAttribute(SecuritySessionUtil.BRICS_PERMISSION_REPORTING)) {
					%>
					<li id="reportingModuleLink"><a
						href="<s:property value="#systemPreferences.get('brics.modules.reporting.url')"/>">
							<s:text name="brics.modules.reporting.label" />
					</a></li>
					<%
						} else {
					%>
					<li id="reportingModuleLink">
						<div class="missingPermission">
							<s:text name="brics.modules.reporting.label" />
						</div>
					</li>
					<%
						}
					%>
				</s:if>
			</s:if>
			<%-- <li id="needHelpLink" style="float:right; padding-left: 3px;">
							<a href="http://ibis-wiki.cit.nih.gov/foswiki/Main/SystemHelp/ProFoRMSHelp" target="_blank">
								<s:text name="app.help" />
							</a>
						</li> --%>
		</ul>
	</div>
</div>
