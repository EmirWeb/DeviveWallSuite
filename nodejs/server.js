var http = require("http");
var url = require("url");

var device_id = 0;
function start(route) {
  function onRequest(request, response) {
    var pathname = url.parse(request.url).pathname;
    console.log("Request for " + pathname + " received.");
    route(pathname, request);

    response.writeHead(200, {"Content-Type": "application/json"});
    response.write("{\"id\": " + device_id+ ",\"width\": 768,\"height\": 1184}");
    device_id += 1;
    response.end();
  }

  http.createServer(onRequest).listen(9999);
  console.log("Server has started.");
}

exports.start = start;