$(function () {
    $("a[href]").click(function (event) {
        window.protocolCheck($(this).attr("href"),
            function () {
                //alert("protocol not recognized");
                setTimeout(getClient, 1000);
            });
        event.preventDefault ? event.preventDefault() : event.returnValue = false;
        top.jzts();
    });
});
