<title>View Studies</title>
<%@include file="/common/taglibs.jsp" %>
<s:if test="!inAdmin"><title>View Studies</title></s:if>
<s:else><title>Manage Studies</title></s:else>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
    <jsp:include page="../navigation/dataRepositoryNavigation.jsp"/>
    <h1 class="float-left">Data Repository</h1>
    <div style="clear:both"></div>

    <!--begin #center-content -->
    <div id="main-content">
        <div class="clear-float">
            <s:if test="!inAdmin">
                <h2>View Studies</h2>
                <p>
                    View Studies lists the studies that the user has permissions to view.
                    The provided filters will allows users to filter the list by ownership, data submission status, and
                    data type.
                    The search capability allows users to search by Study Title, Study ID, Principle Investigator (PI),
                    and by the Permission Type that the user holds for a particular study (Owner, Admin, Read, Write).
                </p>
                <p>
                    Results are shown in a tabular format to include the following:
                </p>
            </s:if>
            <s:else>
                <h2>Manage Studies</h2>
                <p>All studies are listed in the Manage Studies content page. Administrators (Admin) can search for a
                    Study by keyword or by the Status state, which can either be Requested, Public, Private, Rejected,
                    or All. Results are shown in a tabular format to include the following:</p>
            </s:else>

            <form>
                <input type="hidden" class="studySelectedFilter" id="-1"/>
                <s:if test="inAdmin">
                <div class="button margin-right" style="float: left;">
                    <input type="button" id="downloadReportBtn" value="Download Report"
                           href="accessRecordDownloadAction!showDownloadAccessReportLightbox.ajax"/>
                </div>
                </s:if>
        </div>
        <div id="studyListTable" class="idtTableContainer">
            <div id="dialog"></div>
            <table id="studyListTableTable" class="table table-striped table-bordered" width="100%"></table>
        </div>
        <script type="text/javascript">
            $(document).ready(function () {
                $('#studyListTableTable').idtTable({
                	<s:if test="inAdmin">
                	idtUrl: "<s:url value='/studyAdmin/studyAction!getStudyTableList.action' />",
                	</s:if>
                	<s:else>
                    idtUrl: "<s:url value='/study/studyAction!getStudyTableList.action' />",
                    </s:else>
                    idtData: {
                        primaryKey: "id"
                    },
                    pageLength: 15,
                    "autoWidth": false,
                    "columns": [
                        <s:if test="inAdmin">
                        {
                            "data": "studyAdminLink",
                            "title": "TITLE",
                            "name": "TITLE",
                            "parameter": "studyAdminLink",
                            "searchable": true,
                            "orderable": true,
                            "width": '40%'
                        },
                        {
                            "data": "organization",
                            "title": "ORGANIZATION",
                            "name": "ORGANIZATION",
                            "parameter": "organization",
                            "searchable": true,
                            "orderable": true,
                            "width": '10%'
                        },
                        {
                       	    "data": "studyKeywords",
                            "title": "STUDY KEYWORDS",
                            "name": "STUDY KEYWORDS",
                            "parameter": "studyKeywords",
                            "searchable": true,
                            "visible": false
                        },
                        </s:if>
                        <s:else>
                        {
                            "data": 'studyNoAdminLink',
                            "title": "TITLE",
                            "name": "TITLE",
                            "parameter": 'studyNoAdminLink',
                            "searchable": true,
                            "orderable": true,
                            "width": '40%'
                        },
                        {
                            "data": "organization",
                            "title": "ORGANIZATION",
                            "name": "ORGANIZATION",
                            "parameter": "organization",
                            "searchable": true,
                            "orderable": true,
                            "width": '10%'
                        },
                        </s:else>
                        {
                            "data": "prefixedId",
                            "title": "STUDY ID",
                            "name": "STUDY ID",
                            "parameter": "prefixedId",
                            "class": "nobreak",
                            "searchable": true,
                            "orderable": true,
                            "width": '10%'
                        },
                        {
                            "data": "principalInvestigator",
                            "title": "PI",
                            "name": "PI",
                            "parameter": "principalInvestigator",
                            "searchable": true,
                            "orderable": true,
                            "width": '10%'
                        },
                        <s:if test="!inAdmin">
                        {
                            "data": "dataTypes",
                            "title": "DATA TYPES",
                            "name": "DATA TYPES",
                            "parameter": "dataTypes",
                            "class": "nobreak",
                            "searchable": true,
                            "orderable": true,
                            "width": '10%'
                        },
                        {
                            "data": "permission",
                            "title": "PERMISSION",
                            "name": "PERMISSION",
                            "parameter": "permission",
                            "searchable": true,
                            "orderable": true,
                            "width": '7.5%'
                        },
                        {
                            "data": "fundingSource",
                            "title": "FUNDING SOURCE",
                            "name": "FUNDING SOURCE",
                            "parameter": "fundingSource",
                            "searchable": true,
                            "orderable": true,
                            "width": '7.5%'
                        },
                        {
                            "data": "sharedData",
                            "title": "SHARED DATA",
                            "name": "SHARED DATA",
                            "parameter": "sharedData",
                            "searchable": true,
                            "orderable": true,
                            "width": '5%'
                        },
                        
                        {
                       	    "data": "studyKeywords",
                            "title": "STUDY KEYWORDS",
                            "name": "STUDY KEYWORDS",
                            "parameter": "studyKeywords",
                            "searchable": true,
                            "visible": false
                        },
                        {
                            "data": "studyType",
                            "title": "STUDY TYPE",
                            "name": "STUDY TYPE",
                            "parameter": "studyType",
                            "searchable": true,
                            "visible": false
                            
                        },
                        </s:if>
                        <s:else>
                        {
                            "data": "owner",
                            "title": "OWNER",
                            "name": "OWNER",
                            "parameter": "owner",
                            "searchable": true,
                            "orderable": true,
                            "width": '10%'
                        },
                        {
                            "data": "status",
                            "title": "STATUS",
                            "name": "STATUS",
                            "parameter": "status",
                            "searchable": true,
                            "orderable": true,
                            "width": '10%'
                        },
                        {
                            "data": "dateCreated",
                            "title": "REQUEST DATE",
                            "name": "REQUEST DATE",
                            "parameter": "dateCreated",
                            "searchable": true,
                            "orderable": true,
                            "width": '10%',
                            "render": IdtActions.formatDate()
                        }
                        </s:else>
                    ],
                    <s:if test="inAdmin">
                    select: 'multi',
                    "drawCallback": function (settings) {
                        var api = new $.fn.dataTable.Api(settings);
                        api.on("select", function (e, dt, type, index) {
                            updateSelectStudies();
                        })
                            .on("deselect", function (e, dt, type, index) {
                                updateSelectStudies();
                            });
                    }
                    </s:if>
                    <s:else>
                    filters: [
                        	{
                                type: 'select',
                                name: 'All Study Types',
                                options: [
                                    {
                                        value: 'Natural History',
                                        label: 'Natural History'
                                    },
                                    {
                                        value: 'Epidemiology',
                                        label: 'Epidemiology'
                                    },
                                    {
                                        value: 'Clinical Trial',
                                        label: 'Clinical Trial'
                                    },
                                    {
                                        value: 'Pre-Clinical',
                                        label: 'Pre-Clinical'
                                    },
                                    {
                                        value: 'Meta Analysis',
                                        label: 'Meta Analysis'
                                    },
                                    {
                                        value: 'Other',
                                        label: 'Other'
                                    },
                                    
                                ],
                                columnIndex: 9
                        },
                        {
                            type: 'select',
                            name: 'All data types',
                            options: [
                                {
                                    value: 'clinical assessment',
                                    label: 'Clinical Assessment',

                                },
                                {
                                    value: 'imaging',
                                    label: 'Imaging'
                                },
                                {
                                    value: 'genomics',
                                    label: 'Genomics'
                                }
                            ],
                            columnIndex: 4
                        },
                        {
                            type: 'select',
                            name: 'All studies',
                            options: [
                                {
                                    value: 'no data',
                                    label: 'Only studies with data'
                                },
                                {
                                	value: 'N',
                                	label: 'All Studies with Shared Data'
                                }
                            ],
                            test: function (oSettings, aData, iDataIndex, filterData) {
                            	
                                var dataTypeNone = aData[4].indexOf("no data") > -1;
                                var dataShared = aData[7].indexOf("N") >-1;
                                
                                if (filterData['All studies'] == aData[4]) {
                                    return !dataTypeNone;
                                } 
                                else if (filterData['All studies'] == aData[7]) {
                                	return !dataShared;
                                }
                                else {
                                    return true;
                                }
                            }
                        },
                        {
                            type: 'select',
                            name: 'Ownership: all',
                            options: [
                                {
                                    value: 'owner',
                                    label: 'Ownership: mine'
                                }
                            ],
                            columnIndex: 5
                        }
                    ]
                    </s:else>
                })
            })
        </script>
        <s:if test="isAdmin">
            <div>
                <p>
							<span class="inline-special-instruction">Note: Study Administrators have administrative permission for all
								studies regardless of any permissions they have been granted.</span>
                </p>
            </div>
        </s:if>
        </form>
    </div>
