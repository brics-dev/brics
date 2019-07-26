<%@include file="/common/taglibs.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<div id="biospecimenDiv">
<s:form name="generateShippedReport" validate="true" action="biospecimenReportAction" method="download" enctype="multipart/form-data">
	<div class="lightbox-orderManager-wrapper" >
		<h3>
			Enter Range for Submitted Date:
		</h3>
		<div> 
		<table>
			<tr>
				<td>
					<label for="submitDateFrom" style="float:none; font-size:14px;">From</label>
					<s:textfield id="submitDateFrom" label="From Date" name="submitDateFrom" maxlength="10" class="date-picker small textfield"/>
				</td>
				<td>
					<label for="submitDateTo" style="float:none; font-size:14px;">To</label>
					<s:textfield id="submitDateTo" label="To Date" name="submitDateTo" maxlength="10" class="date-picker small textfield"/>
				</td>
			</tr>
			<tr>
				<td>
					<s:fielderror fieldName="submitDateFrom" />
				</td>
				<td>
					<s:fielderror fieldName="submitDateTo" />
				</td>
			</tr>
		</table>
		</div>
		<div class="clear-float" style="text-align: center;">
			<div id="boxBtns" class="button">
				<input type="button" class="submit" value="Create Report" onClick="javascript:generateBiospecimenReport();"></input>
			</div>
		</div>
	</div>
</s:form>
</div>

<script type="text/javascript">
$('document').ready(function() 
		{ 
			$( "#submitDateFrom, #submitDateTo" ).datepicker({
			      showOn: "button",
			      buttonImage: "../images/calendar.gif",
			      buttonImageOnly: true,
			      dateFormat: "yy-mm-dd"
			    });
		});

</script>

