var ajaxWrapper = function () {
    return {
        getJSON: function (url) {
            var promise = $.ajax({
                dataType: "json",
                cache: false,
                url: url
            });
            return promise;
        },
        getJSONCache: function (url) {
            var promise = $.ajax({
                dataType: "json",
                cache: true,
                url: url
            });
            return promise;
        },
        postJSON: function (url, data) {
            var promise = $.post(url, data);
            return promise;
        },
        postJSONCustom: function(url,data) {
            var promise = $.ajax({
                type: 'POST',
                url: url,
                contentType: 'application/json',
                dataType: 'json',
                data: JSON.stringify(data),
            });
            return promise;
        } 
    }
}();


