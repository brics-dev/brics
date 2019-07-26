<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.util.ProtocolUserRoleElement" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.ProtocolUser" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.Role" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.ProtocolRoleUser" %>
<%@ page import="gov.nih.nichd.ctdb.site.domain.Site" %>
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="java.util.List, org.json.JSONArray, org.json.JSONObject" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%
	List<ProtocolRoleUser> roleUsers = (List<ProtocolRoleUser>) request.getAttribute("roleUsers");
	Protocol study = (Protocol) request.getAttribute("study");
	List<Role> roles = (List<Role>) request.getAttribute("roles");
	User currentUser = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewaudittrails"/>

<html>

<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="study.roles.title.display"/>
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript" src="<s:property value="#webRoot"/>/common/filterlist.js"></script>

<script type="text/javascript">
var canEdit = false;

$(document).ready(function() {

		
	// Check if the user can edit the page. If not disable all fields 
	if ( !canEdit )
	{
		$("input").attr("disabled", "disabled");
		$("input[type=button]").hide();
		$("input[type=reset]").hide();
		$("input[type=submit]").hide();
		$("select").attr("disabled", "disabled");
		$("textarea").attr("disabled", "disabled");
		$("div.dataTables_filter input:disabled").removeAttr("disabled");
	};
});

var studyId = <%= ((Protocol) request.getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY)).getId() %>;

function roleChanged(dropdown) {
	var target = $(dropdown);
	var otherDrop = target.parent().next().children("select");
	// add a change to the changehandler since this is a change 
	// get the userID 
	var userId = target.attr("id").split("_")[1];
	var userName = $("#username_" + userId).val();
	var roleId = target.val();
	var siteId = 0;
	
	if (otherDrop.length > 0) {
		if (roleId == 2 || roleId == 3) {
			otherDrop.prop("disabled", false);
			siteId = otherDrop.val();
		}
		else {
			otherDrop.prop("disabled", true);
		}
		
		ChangeHandler.addChange(userId, userName, roleId, siteId);
	}
	else {
		// site ID does not exist 
		ChangeHandler.addChange(userId, userName, roleId, 0);
	}
	
	var columnIdx = 2;
	updateDTCellContent(columnIdx, target);
}

function siteChanged(dropdown) {
	var target = $(dropdown);
	var roleId = target.parent().prev().children("select").val();
	var userId = target.attr("id").split("_")[1];
	var userName = $("#username_" + userId).val();
	var siteId = target.val();
	ChangeHandler.addChange(userId, userName, roleId, siteId);
		
	var columnIdx = 3;
	updateDTCellContent(columnIdx, target);
}

/*set hidden p element value and update the cell data in datatable*/
function updateDTCellContent(columnIdx, target){
	
	var rowData = $("#protocolUsersListTable").idtApi('getApiRow', target.parent()).data(); 
	var rowIndex = $("#protocolUsersListTable").idtApi('getApiRow', target.parent()).index(); //console.log("current row index: "+rowIndex);
	
	var roleName = target.find('option:selected').text();
	var selectedOptionValue = target.find('option:selected').val(); //console.log("selectedOptionValue: "+selectedOptionValue);
	//clear the selected option for further selection
	target.find("option").attr("selected",false);
	target.find("option[value='"+selectedOptionValue+"']").attr("selected",true);
	//console.log("target parent html: "+target.parent().html());

	var hiddenElement = target.next(); 
	hiddenElement.text(roleName);
	rowData.role = target.parent().html();
	//console.log("roleChanged() after rowData.role: "+JSON.stringify(rowData.role));
	
	$("#protocolUsersListTable").dataTable().fnUpdate(rowData.role, rowIndex, columnIdx);
}

var ChangeHandler = {
	changes : new Array(),
	/**
	 * Adds a change to the list of changes.  This function will never allow
	 * two changes to be created for the same user.
	 * They will just be updated.
	 */
	addChange : function(userId, userName, roleId, siteId) {
		var currentChange = this.findUser(userId);
		if (currentChange != null) {
			currentChange.update(userId, userName, roleId, siteId);
		}
		else {
			var change = new Change();
			change.update(userId, userName, roleId, siteId);
			this.changes.push(change);
		}
	},
	
	findUser : function(userId) {
		var index = this.findUserIndex(userId);
		return (index >= 0) ? this.changes[index] : null;
	},
	
	findUserIndex : function(userId) {
		for (var i = 0; i < this.changes.length; i++) {
			if (this.changes[i].userId == userId) {
				return i;
				break;
			}
		}
		return -1;
	},
	
	toJSON : function() {
		return JSON.stringify(this.changes, function(key, value) {
			if (typeof value != "function") {
				return value;
			}
		});
	}
};

/**
 * A change is a "current state after a change" that a user has experienced.
 * This object will be stored and sent to the server as an array to be a list
 * of changes to be made to the db.
 */
