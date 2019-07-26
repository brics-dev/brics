
        // Default plugin options

$(document).ready(function() {
     $("#tableTest").idtTable({
              idtUrl: 'http://fitbir-portal-local.cit.nih.gov:8082/proforms/form/formList.action',
              idtData: {
                  primaryKey: "name"
              },
              length: 10,
              processing: false,
              serverSide: false,
              "columns": [
                  {
                      "data": '<%=rs.getValue("form.name.display", l)%>',
                      "title":'<%=rs.getValue("form.name.display", l)%>',
                      "name": '<%=rs.getValue("form.name.display", l)%>',
                      "parameter" : 'name',
                      "searchable": true,
                      "orderable": true
                  },
                  {
                      "data": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                      "title": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                      "name": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                      "parameter" : 'numQuestions',
                      "searchable": true,
                      "orderable": true
                  },
                  {
                      "data": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                      "title": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                      "name": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                      "parameter" : 'status.shortName',
                      "searchable": true,
                      "orderable": true
                  },
                  {
                      "data": '<%=rs.getValue("form.administered.display", l)%>',
                      "title": '<%=rs.getValue("form.administered.display", l)%>',
                      "name": '<%=rs.getValue("form.administered.display", l)%>',
                      "parameter" : 'administered',
                      "searchable": true,
                      "orderable": true
                  },
                  {
                      "data": '<%=rs.getValue("form.public.search.date.display", l)%>',
                      "title": '<%=rs.getValue("form.public.search.date.display", l)%>',
                      "name": '<%=rs.getValue("form.public.search.date.display", l)%>',
                      "parameter" : 'checkOutDate',
                      "searchable": true,
                      "orderable": true
                  }
              ],
          searchable: false,
          dom: 'Bfrtip',
          fixedHeader: true,
          select: 'multi',
              filters: [{
                                    containerId: "data_table_second",
                                    name: "weirded",
                                    defaultValue: "Charde",
                                    options: [
                                      {
                                        text: "Charde",
                                        value: "Charde"
                                      },
                                      {
                                        text: "Colleen",
                                        value: "Colleen"
                                      }
                                    ],
                                    test : function(oSettings, aData, iDataIndex, filterData) {
                                      if (filterData['weirded'] == "Colleen") {
                                                if (aData[0] == "Colleen") {
                                                  return true;
                                                }
                                                else {
                                                  return false;
                                                }
                                      }
                                    }
                                  },
                  {
                      containerId: "data_table_second",
                      name: "check",
                      defaultValue: "off",
                      render : function(tableModel, $container) {
                          $container.append('<li><input type="checkbox" id="check" name="check" value="check:on">check</input></li>');
                      },
                      test : function(oSettings, aData, iDataIndex, filterData) {
                          if (filterData.check == true) {
                              if (aData[4] == "Textbox") {
                                  return true;
                              }
                              else {
                                  return false;
                              }
                          }
                          else {
                              return true;
                          }
                      }
                  }
                  ],
          buttons: {
          buttons: [{
                  text: 'Row selected data',
                  action: function(e, dt, node, config) {
                      alert(
                          'Row data: ' +
                          JSON.stringify(dt.row({ selected: true }).data())
                      );
                  },
                  enabled: false,
                  className: 'test'

              },

              'delete',
              'view',
              'pdf',
              {
                  extend: 'excel',
                  exportOptions: {
                      modifier: {
                          page: 'current'
                      }
                  }
              },
              {
                  extend: 'print',
                  text: 'Print selected',
                  exportOptions: {
                      modifier: {
                          selected: true
                      }
                  }
              }
          ]
          }
    });
});