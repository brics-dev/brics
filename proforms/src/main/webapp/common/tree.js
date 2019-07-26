// Title: Tigra Tree Menu PRO
// URL: http://www.softcomplex.com/products/tigra_menu_tree_pro/
// Version: 1.3
// Date: 12/14/2005
// Technical Support: support@softcomplex.com (specify product title and order ID)
// Notes: Registration needed to use this script legally.
// Visit official site for details.

//Code prepared by Tigra Javascript Scrambler version 1.0 (http://www.softcomplex.com/products/tigra_code_compressor/)

function tree(tmp1,tmp8,tmp09){var i;this.a_tpl=tmp8;this.a_config=tmp1;
    this.o_root=this;this.a_index=[];
    this.a_nodes=[];this.o_selected=0;
    this.n_depth=-1;
    this.tmpK=Boolean(tmp8['multiselections']);
    this.tmpw=tmp8['cookie_ext']?tmp8['cookie_ext']:'';this.tmp9=tmp8['b_solid'];
    this.tmpC=!tmp8['b_rootcollapse'];this.tmpH=!tmp8['keep_states'];
    this.tmpD=!tmp8['disable_states'];var tmpm=new Image(),tmpn=new Image();
    tmpm.src=tmp8['icon_e'];tmpn.src=tmp8['icon_l'];tmp8['im_e']=tmpm;tmp8['im_l']=tmpn;
    for(i=0;i<128;i++)if(tmp8['icon_'+i]){var tmpl=new Image();tmpl.src=tmp8['icon_'+i];
        tmp8['im_'+i]=tmpl}

    this.toggle=function(n_id){
        var tmpp=this.a_index[n_id];
        tmpp.open(tmpp.n_state&8)
    };
    this.select=function(n_id){if(this.tmpK){var tmpp=this.a_index[n_id];
        return tmpp.select(tmpp.n_state&4)}return this.a_index[n_id].select()};
    this.mover=function(n_id){var tmpp=this.a_index[n_id];tmpp.tmp0H();tmpp.mover(true)};
    this.mout=function(n_id){var tmpp=this.a_index[n_id];tmpp.tmp0H(true);tmpp.mout(true)};

    this.find_item=function(searchString,tmpI){
        var tmp1=[];
        tmpI=tmpI?1:0;
        for(var i=0;i<this.a_index.length;i++)
            if(this.a_index[i].a_config[tmpI]==searchString){
                tmp1[tmp1.length]=this.a_index[i]
            }
        return tmp1};
    this.find_item_substring=function(searchString,inCaption){
        var a_result=[];
        inCaption=inCaption?1:0;
        for(var i=0;i<this.a_index.length;i++){
            var str =  this.a_index[i].a_config[inCaption];
            if(str.indexOf(searchString) >0){
                a_result[a_result.length]=this.a_index[i];
              //  this.a_index[i].a_config[2]['ho'] = "style.color='red';";
                //this.a_index[i].a_config[2]['s0'] = 'redtext';

            }
        }
        return a_result};
    this.find_item_by_key=function(tmp05,tmp0D){
        var tmp1=[];
        for(var i=0;i<this.a_index.length;i++)
            if(this.a_index[i].a_config[2][tmp05]==tmp0D){
                tmp1[tmp1.length]=this.a_index[i]
            }
        return tmp1
    };
    this.find_item_by_state=function(tmph){var tmp1=[];for(var i=0;i<this.a_index.length;i++)if(this.a_index[i].n_state&tmph){tmp1[tmp1.length]=this.a_index[i]}return tmp1};this.tmpL=false;this.ndom_refresh=function(){if(!B_DOM&&this.tmpL)window.location=window.location};this.reset_state=function(){var exp=new Date();exp.setTime(exp.getTime()-1000);document.cookie='tree_'+this.n_id+'_state=;'+this.tmpw+';expires='+exp.toGMTString();document.cookie='tree_'+this.n_id+'_selected=;'+this.tmpw+';expires='+exp.toGMTString();this.a_states=[];this.tmp6=[]};while(!this.a_config[this.a_config.length-1])this.a_config.length=this.a_config.length-1;B_DOM=Boolean(document.body&&document.body.innerHTML);this.n_id=tmp09?tmp09:TREES.length;TREES[this.n_id]=this;if(tmp8['onConstruct']){eval('var tmpN='+tmp8['onConstruct']+'(this);');if(!tmpN)return false}this.tmpt=function(tmp0,tmp05){var tmpu=new RegExp('^\\s*(\\S+)\\s*=\\s*([\\d_]*)\\s*$'),tmp0D;for(var i=0;this.tmpD&&i<tmp0.length;i++){if(!tmpu.exec(tmp0[i])||RegExp.$1!=tmp05)continue;tmp0D=RegExp.$2;return tmp0D.split('_')}return[]};var tmp0=document.cookie.split(';'),tmp06='tree_'+this.n_id+'_state',tmp07='tree_'+this.n_id+'_selected';this.a_states=this.tmpt(tmp0,tmp06);this.tmp6=this.tmpt(tmp0,tmp07);if(!this.a_states.length)this.tmpJ=1;this.tmpH=this.tmpJ||this.tmpH;this.a_children=[];for(i=0;i<tmp1.length;i++)this.a_children[this.a_children.length]=new tmp0G(this,i);if(tmp8['beforeInit']){eval('var tmpN='+tmp8['beforeInit']+'(this);');if(!tmpN)return false}for(i=0;i<this.a_children.length;i++){this.a_children[i].tmpS=tmpU;document.write(this.a_children[i].tmpS())}if(tmp8['afterInit'])eval(tmp8['afterInit']+'(this);')}
