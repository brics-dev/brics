/**
 * This function provides an extension to the defaults function from underscore
 * which will recursively call defaults on any object properties of the objects
 * that are passed in.
 */
 _.deepDefaults = function(obj) {
	_.each(Array.prototype.slice.call(arguments, 1), function(source) {
		if (source) {
			for (var prop in source) {
				if (typeof source[prop] === "object") {
					obj[prop] = _.deepDefaults(obj[prop], source[prop]);
				}
				else {
					if (obj[prop] === void 0) obj[prop] = source[prop];
				}
			}
		}
	});
	return obj;
};

_.overlaps = (function () {
	function getPositions( elem ) {
		var pos, width, height;
		pos = $( elem ).position();
		width = $( elem ).width();
		height = $( elem ).height();
		return [ [ pos.left, pos.left + width ], [ pos.top, pos.top + height ] ];
	}
	
	function comparePositions( p1, p2 ) {
		var r1, r2;
		r1 = p1[0] < p2[0] ? p1 : p2;
		r2 = p1[0] < p2[0] ? p2 : p1;
		return r1[1] > r2[0] || r1[0] === r2[0];
	}
	
	return function ( a, b ) {
		var pos1 = getPositions( a ),
		pos2 = getPositions( b );
		return comparePositions( pos1[0], pos2[0] ) && comparePositions( pos1[1], pos2[1] );
	};
})(); 

_.replaceAll = function(strOrg,strFind,strReplace){
	 var index = 0;
	 while (strOrg.indexOf(strFind,index) != -1){
	  strOrg = strOrg.replace(strFind,strReplace);
	  index++;
	}
	 return strOrg;
};

/**
 * A terrible re-work on charAt to fit with the version in
 * ValueRangeSorter.  I feel dirty doing this.
 * 
 * @param s the String to find the character at i
 * @param i the index of the character to find
 */
_.getChar = function(s, i) {
	if (i >= s.length) {
		return "0";
	}
	else {
		return s.charAt(i);
	}
};

/**
 * Fixes encodeURIComponent in the standard JS library with the notes
 * as described on https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURIComponent
 * Makes the function adhere to RFC 3986
 */
_.fixedEncodeURIComponent = function(str) {
	return encodeURIComponent(str).replace(/[!'()*]/g, function(c) {
		return '%' + c.charCodeAt(0).toString(16);
	});
};


//add "trim" functionality to String in IE8ish
if(typeof String.prototype.trim !== 'function') {
  String.prototype.trim = function() {
    return this.replace(/^\s+|\s+$/g, ''); 
  };
}

/**
 * Fix for <= IE8.  Their array object doesn't have indexOf
 */
if (!Array.prototype.indexOf) {
	Array.prototype.indexOf = function(needle) {
        for(var i = 0; i < this.length; i++) {
            if(this[i] === needle) {
                return i;
            }
        }
        return -1;
    };
}

if (typeof jQuery !== "undefined") {
	jQuery.fn.reverse = [].reverse;
}

//// NOTE: we may want to include underscore.string.js as well for a full string package
//// see https://github.com/epeli/underscore.string
//
///* alphanum.js (C) Brian Huisman
// * Based on the Alphanum Algorithm by David Koelle
// * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
// *
// * Distributed under same license as original
// * 
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2.1 of the License, or any later version.
// * 
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public
// * License along with this library; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
// */
//
///* ********************************************************************
// * Alphanum Array prototype version
// *  - Much faster than the sort() function version
// *  - Ability to specify case sensitivity at runtime is a bonus
// *
// */
//Array.prototype.alphanumSort = function(caseInsensitive) {
//  for (var z = 0, t; t = this[z]; z++) {
//    this[z] = new Array();
//    var x = 0, y = -1, n = 0, i, j;
//
//    while (i = (j = t.charAt(x++)).charCodeAt(0)) {
//      var m = (i == 46 || (i >=48 && i <= 57));
//      if (m !== n) {
//        this[z][++y] = "";
//        n = m;
//      }
//      this[z][y] += j;
//    }
//  }
//
//  this.sort(function(a, b) {
//    for (var x = 0, aa, bb; (aa = a[x]) && (bb = b[x]); x++) {
//      if (caseInsensitive) {
//        aa = aa.toLowerCase();
//        bb = bb.toLowerCase();
//      }
//      if (aa !== bb) {
//        var c = Number(aa), d = Number(bb);
//        if (c == aa && d == bb) {
//          return c - d;
//        } else return (aa > bb) ? 1 : -1;
//      }
//    }
//    return a.length - b.length;
//  });
//
//  for (var z = 0; z < this.length; z++)
//    this[z] = this[z].join("");
//}
//
//
///* ********************************************************************
// * Alphanum sort() function version - case sensitive
// *  - Slower, but easier to modify for arrays of objects which contain
// *    string properties
// *
// */
//function alphanum(a, b) {
//  function chunkify(t) {
//    var tz = new Array();
//    var x = 0, y = -1, n = 0, i, j;
//
//    while (i = (j = t.charAt(x++)).charCodeAt(0)) {
//      var m = (i == 46 || (i >=48 && i <= 57));
//      if (m !== n) {
//        tz[++y] = "";
//        n = m;
//      }
//      tz[y] += j;
//    }
//    return tz;
//  }
//
//  var aa = chunkify(a);
//  var bb = chunkify(b);
//
//  for (x = 0; aa[x] && bb[x]; x++) {
//    if (aa[x] !== bb[x]) {
//      var c = Number(aa[x]), d = Number(bb[x]);
//      if (c == aa[x] && d == bb[x]) {
//        return c - d;
//      } else return (aa[x] > bb[x]) ? 1 : -1;
//    }
//  }
//  return aa.length - bb.length;
//}
//
//
///* ********************************************************************
// * Alphanum sort() function version - case insensitive
// *  - Slower, but easier to modify for arrays of objects which contain
// *    string properties
// *
// */
//function alphanumCase(a, b) {
//  function chunkify(t) {
//    var tz = new Array();
//    var x = 0, y = -1, n = 0, i, j;
//
//    while (i = (j = t.charAt(x++)).charCodeAt(0)) {
//      var m = (i == 46 || (i >=48 && i <= 57));
//      if (m !== n) {
//        tz[++y] = "";
//        n = m;
//      }
//      tz[y] += j;
//    }
//    return tz;
//  }
//
//  var aa = chunkify(a.toLowerCase());
//  var bb = chunkify(b.toLowerCase());
//
//  for (x = 0; aa[x] && bb[x]; x++) {
//    if (aa[x] !== bb[x]) {
//      var c = Number(aa[x]), d = Number(bb[x]);
//      if (c == aa[x] && d == bb[x]) {
//        return c - d;
//      } else return (aa[x] > bb[x]) ? 1 : -1;
//    }
//  }
//  return aa.length - bb.length;
//}