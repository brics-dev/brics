<script type="text/javascript">
Handlebars.registerHelper('listClinicalLocation', function (context, options) {
	  var out = '', data;

	  if (options.data) {
	    data = Handlebars.createFrame(options.data);
	  }
	  
	  var displayLen = 6;
	  for (var i=0; i<context.length; i++) {
	    if (data) {
	      data.index = i;
	    }
	    var tdCell =  options.fn(context[i], { data: data });
		if (i % displayLen == 0) {
	    	out += "<tr>" + tdCell;
		} else if(i % displayLen == (displayLen-1)) {
			out += tdCell + "</tr>";
		} else {
			out += tdCell;
		}
	  }

	  return out;
	});

</script>
<script id="intervalClinicalTemplate" type="text/x-handlebars-template">
	<table id="intervalClinicals" style="width: 60%;">
		{{#listClinicalLocation intervalClinicalArray}}
				<td style="width: 10px;">
					<input type="checkbox" id="clinical_{{id}}" name="clinical_{{id}}" value="{{name}}" class="intervalClinicalChkBox"/>
				</td>
				<td class="intervalClinicalName">{{name}}</td>

		{{/listClinicalLocation}}
	</table>
</script>