function tmp0G(o_parent,n_order){this.o_root=o_parent.o_root;this.n_depth=o_parent.n_depth+1;this.a_config=o_parent.a_config[n_order+(this.n_depth?3:0)];this.o_parent=o_parent;this.n_order=n_order;while(!this.a_config[this.a_config.length-1])this.a_config.length=this.a_config.length-1;if(!this.a_config[2])this.a_config[2]={};this.n_id=this.o_root.a_index.length;this.o_root.a_index[this.n_id]=this;if(this.o_root.tmpH&&typeof(this.a_config[2]['sl'])=='number')if(Boolean(this.a_config[2]['sl']))if(!this.o_root.tmpK)this.o_root.o_selected=this;this.state=tmpb;this.tmpG=this.a_config[2]['fn'];if(this.a_config.length<4&&!this.tmpG)return;this.n_node_id=this.o_root.a_nodes.length;this.o_root.a_nodes[this.n_node_id]=this;for(var i=3;i<this.a_config.length;i++)new tmp0G(this,i-3)}
function openFunction(tmpB,tmpM){
    if(Boolean(this.n_state&8)!=Boolean(tmpB))
        return;
    var tmpy=this.a_config[2][tmpB?'hc':'ho'];
    tmpy=(tmpy?tmpy:this.o_root.a_tpl[tmpB?'onItemClose':'onItemOpen']);
    if(tmpy){
        eval('var tmpN='+tmpy+'(this);');
        if(!tmpN)
            return false
    }
    this.n_state^=8;
    this.o_root.tmpL=true;
    this.state_lookup();
    this.tmp0H();
    if(this.o_root.tmpD)
        this.save();
    if(B_DOM){
        if(this.a_config.length>3){
            var tmpo=tmpR('c'+this.o_root.n_id+'_'+this.n_id);
            if(!tmpo.innerHTML)
                tmpo.innerHTML=this.tmp0F();
            tmpo.style.display=(tmpB?'none':'block')
        }
    }else if(!tmpM&&this.a_config.length>3)
        window.location=window.location
}
function tmp_(tmpF){var tmpy=this.a_config[2][tmpF?'hd':'hs'];tmpy=(tmpy?tmpy:this.o_root.a_tpl[tmpF?'onItemDeselect':'onItemSelect']);if(tmpy){eval('var tmpN='+tmpy+'(this);');if(!tmpN)return false}if(tmpF){this.n_state&=~4}else{if(!this.o_root.tmpK){var tmpr=this.o_root.o_selected;if(tmpr&&tmpr!=this&&tmpr.tmp0H){tmpr.select(true);this.o_root.o_selected=this}this.o_root.o_selected=this}this.n_state|=4}if(this.o_root.tmpD)this.selsave();this.state_lookup();this.tmp0H();return Boolean(this.a_config[1])}
function tmpX(){var tmpy=this.a_config[2]['hv'];tmpy=(tmpy?tmpy:this.o_root.a_tpl['onItemMover']);if(tmpy){eval('var tmpN='+tmpy+'(this);');if(!tmpN)return false}this.n_state|=64;this.state_lookup()}
function tmpW(){var tmpy=this.a_config[2]['hu'];tmpy=(tmpy?tmpy:this.o_root.a_tpl['onItemMout']);if(tmpy){eval('var tmpN='+tmpy+'(this);');if(!tmpN)return false}this.n_state&=~64;this.state_lookup()}
function tmpb(){var tmps={'inited':false};if(this.tmp0H){tmps['inited']=true;tmps['root']=this.n_state&32?true:false;tmps['node']=this.n_state&16?true:false;tmps['opened']=this.n_state&8?true:false;tmps['selected']=this.n_state&4?true:false;tmps['hovered']=this.n_state&64?true:false;tmps['last']=this.n_state&1?true:false}return tmps}
function tmpe(tmpA){window.setTimeout("window.status=unescape('"+(tmpA?'':(this.a_config[2]['sb']?escape(this.a_config[2]['sb']):escape(this.a_config[0])+(this.a_config[1]?' ('+escape(this.a_config[1])+')':'')))+"')",10)}
function tmpU(){var a_index=this.o_root.a_index,n_id=this.n_id,i;this.state_lookup=tmpc;this.mover=tmpX;this.mout=tmpW;this.select=tmp_;this.selsave=tmpa;this.load=tmpV;this.tmp0H=tmpe;var tmp5=[],tmpv=new RegExp("o_tree_item","g");tmp5[1]=this.a_config[2]['hie']||this.o_root.a_tpl['userIconEvent'];tmp5[2]=this.a_config[2]['hte']||this.o_root.a_tpl['userTextEvent'];if(this.a_config.length>3||this.tmpG){tmp5[0]=this.a_config[2]['hje']||this.o_root.a_tpl['userJoinEvent'];this.a_children=[];while(true){n_id++;if(n_id==a_index.length)break;if(a_index[n_id].n_depth<=this.n_depth)break;if(a_index[n_id].n_depth==this.n_depth+1){a_index[n_id].tmpS=tmpU;this.a_children[this.a_children.length]=a_index[n_id]}}this.open=openFunction;this.save=tmpZ;this.tmp0F=tmpd}else{this.open=function(){}}

this.n_state=(this.n_depth?0:32)+(this.a_children||this.tmpG?16:0)+(this.n_order==this.o_parent.a_children.length-1?1:0);

var tmp00=(this.o_root.a_tpl['style_icons']?' class="'+this.o_root.a_tpl['style_icons']+'"':''),tmp2=[],tmpk=this.o_parent,tmp3=this.a_config[2];
    var tmpx=this.o_root.a_tpl['alt_e']?this.o_root.a_tpl['alt_e']:"";
    var tmp08=this.o_root.a_tpl['alt_l']?this.o_root.a_tpl['alt_l']:"";
    for(i=this.n_depth;i>this.o_root.tmpC;i--){
        tmp2[i]='<img src="'+this.o_root.a_tpl[tmpk.n_state&33?'icon_e':'icon_l']+'" alt="'+(tmpk.n_state&33?tmpx:tmp08)+'" border="0"'+tmp00+'>';tmpk=tmpk.o_parent}this.load();
    for(i in tmp5){
        if(tmp5[i]){tmp5[i]=tmpT(tmp5[i]);
            tmp5[i]=tmp5[i].replace(tmpv,'TREES[\''+this.o_root.n_id+'\'].a_index['+this.n_id+']')}else tmp5[i]=''}
    var tmp4=this.state_lookup(true);
    var tmpQ='',tmpP='nowrap';var tmp0B='onmouseover="TREES[\''+this.o_root.n_id+'\'].mover('+this.n_id+')" onmouseout="TREES[\''+this.o_root.n_id+'\'].mout('+this.n_id+')"';
    if(tmp3['wr']){this.tmpj=tmp3['wr'];tmpP=' valign=top rowspan='+tmp3['wr'];i=tmp3['wr'];
        while(--i){tmpQ+='<tr><td nowrap>'+tmp2.join('')+'<img src="'+this.o_root.a_tpl[this.n_state&1?'icon_e':'icon_l']+'" border="0"'+tmp00+'>';
            if(this.a_children)tmpQ+='<img src="'+this.o_root.a_tpl[this.n_state&8?'icon_l':'icon_e']+'"  name="_i'+this.o_root.n_id+'_'+this.n_id+'_'+i+'" border="0"'+tmp00+'>';tmpQ+='</tr>'}}
    return'<table cellpadding="0" cellspacing="0" border="0"><tr><td nowrap>'
            +tmp2.join('')
            +(tmp4[1]?(this.a_children?'<a href="javascript: TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+')" onclick = "TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+');return false;" '+tmp5[0]+'><img src="'+tmp4[1]+'" border="0" name="j'+this.o_root.n_id+'_'+this.n_id+'" alt="'+tmp4[3]+'" '+tmp00+'></a>':'<img src="'+tmp4[1]+'" border="0" alt="'+tmp4[3]+'" '+tmp00+'>'):'')
            +(tmp4[0]?'<a href="Javascript:void(0);" target="'+(tmp3['tw']?tmp3['tw']:this.o_root.a_tpl['target'])+'" onclick="return TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+')" ondblclick="TREES[\''+this.o_root.n_id+'\'].'+(this.a_children?'toggle(':'select(')+this.n_id+')" '+tmp5[1]+'><img src="'+tmp4[0]+'" border="0" name="i'+this.o_root.n_id+'_'+this.n_id+'" alt="'+tmp4[4]+'" '+tmp00+'></a>':'')
            +'</td><td '+tmpP+(tmp4[2]?' class="'+tmp4[2]+'"':'')
            +' id="t'+this.o_root.n_id+'_'+this.n_id+'">'+this.a_config[0]+'</a></td></tr>'+tmpQ+'</table>'
            +(this.a_children?'<div id="c'+this.o_root.n_id+'_'+this.n_id+'" style="display:'+(this.n_state&8?'block">'+this.tmp0F():'none">'
            +(this.o_root.tmp9?this.tmp0F():''))+'</div>':'')}

