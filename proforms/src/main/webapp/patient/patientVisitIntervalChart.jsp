<%@ taglib uri="/struts-tags" prefix="s" %>


<div id="intervalChartContainer" >
	
	<s:if test="{#request.intervalSchedulerStatus.size() > 0 }">
		<table id="intervalChart" style="margin-left:20px;" class="table table-striped table-bordered" width="100%">
			<tr>
				<th style="text-align: center">Visit Type</th>
				<th>Status</th>
			</tr>
			<s:iterator value="#request.intervalSchedulerStatus">
			<tr>
				<td>
					<s:if test="%{schedulerStatus==@gov.nih.nichd.ctdb.protocol.domain.IntervalScheduleDisplay@SCHEDULER_STATUS_SCHEDULED}">
						<div class="greenCircle floatLeft statusCir"></div>
					</s:if>
					<s:if test="%{schedulerStatus==@gov.nih.nichd.ctdb.protocol.domain.IntervalScheduleDisplay@SCHEDULER_STATUS_NOT_SCHEDULED}">
						<div class="redCircle floatLeft statusCir"></div>
					</s:if>
					<div class="floatLeft" style="margin: 2px 3px 0;"><s:property value="intervalName"/></div>
				</td>
				<td>
					<div class="floatLeft" ><s:property value="schedulerStatus"/></div>
				</td>
			</tr>
			</s:iterator>
		</table>
	</s:if>
</div>