taskkill /f /t /im java.exe
%jps -l |grep linux.jar | awk '{print $1 }'|xargs kill -9%