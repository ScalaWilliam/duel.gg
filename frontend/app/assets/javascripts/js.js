function DuelGG() {

    startSocket();

    function startSocket() {
        var url = "ws://" + location.host + "/stream";
        var socket = new WebSocket(url);
        socket.onmessage = function(m) {
            var liHtml = $.parseJSON(m.data)["new index item"];
            var addLi = $($.parseHTML(liHtml));
            addLi.find("a").addClass("new");

            if ( $(".player-profile-name").length > 0 ) {
                var playerName = $(".player-profile-name").text();
                if ( liHtml.indexOf(playerName) == -1 ) {
                    return;
                }
            }

            var justNowContainer = $(".duels-day").filter(function(x) { return $(this).find("h3").first().text() == "Just now"; });

            if ( justNowContainer.length == 0 ) {
                var text = "<li class=\"duels-day\">"+
                    "<h3>Just now</h3>" +
                    "<ol class=\"duels-day-items\"></ol></li>";
                justNowContainer = $(text).prependTo($(".duels-days-list"));
            }
            addLi.prependTo(justNowContainer.find("ol.duels-day-items"));
        };
        socket.onclose = function(){
            setTimeout(startSocket, 5000);
        };
        globalSocket = socket;
    }

}
