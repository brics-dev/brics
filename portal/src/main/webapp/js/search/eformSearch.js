// Load a search at the start
$('document').ready(function() {

	var action = "eFormSearchAction!getSearch.ajax";

	$('.searchEformForm').idtCustomSearch({
		searchList: [{
			type: 'radio',
			name: 'ownerId',
			containerId: 'eformOwnerOptions',
			legend: 'Ownership',
			defaultValue: '1',
			options: [{
				value: '1',
				id: 'ownerAll',
				label: 'All'
			}, {
				value: '0',
				id: 'ownerMine',
				label: 'Mine'
			}]
		}],
		idtTableId: 'eFormResultsTable',
		idtConfigs: {
			idtUrl: action,
			serverSide: false,
			processing: false,
			select: false,
			searching: true,
			filtering: true,
			pageLength: 15,
			autoWidth: false,
			columns: [
				{
					name: 'id',
					title: 'id',
					data: 'id',
					parameter: 'id',
					visible: false,
					searchable: false
				},
				{
					name: 'eFormTitle',
					title: 'eForm Title',
					data: 'title',
					parameter: 'title',
					width: '30%',
					render: function(data, type, row, full) {
						var newContent = '';
						if ($('#inAdmin').val() == "true") {
							newContent = '<a class=tdLink href=\"/portal/dictionaryAdmin/eFormAction!view.action?eformId='
								+ row.id
								+ '\">'
								+ data
								+ '</a>';
						}
						else {
							newContent = '<a class=tdLink href=\"/portal/dictionary/eFormAction!view.action?eformId='
								+ row.id
								+ '\">'
								+ data
								+ '</a>';
						}
						return newContent;
					}
				},
				{
					name: 'shortName',
					title: 'Short Name',
					parameter: 'shortName',
					data: 'shortName',
					width: '17%',
				},
				{
					name: 'Status',
					title: 'Status',
					parameter: 'status',
					data: 'status',
					width: '10%',
				},
				{
					name: 'modifiedDate',
					title: 'Modified Date',
					parameter: 'createDate',
					data: 'modifiedDate',
					width: '14%',
					render: IdtActions.formatDate()
				},
				{
					name: 'formStructureTitle',
					title: 'Form Structure Title',
					parameter: 'formStructureTitle',
					data: 'formStructureTitle',
					width: '30%'
				}
			],
			"order": [[1, "asc"]]
		}
	})
});
