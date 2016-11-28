function httpGetXML(url)
{
    var xmlHttp = null;

    xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", url, false );
    xmlHttp.send( null );
    return xmlHttp.responseText;
}

function httpGetJSON(MSG)
 {
  var objJSON = {
   "msg" : MSG
  };
  var strJSON = encodeURIComponent(JSON.stringify(objJSON));
  new Ajax.Request("ReceiveJSON.jsp",
   {
    method: "post",
    parameters: "strJSON=" + strJSON,
    onComplete: Respond
   });
 }

function Respond(REQ)
 {
  document.getElementById("ResponseDiv").innerHTML=REQ.responseText;
 }