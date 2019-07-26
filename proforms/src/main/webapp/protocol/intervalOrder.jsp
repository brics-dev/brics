<jsp:include page="/common/doctype.jsp" />

<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditvisittypes" />

<html>
	<%-- Include Header --%>
	<s:set var="pageTitle" scope="request">
		<s:text name="protocol.visitType.create.title.display" />
	</s:set>
	<jsp:include page="/common/header_struts2.jsp" />


	<script type="text/javascript">
	
		$(document).ready(function() 
		{
			// Listener for the Up button for re-ordering a form
			$("#shiftUpBtn").click(function(event) {
				var $selectedOpt = $("#intervalOrderBox option:selected");
				$selectedOpt.prev().before($selectedOpt);
			});
			
			// Listener for the Down button for re-ordering a form
			$("#shiftDownBtn").click(function(event) {
				var $selectedOpt = $("#intervalOrderBox option:selected");
				$selectedOpt.next().after($selectedOpt);
			});
			
			// Submit listner 
			$("#updateIntervalOrder").submit(_.debounce(function(event) {
				// Disable the submit button.
				_.defer(function() {
					$(".submitButton").prop("disabled", true);
				});
				
				// Set the order of the selected intervals
				var intervalOrder = [];
				$("#intervalOrderBox option").each(function (index) {
					var intervalId = $(this).val();
					intervalOrder.push(intervalId);
				});
					
				var intervalOrderStringified = JSON.stringify(intervalOrder);
				$("#intervalStrList").val(intervalOrderStringified);

				// Deselect any selected options in the ordering select box to avoid any extra data that may get submitted
				$("#intervalOrderBox option:selected").prop("selected", false);
				
			}, 3000, true));

		}); //end document.ready
		
		function resetOrder() {
			redirectWithReferrer('<s:property value="#webRoot"/>/protocol/orderVisitType.action');
		}
	
	</script>

<%-- ---------------------------------------------------Interval Order---------------------------------------------------- --%>
	<h3 id="orderInfo" class="toggleable">
		<s:text name="protocol.intervalorder.displayOrder.title" />
	</h3>
	<div id="orderInfoDiv">
		<s:form theme="simple" method="post" id="updateIntervalOrder">
			<s:hidden name="intervalStrList" id="intervalStrList" />
			<p><s:text name="protocol.intervalorder.displayOrder.instruction" /></p>
			<br/><br/>
			<div class="formrow_1">
	    		<s:select name="intervalOrderBox" size="10" id="intervalOrderBox" list="#request.__StudyIntervalList" listKey="id" listValue="name"/>
			
				<div style="position: relative; top: 48px; margin-left: 460px;">
				    <input type="button" id="shiftUpBtn" value="Up" title="Shifts the selected form up one."/>
				    <br/><br/>
				    <input type="button" id="shiftDownBtn" value="Down" title="Shifts the selected form down one."/>
				</div>
			</div>

			<%-- ---------------------------------------------------Interval Order Buttons------------------------------------------- --%>
				<div class="formrow_1">	
					<input type="button" id="cancleBtn" value="<s:text name='button.Cancel'/>" onclick="resetOrder()" title="Click to cancel (changes will not be saved)." />
					<input type="reset" id="resetBtn" value="<s:text name='button.Reset'/>" onclick="resetOrder()" title="Click to reset all fields to their original values" />
					<s:submit action="saveVisitTypeOrder" id="orderVisitTypeBtn" key="button.intervalOrder.orderVisitType" 
						cssClass="submitButton" title="Click to re-order visit types" />
				</div>
			<%-- ---------------------------------------------------Interval Order Buttons------------------------------------------- --%>
		</s:form>

	</div>
	
	<%-- Include Footer --%>
	<jsp:include page="/common/footer_struts2.jsp" />
		
</html>