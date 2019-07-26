<script type="text/javascript">
Handlebars.registerHelper('listProcedure', function (context, options) {
	  var out = '<table style="width: 100%; margin-bottom: 20px;">', data;

	  if (options.data) {
	    data = Handlebars.createFrame(options.data);
	  }
	  
	  var displayLen = 5;
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
	  out += "</table>";
	  return out;
	});

</script>


<script id="intervalProcedureTemplate" type="text/x-handlebars-template">
	<table id="intervalProcedure" style="width: 65%;">
		{{#each intervalProcedureArray}}
			<tr id="{{proceduretype}}_procedure" >
				<td style="width: 18%; font-weight: bold; font-size: 10px;" >{{proceduretype}}</td>
				<td style="width: 82%;" id="{{proceduretype}}">
					{{#listProcedure procedureList}}					
						<td style="width: 10px;" >
							<input type="checkbox" id="procedure_{{id}}" name="procedure_{{id}}" value="{{name}}" class="intervalProcedureChkBox"/>
						</td>
						<td class="intervalProcedureName" >{{name}}</td>					
					{{/listProcedure}}

				</td>
			</tr>
		{{/each}}
	</table>
</script>