var messageDialog;
messageDialog = messageDialog || (function () {
    $(function () {
        $('#messagedialog').dialog({
            modal: true,
            autoOpen: false,
            draggable: true,
            resizable: true,
            zIndex: 1005,
            closeOnEscape: false,
            title : 'Message',
            buttons: {
                Close: function () {
                    $(this).dialog("close");
                }
            }
        });
    });
    return {
        show: function (message) {
            $('#messagedialog').find('div p').text(message);
            $('#messagedialog').dialog('open');
        },
        hide: function () {
            $('#messagedialog').dialog('close');
        },
    };
})();

var confirmDialog;
confirmDialog = confirmDialog || (function () {
    $(function () {
        $('#confirmdialog').dialog({
            modal: true,
            autoOpen: false,
            draggable: true,
            resizable: true,
            zIndex: 1005,
            closeOnEscape: false,
            title: 'Confirm',
            buttons: {
                Confirm: function () {
                    confirm();
                },
                Close: function () {
                    $(this).dialog("close");
                }
            }
        });

        function confirm() {
            confirmDialog.callbackroutine();
        }
    });
    return {
        show: function (message, callback) {
            confirmDialog.callbackroutine = callback;
            $('#confirmdialog').find('div p').text(message);
            $('#confirmdialog').dialog('open');
        },
        hide: function () {
            $('#confirmdialog').dialog('close');
        },
    };
})();