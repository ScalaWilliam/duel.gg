function DuelGG() {

    startSocket();

    function startSocket() {
        var url = "ws://127.0.0.1:9000/stream";
        var socket = new WebSocket(url);
        socket.onmessage = function(m) {
            var liHtml = $.parseJSON(m.data)["new index item"];
            var addLi = $($.parseHTML(liHtml));
            addLi.find("a").addClass("new");
            addLi.prependTo($("ol"));
        };
        socket.onclose = function(){
            setTimeout(startSocket, 5000);
        };
        globalSocket = socket;
    }

}
