<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
				gov.nih.nichd.ctdb.protocol.domain.Protocol,
				gov.nih.nichd.ctdb.security.domain.User,
				gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewprotocols, addeditprotocols, validuser"/>

<html>
<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="myworkspace.title.display" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />
<jsp:include page="/workspace/dashboardJsCssLib.jsp" />


<%
	Locale l = request.getLocale();
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
%>

<script type="text/javascript">

function validateCheckbox(form) {

}


var Calendar = {
	Settings : {
		daysOfWeek : ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
		monthNames : ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
		startOnMonday : false,
		currentData : [],
		localData : [],
		currentDate : new Date(),
		divRef : null
	},
	
	LocalEvent : function() {
		this.index = 0;
		this.data = {};
	},
	
	/**
	 * Initializes the Calendar object and begins the process of drawing the
	 * calendar and data display.
	 */
	init : function($div, settings) {
		$div.addClass("ibisActiveCalendar");
		
		// handle settings merging
		if (typeof settings == "undefined") {
			settings = {};
		}
		this.Settings = $.extend(this.Settings, settings);
		this.Settings.divRef = $div;
		
		// requests, via ajax, the data for the current date
		this.requestData($div, settings);
	},
	
	/**
	 * Performs the actual building of the calendar and data display.
	 * Called by requestData after successful retrieval of data
	 */
	initStepTwo : function($div, settings) {
		// translate data into local version
		this.translateDataToLocal();
		
		// draw the calendar table and output list inside the div
		this.clearTable();
		this.drawTable($div);
		
		// make the table cells clickable
		$div.find("td").each(function() {
			$(this).click(function() {
				Calendar.changeDay($(this));
			});
		});
		
		// make the navigation elements clickable
		$div.find(".backYear").click(function() {
			Calendar.changeMonth(Number(Calendar.Settings.currentDate.getFullYear() - 1), Calendar.Settings.currentDate.getMonth());
		});
		$div.find(".backMonth").click(function() {
			if (Calendar.Settings.currentDate.getMonth() == 0) {
				Calendar.changeMonth(Number(Calendar.Settings.currentDate.getFullYear() - 1), 11);
			}
			else {
				Calendar.changeMonth(Calendar.Settings.currentDate.getFullYear(), Calendar.Settings.currentDate.getMonth() - 1);
			}
			
		});
		$div.find(".nextYear").click(function() {
			Calendar.changeMonth(Number(Calendar.Settings.currentDate.getFullYear() + 1), Calendar.Settings.currentDate.getMonth());
		});
		$div.find(".nextMonth").click(function() {
			if (Calendar.Settings.currentDate.getMonth() == 11) {
				Calendar.changeMonth(Calendar.Settings.currentDate.getFullYear() + 1, 0);
			}
			else {
				Calendar.changeMonth(Calendar.Settings.currentDate.getFullYear(), Number(Calendar.Settings.currentDate.getMonth() + 1));
			}
		});
		
		// select the current day
		this.highlightCurrentDay($div);
		
		// highlight days with events
		this.highlightEventDays($div);
		
		// display current day's events in output list
		this.displayDayDetails($div);
	},
	
	drawTable : function($div) {
		$div.append('<div class="calendarContainer"><s:text name="myworkspace.dashboard.SelectAppointments" /><table cellpadding="0" cellspacing="0"><thead></thead><tbody></tbody></table></div>');		
		// draw the navigation row
		var $thead = $div.find("thead");
		$thead.append('<tr><th class="backYear calendarNavigation">&lt;&lt;</th><th class="backMonth calendarNavigation">&lt;</th><th colspan="3">'+ this.Settings.monthNames[this.Settings.currentDate.getMonth()] + ' ' + this.Settings.currentDate.getFullYear() +'</th><th class="nextMonth calendarNavigation">&gt;</th><th class="nextYear calendarNavigation">&gt;&gt;</th></tr>');
		
		// draw the data container
		$div.append('<div class="calendarDataDisplay"></div>');
		
		// draw the head row
		$thead.append('<tr></tr>');
		var $theadRow = $thead.children("tr").last();
		
		for (var i = 0; i < 7; i++) {
			$theadRow.append('<th>'+this.Settings.daysOfWeek[i]+'</th>');
		}
		// draw the body
		var $tbody = $div.find("tbody");
		var drawnDays = 0;
		var numDays = this.numberOfDaysInMonth(this.Settings.currentDate.getFullYear(), this.Settings.currentDate.getMonth());
		var firstDayIndex = this.dayOfWeekOfFirstDay(this.Settings.currentDate.getFullYear(), this.Settings.currentDate.getMonth());
		// draw the first row
		$tbody.append('<tr></tr>');
		var $row = $tbody.children('tr');

		// cells for the first row are different than other rows
		for (var i = 0; i < 7; i++) {
			if (i < firstDayIndex) {
				$row.append("<td></td>");
			}
			else {
				$row.append(this.drawDay(++drawnDays));
			}
		}
		
		var displayDay = drawnDays + 1;
		while (drawnDays < numDays) {
			// draw a week row
			$tbody.append('<tr></tr>');
			$row = $tbody.children('tr').last();
			for (var i = 0; i < 7; i++) {
				displayDay = (drawnDays > numDays-1) ? "" : ++drawnDays;
				$row.append(this.drawDay(displayDay));
			}
		}
	},
	
	drawDay : function(dayNumber) {
		var date = new Date(this.Settings.currentDate.valueOf());
		date.setDate(dayNumber);
		var id = "calendar_" + date.getFullYear() + "_" + String(Number(date.getMonth())+1) + "_" + date.getDate();
		var day = '<td id="' + id + '">' + dayNumber + '</td>';
		return day;
	},	
	
	/**
	 * Requests the data for a table on the currentDate and saves it
	 */
	requestData : function($div, settings) {
		this.Settings.currentData = [];
		this.Settings.localData = [];
		var month = this.Settings.currentDate.getMonth();
		var year = this.Settings.currentDate.getFullYear();
		
		$.ajax({
			type: "POST",
			url : "<s:property value="#webRoot"/>/patient/patientJson.action",
			data: {
				year: year,
				month: month
			},
			success : function(response) {
				if (response != "") {
					response = jQuery.parseJSON(response);
					Calendar.Settings.currentData = response;
					Calendar.initStepTwo($div, settings);
				}
			}
		});
	},
	
	highlightCurrentDay : function($div) {
		var id = "#calendar_" + this.Settings.currentDate.getFullYear();
		id += "_"+String(Number(this.Settings.currentDate.getMonth()+1));
		id += "_"+this.Settings.currentDate.getDate();
		$div.find(id).addClass("activeCalendarCell");
	},	
	
	highlightEventDays : function($div) {
		// we are going to go through the event list and highlight those days that have events.  easy enough
		var localRef = this.Settings.localData;
		var id = "";
		// construct the ID BEFORE we start looping, please
		var idTemplate = "calendar_" + this.Settings.currentDate.getFullYear() + "_" + String(Number(this.Settings.currentDate.getMonth()+1)) + "_";
		// the day number is stored in localref[i][0].index
		// every entry in localref is guaranteed to have at least one (IE, index 0) entry
		for (var i = 0; i < localRef.length; i++) {
			$("#"+idTemplate+localRef[i][0].index).addClass("eventExistsCalendarCell");
		}
	},
	
	// see http://stackoverflow.com/questions/315760/what-is-the-best-way-to-determine-the-number-of-days-in-a-month-with-javascript
	numberOfDaysInMonth : function(year, month) {
		return new Date(year, month+1, 0).getDate();
	},
	
	/**
	 * Calculates the day number (zero based) for the first day of the month.
	 * So, if the first day is a Sunday, this method will return zero (0)
	 */
	dayOfWeekOfFirstDay : function(year, month) {
		var d = new Date(year, month, 1);
		return d.getDay();
	},
	
	/**
	 * gets the jquery representation of the div container of a td
	 */
	getDiv : function($td) {
		return $td.parents("div").eq(0);
	},
	
	changeDay : function($td) {
		// change the day highlight
		var $div = this.getDiv($td);
		$div.find("td.activeCalendarCell").removeClass("activeCalendarCell");
		$td.addClass("activeCalendarCell");
		this.updateDate($div);
	},
	
	changeMonth : function(year, month) {
		var date = this.Settings.currentDate;
	date.setFullYear(year, month);
		this.init(this.Settings.divRef, this.Settings);
	},
	
	clearTable : function() {
		this.Settings.divRef.empty();
	},
	
	updateDate : function($div) {
		var id = $div.find(".activeCalendarCell").attr("id");
		var arrDate = id.split("_");
		this.Settings.currentDate = new Date(arrDate[1], arrDate[2]-1, arrDate[3]);
		
		// display data for day in the detail pane
		// clear the detail pane
		$div.parent().find(".calendarDataDisplay").empty();
		
		// display data
		this.displayDayDetails($div.parent());
		
	},
	
	translateDataToLocal : function() {
		this.Settings.localData = [];
		this.Settings.currentData.sort(function(a, b) {
			// concatenates the times together and subtracts something like 1430 - 1400 
			return Number(String(a.hour) + String(a.minute)) - Number(String(b.hour) + String(b.minute));
		});
		
		var dayNumber = 0;
		var localRef = null;
		var index = -1;
		// creates a sparse array containing all day numbers as the index and array of events within the day as value
		for (var i=0; i < this.Settings.currentData.length; i++) {
			dayNumber = Number(this.Settings.currentData[i].day);
			localRef = this.Settings.localData;
			index = this.findLocalDay(dayNumber);
			if (index == -1) {
				localRef.push([{index : dayNumber, data : this.Settings.currentData[i]}]);
			}
			else {
				localRef[index].push({index : dayNumber, data : this.Settings.currentData[i]});
			}
		}
	},
	
	/**
	 * Finds the listing index for a day entry 
	 */
	findLocalDay : function(dayNumber) {
		var localRef = this.Settings.localData;
		for (var i = 0; i < localRef.length; i++) {
			if (typeof localRef[i][0] != "undefined" && localRef[i][0].index == dayNumber) {
				return i;
			}
		}
		return -1;
	},
	
	clearHighlights : function() {
		this.Settings.divRef.find("td").removeClass("eventExistsCalendarCell");
	},
	
	clearDetailsList : function() {
		this.Settings.divRef.find(".displayEntry").remove();
	},
	
	/**
	 * Displays listings for the currentDate in the details pane.
	 * RELIES ON currentDate BEING SET CORRECTLY
	 */
	displayDayDetails : function($div) {
		var dayNumber = this.Settings.currentDate.getDate();
		var index = this.findLocalDay(dayNumber);
		var arrEvents = this.Settings.localData[index];
		if (arrEvents == null) {
			arrEvents = new Array();
		}
		$div.children(".calendarDataDisplay").append('<s:text name="myworkspace.dashboard.ScheduledAppointments" /><table cellpadding="0" cellspacing="0"><thead><tr></tr></thead></table>');
		var $table = $div.children(".calendarDataDisplay").find("table");
		var $theadRow = $table.find("tr").last();
		$theadRow.append('<th><s:text name="time" /></th><th><s:text name="response.resolveHome.tableHeader.subjectGUID" /></th><th><s:text name="response.label.interval" /></th><th><s:text name="study.add.number.display" /></th>');
		$table.append("<tbody></tbody>");
		$tableBody = $table.children("tbody");
		var eventData = null;
		var cell = "";
		for (var i = 0; i < arrEvents.length; i++) {
			$tableBody.append("<tr></tr>");
			var $trow = $tableBody.find("tr").last();
			eventData = arrEvents[i].data;
			var hour = 0;
			var meridiem = "";
			// 
			if (eventData.hour > 12) {
				hour = eventData.hour - 12;
			} else {
				hour = eventData.hour;
			}
			
			if (eventData.hour > 12) {
				meridiem = "pm";
			} else if (eventData.hour < 12) {
				meridiem = "am";
			} else {
				meridiem = "noon";
			}
			
			cell = hour + ":" + eventData.minute + " " + meridiem;
			$trow.append('<td>'+ cell + "</td>");
		<%
			Protocol protocol = (Protocol)session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			if (protocol == null) {
		%>
				$trow.append('<td>'+eventData.subject+'</td>');
		<%
			} else {
		%>
				$trow.append('<td><a href="<s:property value="#webRoot"/>/patient/viewPatient.action?patientId='+ eventData.subjectId +'">' + eventData.subject + '</a></td>');
		<%
			}
		%>
			
			$trow.append("<td>" + eventData.type + "</td>");
			$trow.append("<td>" + eventData.protocolNumber + "</td>");
		}
	}
};

