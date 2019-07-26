<script type="text/javascript">
Handlebars.registerHelper('listArr', function (context, options) {
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
<script id="intervalContactTemplate" type="text/x-handlebars-template">
	<table id="intervalContacts" style="width: 60%;">
		{{#listArr intervalContactArray}}

				<td style="width: 10px;" >
					<input type="checkbox" id="poc_{{id}}" name="poc_{{id}}" value="{{fullname}}" class="intervalContactChkBox"/>
				</td>
				<td class="intervalContactName" >{{fullname}}</td>
		{{/listArr}}
	</table>
</script>