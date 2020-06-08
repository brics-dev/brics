<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale, java.util.Date, java.text.DateFormat, java.text.SimpleDateFormat" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%
	Locale l = request.getLocale();
	DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	String startDate = df.format(((Date)request.getAttribute("startDate")));
	String endDate = df.format(((Date)request.getAttribute("endDate")));
	String psrHeader = (String)request.getAttribute("psrHeader");
%>

<html>
	<jsp:include page="/selfreporting/header_selfReporting.jsp" />
	
	<script type="text/javascript">
	var LogoutWarning = {
			timer : null,
			logoutAfterTimer : null,
			logoutWarningId : null,
			html : 'Your authentication session is about to expire due to inactivity.  Please click the button below to extend your session.',	
			init : function() {
				this.startTimer();
			},
			
			openPopup : function() {
				this.logoutWarningId = $.ibisMessaging(
						"dialog", 
						"warning", 
						this.html, 
						{
							buttons: [{
								text: "Extend My Session", 
								click: function(){LogoutWarning.cancelLogout();}
							}]
						}
				);
				this.startLogoutTimer();
			},
			
			startTimer : function() {
				this.timer = setTimeout(function(){LogoutWarning.openPopup();}, <s:property value="#systemPreferences.get('app.warningTimeout')"/> * 60 * 1000 );
			},
			
			cancelLogout : function() {
				$.ibisMessaging("close", {id: LogoutWarning.logoutWarningId});
				this.cancelLogoutTimer();
				this.startTimer();
			},
			
			logout : function() {
				this.redirectToLogout();
			},
			
			redirectToLogout : function() {
				top.location.href = '<s:property value="#webRoot"/>/selfreporting/list?token=sessionExpired';
			},
			
			startLogoutTimer : function() {
				this.logoutAfterTimer = setTimeout(function(){LogoutWarning.logout();}, 120000);
			},
			
			cancelLogoutTimer : function() {
				window.clearTimeout(this.logoutAfterTimer);
				this.logoutAfterTimer = null;
			}
		};
	
	
	$(document).ready(function() {
		LogoutWarning.init();
	});
	</script>

	<div class="selfReportingPSRHeader">
	<%
		if(psrHeader != null) {%>
			<%=psrHeader %>
			<br><br>
	<%	}
	%>
	</div>
	
	<div>
	<s:text name="selfreporting.selfreportingHome.instruction">
		<s:param><%=startDate%></s:param>
		<s:param><%=endDate%></s:param>
	</s:text>
	<br></div>
	<h3><s:text name="selfreporting.selfreportingHome.myforms.display" /></h3>
	
	<s:form action="selfReportingHomeAction" onsubmit="return false">
		<div class="dataTableContainer dataTableJSON" id="selfReportingListDiv">
			<ul></ul>
			
			<idt:jsontable name="selfReportingList" scope="request" 
					decorator="gov.nih.nichd.ctdb.selfreporting.tag.SelfReportingHomeDecorator">
				<idt:setProperty name="basic.msg.empty_list"
					value="There are no Subject Forms for this protocol found at this time." />
				<idt:column property="formName" title='<%=rs.getValue("form.name.display", l)%>' />
				<idt:column property="status" title='<%=rs.getValue("app.label.lcase.status", l)%>' />
				<idt:column property="lastUpdated" title='<%=rs.getValue("app.label.lcase.lastupdated", l)%>' />
			</idt:jsontable>
		</div>
	
	</s:form>
<%-- Include Footer --%>
<jsp:include page="/selfreporting/footer_selfReporting.jsp" />
</html>
	