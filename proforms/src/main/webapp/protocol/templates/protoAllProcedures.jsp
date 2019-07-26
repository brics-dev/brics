	<script type="text/javascript">
	
	Handlebars.registerHelper('listProcedure', function (context, options) {
		  var out = '<table style="width: 600px; margin-bottom: 20px;">', data;

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
		  //console.log("listProcedure out: "+out);
		  return out;
	});
	</script>
	<script id="allProcedureTemplate" type="text/x-handlebars-template">
		{{#each allProcedureArray}}

			<div class="formrow_1">
				<label for="{{proceduretype}}_procedure" style="font-size: 11px;">
					{{proceduretype}}
				</label>

				<div id="{{proceduretype}}">
					{{#listProcedure procedureList}}					
						<td style="width: 10px;" >
							<input type="checkbox" id="procedure_{{id}}" name="procedure_{{id}}" value="{{name}}" class="protoProcedureChkBox"/>
						</td>
						<td class="protoProcedureName" style="text-align:left; padding-left: 5px;" >{{name}}</td>					
					{{/listProcedure}}
				</div>
			</div>

		{{/each}}
	</script>