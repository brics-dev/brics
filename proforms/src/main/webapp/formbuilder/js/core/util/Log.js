/**
 * 
 */
var Log = {
	developer : {
		consoleExists : window.console && window.console.log,
		
		error : function(object) {
			if (this.consoleExists) {
				console.error(object);
			}
		},
		
		warning : function(object) {
			if (this.consoleExists) {
				console.warning(object);
			}			
		},
		
		info : function(object) {
			if (this.consoleExists) {
				console.log(object);
			}
		},
		
		success : function(object) {
			if (this.consoleExists) {
				console.log(object);
			}
		},
		
		message : function(message) {
			if (this.consoleExists) {
				alert(message);
			}
		}
	},
	
	user : {
		error : function(message) {
			this.message(message, "error");
		},
		
		warning : function(message) {
			this.message(message, "warning");
		},
		
		info : function(message) {
			this.message(message, "info");
		},
		
		success : function(message) {
			this.message(message, "sucess");
		},
		
		message : function(message, level) {
			if ($.ibisMessaging) {
				$.ibisMessaging("dialog", level, message);
			}
			else {
				alert(level + ": " + message);
			}
		}
	}
};