/*
 * init AE
 */
 var AE = {
			Settings : {
				aesData : [],
				currentDate : new Date(),
				divRef : null
			},
			
			
			/**
			 * Initializes the AE object Data
			 */
			init : function($div, settings) {
				$div.addClass("ibisActiveCalendar"); 
				
				// handle settings merging
 				if (typeof settings == "undefined") {
 					settings = {};
 				}
				this.Settings = $.extend(this.Settings, settings);
				this.Settings.divRef = $div;
				
				// requests, via ajax
				this.requestAEData($div, settings);
			},
			/**
			 * Requests the data for ae table
			 */
			requestAEData : function($div, settings) {
				this.Settings.currentData = [];
				$.ajax({
					type: "POST",  
					url : "<s:property value="#webRoot"/>/workspace/aeJson.action",
					success : function(response) {
						if (response != "") {
							response = jQuery.parseJSON(response);
							AE.Settings.aesData = response;
							AE.initStepTwo($div, settings);
						}
					}
				});
			},
			
			initStepTwo : function($div, settings) {
				// draw the AE Log table and output list inside the div
				this.clearTable();
				this.displayAEDetails($div);
			},
			clearTable : function() {
				this.Settings.divRef.empty();
			},
/**
 * Displays listings for the Adverse Events in the details pane.
 */
displayAEDetails : function($div) {

	var arrAesData = this.Settings.aesData;
	if (arrAesData == null) {
		arrAesData = new Array();
	}
    
	
	$div.append(' <div class="calendarContainer"><s:text name="Adverse Event Log" /><table cellpadding="0" cellspacing="0"><thead><tr></tr></thead></table></div> ');
	var $table = $div.children(".calendarContainer").find("table");
	
	var $theadRow = $table.find("tr").last();
	$theadRow.append('<th><s:text name="Date" /></th><th width="80"><s:text name="Incident Report" /></th>');
	$table.append("<tbody></tbody>");
	$tableBody = $table.children("tbody");
	var aeData = null;
	var cell = "";

	for (var i = 0; i < arrAesData.length; i++) {
		$tableBody.append("<tr></tr>");
		var $trow = $tableBody.find("tr").last();

 		aeData = arrAesData[i];
 		cell= aeData.formCompleteddate;
 		if(cell !=null) {cell=cell.substring(0, cell.indexOf(" "));}
 		$trow.append('<td>'+ cell + "</td>");
	<%
		if (protocol == null) {
	%>
			$trow.append('<td>'+aeData.subject+'</td>');
	<%
		} else {
	%>
		var selected_Form_Ids =aeData.administeredformId; 
		$trow.append('<td><a href="<s:property value="#webRoot"/>/response/dataCollection.action?action=editForm&mode=formPatient&aformId=' +selected_Form_Ids+ '&editUser=' +'1'+ '">' + aeData.subject + '</a></td>');
	<%
		}
	%>
	}
}
};