/*      ORIGINAL CODE
return'<table cellpadding="0" cellspacing="0" border="0"><tr '+tmp0B+'><td nowrap>'
            +tmp2.join('')
            +(tmp4[1]?(this.a_children?'<a href="javascript: TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+')" onclick = "TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+');return false;" '+tmp0B+' '+tmp5[0]+'><img src="'+tmp4[1]+'" border="0" name="j'+this.o_root.n_id+'_'+this.n_id+'" alt="'+tmp4[3]+'" title="'+tmp4[3]+'"'+tmp00+'></a>':'<img src="'+tmp4[1]+'" border="0" alt="'+tmp4[3]+'" title="'+tmp4[3]+'"'+tmp00+'>'):'')
            +(tmp4[0]?'<a href="'+this.a_config[1]+'" target="'+(tmp3['tw']?tmp3['tw']:this.o_root.a_tpl['target'])+'" title="'+(tmp3['tt']?tmp3['tt']:'')+'" onclick="return TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+')" ondblclick="TREES[\''+this.o_root.n_id+'\'].'+(this.a_children?'toggle(':'select(')+this.n_id+')" '+tmp5[1]+'><img src="'+tmp4[0]+'" border="0" name="i'+this.o_root.n_id+'_'+this.n_id+'" alt="'+tmp4[4]+'" title="'+tmp4[4]+'"'+tmp00+'></a>':'')
            +'</td><td '+tmpP+(tmp4[2]?' class="'+tmp4[2]+'"':'')
            +' id="t'+this.o_root.n_id+'_'+this.n_id+'"><a href="'+this.a_config[1]+'" target="'
            +(tmp3['tw']?tmp3['tw']:this.o_root.a_tpl['target'])
            +'" title="'+(tmp3['tt']?tmp3['tt']:'')
            +'" onclick="return TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+')" ondblclick="TREES[\''+this.o_root.n_id+'\'].'
            +(this.a_children?'toggle(':'select(')+this.n_id+')" '+tmp5[2]+'>'+this.a_config[0]+'</a></td></tr>'+tmpQ+'</table>'
            +(this.a_children?'<div id="c'+this.o_root.n_id+'_'+this.n_id+'" style="display:'+(this.n_state&8?'block">'+this.tmp0F():'none">'
            +(this.o_root.tmp9?this.tmp0F():''))+'</div>':'')}
            */
    /*
return '<table cellpadding="0" cellspacing="0" border="0"><tr '+tmp0B+'><td nowrap>'
        +tmp2.join('')
        +(tmp4[1]?
          (this.a_children?    // lines or plus icons ?
                '<a href="javascript: TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+')" '
                        +' onclick = "TREES[\''+this.o_root.n_id+'\'].toggle('+this.n_id+');return false;" '
                        +tmp0B+' '+tmp5[0]+'><img src="'+tmp4[1]+'" border="0" name="j'+this.o_root.n_id+'_'+this.n_id+'" alt="'
                        +tmp4[3]+'" title="'+tmp4[3]+'"'+tmp00+'></a>'
                  :'<img src="'+tmp4[1]+'" border="0" alt="'+tmp4[3]+'" title="'+tmp4[3]+'"'+tmp00+'>')
            :'')
        +(tmp4[0]?    // icons?
               '<a href="javascript: void(0);" target="'
                       +(tmp3['tw']?
                            tmp3['tw']:
                            this.o_root.a_tpl['target'])
                       +'" title="'
                       +(tmp3['tt']?
                            tmp3['tt']:
                            '')
                       +'" onclick="return TREES[\''+this.o_root.n_id+'\'].select('+this.n_id+')" ondblclick="TREES[\''+this.o_root.n_id+'\'].'
                       +(this.a_children?'toggle(':'select(')+this.n_id+')" '+tmp5[1]+'> '
                       //+ '<img src="'+tmp4[0]+'" border="0" name="i'+this.o_root.n_id+'_'+this.n_id+'" alt="'+tmp4[4]+'" title="'+tmp4[4]+'"'+tmp00+'></a>'
                        + '<img src="'+tmp4[0]+'" border="0" name="i'+this.o_root.n_id+'_'+this.n_id+'" alt="'+tmp4[4]+'" title="'+tmp4[4]+'"'+tmp00+'></a>'
            :'')
        +'</td><td '
        +tmpP
        +(tmp4[2]?' class="'+tmp4[2]+'"':'')
        +' id="t'+this.o_root.n_id+'_'+this.n_id+'"><a href="'+this.a_config[1]+'" '
        + 'target="'
        +(tmp3['tw']?
            tmp3['tw']:
            this.o_root.a_tpl['target'])
        +'" title="'+(tmp3['tt']?tmp3['tt']:'')+'" '
        +' onclick="return TREES[\''+this.o_root.n_id+'\'].select('+this.n_id+')" ondblclick="TREES[\''+this.o_root.n_id+'\'].'
        +(this.a_children?'toggle(':'select(')+this.n_id+')" '
        +tmp5[2]+'>'+this.a_config[0]+'</a></td></tr>'+tmpQ+'</table>'
        +(this.a_children?
          '<div id="c'+this.o_root.n_id+'_'+this.n_id+'" style="display:'
                  +(this.n_state&8?
                    'block">'+this.tmp0F()
                  :'none">'
                    +(this.o_root.tmp9?this.tmp0F():''))
          +'</div>':'')
    */
