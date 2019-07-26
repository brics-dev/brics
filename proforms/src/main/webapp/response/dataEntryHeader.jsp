<%@ page import="gov.nih.nichd.ctdb.response.domain.DataEntryHeader,gov.nih.nichd.ctdb.protocol.domain.Protocol,gov.nih.nichd.ctdb.common.rs"%>
<%@ page import="gov.nih.nichd.ctdb.response.common.ResponseConstants,java.util.Locale,gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<%
	DataEntryHeader deh = (DataEntryHeader) session.getAttribute(ResponseConstants.DATAENTRYHEADER_SESSION_KEY);
	Object obj = request.getAttribute("comingFromViewAudit");
	boolean comingFromViewAudit;
	Locale l = request.getLocale();
	Protocol protocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	int subjectDisplayType = protocol.getPatientDisplayType();
	if (obj == null || obj.equals("")) {
		comingFromViewAudit = false;
	} else {
		comingFromViewAudit = true;
	} 
				
	if (comingFromViewAudit) {
%>						
<tr>
	<td>
		<table id="dataEntryHeader" border=0>
			<tr>
				<td align="left">
					<b><s:text name="response.collect.label.formname" />:</b>
				</td>
				<td class="labelTextnocolors" align="left"><%=deh.getFormDisplay()%></td>
			</tr>
			<tr>
				<td align="left">
					<b><s:text name="study.add.name.display" />: </b>
				</td>
				<td class="labelTextnocolors" align="left"><%=deh.getStudyName()%></td>
			</tr> 
			<tr>
				<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) { %>
					<td align="left" width="150px">
						<b>
							<s:text name="response.resolveHome.tableHeader.subjectGUID" />: 
						</b>
					</td>
					<td class="labelTextnocolors" align="left">
						<%=deh.getGuid()%>
					</td>
					<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {%>					
						<td align="left" width="150px">
							<b>
								<s:text name="subject.table.subjectID"/>: 
							</b>
						</td>
						<td class="labelTextnocolors" align="left">
							<%=deh.getPatientDisplay()%>
						</td>
					<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_SUBJECT) {%>						
						<td align="left" width="150px">
							<b>
								<s:text name="patient.label.SubjectNumber" />: 
							</b>
						</td>
						<td class="labelTextnocolors" align="left">
							<%=deh.getPatientDisplay()%>
						</td>
					<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_NAME) {%>
						<td align="left"  width="150px">
							<b>
								<s:text name="patient.label.SubjectName" />: 
							</b>
						</td>
						<td class="labelTextnocolors" align="left">
							<%=deh.getPatientDisplay()%>
						</td>
				<%} %>
			</tr>
			<tr>
				<td align="left">
					<b><s:text name="visitdate.display" />:</b>
				</td>
				<td class="labelTextnocolors" align="left" id="visitdatedisplay"><%=deh.getDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left">
					<b><s:text name="scheduledvisitdate.display" />:</b>
				</td>
				<td class="labelTextnocolors" align="left"><%=deh.getScheduledVisitDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left">
					<b><s:text name="protocol.visitType.title.display" />:</b>
				</td>
				<td class="labelTextnocolors" align="left">
					<%=deh.getIntervalDisplay()%>
				</td>
			</tr>
		</table>
	</td>
</tr>			
		<%					
			} else {
 		%>
<tr>
	<td>
		<table border=0>
			<tr>
				<td align="left" width="150px">
					<b><s:text name="study.add.name.display" />: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getStudyName()%></td>
			</tr>
			<tr>
				<td align="left" width="150px">
					<b><s:text name="response.collect.label.formname" />:</b>
				</td>
				<td class="labelTextnocolors" align="left"><%=deh.getFormDisplay()%></td>
			</tr>
			<tr>
				<%if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) { %>
					<td align="left" width="150px">
						<b>
							<s:text name="response.resolveHome.tableHeader.subjectGUID" />: 
						</b>
					</td>
					<td class="labelTextnocolors" align="left">
						<%=deh.getGuid()%>
					</td>
					<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {%>					
						<td align="left" width="150px">
							<b>
								<s:text name="subject.table.subjectID"/>: 
							</b>
						</td>
						<td class="labelTextnocolors" align="left">
							<%=deh.getPatientDisplay()%>
						</td>
					<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_SUBJECT) {%>						
						<td align="left" width="150px">
							<b>
								<s:text name="patient.label.SubjectNumber" />: 
							</b>
						</td>
						<td class="labelTextnocolors" align="left">
							<%=deh.getPatientDisplay()%>
						</td>
					<%} else if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_NAME) {%>
						<td align="left"  width="150px">
							<b>
								<s:text name="patient.label.SubjectName" />: 
							</b>
						</td>
						<td class="labelTextnocolors" align="left">
							<%=deh.getPatientDisplay()%>
						</td>
				<%} %>
				</tr>

			
			
			<%
				if (deh.getEntry2() != null) {
					String dataEntry = (String) request.getAttribute("dataEntry");
					if (dataEntry.equals("1")) {
			%>

			<tr>
				<td align="left"><b>Visit Date 1: </b></td>
				<td class="labelTextnocolors" align="left" id="visitdatedisplay"><%=deh.getDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left">
					<b><s:text name="scheduledvisitdate.display" />:</b>
				</td>
				<td class="labelTextnocolors" align="left"><%=deh.getScheduledVisitDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left"><b>Visit Type 1: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getIntervalDisplay()%></td>
			</tr>
			<tr>
				<td align="left">
					<b><s:text name="response.collect.myCollections.user1" />: </b>
				</td>
				<td class="labelTextnocolors" align="left"><%=deh.getEntry()%></td>
			</tr>
			<%
						if (deh.getLockDate() != null) {
			%>
			<tr>
				<td align="left"><b>Lock Date 1: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getLockDate()%></td>
			</tr>
			<%
						}
					} else if (dataEntry.equals("2")) {
			%>

			<tr>
				<td align="left"><b>Visit Date 2: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getDateDisplay2()%></td>
			</tr>
			<tr>
				<td align="left"><b>Visit Type 2: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getIntervalDisplay()%></td>
			</tr>
			<tr>
				<td align="left"><b><s:text name="response.collect.myCollections.user2" />: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getEntry2()%></td>
			</tr>

			<%
						if (deh.getLockDate2() != null) {
			%>
			<tr>
				<td align="left"><b>Lock Date 2: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getLockDate2()%></td>
			</tr>
			<%
						} 
					} else {
			%>
			<tr>
				<td align="left"><b>Visit Date 1: </b></td>
				<td class="labelTextnocolors" align="left" id="visitdatedisplay"><%=deh.getDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left">
					<b><s:text name="scheduledvisitdate.display" />:</b>
				</td>
				<td class="labelTextnocolors" align="left"><%=deh.getScheduledVisitDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left"><b>Visit Type 1: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getIntervalDisplay()%></td>
			</tr>
			<tr>
				<td align="left"><b><s:text name="response.collect.myCollections.user1" />: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getEntry()%></td>
			</tr>

			<%
						if (deh.getLockDate() != null) {
			%>
			<tr>
				<td align="left"><b>Lock Date 1: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getLockDate()%></td>
			</tr>
			<%
						} 
			%>

			<tr>
				<td align="left"><b>Visit Date 2: </b></td>
				<td class="labelTextnocolors" align="left" id="visitdatedisplay"><%=deh.getDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left">
					<b><s:text name="scheduledvisitdate.display" />:</b>
				</td>
				<td class="labelTextnocolors" align="left"><%=deh.getScheduledVisitDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left"><b>Visit Type 2: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getIntervalDisplay()%></td>
			</tr>
			<tr>
				<td align="left"><b><s:text name="response.collect.myCollections.user2" />: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getEntry2()%></td>
			</tr>
			
			<%
						if (deh.getLockDate2() != null) {
			%>
			<tr>
				<td align="left"><b>Lock Date 2: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getLockDate2()%></td>
			</tr>
			<%
						} 
					}
				} else {
			%>

			<tr>
				<td align="left"><b><s:text name="visitdate.display" />:</b></td>
				<td class="labelTextnocolors" align="left" id="visitdatedisplay"><%=deh.getDateDisplay()%></td>
			</tr>
			<tr>
				<td align="left"><b><s:text name="scheduledvisitdate.display" />:</b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getScheduledVisitDateDisplay()%></td>
			</tr>
			<tr>

				<td align="left"><b><s:text name="protocol.visitType.title.display" />: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getIntervalDisplay()%></td>
			</tr>

			<%
					if (deh.getSingleDoubleKeyFlag() == 1) {
			%>
			<tr>
				<td align="left"><b><s:text name="response.collect.myCollections.user" />: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getEntry()%></td>
			</tr>

			<%
					} else {
			%>
			
			<tr>
				<td align="left"><b><s:text name="response.collect.myCollections.user1" />: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getEntry()%></td>
			</tr>
			
			<%
					}
			
					if (deh.getFinalLockDate() != null) {
			%>
			<tr>
				<td align="left"><b><s:text name="response.dataentry.lockDate" />: </b></td>
				<td class="labelTextnocolors" align="left"><%=deh.getFinalLockDate()%></td>
			</tr>
			<%
					} 
				}
			%>
		</table>
	</td>
</tr>

	<%
			}
	%>
