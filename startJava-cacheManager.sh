cd D:/cacheResearch/workspace
rm manager.txt
#System.out.println("[cacheletCount(int+), portStart(int+), cacheletName(string), hotspot_threshold(0.0f), awareDispatcherFlag(true/false)]");
java -jar cacheManager-dev.jar 32 33331 agent true 0.5 >> manager.txt  &
