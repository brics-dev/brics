<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs"%>
<%@ page import="gov.nih.nichd.ctdb.form.common.FormResultControl"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="java.util.Locale"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewforms" />

<%
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
	Locale l = request.getLocale();
%>
<html>
<c:set var="pageTitle" scope="request">
	<s:text name="form.forms.manageFormsDisplay" />
</c:set>

<jsp:include page="/common/header_sofi_struts2.jsp" />
<!-- -- enter your stuff after here -->
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/idtTableWidget.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/fnDtFilterPlugin.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/idtTableActions.js" type="text/javascript"></script>

<style type="text/css">
.morecontent span {
    display: none;
}
</style>


<body>
    <div id="data_table_second" class="tableContainer">
        <div id="test"></div>
        <table id="tableTest" class="dataTable table table-striped table-bordered" cellspacing="0" width="100%">
        </table>
    </div>
    <br />
    <br />
    <div id="data_table_second" class="tableContainer1">
    	<div id="dialog"></div>
        <div id="test1"></div>
        <table id="tableTest1" class="table table-striped table-bordered" cellspacing="0" width="100%">
        </table>
    </div>
    <style type="text/css">
    .morecontent span {
        display: none;
    }
    /*.morelink {
    display: block;
}*/
    </style>
<script type="text/javascript">

$(document).ready(function() {
	  $("#tableTest").idtTable({
          idtUrl: 'http://fitbir-portal-local.cit.nih.gov:8082/proforms/form/formList.action',
          idtData: {
              primaryKey: "name"
          },
          pages: 1,
          "processing": false,
          "serverSide": false,
          length: 10,
          "columns": [
            {
                "data": '<%=rs.getValue("form.name.display", l)%>',
                "title":'<%=rs.getValue("form.name.display", l)%>',
                "name": '<%=rs.getValue("form.name.display", l)%>',
                "parameter" : 'name',
                "searchable": true,
                "orderable": true,
                "render": IdtActions.ellipsis(10)
            },
            {
                "data": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                "title": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                "name": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                "parameter" : 'numQuestions',
                "searchable": true,
                "orderable": true
            },
            {
                "data": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                "title": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                "name": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                "parameter" : 'status.shortName',
                "searchable": true,
                "orderable": true
            },
            {
                "data": '<%=rs.getValue("form.administered.display", l)%>',
                "title": '<%=rs.getValue("form.administered.display", l)%>',
                "name": '<%=rs.getValue("form.administered.display", l)%>',
                "parameter" : 'administered',
                "searchable": true,
                "orderable": true
            },
            {
                "data": '<%=rs.getValue("form.public.search.date.display", l)%>',
                "title": '<%=rs.getValue("form.public.search.date.display", l)%>',
                "name": '<%=rs.getValue("form.public.search.date.display", l)%>',
				"parameter" : 'updatedDate',
				"searchable" : true,
				"orderable" : true,
				"render": IdtActions.formatDate()
			} ],
			dom : 'Bfrtip',
			fixedHeader : true,
			select : 'multi',
			bFilter: true,
            filters: [{
                type: 'select',
                name: 'status',
                defaultValue: 'in progress',
 /*                options: [{
                        value: 'marshall',
                        label: 'name',

                    },
                    {
                        value: 'regional director',
                        label: 'regional director'
                    },
                    {
                        value: 'seach',
                        label: 'search'
                    }
                ], */
/*                 bRegex: true,
 */                options: ['active', 'in progress'],
                // containerId: 'test',
                columnIndex: 2,
                // test: function(oSettings, aData, iDataIndex, filterData) {
                //     if(aData[1] == 'Marshall'){
                //         console.log('test it', aData[1]);
                //         return true;
                //     }
                // }
            }
        ],
      buttons: {
            buttons: [{
                    text: 'Row selected data',
                    action: function(e, dt, node, config) {
                        alert(
                            'Row data: ' +
                            JSON.stringify(dt.row({ selected: true }).data())
                        );
                    },
                    enabled: false,
                    className: 'test'

                },
                {
                    extend: 'delete',
/*                     url: 'http://localhost:8089/post'
 */                },
                {
                    extend: 'addRow',
                    values: [true, true, true, true, {'false': "2014-06-10 13:02"}],
                    url: 'http://localhost:8089/post'
 
                },               
                {
                    extend: 'print',
                    exportSelected: true,
                    action: IdtActions.exportAction()

                },
                {
                    extend: 'csv',
                    action: IdtActions.exportAction()
                },

                {
                    extend: 'pdf',
                    exportSelected: true,
                    action: IdtActions.exportAction()
                },
                {
                    extend: 'excel',
                    exportSelected: true,
                    action: IdtActions.exportAction()
                }
            ]
        } 
  });
});

</script>

</html>

