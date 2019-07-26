/**
 * 
 */
var RepeatableGroupView = BaseView.extend({
	dialogTitle : "Repeatable Group detail",

	template : "<div class='dataTableContainer dataTableJSON' >	<table id='repeatableGroupTable' >"+
	"</table></div>",
		
	initialize : function() {
		RepeatableGroupView.__super__.initialize.call(this);
	},
	
	close : function() {
		this.Dialog.close();
		RepeatableGroupView.__super__.close.call(this);
	},
	
	
	render : function(model) {
		this.model = model;
		var editView = FormBuilder.page.get("sectionEditView");
		var repeatableGroup = FormBuilder.form.repeatableGroups.byName(editView.model.get("repeatableGroupName"));
		var topTable = "<table>" + 
		"<tr><th align='left' colspan='2'><font color='red'>General Details</font></th></tr>" +
		"<tr><td align='left'><b>Repeatable Group Name:</b></td><td align='left'>&nbsp;&nbsp;" + repeatableGroup.get("repeatableGroupName") + "</td></tr>" +
		"<tr><td align='left'><b>Number of Times Repeated:</b></td><td align='left'>&nbsp;&nbsp;" + repeatableGroup.get("repeatableGroupType") + "</td></tr>" +
		"<tr><td align='left'><b>Threshold:</b></td><td align='left'>&nbsp;&nbsp;" + repeatableGroup.get("repeatableGroupThreshold") + "</td></tr>" +
		"</table>"+
		"<h3 align='left' id='rGroupPopupH'>Data Elements Included</h3>";
		this.$el.html(topTable+this.template);
		this.Dialog.initAndOpen(this);
		RepeatableGroupView.__super__.render.call(this);
	},
	
	destroy : function() {
		this.Dialog.destroy(this);
		RepeatableGroupView.__super__.destroy.call(this);
	},
	
	Dialog : {
   		initAndOpen : function(view) {
   			view.$el.dialog({
   				title : view.dialogTitle,
				modal : true,
				width : 1100,
				
				close : function(event, ui) {
					$(this).dialog("destroy");
				},
				
				open : function(event, ui) {
					var editView = FormBuilder.page.get("sectionEditView");
					var dataElements = FormBuilder.form.dataElements;
					//var preRepeatableGroup = FormBuilder.form.repeatableGroups.byName(editView.model.get("repeatableGroupName"));
					var availableDataElementsArray = new Array();
					
					dataElements.forEach(function(dataElement){
						var DEFullName = dataElement.get("dataElementName");
						var index = DEFullName.indexOf(".");
						var group = DEFullName.substring(0,index);
						if(editView.model.get("repeatableGroupName") == group){
							var deName = DEFullName.substring(index+1);
							var deType = dataElement.get("dataElementType");
							var restrictionName = dataElement.get("restrictionName");
							var description = dataElement.get("description");
							var requiredType = dataElement.get("requiredType");
							var suggestedQuestion = dataElement.get("suggestedQuestion");
							
							
							var de = new Array();
							de.push(deName);
							de.push(group);
							de.push(deType);
							de.push(restrictionName);
							de.push(description);
							de.push(requiredType);
							de.push(suggestedQuestion);
							
							availableDataElementsArray.push(de);
						}
					});   //end big loop
					

					var availableDataElementsColumns =    [
						            						   {"sTitle":"Data Element"},
						            						   {"sTitle":"Group Name"},
						            						   {"sTitle":"Data Type"},
						            						   {"sTitle":"Restriction Type"},
						            						   {"sTitle":"Short Description"},
						            						   {"sTitle":"Required Type"},
						            						   {"sTitle":"Suggested Question"}
						            						];
					
					var availableDataElementsTable = {
							"aaData" : availableDataElementsArray,
						 	"aoColumns" : availableDataElementsColumns
				    };
					
					IDT.buildSingleData(availableDataElementsTable, $("#repeatableGroupTable"));
					
					view.$el.dialog({position: {
						my : "center",
						at : "center",
						of : window
					}});
				}
   			});
   		}
	}
	
});