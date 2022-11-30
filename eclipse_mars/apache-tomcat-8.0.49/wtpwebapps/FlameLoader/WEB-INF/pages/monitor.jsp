<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport"
	content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<meta name="renderer" content="webkit">
<title>物理机监控</title>
<link rel="stylesheet" href="statics/css/pintuer.css">
<link rel="stylesheet" href="statics/css/admin.css">
<link rel="stylesheet" href="statics/css/physical.css">
</head>

<body>
	<div id="mainDiv"> 
		<div id="container1"></div>
		<div id="container2"></div>
		
		<script type="text/javascript" src="statics/js/jquery-1.9.1.js"></script>
		<script type="text/javascript" src="statics/js/highcharts.js"></script>
		<script type="text/javascript" src="statics/js/highcharts-more.js"></script>
		<script type="text/javascript"> 
		var returnedPqosData = null;
		var flag=true;//flag为true,开启曲线绘制
		
	  	//定时函数,每隔1000ms执行一次,向后端请求新的数据
		setInterval(function() {
			if(flag==true){
				$.ajax({
					async:true,
					type:"get",
					url:"getPqos.do",//发送的get请求
					data:{},
					dataType:"json",
					success:function(returned) {  
						if(returned!=null&&returned!=""&&returned!="null"){
							returnedPqosData = returned; //用当前页面的临时变量接受该返回值
						}
					}	
				}); 
		   }
	    },1000);
	 
		 $(document).ready(function (){
		    Highcharts.setOptions({
		        global: {
		            useUTC: false
		        }
		 }); 
       /**
       * LLC miss 
       * 在container1的div里绘制该图
       */
        Highcharts.chart('container1', {
        	credits:{ 
        	      enabled:false 
        		},
            chart: {
                type: 'area',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function () {
                        // set up the updating of the chart each second
                        var series = this.series[0];
                        var lastcollecttime=null;
                        var x,y;
                        setInterval(function (){
                        	if(flag==true){
    			              if(returnedPqosData!=null&&returnedPqosData[0].time!=0){ 
    			            	    x = returnedPqosData[0].time; //取得当前页面变量里的时间戳
    			            	    y = returnedPqosData[0].cachemiss; //取得当前页面变量里的cacheMiss值
    			            	    if(lastcollecttime==null){//如果第一次判断 直接添加点进去
    			            	    	 series.addPoint([x,y], true, true); //绘制该点
    			            	    	 lastcollecttime = x; //把刚刚绘制的点的时间更新
    			            	    }else{ 
    			            	    	if(lastcollecttime<x){//如果不是第一次判断，则只有上次时间小于当前时间时才添加点
    			            	    		series.addPoint([x,y], true, true);  //绘制该点
    				            	    	lastcollecttime = x; //把刚刚绘制的点的时间更新
    			            	    	}
    			            	    }
    				               
    			               }    
                        	}
                        }, 200);//每隔200ms读取一次当前页面的临时变量returnedPqosData,判断如果是最新的值就添加到曲线当中
                    }
                }
            },
            title: {
                text: 'LLC miss(%)'
            },
            xAxis: {
            	type: 'datetime',
                tickPixelInterval: 150,
                gridLineWidth: 1
            },
            yAxis: {
                title: {
                    text: 'miss rate'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }],
               // max:100,
    			min:0
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.name + '</b><br/>' +
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                        Highcharts.numberFormat(this.y, 2)+'%';
                }
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                area: {
                    fillColor: {
                        linearGradient: {
                            x1: 0,
                            y1: 0,
                            x2: 0,
                            y2: 1
                        },
                        stops: [
                            [0, Highcharts.getOptions().colors[0]],
                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                        ]
                    },
                    marker: {
                        radius: 2
                    },
                    lineWidth: 1,
                    states: {
                        hover: {
                            lineWidth: 1
                        }
                    },
                    threshold: null
                }
            },
            exporting: {
                enabled: false
            },
            series:[${llc}] //接受前端页面传递过来的曲线字符串
        });
       
       /**
       * 绘制MBL和MBR的曲线
       * 在container2的div里绘制该图
       */
        Highcharts.chart('container2', {
        	credits:{ 
        	      enabled:false 
        		},
            chart: {
                type: 'line',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function () {
                        // set up the updating of the chart each second
                        var series0 = this.series[0];
                        var series1 = this.series[1];
                        var lastcollecttime=null;
                        var x,y;
                        setInterval(function (){
                     	   if(flag==true){
      			              if(returnedPqosData!=null&&returnedPqosData[0].time!=0){
      			            	    x = returnedPqosData[0].time;
      			            	    y = returnedPqosData[0].mbl;
      			            	    z=  returnedPqosData[0].mbr;
      			            	    if(lastcollecttime==null){
      			            	    	 series0.addPoint([x,y], true, true); 
      			            	    	 series1.addPoint([x,z], true, true); 
      			            	    	 lastcollecttime = x;
      			            	    }else{
      			            	    	if(lastcollecttime<x){
      			            	    		series0.addPoint([x,y], true, true); 
      			            	    		series1.addPoint([x,z], true, true); 
      				            	    	lastcollecttime = x;
      			            	    	}
      			            	    }
      				               
      			               }    
                          	}
                        }, 200);
                    }
                }
            },
            title: {
                text: 'MemBandwith used(MB/s)'
            },
            xAxis: {
            	type: 'datetime',
                tickPixelInterval: 150,
                gridLineWidth: 1
            },
            yAxis: {
                title: {
                    text: 'usage MB/s'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }], 
    			min:0
            },
            legend: {
               enabled:false
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.name + '</b><br/>' +
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                        Highcharts.numberFormat(this.y, 2)+'MB/s';
                }
            },
            
            exporting: {
                enabled: false
            },
            series:[${mbm}] //接受前端页面传递过来的曲线字符串
        });
 });
</script>
</div>
</body>
</html>