function Change() {
	this.userId = 0;
	this.userName = '';
	this.roleId = 0;
	this.siteId = 0;
}
Change.prototype.fromJSON = function(jsonString) {
	var obj = (typeof jsonString == "string") ? jQuery.parseJson(jsonString) : jsonString;
	this.userId = (typeof obj.userId == "undefined") ? 0 : obj.userId;
	this.userName = (typeof obj.userName == "undefined") ? '' : obj.userName;
	this.roleId = (typeof obj.roleId == "undefined") ? 0 : obj.roleId;
	this.siteId = (typeof obj.siteId == "undefined") ? 0 : obj.siteId;
}
Change.prototype.update = function(userId, userName, roleId, siteId) {
	this.userId = userId;
	this.userName = userName;
	this.roleId = roleId;
	this.siteId = siteId;
}


function OriginalSelect(selectId, originalSelectedIndex, disableStatus) {
	this.selectId = selectId;
	this.originalSelectedIndex = originalSelectedIndex;
	this.disableStatus = disableStatus;
}

var OriginalSelectStatusHandle = {
		originalSelectArray : new Array(),
		addOriginalSelect : function(selectId, originalSelectedIndex, disableStatus) {
			var currentOriginalSelect = this.findOriginalSelect(selectId);
			if (currentOriginalSelect == null) {
				var newOriginalSelect = new OriginalSelect(selectId, originalSelectedIndex, disableStatus);
				this.originalSelectArray.push(newOriginalSelect);
			}
		},
		findOriginalSelect : function(selectId) {
			for (var i = 0; i < this.originalSelectArray.length; i++) {
				if (this.originalSelectArray[i].selectId == selectId) {
					return this.originalSelectArray[i];
				}
			}
			return null;
		},
};

function createOriginalSelectList (){
	$("#protocolUsersListTable").find("select").each(function() {
		var selectId = $(this).attr("id");
		var originalSelectedIndex = $(this).prop("selectedIndex");
		var disableStatus = $(this).prop("disabled");
		//console.log("selectId: "+selectId+" | originalSelectedIndex: "+originalSelectedIndex+ " | disableStatus: "+disableStatus);
		OriginalSelectStatusHandle.addOriginalSelect(selectId, originalSelectedIndex, disableStatus);
	}); 
}

function resetSelects() {
	$("#protocolUsersListTable").find("select").each(function() { 
		var selectId = $(this).attr("id");
		var originalSelect = OriginalSelectStatusHandle.findOriginalSelect(selectId);
		//console.log(" this selectId: "+selectId+" | originalSelect.originalSelectedIndex:"+originalSelect.originalSelectedIndex+" | originalSelect.disableStatus: "+originalSelect.disableStatus);
		$(this).prop("selectedIndex", originalSelect.originalSelectedIndex);
		$(this).prop("disabled", originalSelect.disableStatus);
	});
	ChangeHandler.changes = [];
	var oTable = $("#protocolUsersListTable").idtApi('getTableApi');
	//oTable.rows().invalidate().draw();
	oTable.ajax.reload();
}

function save() {
	$("#userRolesAssignment").val(ChangeHandler.toJSON());
	$("#assignForm").submit();
}

function cancel() {
	redirectWithReferrer('<s:property value="#webRoot"/>/protocol/protocolUser.action');
}

</script>

<!-- Check if the user can edit the page -->
<security:hasProtocolPrivilege privilege="assignuserstoprotocol">
	<script type="text/javascript">
		canEdit = true;
	</script>
</security:hasProtocolPrivilege>

<s:form id="assignForm" method="post" theme="simple" action="saveAssignment">
	<s:hidden name="id" id="protocolId" />
	<s:hidden name="siteId" id="siteId" />
	<s:hidden name="userRolesAssignment" id="userRolesAssignment" />
</s:form>

<p><s:text name="protocol.users.subtitle.display"/></p>
<br>
<div id="protocolUsersListContainer" class="idtTableContainer brics">
	<table id="protocolUsersListTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>
