cd D:/cacheResearch/workspace
rm log_agent*txt
#System.out.println("[agentName, memorySize(int), ip, port, keepaliveTime(ms)]"); 
java -jar cachelet-dev.jar agent1-dev $1 localhost 33331 $2 >> log_agent1.txt &