//}
function tmpd(){var tmp7=[];for(var i=0;i<this.a_children.length;i++)tmp7[i]=this.a_children[i].tmpS();return tmp7.join('')}
function tmpZ(){var tmpi=Math.floor(this.n_node_id/31);this.o_root.a_states[tmpi]=(this.n_state&8?this.o_root.a_states[tmpi]|(1<<(this.n_node_id%31)):this.o_root.a_states[tmpi]&~(1<<(this.n_node_id%31)));document.cookie='tree_'+this.o_root.n_id+'_state='+this.o_root.a_states.join('_')+';'+this.o_root.tmpw}
function tmpa(){var tmpi=Math.floor(this.n_id/31);this.o_root.tmp6[tmpi]=(this.n_state&4?this.o_root.tmp6[tmpi]|(1<<(this.n_id%31)):this.o_root.tmp6[tmpi]&~(1<<(this.n_id%31)));document.cookie='tree_'+this.o_root.n_id+'_selected='+this.o_root.tmp6.join('_')+';'+this.o_root.tmpw}
function tmpV(){var tmpy=(tmpy?tmpy:this.o_root.a_tpl['onItemLoad']);if(tmpy){eval('var tmpN='+tmpy+'(this);');if(!tmpN)return false}if(this.o_root.o_selected&&!this.o_root.tmpE){if(this.o_root.o_selected==this&&this.o_root.tmpH){this.n_state|=4;if(this.o_root.tmpD)this.selsave()}}else{if(this.o_root.tmpH&&typeof(this.a_config[2]['sl'])=='number'){if(Boolean(this.a_config[2]['sl']))this.n_state|=4;else this.n_state&=~4;if(this.o_root.tmpD)this.selsave()}else{var tmpi=Math.floor(this.n_id/31);if(Boolean(this.o_root.tmp6[tmpi]&(1<<(this.n_id%31)))){this.n_state|=4;if(!this.o_root.tmpK){this.o_root.tmpE=1;this.o_root.o_selected=this}}else this.n_state&=~4}}if(!this.a_children)return;if(this.o_root.tmpJ&&!this.n_depth&&typeof(this.a_config[2]['st'])!='number')this.a_config[2]['st']=1;if(this.o_root.tmpH&&typeof(this.a_config[2]['st'])=='number'){if(Boolean(this.a_config[2]['st']))this.n_state|=8;else this.n_state&=~8;if(this.o_root.tmpD)this.save();return}var tmpi=Math.floor(this.n_node_id/31);if(Boolean(this.o_root.a_states[tmpi]&(1<<(this.n_node_id%31))))this.n_state|=8;else this.n_state&=~8}

