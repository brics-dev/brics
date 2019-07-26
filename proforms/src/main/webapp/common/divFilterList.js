
 function StringBuffer() {
   this.buffer = [];
 }

 StringBuffer.prototype.append = function append(string) {
   this.buffer.push(string);
   return this;
 };

 StringBuffer.prototype.toString = function toString() {
   return this.buffer.join("");
 };


function divFilterlist(arrayOfObjs, tehDiv) {

  this.items = arrayOfObjs;
  this.target = tehDiv;


  // Flags for regexp matching.
  // "i" = ignore case; "" = do not ignore case
  // You can use the set_ignore_case() method to set this
  this.flags = 'i';

  //==================================================
  // METHODS
  //==================================================

  //--------------------------------------------------
  this.init = function() {
    // This method initilizes the object.
    // This method is called automatically when you create the object.
    // You should call this again if you alter the selectobj parameter.

    if (!this.target) return this.debug('target not defined');
    if (!this.items) return this.debug('items not defined');
    this.set('');

  }

  //--------------------------------------------------
  this.reset = function() {
    // This method resets the select list to the original state.
    // It also unselects all of the options.

    this.set('');
  }


  //--------------------------------------------------
  this.set = function(pattern) {
    // This method removes all of the options from the select list,
    // then adds only the options that match the pattern regexp.
    // It also unselects all of the options.

    //if (!this.target) return this.debug('target not defined');
    //if (!this.items) return this.debug('items not defined');

    // Clear the select list so nothing is displayed
    //this.target.innerHtml = '';

    // Set up the regular expression.
    // If there is an error in the regexp,
    // then return without selecting any items.
    try {
      regexp = new RegExp(pattern, this.flags);

    } catch(e) {
         return;
    }
    var newStr = new StringBuffer().append("<table cellspacing=1 cellpadding=0 border=0 width='100%'>");
    for (loop=0, len=this.items.length; loop < len; loop++) {
        if (loop&1){
        bgcolor="#FFFFFF";
        } else {
            bgcolor ="#EBEBEB";
        }

      var option = this.items[loop];
      if (regexp.test(option.getFilterOperator())) {
        newStr.append("<tr style='background-color:"+bgcolor+";' onMouseover="+'"'+"this.style.backgroundColor='#CCCC99'"+'"'+" onMouseout="+'"'+"this.style.backgroundColor='"+bgcolor+"'"+'"'+">");
        newStr.append(option.getLink());
      }
    }
    newStr.append("</table>");
    this.target.innerHTML = newStr.toString();

  }


  //--------------------------------------------------
  this.set_ignore_case = function(value) {
    // This method sets the regexp flags.
    // If value is true, sets the flags to "i".
    // If value is false, sets the flags to "".

    if (value) {
      this.flags = 'i';
    } else {
      this.flags = '';
    }
  }


  //--------------------------------------------------
  this.debug = function(msg) {
    if (this.show_debug) {
      alert('FilterList: ' + msg);
    }
  }


  //==================================================
  // Initialize the object
  //==================================================
  this.init();

}
