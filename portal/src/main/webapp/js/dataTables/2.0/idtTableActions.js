if (!Array.prototype.fill) {
     Array.prototype.fill = function(value) {

    // Steps 1-2.
    if (this == null) {
      throw new TypeError('this is null or not defined');
    }

    var O = Object(this);

    // Steps 3-5.
    var len = O.length >>> 0;

    // Steps 6-7.
    var start = arguments[1];
    var relativeStart = start >> 0;

    // Step 8.
    var k = relativeStart < 0 ?
      Math.max(len + relativeStart, 0) :
      Math.min(relativeStart, len);

    // Steps 9-10.
    var end = arguments[2];
    var relativeEnd = end === undefined ?
      len : end >> 0;

    // Step 11.
    var final = relativeEnd < 0 ?
      Math.max(len + relativeEnd, 0) :
      Math.min(relativeEnd, len);

    // Step 12.
    while (k < final) {
      O[k] = value;
      k++;
    }

    // Step 13.
   return O;
   };
}
 var IdtActions = {

     ellipsis: function(showChars) {
         return function(data, type, row, full) {
             var moreText = "more";
             var lessText = "less";
             var ellipsestext = '...';
             var oTableId = full.settings.nTable.id;
             // $(full.settings.aanFeatures.t[0]).off("click", '.morelink');
             $('#' + oTableId).on("click", '.morelink', function(e) {
                 e.stopImmediatePropagation();
                 if ($(this).hasClass("less")) {
                     $(this).removeClass("less");
                     $(this).html(moreText);
                    
                     
                 } else {
                     $(this).addClass("less");
                     $(this).html(lessText);
                     //$(".morecontent span").css("display", "inline-block");
                 }
                 $(this).parent().prev().toggle();
                 $(this).prev().toggle();
                 console.log($(this).parent().prev());
                 return false;
             });

             if (type !== 'display') {
                 return data;
             }

             if (typeof data !== 'number' && typeof data !== 'string') {
                 return data;
             }

             data = data.toString(); // cast numbers

             if (data.length <= showChars) {
                 return data;
             }

             var shortened = data.substr(0, showChars);
             var h = data.substr(showChars, data.length - showChars);

             return '<div class="showMoreContainer">' + shortened + '<span class="moreellipses">' + ellipsestext + '&nbsp;</span><span class="morecontent"><span>' + h + '</span>&nbsp;&nbsp;<a href="" class="morelink">' + moreText + '</a></span></div>'
         };
     },
     // method to make columns as a clickable links
     render_urlfn: function(path) {
         return function(data, type, row, full) {
             var parameter = full.settings.oInit.columns[full.col].param;
             return '<a href=' + path + '/"' + parameter + '">' + data + '</a>';
         }
     },

     formatDate: function() {
         var that = this;
         return function(data) {
        	var formatDate = '';
        	if(data !== "") {
			    var date = data.replace(/(\d{4})-(\d{1,2})-(\d{1,2})/, function(match,y,m,d) { 
			        	return y + '/' + m + '/' + d;  
			    	}).match(/[^\.]*/);        	
	             var newDate = new Date(date);
	             var sMonth = that.getVal(newDate.getMonth() + 1);
	             var sDay = that.getVal(newDate.getDate());
	             var sYear = newDate.getFullYear();
	             var sHour = that.getVal(newDate.getHours());
	             var sMinute = that.getVal(newDate.getMinutes());
	             var sSecond = that.getVal(newDate.getSeconds());
	
	             formatDate = sYear + "-" + sMonth + "-" + sDay + " " + sHour + ":" + sMinute;
	         } else {
	        	 formatDate = data;
	         }
        	return formatDate;
         }      	
     },

     getVal: function(value) {
         return (value < 10) ? "0" + value : value;
     },
     
     // Function to convert an img URL to data URL
     getBase64Image: function(img) {
	      var canvas = document.createElement("canvas");
	      canvas.width = img.width;
	      canvas.height = img.height;
	      var ctx = canvas.getContext("2d");
	      ctx.drawImage(img, 0, 0);
	      return canvas.toDataURL("image/png");

     },
     
     pdfCustomizer: function(columnsWidth) {
    	 var that = this;
    	 return function(doc) {
             // Get the image id, style and the url from it
             var img = document.getElementById('logo'),
               style = img.currentStyle || window.getComputedStyle(img, false),
               bi = style.backgroundImage.slice(4, -1).replace(/"/g, ""),
               title = doc.content[0].text;
             //Remove the title created by datatTables
             doc.content.splice(0,1);
             //delete doc.content[0].layout;
             //Create a date string that we use in the footer. Format is yyyy-mm-dd
             var now = new Date();
             var sMonth = that.getVal(now.getMonth() + 1);
             var sDay = that.getVal(now.getDate());
             var sYear = now.getFullYear();
             var sHour = that.getVal(now.getHours());
             var jsDate = sYear+ '-' + sMonth + '-' + sDay;
             //ensure doc.images exists
             doc.images = doc.images || {};
             var myGlyph = new Image();
             myGlyph.src = bi;
             //doc.pageSize = "LEGAL";
             //doc.pageMargins = [20,60,20,30];
             doc.pageMargins = [50,100,50,30];
             // Set the font size fot the entire document
             doc.defaultStyle.fontSize = 8;
             //doc.defaultStyle.lineWidth = 5;
             // Set the fontsize for the table header
             //doc.styles.tableHeader.fontSize = 7;

             // Create a header object with 3 columns
             // Left side: Logo
             // Middle: brandname
             // Right side: A document title
             doc['header']=(function() {
               return {
                     columns: [{
                         table: {
                           widths: ['50%', '50%'],
                           body: [
                             [{
                               image: IdtActions.getBase64Image(myGlyph),
                               //width:300,
                              // fit: [300, 300],
                               border: [true, true, false, true],
                               //height: 26,
                             }, {
                               text: title,
                          	   fontSize: 14,
                               alignment: 'right',
                               bold: true,
                               margin:[0,10,0],
                               border: [false, true, true, true],
                             }]
                           ]
                         }
                     }],
                     margin: 20
               }
             });
             
             doc.styles.tableHeader = {
            		 alignment: "left",
            		 bold: true,
            		 color: "white",
            		 fillColor: "#4a545b",
            		 fontSize: 10
             };
             
             $.extend(doc.styles.tableBodyOdd, {'white-space':'nowrap'});
             
             // Create a footer object with 2 columns
             // Left side: report creation date
             // Right side: current page and total pages
             doc['footer']=(function(page, pages) {
               return {
                 columns: [
                   {
                     alignment: 'left',
                     text: ['Created on: ', { text: jsDate.toString() }]
                   },
                   {
                     alignment: 'right',
                     text: ['page ', { text: page.toString() },  ' of ', { text: pages.toString() }]
                   }
                 ],
                 margin: 20
               }
             });
             //doc.content[1].table.widths = [ '20%', '30%', '30%', '10%', '10%'];
             var columnsLength = doc.content[0].table.body[0].length;
             var width = 100;
             var widths = new Array(columnsLength);
             widths.fill((100 / columnsLength) + "%");
             if(columnsWidth) {
                 doc.content[0].table.widths = columnsWidth;
             }else {
                 doc.content[0].table.widths = widths; 
             }
             
             //doc.content[0].table.alignment = 'right';
             var objLayout = {};
             //objLayout['hLineWidth'] = function(i) { return .5; };
             //objLayout['vLineWidth'] = function(i) { return .5; };
             objLayout['hLineWidth']= function(i, node) {
                 return (i === 0 || i === node.table.body.length) ? .5 : .5;
               };
              objLayout['vLineWidth']= function(i, node) {
                 return (i === 0 || i === node.table.widths.length) ? .5 : .5;
               };
             objLayout['hLineColor'] = function(i) { return '#aaa'; };
             objLayout['vLineColor'] = function(i) { return '#aaa'; };
             objLayout['paddingLeft'] = function(i) { return 4; };
             objLayout['paddingRight'] = function(i) { return 4; };
             doc.content[0].layout = objLayout;
             //doc.content[1].layout = objLayout;
           }
     },

     checkExportType: function(self, e, dt, button, config) {
         if (button[0].className.indexOf('buttons-excel') >= 0) {
             if ($.fn.dataTable.ext.buttons.excelHtml5.available(dt, config)) {
                 $.fn.dataTable.ext.buttons.excelHtml5.action.call(self, e, dt, button, config);
             } else {
                 $.fn.dataTable.ext.buttons.excelFlash.action.call(self, e, dt, button, config);
             }
         } else if (button[0].className.indexOf('buttons-print') >= 0) {
             $.fn.dataTable.ext.buttons.print.action(e, dt, button, config);
         } else if (button[0].className.indexOf('buttons-csv') >= 0) {
             if ($.fn.dataTable.ext.buttons.csvHtml5.available(dt, config)) {
                 $.fn.dataTable.ext.buttons.csvHtml5.action.call(self, e, dt, button, config);
             } else {
                 $.fn.dataTable.ext.buttons.csvFlash.action.call(self, e, dt, button, config);
             }
         } else if (button[0].className.indexOf('buttons-pdf') >= 0) {
             if ($.fn.dataTable.ext.buttons.pdfHtml5.available(dt, config)) {
                 $.fn.dataTable.ext.buttons.pdfHtml5.action.call(self, e, dt, button, config);
             } else {
                 $.fn.dataTable.ext.buttons.pdfFlash.action.call(self, e, dt, button, config);
             }
         }
     },

     exportAction: function() {
         var that = this;
         return function(e, dt, button, config) {
             e.preventDefault();
             var self = this,
             columnsList = [],
             dtCollection,
             columns = dt.columns(':visible').nodes(),
             dtButtons = dt.settings()[0].oInit.buttons;
             for(var i = 0; i < dtButtons.length; i++) {
                if(dtButtons[i].extend == 'collection') {
                	dtCollection = dtButtons[i];
                }
             }
             $.extend(config, {title: dtCollection.title});
             for(var i =0; i < columns.length; i++) {
                columnsList.push(i);
             };
             
             if(jQuery.isEmptyObject(config.exportOptions)) {
                 
            	 Object.assign(config.exportOptions, { orthogonal: 'export', columns: columnsList });

             }

             var selected = dt.settings()[0].oInit.select;
             
             if (selected.style == 'multi' || selected.style == 'single') {
            	 columnsList.shift();
                 var sConfig = jQuery.extend(true, {exportOptions: { modifier: { selected: true }, orthogonal: 'export', columns: columnsList } }, config);
            	 if (dt.rows({ selected: true }).indexes().length !== 0) {
	                 $("#dialog").dialog({
	                     modal: true,
	                     title: 'Export Table',
	                     autoOpen: true,
	                     width: '400px',
	                     resizable: false,
	                     dialogClass: 'dialog-wrapper',
	                     open: function() {
	                         var markup = 'Are you sure you want to export the selected Row(s)?';
	                         $(this).html(markup);
	                     },
	                     buttons: {
	                         'ExportSelected': {
		                    	 text: 'Export',
		                    	 class: 'exportSelected-button dialog-btn-row',
	                        	 click: function() {
		                             // $(obj).removeAttr('onclick');
		                             // $(obj).parents('.Parent').remove();
		
		                             that.checkExportType(self, e, dt, button, sConfig);
		
		                             $(this).dialog("close");
		                         },
	                         },
	                         'Cancel': {
	                             text: 'Cancel',
	                             class: 'Cancel-button dialog-btn-cancel',
	                             click: function() {
	                                 $(this).dialog("close");
	                             }
	                         }
	                     }
	                 });
            	 }
            	 else if (dt.rows({ selected: true }).indexes().length == 0 || undefined) {

            	 	$.when(that.runExportAction(self, e, dt, button, config)).done(function(e) {
            	 		
            	 		setTimeout(function(){dt.buttons(['.buttons-excel', '.buttons-csv', '.buttons-pdf', '.buttons-print']).enable()}, 5000);
            	 	})
                  }
             } 
             else {
         	 	$.when(that.runExportAction(self, e, dt, button, config)).done(function(e) {
        	 		
        	 		setTimeout(function(){dt.buttons(['.buttons-excel', '.buttons-csv', '.buttons-pdf', '.buttons-print']).enable()}, 5000);
        	 	})
             }
         }
     },

     runExportAction: function(self, e, dt, button, config) {
    	 var that = this;
    	 dt.buttons(['.buttons-excel', '.buttons-csv', '.buttons-pdf', '.buttons-print']).disable();
         if (dt.settings()[0].oInstance.fnSettings().oFeatures.bServerSide == true) {
             var oldStart = dt.settings()[0]._iDisplayStart;
             var recordsTotal = dt.settings()[0]._iRecordsTotal;
             // dt.settings()[0]._iRecordsTotal
             // console.log('teable selected ',
				// table.idtTable('getOptions'));
             
             dt.one('preXhr', function(e, s, data) {
                 // Just this once, load all data from the server...
            	 //console.log('test test test',data.length);
            	 var oldLength = data.length;
                 s.clearCache = true;
                 data.start = 0;
                 // data.sLength = recordsTotal;
                 data.length = recordsTotal;
                 data.export = true;
                 dt.one('preDraw', function(e, settings) {
                     // Call the original action function
                     that.checkExportType(self, e, dt, button, config);

                     dt.one('preXhr', function(e, s, data) {
                         // Set the property to what it was before exporting.
                         settings._iDisplayStart = oldStart;
                         data.start = oldStart;
                         data.length = oldLength;


                     });

                     setTimeout(dt.ajax.reload, 0);

                     // Prevent rendering of the full data to the DOM
                     return false;

                 })
             });

             // Requery the server with the new one-time export settings
             dt.ajax.reload();
         } else {
             this.checkExportType(self, e, dt, button, config);

         }


     },

     ajaxRequest: function(url, selectedRows, runDeleteRows) {
         $.ajax({
             url: url + selectedRows,
             method: 'POST',
             data: {isMetaStudyEdit: true},
             dataType: 'json',
             success: function(data, status, xhr) {
            	 console.log(runDeleteRows);
                 runDeleteRows;
                 selectedRows.length = 0;
                 console.log(selectedRows);
             },
             error: function(jqXHR, textStatus, errorThrown) {
                 console.log(errorThrown);
             }
         });
     },

     runDeleteAction: function(options) {
         var that = this;
         return function(e, dt, node, config) {
             var dt = dt;
             var rows = dt.rows().data().to$();
             var rowsIDs = [];
             for (var i = 0; i < rows.length; i++) {
                 rowsIDs.push(rows[i].DT_RowId)
             }
             console.log(rowsIDs);
             $("#dialog").dialog({
                 modal: true,
                 title: 'Confirm Deletion',
                 type: 'warning',
                 zIndex: 10000,
                 autoOpen: true,
                 width: '400px',
                 resizable: false,
                 dialogClass: 'dialog-wrapper',
                 open: function() {
                     var markup = 'Are you sure you want to delete the item(s)?';
                     $(this).html(markup);
                 },
                 buttons: {
                	 'deleteRow':{
                		 text: 'Delete',
                		 class: 'deleteRow dialog-btn-row',
                		 click: function() {
	                         // $(obj).removeAttr('onclick');
	                         // $(obj).parents('.Parent').remove();
                             if (options.select == 'multi') {
                                 that.runAjax(config.url, selectedRows, dt.rows('.selected').remove().draw(false));

                             } else if (options.select == 'single') {
                                 that.runAjax(config.url, selectedRows, dt.row('.selected').remove().draw(false));
                             }
	                     
	                         $(this).dialog("close");
	                     }
                	 },
                     'deleteAll': {
	                	 text: 'DeleteAll',
	                	 class: 'deleteAll dialog-btn-all',
                    	 click: function() {
	                         that.ajaxRequest(config.url, rowsIDs, dt.rows().remove().draw(false));
	                         $(this).dialog("close");
	                     }
                     }
                 }
             });
         }
     },
     // add row methods
     checkAddRowValues: function(values, aoColumns) {
         if (Array.isArray(values)) {
             for (var j = 0; j < values.length; j++) {
                 if (typeof(values[j]) !== 'object') {
                     if (values[j] == true) {
                         var aocolumn = (aoColumns[j]).replace(/ /g, "_");
                         $('label[for=' + aoColumns[j].replace(/\s+/g, "_") + '], input#' + aoColumns[j].replace(/\s+/g, "_")).show();
                     } else {
                         alert('please check the right config for labels');
                     }
                 } else {

                     if (Object.keys(values[j]) == 'false') {
                         $('label[for=' + aoColumns[j].replace(/\s+/g, "_") + '], input#' + aoColumns[j].replace(/\s+/g, "_")).hide();
                         $('label[for=' + aoColumns[j].replace(/\s+/g, "_") + '], input#' + aoColumns[j].replace(/\s+/g, "_")).val(values[j].false);
                     } else {
                         alert('please check the right config for labels');
                     }
                 }
             }
         } else {
             if (values == true) {
                 for (var i = 0; i < aoColumns.length; i++) {
                     $('label[for=' + aoColumns[i].replace(/\s+/g, "_") + '], input#' + aoColumns[i].replace(/\s+/g, "_")).show();

                 }
             } else {
                 alert('pealse check the right config for labels');
             }
         }
     },

     addRowAction: function() {
         var that = this;
         return function(e, dt, node, config) {
             var dt = dt;
             var aoColumns = (dt.settings()[0].aoColumns).map(function(aocolumn) {
                 return aocolumn.name;
             });
             $("#dialog").dialog({
                 modal: true,
                 title: 'Add Row',
                 zIndex: 10000,
                 autoOpen: true,
                 width: 'auto',
                 resizable: false,
                 open: function() {
                     var form = '<form>';
                     for (var i = 0; i < aoColumns.length; i++) {
                         form += '<label for=' + aoColumns[i].replace(/\s+/g, "_") + '>' + aoColumns[i] + '</label><input type=text id=' + aoColumns[i].replace(/\s+/g, "_") + ' name=' + aoColumns[i].replace(/\s+/g, "_") + ' /></br>';

                     }

                     form += '</label>'
                     $(this).html(form);
                     // $('form').find('label[for=name], input#name').hide();
                     that.checkAddRowValues(config.values, aoColumns);

                 },
                 buttons: {
                     'cancel': {
	                	 text: 'Cancel',
	                	 class: 'dialog-btn-cancel',
                    	 click: function() {
	                         $(this).dialog("close");
	                     }
                     },
                	 'addRow':{
	                	 text: 'Submit',
	                	 class: 'dialog-btn-addRow',
                    	 click: function() {
	                         // $(obj).removeAttr('onclick');
	                         // $(obj).parents('.Parent').remove();
	                         var obj = aoColumns.reduce(function(acc, cur, i) {
	                             acc[cur] = $('input[name=' + cur.replace(/\s+/g, "_") + ']').val();
	                             return acc;
	                         }, {});
	                         $.extend(obj, { DT_RowId: '' });
	
	                         if (config.url !== undefined) {
	                             $.ajax({
	                                 url: config.url,
	                                 method: 'POST',
	                                 data: obj,
	                                 dataType: 'json',
	                                 success: function(data, status, xhr) {
	                                     dt.row.add(data).draw(false);
	                                 },
	                                 error: function(jqXHR, textStatus, errorThrown) {
	                                     console.log(errorThrown);
	                                 }
	                             });
	                         } else {
	                             dt.row.add(obj).draw(false);
	                         }
	
	                         $(this).dialog("close");
	                     }
                     }
                 }
             });
         }
     }
 }
