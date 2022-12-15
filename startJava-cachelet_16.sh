cd D:/cacheResearch/workspace
rm log_agent*txt
#System.out.println("[agentName, memorySize(int), ip, port, keepaliveTime(ms)]"); 
java -jar cachelet-dev.jar agent1-dev $1 localhost 33331 $2 >> log_agent1.txt &
java -jar cachelet-dev.jar agent2-dev $1 localhost 33332 $2 >> log_agent2.txt &
java -jar cachelet-dev.jar agent3-dev $1 localhost 33333 $2 >> log_agent3.txt &
java -jar cachelet-dev.jar agent4-dev $1 localhost 33334 $2 >> log_agent4.txt &
java -jar cachelet-dev.jar agent5-dev $1 localhost 33335 $2 >> log_agent5.txt &
java -jar cachelet-dev.jar agent6-dev $1 localhost 33336 $2 >> log_agent6.txt &
java -jar cachelet-dev.jar agent7-dev $1 localhost 33337 $2 >> log_agent7.txt &
java -jar cachelet-dev.jar agent8-dev $1 localhost 33338 $2 >> log_agent8.txt &
java -jar cachelet-dev.jar agent9-dev $1 localhost 33339 $2 >> log_agent9.txt &
java -jar cachelet-dev.jar agent10-dev $1 localhost 33340 $2 >> log_agent10.txt &
java -jar cachelet-dev.jar agent11-dev $1 localhost 33341 $2 >> log_agent11.txt &
java -jar cachelet-dev.jar agent12-dev $1 localhost 33342 $2 >> log_agent12.txt &
java -jar cachelet-dev.jar agent13-dev $1 localhost 33343 $2 >> log_agent13.txt &
java -jar cachelet-dev.jar agent14-dev $1 localhost 33344 $2 >> log_agent14.txt &
java -jar cachelet-dev.jar agent15-dev $1 localhost 33345 $2 >> log_agent15.txt &
java -jar cachelet-dev.jar agent16-dev $1 localhost 33346 $2 >> log_agent16.txt &
