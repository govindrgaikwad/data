function compareStrings(string1, string2, ignoreCase, useLocale) {
    if (!!ignoreCase) {
        if (!!useLocale) {
            string1 = string1.toLocaleLowerCase();
            string2 = string2.toLocaleLowerCase();
        }
        else {
            string1 = string1.toLowerCase();
            string2 = string2.toLowerCase();
        }
    }

    return string1 === string2;
}


function getDataProperty(fullName, data) {
    var dataPart;
    var nameParts = fullName.split(".");
    for (var idx = 0; idx < nameParts.length; idx++) {
        if (idx == 0) {
            dataPart = data[nameParts[idx]];
        }
        else {
            dataPart = dataPart[nameParts[idx]];
        }
    }
    return dataPart;
}

//filter implementation for IE8
if (!Array.prototype.filter) {
    Array.prototype.filter = function (fun /*, thisp */) {
        "use strict";

        if (this === void 0 || this === null)
            throw new TypeError();

        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun !== "function")
            throw new TypeError();

        var res = [];
        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
            if (i in t) {
                var val = t[i]; // in case fun mutates this
                if (fun.call(thisp, val, i, t))
                    res.push(val);
            }
        }

        return res;
    };
}

function flattenObject(objectToFlatten) {
    var returnVal = new Array();

    for (var property in objectToFlatten) {
        if (!objectToFlatten.hasOwnProperty(property)) continue;

        if ((typeof objectToFlatten[property]) == 'object' && !Array.isArray(objectToFlatten[property])) {
            var flatObject = flattenObject(objectToFlatten[property]);
            for (var prop in flatObject) {
                if (!flatObject.hasOwnProperty(prop))
                    continue;
                if ((property + '.' + flatObject).indexOf("jQ") == -1)
                    returnVal[property + '.' + prop] = flatObject[prop];
            }
        }
        else {
            if (property.indexOf("jQ") == -1)
                returnVal[property] = objectToFlatten[property];
        }
    }
    return returnVal;
};