/**
 * determine the language to use in the datatable and give the table that
 * language's values
 */
var DataTableLanguage = {
	language : function() {
		if (typeof language == "undefined" || language == "") {
			return this.oLanguage_en_US;
		}
		
		if (language.indexOf("zh") > -1) {
			return this.oLanguage_zh_TW;
		}
		else if (language.indexOf("es") > -1) {
			return this.oLanguage_es;
		}
		else {
			return this.oLanguage_en_US;
		}
	},
	
	oLanguage_en_US : {
			"oAria": {
				"sSortAscending": ": activate to sort column ascending",
				"sSortDescending": ": activate to sort column descending"
			},
			"oPaginate": {
				"sFirst": "First",
				"sLast": "Last",
				"sNext": "Next",
				"sPrevious": "Previous"
			},
			"sEmptyTable": "No data available in table",
			"sInfo": "Showing _START_ to _END_ of _TOTAL_ entries",
			"sInfoEmpty": "Showing 0 to 0 of 0 entries",
			"sInfoFiltered": "(filtered from _MAX_ total entries)",
			"sInfoPostFix": "",
			"sInfoThousands": ",",
			"sLengthMenu": "Show _MENU_ entries",
			"sLoadingRecords": "Loading...",
			"sProcessing": "Processing...",
			"sSearch": "Search:",
			"sUrl": "",
			"sZeroRecords": "No matching records found"
		},

		oLanguage_zh_TW : {
		    "sProcessing":   "處理中...",
		    "sLengthMenu":   "顯示 _MENU_ 項結果",
		    "sZeroRecords":  "沒有匹配結果",
		    "sInfo":         "顯示第 _START_ 至 _END_ 項結果，共 _TOTAL_ 項",
		    "sInfoEmpty":    "顯示第 0 至 0 項結果，共 0 項",
		    "sInfoFiltered": "(從 _MAX_ 項結果過濾)",
		    "sInfoPostFix":  "",
		    "sSearch":       "搜索:",
		    "sUrl":          "",
		    "oPaginate": {
		        "sFirst":    "首頁",
		        "sPrevious": "上頁",
		        "sNext":     "下頁",
		        "sLast":     "尾頁"
		    }
		},

		oLanguage_sp : {
		    "sProcessing":     "Procesando...",
		    "sLengthMenu":     "Mostrar _MENU_ registros",
		    "sZeroRecords":    "No se encontraron resultados",
		    "sEmptyTable":     "Ningún dato disponible en esta tabla",
		    "sInfo":           "Mostrando registros del _START_ al _END_ de un total de _TOTAL_ registros",
		    "sInfoEmpty":      "Mostrando registros del 0 al 0 de un total de 0 registros",
		    "sInfoFiltered":   "(filtrado de un total de _MAX_ registros)",
		    "sInfoPostFix":    "",
		    "sSearch":         "Buscar:",
		    "sUrl":            "",
		    "sInfoThousands":  ",",
		    "sLoadingRecords": "Cargando...",
		    "oPaginate": {
		        "sFirst":    "Primero",
		        "sLast":     "Último",
		        "sNext":     "Siguiente",
		        "sPrevious": "Anterior"
		    },
		    "fnInfoCallback": null,
		    "oAria": {
		        "sSortAscending":  ": Activar para ordernar la columna de manera ascendente",
		        "sSortDescending": ": Activar para ordernar la columna de manera descendente"
		    }
		}
};