// get icon?
function tmpc(tmpO){


    var tmpf=this.n_state&~3;
    var tmpg=this.n_state&~68|2;
   // var mtTmpf = tmpf + this.n_depth*100;
    //var mtTmpg = tmpg + this.n_depth*100;
    var tmp02=this.a_config[2]['i'+(tmpf&~48)];
    if(!tmp02)tmp02=this.o_root.a_tpl['icon_'+tmpf];
    if(!tmp02)tmp02=this.o_root.a_tpl['icon_'+((tmpf&~64))];

    var tmp04=this.o_root.a_tpl['icon_'+tmpg];
    var tmp0C=this.a_config[2]['s'+(tmpf&~48)];
    if(!tmp0C)tmp0C=this.o_root.a_tpl['style_'+tmpf];
    if(!tmp0C)tmp0C=this.o_root.a_tpl['style_'+(tmpf&~64)];
    var tmp03=this.o_root.a_tpl['alt_'+tmpg];
    tmp03=tmp03?tmp03:"";
    var tmpz=this.a_config[2]['a'+(tmpf&~48)]||this.o_root.a_tpl['alt_'+tmpf];
    if(!tmpz)tmpz=this.o_root.a_tpl['alt_'+tmpf];
    tmpz=tmpz?tmpz:"";
    if(tmpO){
        //alert ([tmp02,tmp04,tmp0C,tmp03,tmpz]);
        return[tmp02,tmp04,tmp0C,tmp03,tmpz];
    }
    var tmpq=document.images['j'+this.o_root.n_id+'_'+this.n_id];
    if(tmpq){
        tmpq.src=tmp04;tmpq.alt=tmpq.title=tmp03
    }
    tmpq=document.images['i'+this.o_root.n_id+'_'+this.n_id];
    if(tmpq){tmpq.src=tmp02;tmpq.alt=tmpq.title=tmpz}
    if(this.tmpj){
        var tmp0E=this.o_root.a_tpl[this.n_state&8?'icon_l':'icon_e'];
        var i=this.tmpj;
        while(--i){
            tmpq=document.images['_i'+this.o_root.n_id+'_'+this.n_id+'_'+i];
            if(tmpq)tmpq.src=tmp0E
        }
    }
    tmpq=tmpR('t'+this.o_root.n_id+'_'+this.n_id);
    if(tmpq)tmpq.className=tmp0C
}

function tmpT(tmp5){var tmp0A=[];for(var tmp05 in tmp5)tmp0A[tmp0A.length]=tmp05+'="'+tmp5[tmp05]+'"';return tmp0A.join(' ')}var TREES=[],B_DOM;tmpR=document.all?function(tmp01){return document.all[tmp01]}:(document.getElementById?function(tmp01){return document.getElementById(tmp01)}:function(tmp01){return null});