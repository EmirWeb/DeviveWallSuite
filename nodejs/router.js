var url = require("url");
var querystring = require("querystring");

function route(pathname, request) {
  console.log("About to route a request for " + pathname);
  var query = url.parse(request.url).query;
  console.log("About to route a request for " + query);
  var parsed = querystring.parse(query);
  console.log("device width " + parsed.width + " height " + parsed.height);

  exports.devicewidth = parsed.width;
  exports.deviceheight = parsed.height;
}

exports.route = route;