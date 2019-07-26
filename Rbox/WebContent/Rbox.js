$(document).ready(function(){

    var terminal = CodeMirror.fromTextArea(document.getElementById("term"), {
        mode: 'r',
        lineNumbers: true
    });
    terminal.setValue("#BEGIN NOTE#\n" +
    						"#data.str='{query tool input}' \n" +
    						"#df <- read.csv(text=data.str, header=TRUE, sep=',')\n" +
    						"#END NOTE#");
    terminal.markText({line: 0, ch: 0}, {line: 4, ch: 0}, {
    	  readOnly: true
    	});
    
    var $flash = $('#form_flash');
    
    var csvData;
    
    $('#csvFileUpload').on('change', prepUpload);
    
    function prepUpload(event){
    	var fileupload = event.target.files[0];
    	
    	 var reader = new FileReader();
         reader.readAsText(fileupload);
         reader.onload = function(event) {
            csvData = event.target.result;
         };
    }

    $('#send_r').click(function(){
        if (terminal.getValue() == ''){
            $flash.text('Please enter a command that I can run!').show();
        } else {
            $flash.hide();
            $('#res').html('<img src="loading.gif">');
            
            $.ajax({
                url: 'service/dataCart/executeRScript',
                type: "post",
                headers: { 
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                dataType: "json",
                data: terminal.getValue(),
                success: function(data) { 
                	$('#res').html(data.consoleOutput);
                	$('#graph').html("<img src='" + data.graphImage + "'>");
                	},
                error: function(data) { $('#res').html('FAILURE'); }
            });
            
        }
    });

    $('#reset_r').click(function(){
        terminal.setValue('');
        $flash.hide();
    });

});