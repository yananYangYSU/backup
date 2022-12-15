cd D:/cacheResearch/workspace
rm log_agent*txt
java -jar faascacheAgent.jar agent1 $1 localhost 22221 >> log_agent1.txt&
java -jar faascacheAgent.jar agent2 $1 localhost 22222 >> log_agent2.txt&
java -jar faascacheAgent.jar agent3 $1 localhost 22223 >> log_agent3.txt&
java -jar faascacheAgent.jar agent4 $1 localhost 22224 >> log_agent4.txt&
java -jar faascacheAgent.jar agent5 $1 localhost 22225 >> log_agent5.txt&
java -jar faascacheAgent.jar agent6 $1 localhost 22226 >> log_agent6.txt&
java -jar faascacheAgent.jar agent7 $1 localhost 22227 >> log_agent7.txt&
java -jar faascacheAgent.jar agent8 $1 localhost 22228 >> log_agent8.txt&
