cd D:/cacheResearch/workspace
rm log_agent*txt
#System.out.println("[agentName, memorySize(int), ip, port, keepaliveTime(ms)]"); 
java -jar cachelet-dev.jar agent1-dev $1 localhost 33331 $2 >> log_agent1.txt &
java -jar cachelet-dev.jar agent2-dev $1 localhost 33332 $2 >> log_agent2.txt &
java -jar cachelet-dev.jar agent3-dev $1 localhost 33333 $2 >> log_agent3.txt &
java -jar cachelet-dev.jar agent4-dev $1 localhost 33334 $2 >> log_agent4.txt &