$(document).ready(function() {
	Calendar.init($("#calendar"));
	
	AE.init($("#aeId"));
	
	
	$("#messagesTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/getQaAlertsList.action",
		idtData: {
			primaryKey: 'id'
		},
		dom: 'Bfrtip',
		select: "multi",
        columns: [
            {
                name: 'studyNumber',
                title: '<%=rs.getValue("study.add.number.display",l)%>',
                parameter: 'studyNumber',
                data: 'studyNumber'
            },
            {
                name: 'alertType',
                title: '<%=rs.getValue("myworkspace.dashboard.alertType",l)%>',
                parameter: 'qaAlertType.shortName',
                data: 'alertType'
            },
            {
                name: 'comments',
                title: '<%=rs.getValue("myworkspace.dashboard.comments",l)%>',
                parameter: 'comments',
                data: 'comments'
            },	      
            {
                name: 'date',
                title: '<%=rs.getValue("myworkspace.dashboard.date",l)%>',
                parameter: 'date',
                data: 'date'
            }
        ],
        buttons: [
	      	  {
	      	   	 extend: 'delete',
	      	   	 className: 'idt-DeleteButton',
	  	    	 action: function(e, dt, node, config) {
	  	    		 var dt = $('#qaAlertsTable').idtApi('getTableApi');
	  	    		 var selectedRows = $("#qaAlertsTable").idtApi("getSelectedOptions");
	  	    		 if (selectedRows.length < 1) {
	  	    			alert("You must select a message to delete");
	  	    		} else {
	  	    			var yesno = confirm("Are you sure you want to delete these messages?");
	  	    			if (yesno) {
	  	    				$.ajax({
	  	    						type: "POST",
	  	    						url: "<s:property value="#webRoot"/>" + "/qa/monitorQuery.action",
	  	    						data: $("#qaQueryForm").serialize(),
	  	    						success: function(response) {
	  	    							// remove all checked messages
	  	    							dt.rows('.selected').remove().draw(false);
	  									selectedRows.length = 0;
	  	    						}
	  	    				});
	  	    			}
	  	    		}
	  	    		return false;
	      	   	} 
	      	  }
      	]
	})
});

function loadViewData(){
	window.location='showViewData.action';
}

var basePath = "<s:property value='#webRoot'/>";
</script>

<s:set var="displayClinicalPoint" value="#systemPreferences.get('display.protocol.clinicalPoint')" />
<s:if test="#displayClinicalPoint">
	<security:hasPrivilege privilege="viewchart">
		<jsp:include page="/workspace/dashboardChart.jsp" />
	</security:hasPrivilege>
</s:if>
<%-- <h3 class="toggleable"><s:text name="myworkspace.dashboard.SubjectVisits" /></h3> --%>
<h3 class="toggleable" style="cursor: pointer;">
	<a class="toggleable" href="javascript:;">
		<s:text name="myworkspace.dashboard.SubjectVisits" />
	</a>
</h3>
<div>
	<div id="calendar"></div>
	<s:if test="#displayClinicalPoint">
		<div id="aeId"></div> 
	</s:if>
</div>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>