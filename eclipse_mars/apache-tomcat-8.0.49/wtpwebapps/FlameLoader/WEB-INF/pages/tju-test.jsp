<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title></title>
</head>
<link rel="stylesheet" type="text/css" href="statics/css/global.css"/>
<style>
body{ 
background-image:url("statics/images/app.jpg");
background-repeat:no-repeat;
background-size: 96%;
background-attachment:fixed;
background-position:center center;
}
#click{
    margin-left: 451px;
    margin-top: 1842px;
    width: 71px;
    height: 71px;
}
</style>
<body>
<div id ="click" onclick="window.location.href='//996315.com/api/scan/'"></div>

<!--  <a href="//996315.com/api/scan/">扫描</a> -->
 
<script>
var qr=GetQueryString("qrresult");
if(qr) {
	var type='${type}';
	var position='${position}';
	var version='${version}';
	window.location = 'input.do?type='+type+'&position='+position+'&version='+version;
}
	//alert(qr);//放入表单输入框或者发送到后台，具体根据自己的业务做相应的处理
 
function GetQueryString(name){
    var reg = new RegExp("\\b"+ name +"=([^&]*)");
    var r = location.href.match(reg);
    if (r!=null) return decodeURIComponent(r[1]);
    
}
</script> 
 
</body>
</html>