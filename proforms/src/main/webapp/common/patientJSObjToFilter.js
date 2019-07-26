function patientObj (patientid, patientDisplay) {
    this.id = patientid;
    this.display = patientDisplay;

    this.getFilterOperator = function () { return this.display;}

    this.getLink = function () {
        str = "<td align=left class=tableCell valign=middle><img src='/ctdb/images/treeIcons/patientNotSel.gif'>&nbsp;&nbsp;<a href='/ctdb/treeview/selectPatient.do?patientId="+this.id+"'>"+this.display+"</a></td>";
        str += "<td width='15%' align=right class=tableCell><a href='/ctdb/patient/viewPatient.do?src=treeview&id="+this.id+"'>view</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>";
        return str;
    }

}