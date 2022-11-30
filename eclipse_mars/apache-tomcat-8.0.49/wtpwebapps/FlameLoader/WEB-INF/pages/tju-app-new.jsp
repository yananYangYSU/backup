<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title></title>
</head>
<link rel="stylesheet" type="text/css" href="statics/css/global.css"/>

<!-- weixin -->
<style>
body{
background-image:url("statics/images/${type}.jpg");
background-repeat:no-repeat;
background-size: 96%;
background-attachment:fixed;
background-position:center center;
}
#clock{
    margin-left: 461px;
    margin-top: 244px;
    width: 437px;
    height: 100px;
    line-height: 100px;
    text-align: left;
    font: bold;
    font-size: 56px;
    font-family: auto;
    color: black;
    background-color: rgba(153,189,145,1); 
}
#clock2{
    margin-left: 480px;
    margin-top: 10px;
    width: 413px;
    height: 100px;
    line-height: 100px;
    text-align: left;
    background-color: rgba(153,189,145,1);

}

</style>
<!-- QQ -->
<!-- <style>
body{
background-image:url("statics/images/${type}.jpg");
background-repeat:no-repeat;
background-size: 98%;
background-attachment:fixed;
background-position:center center;
}
#clock{
    margin-left: 458px;
    margin-top: 272px;
    width: 448px;
    height: 100px;
    line-height: 100px;
    text-align: left;
    font: bold;
    font-size: 58px;
    font-family: auto;
    color: black;
    background-color: rgba(153,189,145,1); 
}
#clock2{
    margin-left: 486px;
    margin-top: 18px;
    width: 413px;
    height: 100px;
    line-height: 100px;
    text-align: left;
    font: bold;
    font-size: 82px;
    color: black;
    background-color: rgba(153,189,145,1);

}

</style>-->

<script>

function showTime(clock,clock2){

var now = new Date();

var year = now.getFullYear();

var month= now.getMonth();

var day = now.getDate();

var hour = now.getHours();

var minu = now.getMinutes();

var second = now.getSeconds();

var sss = now.getMilliseconds();

month = month+1;

var arr_work = new Array("星期日","星期一","星期二","星期三","星期四","星期五","星期六");

var week = arr_work[ now.getDay()];

if(month<10){
	month="0"+month;
}
if(day<10){
	day="0"+day;
}
var date = year+"年"+month+"月"+day+"日";

clock.innerHTML=date;

if(hour<10){
	hour="0"+hour;
}
if(minu<10){
	minu="0"+minu;
}
if(second<10){
	second="0"+second;
}
sss=parseInt(sss/(100));
var time = "<font style=\"font-size: 65px; color: black;\">"+hour+":"+minu+":</font><font style=\"font-size: 95px; color: #0001f3;font-weight: bold;\">"+second+".</font><font style=\"font-size: 120px; color: #fc0002;font-weight: bold;\">"+sss;
//var time = hour+":"+minu+":"+second+"."+sss;
clock2.innerHTML=time;
}

window.onload = function(){

var clock = document.getElementById("clock");
var clock2 = document.getElementById("clock2");
window.setInterval("showTime(clock,clock2)",100);
}

</script>
<body>
<div id ="clock"></div>
<div id ="clock2"></div>

</body>
</html>