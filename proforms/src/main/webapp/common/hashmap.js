

function HashMap()
{
    this.length = 0;
    this.prefix = "hashmap_prefix_20050524_";
}

HashMap.prototype.put = function (key, value)
{
  
    this[this.prefix + key] = value;
    this.length ++;
}

HashMap.prototype.get = function(key)
{
    return typeof this[this.prefix + key] == "undefined"
            ? null : this[this.prefix + key];
}
/**
 * ��HashMap�л�ȡ����key�ļ��ϣ���������ʽ����
 */
HashMap.prototype.keySet = function()
{
    var arrKeySet = new Array();
    var index = 0;
    for(var strKey in this)
    {
        if(strKey.substring(0,this.prefix.length) == this.prefix)
            arrKeySet[index ++] = strKey.substring(this.prefix.length);
    }
    return arrKeySet.length == 0 ? null : arrKeySet;
}
/**
 * ��HashMap�л�ȡvalue�ļ��ϣ���������ʽ����
 */
HashMap.prototype.values = function()
{
    var arrValues = new Array();
    var index = 0;
    for(var strKey in this)
    {
        if(strKey.substring(0,this.prefix.length) == this.prefix)
            arrValues[index ++] = this[strKey];
    }
    return arrValues.length == 0 ? null : arrValues;
}
/**
 * ��ȡHashMap��valueֵ����
 */
HashMap.prototype.size = function()
{
    return this.length;
}
/**
 * ɾ��ָ����ֵ
 */
HashMap.prototype.remove = function(key)
{
    delete this[this.prefix + key];
    this.length --;
}
/**
 * ���HashMap
 */
HashMap.prototype.clear = function()
{
    for(var strKey in this)
    {
        if(strKey.substring(0,this.prefix.length) == this.prefix)
            delete this[strKey];
    }
    this.length = 0;
}
/**
 * �ж�HashMap�Ƿ�Ϊ��
 */
HashMap.prototype.isEmpty = function()
{
    return this.length == 0;
}
/**
 * �ж�HashMap�Ƿ����ĳ��key
 */
HashMap.prototype.containsKey = function(key)
{
    for(var strKey in this)
    {
       if(strKey == this.prefix + key)
          return true;
    }
    return false;
}
/**
 * �ж�HashMap�Ƿ����ĳ��value
 */
HashMap.prototype.containsValue = function(value)
{
    for(var strKey in this)
    {
       if(this[strKey] == value)
          return true;
    }
    return false;
}
/**
 * ��һ��HashMap��ֵ���뵽��һ��HashMap�У�����������HashMap
 */
HashMap.prototype.putAll = function(map)
{
    if(map == null)
        return;
    if(map.constructor != JHashMap)
        return;
    var arrKey = map.keySet();
    var arrValue = map.values();
    for(var i in arrKey)
       this.put(arrKey[i],arrValue[i]);
}
//toString
HashMap.prototype.toString = function()
{
    var str = "";
    for(var strKey in this)

    {
        if(strKey.substring(0,this.prefix.length) == this.prefix)
              str += strKey.substring(this.prefix.length)
                  + " : " + this[strKey] + "\r\n";
    }
    //alert("the string is from hashmap: "  + str);
    return str;
}




/*
function HashMap () {

    this.keys = new Array();
    this.data = new Array();
    this.get = get;
    this.put = put;
    this.print = print;
    this.remove = remove;
}

function get(str) {
    for (i = 0; i < this.data.length; i++) {
        if (this.keys[i] == str) {
            return this.data [i];
        }
    }
}

function put (str, obj) {
    alert ('put');

    found = false;
    for (i = 0; i < this.data.length; i++) {
        if (keys[i] == str) {
            found = true;
           }
    }
    if (! found ) {
        this.keys[this.keys.length] = str;
        this.data[this.data.length] = obj;
    }
}

function remove (str) {
    tmpKeys = new Array();
    tmpData = new Array();

    for (i=0; i < this.data.length; i++){
        if (this.keys[i] != str) {
            tmpKeys[tmpKeys.length] = this.keys[i];
            tmpData[tmpData.length] = this.data[i];
        }
    }
    this.data = tmpData;
    this.keys = tmpKeys;
}

function print () {
    msg = " The hashmap contains : \n";
    for (i = 0; i < this.data.length; i++) {
        msg += this.keys[i] + " = " + this.data[i] + "\n";
    }
    alert (msg);
}
  */