<script type="text/javascript">
$(document).ready(function() {
 		$("#protocolUsersListTable").idtTable({
			idtUrl: "<s:property value='#webRoot'/>/protocol/getProtocolRoleUser.action",
			order: [[2, "desc"]],
			columns: [
				{
			          name: 'username',
			          title: "<s:text name="user.userlist.search.username.display" />",
			          data: 'username',
			          parameter:'userName',
				},
				{
			          name: 'fullname',
			          title: "<s:text name="assignRole.name" />",
			          data: 'fullname',
			          parameter:'fullName',
			         
			          
				},
				{
			          name: 'role',
			          title: "<s:text name="role.subtitle.role.display" />",
			          data: 'role',
			          parameter:'roleSelected',
			          orderDataType: "dom-select",
			          type: 'string'
				},
				<%if (study.getStudySites() != null && study.getStudySites().size()> 0) 
				{
					%>
				{
			          name: 'studysite',
			          title: "<s:text name="protocol.currentStudy.other.studySite" />",
			          data: 'studysite',
			          parameter:'siteSelect',
			          orderDataType: "dom-select",
			          type: 'string'
				},
				<%
				}
				%>

			],
          	initComplete: function(){
          		var table = $("#protocolUsersListTable");
          		var fullRows = table.idtApi("getRows");
          		var checkedColumns = [];

          		$('#protocolUsersListTable_wrapper').find('.idt_searchContainer').mouseover(function(){
						
      				if (checkedColumns.length == 0){
      					checkedColumns = getDTCheckedColumns();
      				}

	          		$('.idt_selectColumnCheckbox').unbind().on('click', function(e) {
	          			
	          			checkedColumns = getDTCheckedColumns();	
	          			//console.log("on click checkedColumns: "+checkedColumns.toString());			          			
	          			var thisVal = $(this).val();
	          			if(this.checked && checkedColumns.toString().indexOf(thisVal) < 0) {
	          				checkedColumns.push(thisVal);
	          			}
	          			//console.log("on click checkedColumns.toString(): "+checkedColumns.toString());
	          			var searchData = $('.idt_searchInput').val();
						searchDataTableColumns(table, searchData, checkedColumns, fullRows);
			  			
          		 	}); //end click
	          		
	          		
          		});//end mouseover
          		
			    $(".idt_searchInput").unbind().on("keyup", _.debounce( function(e) {
			    	var thisVal = $(this).val();
		  			//console.log("keyup checkedColumns.toString(): "+checkedColumns.toString());

	  				searchDataTableColumns(table, thisVal, checkedColumns, fullRows);

		  		}, 100, true));

			    createOriginalSelectList();
          	} //end intiComplete
			
		}); //end idtTable
		
		function getDTCheckedColumns() {
			var columnCheckbox = $('.idt_searchContainer').find(".idt_selectColumnCheckbox");
			var checkedColumns = [];
			if(columnCheckbox.length){
				var checked = [];
	  			$('#protocolUsersListTable_wrapper').find('.idt_selectColumnCheckbox').each(function(i){
	  				if(this.checked) {
	  					checked.push($(this).val());
	  				}
	  		    });
	  			checkedColumns = checked;
			}
			return checkedColumns;
		}
		
	    $.fn.dataTable.ext.order['dom-select'] = function  ( settings, col ) {
	        return this.api().column( col, {order:'index'} ).nodes().map( function ( td, i ) {
	        	var $target = $('select', td);
	        	var value = $target.find("option:selected").text(); //console.log("select val(): "+value);
	            return value;
	        } );
    }

})


	function searchDataTableColumns(table, searchData, checkedColumns, fullRows) {
		var oTable = table.idtApi('getTableApi');
		//console.log("searchDataTableColumns checkedColumns: "+checkedColumns.toString()+"\nsearchData: "+searchData+" length: "+searchData.length);
		
		if (checkedColumns.length == 0) {
			oTable.clear().draw();
		} else if (searchData.length > 0){
			oTable.clear().rows.add(fullRows).draw();
			var displayRowList = [];
			
			oTable.rows().every( function ( rowIdx, tableLoop, rowLoop ) { 
				var rowData = this.data(); //console.log("rowData: "+JSON.stringify(rowData));
				for (var i = 0; i < checkedColumns.length; i++) {
					columnName = checkedColumns[i]; //console.log("columnName "+columnName);
					var rowColumnData = rowData[columnName];//console.log("row data: "+row[columnName]);
					if(columnName == "username" ) {
						 
						if (rowColumnData.toLowerCase().indexOf(searchData.toLowerCase())>=0) {
							displayRowList.push(rowData);
							return true;
						}
					} else if (columnName == "fullname") {						
						var $div = $('<div>').html(rowColumnData);//console.log("$div: "+$div);
						var selectedVal = $div.text();//console.log("selectedVal: "+selectedVal);
						if(selectedVal.toLowerCase().indexOf(searchData.toLowerCase())>=0) {//console.log("selectedVal: "+selectedVal);
							displayRowList.push(rowData);
							return true;
						}
						
					} else if (columnName == "role" || columnName == "studysite"){
						var $div = $('<div>').html(rowColumnData);//console.log("$div: "+$div);
						var selectedVal = $div.find('p').text();//console.log("selectedVal: "+selectedVal);
						if(selectedVal.toLowerCase().indexOf(searchData.toLowerCase())>=0) {//console.log("selectedVal: "+selectedVal);
							displayRowList.push(rowData);
							return true;
						}
					}	 
				}
			});
			//console.log("displayRowList.length: "+displayRowList.length);
			oTable.clear().rows.add(displayRowList).draw();
		} else {
			oTable.clear().rows.add(fullRows).draw();
		}
	}  //end function searchDataTableColumns
</script>
<security:hasProtocolPrivilege privilege="assignuserstoprotocol">
	<div class="formrow_1">
		<input type="button" value="<s:text name='button.Cancel'/>" onclick="cancel()" title = "Click to cancel (changes will not be saved)." />
		<input type="reset" value="<s:text name='button.Reset'/>" onclick="resetSelects()" title = "Click to clear fields" />
		<input type="button" value="<s:text name='button.Save'/>" onclick="save()" title ="Click to save changes" />
	</div>
</security:hasProtocolPrivilege>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>