</div>
</div>
<!-- end of .border-wrapper -->


<script src ="/portal/js/search/studySearch.js"></script>
<script type="text/javascript">
    <s:if test="!inAdmin" >
    setNavigation({
        "bodyClass": "primary",
        "navigationLinkID": "dataRepositoryModuleLink",
        "subnavigationLinkID": "studyToolLink",
        "tertiaryLinkID": "browseStudyLink"
    });
    </s:if>
    <s:else>
    setNavigation({
        "bodyClass": "primary",
        "navigationLinkID": "dataRepositoryModuleLink",
        "subnavigationLinkID": "contributeDataToolsLink",
        "tertiaryLinkID": "studyList"
    });
    </s:else>

    // Load a search at the start
    $('document').ready(function () {

        $("#downloadReportBtn").fancybox({
            'width': 200,
            'overlayShow': true,
            onComplete: function () {
                $("#fancybox-content > div").css({'overflow-x': 'hidden'});
            }
        });

        /*Download Report button is greyed out by default*/
        $("#downloadReportBtn").prop("disabled", true);
        $("#downloadReportBtn").parent().closest('div').addClass("disabled");
    });

    function submitSimpleForm(theForm) {
        var theForm = document.forms['accessRecordForm'];
        theForm.action = "accessRecordDownloadAction!adminDownloadReport.action";
        $.fancybox.close();
        theForm.submit();
    }

    function submitFormAjax() {
        var myXHR = $.ajax({
            type: "POST",
            cache: false,
            url: "accessRecordDownloadAction!validationSuccess.ajax",
            data: $("#accessRecordForm").serializeArray(),
            success: function (data) {
                if (data == "success") {
                    var theForm = document.forms['accessRecordForm'];
                    theForm.action = "accessRecordDownloadAction!adminDownloadReport.action";
                    $.fancybox.close();
                    theForm.submit();
                }
                else {
                    $.fancybox(data);
                }
            }
        });
    }
</script>
