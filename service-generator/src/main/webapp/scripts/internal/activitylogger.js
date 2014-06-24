var activitylogger = function () {
    var isLoggingActive = true;

    var elementIDs = {
        activityLog: "activitylog",
        activityLogJQ: "#activityLog",
        activityGridLog: "activityLogGrid",
        activityLogGridJQ: "#activityLogGrid"
    }

    function activateLogging(activate) {
        isLoggingActive = activate;
    }

    function init() {
        if (isLoggingActive) {
            loadActivityGrid();
        }
    }

    function loadActivityGrid() {
        $(elementIDs.activityLogGridJQ).jqGrid('GridUnload');
        var pagerElement = "#p_" + elementIDs.activityGridLog;
        $(elementIDs.activityLogGridJQ).parent().append("<div id='p_" + elementIDs.activityGridLog + "'></div>");
        $(elementIDs.activityLogGridJQ).jqGrid({
            datatype: "local",
            colNames: ['ID', 'Form', 'Section', 'Description'],
            colModel: [{ name: 'ID', index: 'ID', key: true, hidden: true },
                       { name: 'Form', index: 'Form', width: '70' },
                       { name: 'Section', index: 'Section', width: '70' },
                       { name: 'Description', index: 'Description', width: '400' }],
            pager: pagerElement,
            altRows: true,
            altclass: 'alternate',
            height: '200',
            hiddengrid: false,
            hidegrid: false,
            rowNum: 50000,
            autowidth: true,
            shrinkToFit: false,
            caption: 'Activity Log',
            gridComplete: function () {
                $(elementIDs.activityLogGridJQ).jqGrid('setGridWidth', $(window).width() - (($(window).width()*4)/100), true);
            }
        });
        $(elementIDs.activityLogGridJQ).jqGrid('navGrid', pagerElement, { edit: false, add: false, del: false, refresh: false, search: false }, {}, {}, {});

        //remove paging
        $(pagerElement).find(pagerElement + '_center').remove();
        $(pagerElement).find(pagerElement + '_right').remove();
        //add button in footer of grid that pops up the add form design dialog
        $(elementIDs.activityLogGridJQ).jqGrid('navButtonAdd', pagerElement,
        {
            caption: '', buttonicon: 'ui-icon-close',
            title: 'Clear Log',
            onClickButton: function () {
                clearLog();
            }
        });
    }

    

    function logActivity(propertyName, previousValue, changedValue, formName, sectionName) {
        propertyName = propertyName.replace(/([a-z])([A-Z])/g, '$1 $2')
                                    .replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
                                    .replace(/^./, function (str) { return str.toUpperCase(); });

        var msg = "Value of <b>" + propertyName + "</b> is changed from <b>" + (previousValue === "" ? "null" : previousValue) + "</b> to <b>" + changedValue + "</b>.";

        var rowCount = $(elementIDs.activityLogGridJQ).getGridParam("reccount");
        $(elementIDs.activityLogGridJQ).jqGrid('addRowData', (rowCount + 1), { ID: (rowCount + 1), Form: formName, Section: sectionName, Description: msg });
    }

    function getLabelText(targetID) {
        var text = $("label[for='" + targetID + "']").text();
        return text.replace(":", "").replace("*", "");
    }

    function clearLog() {
        $(elementIDs.activityLogGridJQ).jqGrid('clearGridData');
    }

    return {
        init: function () {
            init();
        },

        log: function (propertyName, previousValue, changedValue, formName, sectionName) {
            logActivity(propertyName, previousValue, changedValue, formName, sectionName);
        },
        clear: function () {

        },
        activate: function (active) {
            activateLogging(active);
        }
    }
}();

