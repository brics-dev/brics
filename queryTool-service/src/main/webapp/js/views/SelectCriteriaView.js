/**
 * model: query
 */
QT.SelectCriteriaView = BaseView.extend({
	isRendered: false,
	hiddenColumnsProperties : [], // used in case this view has not been rendered yet

	events: {
		"change .selectCriteriaDeCheckbox": "includeInTable",
		"click .selectCriteriaGroupExpandCollapse": "expandCollapse",
		"click .selectCriteriaDeFilter": "filter",
		"click .deInformation": "deInformation",
		"click .hideBlankCol": "hideAllBlankCol",
		"click .showBlankCol": "showAllBlankCol",
		"click .formExpandCollapse": "expandCollapse",
		"click .formAddRemoveAll" : "addRemoveAll",
		"click .sectionAddRemoveAll" : "addRemoveAll"
	},

	initialize: function() {
		EventBus.on("query:formDetailsAvailable", this.reRender, this);
		EventBus.on("add:queryFilter", this.onAddFilter, this);
		EventBus.on("remove:filter", this.onRemoveFilter, this);
		EventBus.on("remove:visualFilter", this.onRemoveFilter, this);
		EventBus.on("clearDataCart", this.onClearDataCart, this);
		EventBus.on("hamburgerview:showHideCol", this.selectDeselectColumn, this);
		EventBus.on("query:reset", this.onClearDataCart, this);
		EventBus.on("runQuery", this.onClearDataCart, this);
		EventBus.on("query:reRun", this.onClearDataCart, this);
		EventBus.on("addTohiddenColumnsProperties", this.addToHiddenColumnProperties, this);

		QT.SelectCriteriaView.__super__.initialize.call(this);
	},

	/**
	 * Data looks like this: forms : [ uri : "", repeatableGroups : [ uri : "",
	 * name : "", position: 0, dataElements : [ uri : "", name: "", title : "",
	 * selected : true ] ] ]
	 */
	render: function() {
		// re-render if needed
		if (this.isRendered) {
			this.empty();
		}

		if (Object.keys(this.model.get("formDetails")).length == 0) {
			if (this.$(".selectCriteriaNoContent").length == 0) {
				this.$(".clearfix").before("<div class=\"selectCriteriaNoContent\">" + Config.language.selectCriteriaEmpty + "</div>");
			}
			else {
				this.$(".selectCriteriaNoContent").show();
			}
		}
		else {
			this.$(".selectCriteriaNoContent").hide();
			var hideAllTemplate = TemplateManager.getTemplate("hideAllBlankColumns");
			var formTemplate = TemplateManager.getTemplate("selectCriteriaForm");
			var groupTemplate = TemplateManager.getTemplate("selectCriteriaGroup");
			var deTemplate = TemplateManager.getTemplate("selectCriteriaDe");
			var data = this.model.get("formDetails");
			
			// add hide all button
			this.$(" > .clearfix").before($(hideAllTemplate(this.model.toJSON())));
			// overwritten inside the loop
			var $formContainer = "There is no data to display";
			for (var i = 0; i < data.length; i++) {
				var formJson = data[i];
				var form = QueryTool.page.get("forms").get(formJson.uri);
				var $formContainer = $(formTemplate(form.attributes));
				var $groupsContainer = $formContainer.find(".selectCriteriaGroupContainer .clearfix");

				
				var groups = formJson.repeatableGroups;
				
				//Fake repeatable group that contains the dataset column won't have rgUri, 
				//for reference purpose it is set to the form uri
				var datasetGroup = { 
						uri : formJson.uri,
					    name : "DataSet Info", 
					    position: 0, 
					    dataElements : [{ uri : "", name: "Dataset", title : "Dataset", selected : true, filterType: "DATASET"} ] 
				};
				
				groups.unshift(datasetGroup);
				
				var groupLength = groups.length;		

				for (var j = 0; j < groupLength; j++) {
					var group = groups[j];
					var $groupContent = $(groupTemplate(group));
					$groupsContainer.before($groupContent);
					var $elementsContainer = $groupContent.find(".selectCriteriaDeContainer .clearfix");
					var dataElements = group.dataElements;
					var dataElementsLength = dataElements.length;

					for (var k = 0; k < dataElementsLength; k++) {
						var de = dataElements[k];
						de.id = "selectCriteriaShowHide_" + i + "_" + j + "_" + k;
						
						// if in pdbp and the user is a non-admin, don't render the "visit date" DE
						if (de.name == "VisitDate"
								&& System.environment == "pdbp"
								&& !System.user.isSysAdmin
								&& !System.user.isQTAdmin) {
							continue;
						}
								
						// render the DE
						$elementsContainer.before(deTemplate(de));
						// /TODO: Figure out a better way to implement hidden
						// filters. r.s.
						if (dataElements[k].name === "GUID" && System.environment === "pdbp") {
							changeInDiagnosisFilter = {
								title: "Highlight Diagnosis",
								name: "highlight_diagnosis",
								selected: false,
								filterType: "CHANGE_IN_DIAGNOSIS",
								uri: "change_in_diagnosis",
								type: "Change in Diagnosis",
							};
							$elementsContainer.before(deTemplate(changeInDiagnosisFilter));
						}
					}
				}
				this.$(" > .clearfix").before($formContainer);

			}

		}

		/*
		 * Disable the filter button for change in diagnosis for PDBP
		 */
		var allFilters = this.model.filters.toJson();
		for (var i = 0; i < allFilters.length; i++) {
			var filter = allFilters[i];
			// this implements the change in diagnosis filter for GUID
			if (filter.elementName == 'GUID' && System.environment === "pdbp") {
				var $diagnosisButton = $('[uri="' + filter.formUri + '"] [groupname="' + filter.groupName + '"] [dename="' + filter.elementName + '"][filterType="CHANGE_IN_DIAGNOSIS"]').find(
								"a.selectCriteriaDeFilter");
				$diagnosisButton.addClass("disabled").prop("disabled", true);
				break;
			}
			var $button = $('[uri="' + filter.formUri + '"] [groupname="' + filter.groupName + '"] [dename="' + filter.elementName + '"]').find("a.selectCriteriaDeFilter");

			$button.addClass("filtering");
		}

		// hide the (i) icon for change_in_diagnosis
		$('[uri="change_in_diagnosis"] a.deInformation').hide();
		
		this.checkStoredHiddenColumns();

		QT.SelectCriteriaView.__super__.render.call(this);
		this.isRendered = true;
	},

	reRender: function() {
		if (QueryTool.query.get("queryType") == "full" || this.isRendered == false) {
			this.render();
		}
	},

	empty: function() {
		this.$el.html("<div class=\"clearfix\"></div>");
	},
	
	checkStoredHiddenColumns : function() {
		var hidden = this.hiddenColumnsProperties;
		for (var i = 0, len = hidden.length; i < len; i++) {
			this.selectDeselectColumn(hidden[i]);
		}
		this.hiddenColumnsProperties = [];
	},
	
	getIncludeCheckboxesByButton : function($button) {
		return $button.parent().parent().find("[id*='selectCriteriaShowHide'].selectCriteriaDeCheckbox");
	},
	
	addRemoveAll : function(event) {
		EventBus.trigger("open:processing", "Hiding/Unhiding Columns");
		var $clickable = $(event.target);
		this.addRemoveProvided($clickable, this.getIncludeCheckboxesByButton($clickable));
	},
	
	addRemoveProvided : function($clickable, $checkboxes) {
		if ($clickable.hasClass("checkboxUnchecked")) {
			// check all
			this.checkUncheck($checkboxes, true);
		}
		else {
			// either checked or indeterminate, uncheck all
			this.checkUncheck($checkboxes, false);
		}
		this.updateSelectAllButtonStyle($clickable, $checkboxes);
	},
	
	checkUncheck : function($checkboxes, finalStateChecked) {
         
        
		var checkBoxesCount = $checkboxes.length;
		var applyCheckBoxProcessingEnd = _.after(checkBoxesCount, function() {
			EventBus.off("processed:columnCheckBox", applyCheckBoxProcessingEnd);
			EventBus.trigger("close:processing");
		});
		EventBus.on("processed:columnCheckBox", applyCheckBoxProcessingEnd, this);
		$checkboxes.each(function() {
			$(this).prop("checked", finalStateChecked).trigger("change");
		});
	},

	/**
	 * Updates the "select all/select none" checkbox icon to reflect the state of the actual checkboxes.
	 * 
	 * @param $button the button to reference
	 * @param $checkboxes (optional) the checkboxes controlled by the button
	 */
	updateSelectAllButtonStyle : function($button, $checkboxes) {
		if (!$checkboxes) {
			var $checkboxes = this.getIncludeCheckboxesByButton($button);
		}
		var allChecked = true;
		var allUnchecked = true;
		$checkboxes.each(function() {
			if (!this.checked) {
				allChecked = false;
			}
			else {
				allUnchecked = false;
			}
		});
		
		if (allChecked) {
			$button.removeClass("checkboxIndeterminate").removeClass("checkboxUnchecked").addClass("checkboxChecked");
		}
		else if (allUnchecked) {
			$button.removeClass("checkboxIndeterminate").removeClass("checkboxChecked").addClass("checkboxUnchecked");
		}
		else {
			$button.removeClass("checkboxUnchecked").removeClass("checkboxChecked").addClass("checkboxIndeterminate");
		}
	},
	
	expandCollapse : function(event) {
		var $collapsible = $(event.target).parent().parent().find(".selectCriteriaCollapsible").first();
		var $clickable = $(event.target); //TODO: verify this is correct
		var collapseIcon = "pe-is-i-minus-circle";
		var expandIcon = "pe-is-i-plus-circle";
		if ($collapsible.is(":visible")) {
			$collapsible.hide();
			$clickable.removeClass(collapseIcon).addClass(expandIcon);
		}
		else {
			$collapsible.show();
			$clickable.removeClass(expandIcon).addClass(collapseIcon);
		}
	},

	filter: function(event) {
		var $target = $(event.target);
		if (!$target.is(".selectCriteriaDeFilter")) {
			$target = $target.parents(".selectCriteriaDeFilter").eq(0);
		}
		if (!$target.is(":disabled") && !$target.hasClass("disabled") && !$target.hasClass("filtering")) {
			var $de = $target.parents(".selectCriteriaDe").eq(0);
			var deUri = $de.attr("uri");
			var deName = $de.attr("deName");

			var $rg = $target.parents(".selectCriteriaGroup").eq(0);
			var rgUri = $rg.attr("uri");
			var rgName = $rg.attr("groupName");

			var formUri = $target.parents(".selectCriteriaForm").eq(0).attr("uri");
			// open the panel for filters
			EventBus.trigger("openFilterPane");

			// below was added to facilitate hidden highlight filter

			if ($de.attr("filterType") && $de.attr("filterType") === "CHANGE_IN_DIAGNOSIS") {
				var localData = {
					formUri: formUri,
					groupUri: rgUri,
					elementUri: deUri,
					groupName: rgName,
					elementName: deName
				};
				var data = {
					name: "DiagnosChangeInd",
					permissibleValues: ["No", "Yes"],
					type: "Change in Diagnosis",
					inputRestrictions: "Radio Values",
					elementName: "highlight_diagnosis",
					filterMode: "CHANGE_IN_DIAGNOSIS",
					filterType: "CHANGE_IN_DIAGNOSIS",
					filterJavaType: "CHANGE_IN_DIAGNOSIS",
					showGenericSelect: false,

				};
				combineData(data,localData);
				
			} else if($de.attr("filterType") && $de.attr("filterType") === "DATASET"){
				
				//Fake repeatable group that contains the dataset column won't have rgUri, 
				//for reference purpose it is set to the form uri
				var localData = {
					formUri: formUri,
					groupUri: formUri,
					elementUri: deUri,
					groupName: rgName,
					elementName: deName
				};
				var data = {
					name: "Dataset",
					permissibleValues: [],
					type: "Dataset",
					inputRestrictions: "Free-Form Entry",
					elementName: "Dataset",
					filterMode: "DATASET",
					filterType: "DATASET",
					filterJavaType: "DATASET",
					showGenericSelect: false,
					showBlankFilter: false

				};
				combineData(data,localData);	
				
			}
			else {

				// this method will retrieve the DE details from the server and
				// then call the eventbus
				this.model.getDeFilterDetails(formUri, rgUri, deUri, rgName, deName);

			}
			
			//This function is currently for Dataset and Change in Diagnosis filter
			// to combine local data with Filter specific data 
			function combineData(data,localData){
				
				var combinedData = $.extend({}, data, localData);
				var model = new QT.QueryFilter();
				model.fromResponseJson(combinedData);
				EventBus.trigger("add:queryFilter", model);
			}
			
			//make sure column is visible
			var formSelectCriteriaContainer = $("div[uri='" + formUri + "']");
			var groupSelectCriteriaContainer = formSelectCriteriaContainer.find("div[uri='" + rgUri + "'][groupname='"+rgName+"']");
			var deSelectCriteriaContainer = groupSelectCriteriaContainer.find("div[uri='" + deUri + "'][dename='" + deName + "']"); 
			var deCheckBox = deSelectCriteriaContainer.find(".selectCriteriaDeCheckbox");
	
			if(!deCheckBox.is(":checked")) { 
			
				EventBus.trigger("hamburgerview:showHideCol", {
					formUri: formUri,
					rgName: rgName,
					rgUri: rgUri,
					deUri: deUri,
					deName: deName,
					// inverting this because the state has NOT been changed yet
					// and visible denotes what it should be set TO
					visible: true
				});
				
				deCheckBox.prop( "checked", true );
			}
			

		}
	},
	
	/**
	 * Responds to a filter being added. If a button is found matching the DE
	 * and RG of the filter, mark it as disabled and other on filter processes.
	 */
	onAddFilter: function(queryFilterModel) {
		var rgUri = queryFilterModel.get("groupUri");
		var elementName = queryFilterModel.get("elementName");
		this.markFilterButtonAsFiltering(elementName, rgUri);
	},

	/**
	 * Responds to a filter being removed. If a button is found matching the DE
	 * and RG of the filter, mark it unused and other on filter processes.
	 */
	onRemoveFilter: function(queryFilterModel) {
		var rgUri = queryFilterModel.get("groupUri");
		var elementName = queryFilterModel.get("elementName");
		this.markFilterButtonAsNotUsed(elementName, rgUri);
	},
	
	findFilterButton : function(elementName, rgUri) {
		var $rg = this.$('[uri="' + rgUri + '"].selectCriteriaGroup');
		var $de = $rg.find('[deName="' + elementName + '"]');
		return $de.find(".selectCriteriaDeFilter");
	},
	
	markFilterButtonAsFiltering : function(elementName, rgUri) {
		this.findFilterButton(elementName, rgUri).addClass("filtering");
	},
	
	markFilterButtonAsNotUsed : function(elementName, rgUri) {
		this.findFilterButton(elementName, rgUri).removeClass("filtering");
	},

	/**
	 * Responds to changes in the checkbox to show/hide the column. NOTE: this
	 * is called AFTER the checkbox changes state
	 */
	includeInTable: function(event) {
		var $target = $(event.target);

		var $de = $target.parents(".selectCriteriaDe").eq(0);
		var deUri = $de.attr("uri");
		var deName = $de.attr("deName");

		var $rg = $target.parents(".selectCriteriaGroup").eq(0);
		var rgUri = $rg.attr("uri");
		var rgName = $rg.attr("groupName");

		var formUri = $target.parents(".selectCriteriaForm").eq(0).attr("uri");

		// reminder: don't change the state of the filter button when added/removed from visual table

		EventBus.trigger("hamburgerview:showHideCol", {
			formUri: formUri,
			rgName: rgName,
			rgUri: rgUri,
			deUri: deUri,
			deName: deName,
			// inverting this because the state has NOT been changed yet
			// and visible denotes what it should be set TO
			visible: $target.is(":checked")
		});
	},

	selectDeselectColumn: function(specs) {
		// find the right checkbox, mark it checked or unchecked
		// we don't have a way to know whether we're showing or hiding so just
		// use the DOM element	
		var $form = this.$('.selectCriteriaForm[uri="' + specs.formUri + '"]');
		var $rg = $form.find('.selectCriteriaGroup[groupname="' + specs.rgName + '"]');
		var $de = $rg.find('.selectCriteriaDe[uri="' + specs.deUri + '"]');
		var $checkbox = $de.find(".selectCriteriaDeCheckbox");
		if (specs.visible != $checkbox.is(":checked")) {
			$checkbox.prop("checked", specs.visible);
		}

		// update filter button
		var deName = $de.attr("dename");
		var $button = $checkbox.parents(".selectCriteriaDe").find("a.selectCriteriaDeFilter");
		if (specs.visible) {
			$button.removeClass("pe-is-i-check-circle-f").addClass("pe-is-i-close-circle");
		}
		else {
			$button.removeClass("pe-is-i-close-circle").addClass("pe-is-i-check-circle-f");
		}
		
		// update select all buttons
		this.updateSelectAllButtonStyle($rg.find(".addRemoveAllButton:first"));
		this.updateSelectAllButtonStyle($form.find(".addRemoveAllButton:first"));
	},
	
	addToHiddenColumnProperties : function(props) {
		// TODO: fill in (see ryan)
		/* { 
												formName: model.shortName, 
												formUri: model.uri, 
												rgName : group.name, 
												rgUri : group.uri , 
												deName : element.shortName, 
												deUri: element.uri, 
												visible: false }
		*/
		this.hiddenColumnsProperties.push(props);
	},

	deInformation: function(event) {
		var $target = $(event.target);
		var deUri = $target.parents(".selectCriteriaDe").eq(0).attr("uri");
		var deModel = QueryTool.page.get("dataElements").get(deUri);
		EventBus.trigger("open:details", deModel);
	},
	
	hideAllBlankCol: function() {
		var $button = $("#hideShowBlankButton");

		$button.removeClass("hideBlankCol");
		$button.addClass("showBlankCol");
		$button.attr("title", "Selects all data elements that have no data submitted against them.");

		EventBus.trigger("query:hideAllBlankColumns");

	},
	showAllBlankCol: function() {
		var $button = $("#hideShowBlankButton");

		$button.removeClass("showBlankCol");
		$button.addClass("hideBlankCol");
		$button.attr("title", "Deselects all data elements that have no data submitted against them.");

		EventBus.trigger("query:showAllBlankColumns");
	},

	onClearDataCart: function() {
		this.empty();
		this.isRendered = false;
		// resets the hide/show column button back to hide.
		var $button = $("#hideShowBlankButton");
		$button.removeClass("showBlankCol");
		$button.addClass("hideBlankCol");
		this.model.set("hideShowColButtonText", "Hide All Blank Columns");
	}
});