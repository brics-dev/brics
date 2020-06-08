import PageView from './PageView';
import Identity from '../../util/Identity';
import SecureAjax from '../../util/SecureAjax';

var getDTHbs = require('../templates/downloadTool.hbs');

let DownloadToolView;
export default DownloadToolView = PageView.extend({
	className: "dt-page-downloadTool",
	PACKAGE_LIST_URL_TAIL : "/downloadtool/queue/",
	DOWNLOAD_URL_TAIL : "filerepository/files/getUserFile/",
	ORIGIN : window.location.origin + "/",
	packageData: {},
	detailsRowCache : {},
	packageTableApi : null,
	detailsTableApi: null,
	cancelRequested : false,
	downloadEnabled : false,
	
	events: {
		"click #dt-downloadButton" : 'doDownload',
		"click #dt-cancelDownload" : "cancelDownload"
	},

	initialize: function(params) {
		DownloadToolView.__super__.initialize.call(this, params);
		this.pageName = "downloadTool";
		EventBus.on("download:complete", this.onAllDownloadsComplete, this);
	},

	render: function() {
		this.renderTemplate(getDTHbs, this, function() {
			this.renderPackageTable();
			this.renderDetailsTable();
			this.getPackageList();
		});
	},
	
	getPackageList : function() {
		SecureAjax(Identity.jwt, {
			url: this.config.apiBaseUrl + this.PACKAGE_LIST_URL_TAIL,
			type: "GET",
			cache: false,
			success: (data) => {
				this.onLoadPackageList(data);
			},
			error: () => {
				$.ibisMessaging("dialog", "error", "There was a problem getting the Download Queue list.  Please refresh this page and try again.");
			}
		});
	},
	
	renderPackageTable : function() {
		this.$("#dt-packages-table").idtTable({
			data: [],
			serverSide: false,
			idtData : {
				primaryKey: "DT_RowId"
			},
			dom : 'Bfrtip',
			pageLength: 10,
			select: "single",
			columns : [{
				title: "Name",
				data: "name",
				parameter: "name",
				name: "name"
			},
			{
				title: "Date Added",
				data: "dateAdded",
				parameter: "dateAdded",
				name: "dateAdded"
			},
			{
				title: "Files",
				data: "files",
				parameter: "files",
				name: "files"
			},
			{
				title: "Size",
				data: "size",
				parameter: "size",
				name: "size"
			},
			{
				title: "Origin",
				data: "origin",
				parameter: "origin",
				name: "origin"
			}],
			buttons: [
				{
					text: "Remove Package(s)",
					action: (e, dt, node, config) => {
						let selectedPackageId = dt.row({selected: true}).id();
						if (selectedPackageId.length > 0) {
							this.removePackage(selectedPackageId);
						}
					},
					enabled: false
				},
				{
					text: "Refresh Queue",
					action: (e, dt, node, config) => {
						this.refreshPackageTable();
					},
					enableControl: {
						count: 0
					},
					enabled: true
				}
			]
		});
		
		this.packageTableApi = this.$("#dt-packages-table").idtApi("getTableApi");
		
		let tableApi = this.packageTableApi;
		tableApi.on("select", (e, dt, type, indexes) => {
			/** 
			 * This may be a little confusing so I'll describe what's going on.
			 * 1. we set up the table to accept the new rows
			 * 2. we pre-compute the details rows (and we actually cache them so loading
			 * 	this package a second time should be even faster) and store them in outputRows
			 * 3. we do a "long-running loop" timeout loop as described in
			 * 		https://dcb-confluence.cit.nih.gov/display/DEV/Javascript+Standards+and+Best+Practices
			 * 		HOWEVER, we batch those in groups of 100 to overcome the time-between-runs delay.
			 * 		100 seems to be an okay spot. It's not very noticable to the user and 2k rows takes
			 * 		about 8 seconds total with batches of 100. At doing each one individually, it took
			 * 		at least 16 seconds (I stopped timing).  This time can't be improved from what I can
			 * 		see because of the time required to add the row data and render it in datatables.
			 * 		I'd love to see some research (or do some) on speeding up the row.add process but
			 * 		so far, this looks to be the best.
			 * 
			 * I tried slice() and rows.add() as well and that was slightly slower
			 */
			let detailsTableApi = this.detailsTableApi;
			this.hideCancelDownload();
			detailsTableApi.rows().deselect().remove();
			// user can only select 1 row at a time, so indexes will only ever contain 1
			let packageRowData = dt.rows(indexes[0]).data()[0];
			let pkgData = this.pageData.filter(pkg => pkg.id == packageRowData.id);
			if (pkgData.length > 0) {
				pkgData = pkgData[0];
				let outputRows = [];
				let downloadables = pkgData.downloadables;
				for (let i = 0, len = downloadables.length; i < len; i++) {
					let dl = downloadables[i];
					outputRows.push(this.createSingleDetailsRow(dl, pkgData.id));
				}
				
				let j = 0;
				let endingNumber = outputRows.length;
				setTimeout(function loopFunction() {
				    try {
				    	for (let k = 0; k < 100 && j < endingNumber; k++) {
				    		detailsTableApi.row.add(outputRows[j]);
				    		j++;
				    	}
				    }
				    catch(e) {
				        // handle any exception
				    }
				    
				    if (j < endingNumber) {
				        setTimeout(loopFunction, 0); // timeout loop
				    }
				    else {
				    	detailsTableApi.rows().select();
						detailsTableApi.draw(true);
				    }
				}, 0);
			}
			// else didn't find the package. Use the empty table to display that.  weird
		})
		.on("deselect", (e, dt, type, indexes) => {
			let detailsTableApi = this.detailsTableApi;
			detailsTableApi.rows().deselect().remove();
			this.setPackageRowToNoneSelected(dt.row(indexes[0]), this);
			dt.draw(false);
			detailsTableApi.draw();
		});
	},
	
	renderDetailsTable : function() {
		this.$("#dt-details-table").idtTable({
			data: [],
			serverSide: false,
			idtData : {
				primaryKey: "DT_RowId"
			},
			pageLength: 15,
			select: "multi",
			columns : [{
				title: "Name",
				data: "name",
				parameter: "name",
				name: "name"
			},
			{
				title: "Study",
				data: "study",
				parameter: "study",
				name: "study",
				render: IdtActions.ellipsis(100)
			},
			{
				title: "File Type",
				data: "fileType",
				parameter: "fileType",
				name: "fileType"
			},
			{
				title: "Size",
				data: "size",
				parameter: "size",
				name: "size"
			},
			{
				title: "Status",
				data: "status",
				parameter: "status",
				name: "status"
			}],
			filters: [{
				type: "select",
				name: "File Types",
				options: ["Data File","Clinical Assessment","Omics","Imaging","Account File","Preclinical","Unknown"],
				columnIndex: 3
			}]
		});
		
		this.detailsTableApi = this.$("#dt-details-table").idtApi("getTableApi");
		
		let tableApi = this.detailsTableApi;
		tableApi.on("select", (e, dt, type, indexes) => {
			this.onDetailsSelectionUpdate();
		})
		.on("deselect", (e, dt, type, indexes) => {
			this.onDetailsSelectionUpdate();
		});
	},
	
	refreshPackageTable : function() {
		let api = this.packageTableApi;
		api.rows().deselect();
		api.clear();
		this.getPackageList();
	},
	
	removeAllSelectedFromDetailsTable : function() {
		this.detailsTableApi.rows({selected: true}).remove();
	},
	
	removePackage : function(selectedPackageId) {
		SecureAjax(Identity.jwt, {
			url: this.config.apiBaseUrl + "/downloadtool/queue/package/" + selectedPackageId,
			type: "DELETE",
			cache: false,
			success: (data) => {
				// no data, just a confirmation that they were deleted
				this.refreshPackageTable();
				this.removeAllSelectedFromDetailsTable();
				
			},
			error: (jqXHR, textStatus, errorThrown) => {
				switch(jqXHR.status) {
					case 400:
						$.ibisMessaging("dialog", "warning", "No packages were selected to remove");
						break;
					case 401:
						$.ibisMessaging("dialog", "error", "You are not authorized to remove those packages");
				}
			}
		});
	},
	
	cancelDownload : function() {
		this.cancelRequested = true;
		$.ibisMessaging("dialog", "info", "Your download will be cancelled. In-progress files will complete their download.");
		this.$("iframe.downloadIFrame").remove();
		this.hideCancelDownload();
	},
	
	showCancelDownload : function() {
		this.$("#dt-cancelDownload").show();
	},
	
	hideCancelDownload : function() {
		this.$("#dt-cancelDownload").hide();
	},
	
	doDownload : function() {
		let view = this;
		if (!this.downloadEnabled) {
			return;
		}
		
		if (this.$("iframe.downloadIFrame").length > 0) {
			$.ibisMessaging("dialog", "warning", "You may have a download still in progress. If you continue, that download will be halted. Do you want to start a new download?", {
				buttons: [
					{
						text: "Start New Download",
						click : function() {
							$(this).dialog("close");
							view.$("iframe.downloadIFrame").remove();
							setTimeout(view.doActualDownload(), 0);
						}
					},
					{
						text: "Cancel New Download",
						click: function() {
							$(this).dialog("close");
						}
					}
				]
			});
		}
		else {
			view.doActualDownload();
		}
	},
	
	/**
	 * Performs the actual download.  This is a loop with as much graceful error handling as I can add
	 */
	doActualDownload : function() {
		if (!this.downloadEnabled) {
			return;
		}
		else {
			this.disableDownloadButton();
		}
		
		$.ibisMessaging("dialog", "info", "Your download has started. If there are errors, they will appear in the \"Package Contents\" table. When your download is finished, you may remove the package by clicking \"Remove Package(s)\"");
		this.showCancelDownload();
		this.cancelRequested = false;
		// get list of file IDs
		let selected = this.$("#dt-details-table").idtApi("getSelectedOptions");
		let selectedLen = selected.length;
		let view = this;
		// create the jwt cookie.  We can't send headers for file downloads (except in ajax but we don't want that)
		document.cookie = "downloadAuth=" + Identity.jwt + ";path=/";
		
		let j = 0;
		let downloadLoopFunction = () => {
		    try {
				let rowId = selected[j];
				// pre-download: we do a preflight HEAD request to see if the file's downloadable or not
				// TODO: this origin should change to this.config.apiBaseUrl once we move this back to the microservices server
				let url = this.ORIGIN + this.DOWNLOAD_URL_TAIL + rowId;
				SecureAjax(Identity.jwt, {
					url: url + "?preflight=true",
					type: "HEAD"				
				}).done((data, textStatus, jqXHR) => {
					if (!this.cancelRequested) {
						let download = () => {
							$('<iframe src="' + url + '" rowId="'+ rowId +'" name="'+ rowId +'" class="downloadIFrame" style="display:none" />')
							.appendTo(view.$el);
						}
						
						if (Identity.isAlmostExpired()) {
							console.log("session almost expired. Renew");
							Identity.renew({
								success : () => {
									document.cookie = "downloadAuth=" + Identity.jwt + ";path=/";
									download();
								},
								error : () => {
									view.setStatusMessage(rowId, false, "ERROR - INTERNAL ERROR");
								}
							});
						}
						else {
							download();
						}
					}
				}).error(function(jqXHR, textStatus, errorThrown) {
					switch(jqXHR.status) {
					case 401:
						view.setStatusMessage(rowId, false, "ERROR - NEEDS RETRY");
						break;
					case 403:
						view.setStatusMessage(rowId, false, "UNAUTHORIZED");
						break;
					case 404:
						view.setStatusMessage(rowId, false, "ERROR - NOT FOUND");
						break;
					case 416:
					case 500:
					default:
						view.setStatusMessage(rowId, false, "ERROR");
					}
				});
		    }
		    catch(e) {
		        // handle any exception
		    }
		     
		    j++;
		    if (j < selectedLen && !this.cancelRequested) {
		        setTimeout(downloadLoopFunction, 1000); // timeout loop
		    }
		    else {
		        // any finalizing code
		    }
		};
		
		setTimeout(downloadLoopFunction, 0);
	},
	
	disableDownloadButton : function() {
		$("#dt-downloadButton").parent().addClass("disabled");
		this.downloadEnabled = false;
	},
	
	enableDownloadButton : function() {
		$("#dt-downloadButton").parent().removeClass("disabled");
		this.downloadEnabled = true;
	},
	
	setStatusMessage : function(rowId, success, message) {
		let detailsTableApi = this.detailsTableApi;
		let rowIndex = detailsTableApi.row("#" + rowId).index();
		detailsTableApi.cell({row: rowIndex, column: 5}).data(message).draw(false);
	},
	
	/**
	 * Responds to a selection on the details table.  This could be from adding the row (automatically 
	 * selected) or from the user manually selecting a row. Fires on each row's selection
	 */
	onDetailsSelectionUpdate: function() {
		let packageTableApi = this.packageTableApi;
		let detailsTableApi = this.detailsTableApi;
		
		// reset all packages to have 0/x before adding back in selections
		let view = this;
		packageTableApi.rows().every(function(rowIdx, tableLoop, rowLoop) {
			view.setPackageRowToNoneSelected(this, view);
		});
		
		let selectedRowsPackages = detailsTableApi.rows({selected: true}).data().map(dat => dat.packageId);
		
		// "rotates" the data in selectedRowsPackages to [packageId] = count
		let counts = {};
		for (let i = 0, selPkgLen = selectedRowsPackages.length; i < selPkgLen; i++) {
			let pkg = selectedRowsPackages[i];
			if (counts.hasOwnProperty(pkg)) {
				counts[pkg] = counts[pkg] + 1;
			}
			else {
				counts[pkg] = 1;
			}
		}
		
		for (const pkgId in counts) {
			let packageRow = packageTableApi.row("#" + pkgId);
			this.getPackageRowFilesMax(packageRow);
			let currentData = packageRow.data();
			currentData.files = counts[pkgId] + "/" + this.getPackageRowFilesMax(packageRow);
			packageRow.invalidate();
		}
		packageTableApi.draw(false);
		this.reDrawTotalDownloadSize();
		this.enableDownloadButton();
	},
	
	getPackageRowFilesMax : function(row) {
		let data = row.data();
		let currentFilesSplit = data.files.split("/");
		return currentFilesSplit[1];
	},
	
	setPackageRowToNoneSelected : function(row, view) {
		row.data().files = "0/" + view.getPackageRowFilesMax(row);
		row.invalidate();
	},
	
	onLoadPackageList: function(data) {
		this.pageData = data;
		
		// load data into the table
		let tableApi = this.$("#dt-packages-table").idtApi("getTableApi");
		tableApi.clear();
		for (let i = 0, length = data.length; i < length; i++) {
			let singlePackage = data[i];
			tableApi.row.add(this.createSinglePackageRow(singlePackage));
			this.cachePackageDetails(singlePackage);
		}
		tableApi.order([2, "desc"]).draw(false);
	},
	
	reDrawTotalDownloadSize : function() {
		// this gets a list of IDs
		// this.formatBytes(singleDetailsRow.userFile.size, 2)
		let $table = this.$("#dt-details-table");
		let tableApi = $table.idtApi("getTableApi");
		let selected = $table.idtApi("getSelectedOptions");
		let totalSize = 0;
		for (let i = 0, len = selected.length; i < len; i++) {
			let row = tableApi.row("#" + selected[i]);
			totalSize += row.data().byteSize;
		}
		this.$("#dt-totalDownloadSize").text(this.formatBytes(totalSize, 2));
	},
	
	createSinglePackageRow: function(singlePackage) {
		let outputPackage = {
			DT_RowId: String(singlePackage.id),
			id: singlePackage.id,
			name: singlePackage.name,
			dateAdded: "",
			files: "",
			size: 0,
			progress: "0%",
			status: "READY",
			origin: ""
		};
		
		let dateAdded = new Date(singlePackage.dateAdded);
		let month = (dateAdded.getMonth() + 1);
		month = (month > 9) ? month : "0" + month;
		let date = dateAdded.getDate();
		let day = (date > 9) ? date : "0" + date;
		outputPackage.dateAdded = dateAdded.getFullYear() + "-" + month + "-" + day; 
		
		outputPackage.files = "0/" + singlePackage.downloadables.length;
		
		let fileSize = 0;
		for (let i = 0, dlSize = singlePackage.downloadables.length; i < dlSize; i++) {
			fileSize += singlePackage.downloadables[i].userFile.size;
		}
		outputPackage.size = this.formatBytes(fileSize, 2);
		
		switch(singlePackage.origin) {
		case "QUERY_TOOL":
			outputPackage.origin = "Query Tool";
			break;
		case "DATASET":
			outputPackage.origin = "Data Repository";
			break;
		case "ACCOUNT":
			outputPackage.origin = "Account";
			break;
		}
		return outputPackage;
	},
	
	/**
	 * Loops through every downloadable in the given package and creates the "row" version
	 * of that downloadable for use later in rendering the details row.
	 */
	cachePackageDetails : function(singlePackage) {
		let downloadables = singlePackage.downloadables;
		let detailsSize = downloadables.length;
		let j = 0;
		setTimeout(function cacheDetailsLoop() {
		    try {
		        this.createSingleDetailsRow(downloadables[j], singlePackage.id);
		    }
		    catch(e) {}
		     
		    j++;
		    if (j < detailsSize) {
		        setTimeout(cacheDetailsLoop, 0); // timeout loop
		    }
		}, 0);
	},
	
	createSingleDetailsRow : function(singleDetailsRow, packageId) {
		let rowId = String(singleDetailsRow.userFile.id);
		let cachedRow = this.detailsRowCache[rowId];
		if (cachedRow) {
			return cachedRow;
		} 
		
		let output = {
			DT_RowId: String(singleDetailsRow.userFile.id),
			id: singleDetailsRow.userFile.id,
			packageId: packageId,
			name: singleDetailsRow.userFile.name,
			study: singleDetailsRow.userFile.study,
			fileType: "",
			size: this.formatBytes(singleDetailsRow.userFile.size, 2),
			byteSize: singleDetailsRow.userFile.size,
			status: "READY"
		}
		
		switch(singleDetailsRow.type) {
		case("DATA_FILE"):
		case("DATA_CART_FILE"):
			output.fileType = "Data File";
			break;
		case("CLINICAL"):
			output.fileType = "Clinical Assessment";
			break;
		case("GENOMICS"):
			output.fileType = "Omics";
			break;
		case("IMAGING"):
			output.fileType = "Imaging";
			break;
		case("ACCOUNT_FILE"):
			output.fileType = "Account File";
			break;
		case("PRECLINICAL"):
			output.fileType = "Preclinical";
			break;
		case("UNKNOWN"):
		case("none"):
			output.fileType = "Unknown";
			break;
		}
		
		this.detailsRowCache[rowId] = output;
		return output;
	},
	
	/**
	 * @see https://stackoverflow.com/questions/15900485/correct-way-to-convert-size-in-bytes-to-kb-mb-gb-in-javascript
	 */
	formatBytes: function(bytes, decimals = 2) {
	    if (bytes === 0) return '0 Bytes';

	    const k = 1024;
	    const dm = decimals < 0 ? 0 : decimals;
	    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
	    const i = Math.floor(Math.log(bytes) / Math.log(k));
	    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
	}

});