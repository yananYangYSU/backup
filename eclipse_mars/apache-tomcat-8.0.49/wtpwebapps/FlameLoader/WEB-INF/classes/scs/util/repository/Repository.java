package scs.util.repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import scs.pojo.AgentBean;
import scs.pojo.FuncMetadataBean;
import scs.pojo.HotspotBean;
import scs.pojo.HotspotMetadata;
import scs.pojo.RequestBean;
import scs.pojo.ServerStatusBean;
import scs.pojo.LoaderDriver; 
import scs.pojo.QueryData;
import scs.pojo.ThreeTuple;
import scs.pojo.TwoTuple;
import scs.util.loadGen.driver.*;
import scs.util.rmi.RmiService;

/**
 * System static repository class
 * Provide memory storage in the form of static variables for data needed in system operation
 * Including some system parameters, application run data, control signs and so on
 * @author Yanan Yang
 *
 */
public class Repository{ 
	private static Repository repository=null;
	private Repository(){}
	public synchronized static Repository getInstance() {
		if (repository == null) {
			repository = new Repository();
		}
		return repository;
	}  
	/**
	 * flame 
	 */
	private static String flameFuncStr="";
	public static String flameFunctionBaseAPI="";
	public static String flameFunctionParm="";
	private static String funcScaleAPI="";
	private static String serverStatusReaderAPI="";
	private static String funcNamespace="";
	private static String loadBalanceRule="";
	public static String flameCacheletListStr="";
	private static String funcLevelOutputFile="";
	public static ArrayList<AgentBean> agentList=new ArrayList<AgentBean>();
	private static int totalRequestNum=0;
	private static int[] stepSizes=new int[4];
	public static Map<Integer,FuncMetadataBean> funcMetadataMap=new HashMap<Integer,FuncMetadataBean>();
	public static Map<Integer,LoaderDriver> loaderMap=new HashMap<Integer,LoaderDriver>();

	public static int NUMBER_LC=-1; //number of LC services
	public static List<ArrayList<Integer>> onlineDataList=new ArrayList<ArrayList<Integer>>();
	public static List<ArrayList<ThreeTuple<Integer,String,Timestamp>>> onlineDataListSpec=new ArrayList<ArrayList<ThreeTuple<Integer,String,Timestamp>>>();// <latency,html,collectTime>
	public static List<ArrayList<Integer>> tempOnlineDataList=new ArrayList<ArrayList<Integer>>();
	public static List<ArrayList<QueryData>> windowOnlineDataList=new ArrayList<ArrayList<QueryData>>();
	private static List<ArrayList<QueryData>> tempWindowOnlineDataList=new ArrayList<ArrayList<QueryData>>();


	public static String serverIp="";
	public static int rmiPort;

	public static int windowSize=60; //window size of latency recorder
	public static int recordInterval=1000; //record interval of latency recorder
	private static boolean rmiServiceEnable=false;


	private static Map<String,ArrayList<Integer>> funcInvocCountTimerMap=new HashMap<String,ArrayList<Integer>>();
	private static LinkedList<TwoTuple<String,Integer>> hotspotScoreSortedList=new LinkedList<TwoTuple<String, Integer>>();
	private static Map<String,TwoTuple<Integer,Boolean>> hotspotFuncMap=new HashMap<String, TwoTuple<Integer,Boolean>>(); 

	private final static long HOTSPOT_STATISTICS_INTERVAL=10*1000;//半小时
	private static float hotspotThreshold=1.0f;

	/**
	 * static code
	 */
	static {
		readProperties();
		initLoaderMap();
		initRecorderList();
		initCacheletAgentList();
		if(Repository.rmiServiceEnable==true)
			RmiService.getInstance().service(Repository.serverIp, Repository.rmiPort);//start the RMI service
	}

	/**
	 * System variables of online load generator module 
	 */
	public static boolean[] onlineQueryThreadRunning=new boolean[NUMBER_LC]; 
	public static boolean[] onlineDataFlag=new boolean[NUMBER_LC]; 
	public static boolean[] sendFlag=new boolean[NUMBER_LC]; 

	public static int[] realRequestIntensity=new int[NUMBER_LC]; 
	public static int[] realQueryIntensity=new int[NUMBER_LC];  
	private static int[] windowOnLineDataListCount=new int[NUMBER_LC];	
	public static int[] statisticsCount=new int[NUMBER_LC];	
	public static int[] totalRequestCount=new int[NUMBER_LC];
	public static int[] totalQueryCount=new int[NUMBER_LC];

	public static int[] concurrency=new int[NUMBER_LC];

	public static QueryData[] latestOnlineData=new QueryData[NUMBER_LC];
	public static float[] windowAvgPerSec99thQueryTime=new float[NUMBER_LC];
	public static float[] windowAvgPerSecAvgQueryTime=new float[NUMBER_LC];

	/**
	 * read properties 
	 */
	private static void readProperties(){
		Properties prop = new Properties();
		InputStream is = Repository.class.getResourceAsStream("/conf/sys.properties");
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Repository.windowSize=Integer.parseInt(prop.getProperty("windowSize").trim());
		Repository.serverIp=prop.getProperty("serverIp").trim();
		Repository.rmiPort=Integer.parseInt(prop.getProperty("rmiPort").trim()); //22222 default
		Repository.recordInterval=Integer.parseInt(prop.getProperty("recordInterval").trim()); 

		if(prop.getProperty("rmiServiceEnable")==null||prop.getProperty("rmiServiceEnable").equals("false")){
			rmiServiceEnable=false;
		}else{
			rmiServiceEnable=true;
		}

		// sdc web service
		Repository.flameFuncStr=prop.getProperty("flameFuncStr").trim();
		Repository.flameFunctionBaseAPI=prop.getProperty("flameFunctionBaseAPI").trim();
		Repository.flameFunctionParm=prop.getProperty("flameFunctionParm").trim();

		Repository.flameCacheletListStr=prop.getProperty("flameCacheletListStr").trim();
		Repository.funcLevelOutputFile=prop.getProperty("funcLevelOutputFile").trim();
		Repository.loadBalanceRule=prop.getProperty("loadBalanceRule").trim();
		Repository.funcScaleAPI=prop.getProperty("funcScaleAPI").trim();
		Repository.serverStatusReaderAPI=prop.getProperty("serverStatusReaderAPI").trim();

		Repository.funcNamespace=prop.getProperty("funcNamespace").trim();

		Repository.hotspotThreshold=Float.parseFloat(prop.getProperty("hotspotThreshold").trim());

	}
	/**
	 * init 
	 */
	private static void initLoaderMap(){
		String[] funcList=flameFuncStr.split("#");
		NUMBER_LC=funcList.length;
		for(String item:funcList){
			String[] splits=item.split("_");
			int funcIndex=Integer.parseInt(splits[0]);
			String funcName=splits[1];
			int funcMem=Integer.parseInt(splits[2]);
			funcMetadataMap.put(funcIndex,new FuncMetadataBean(funcName, funcMem, -1, -1, -1));
			System.out.println("repository: init funcMetadataMap, funcIndex="+funcIndex+",funcName="+funcName+", funMemory="+funcMem);
			loaderMap.put(funcIndex, new LoaderDriver(splits[1], new FuncInvokeDriver(funcIndex))); 
		}
	}
	private static void initRecorderList(){
		for(int i=0;i<NUMBER_LC;i++){
			onlineDataList.add(new ArrayList<Integer>());
			onlineDataListSpec.add(new ArrayList<ThreeTuple<Integer, String, Timestamp>>());
			tempOnlineDataList.add(new ArrayList<Integer>());
			tempWindowOnlineDataList.add(new ArrayList<QueryData>());
			windowOnlineDataList.add(new ArrayList<QueryData>());
		}
	}
	private static void initCacheletAgentList(){
		totalRequestNum=0; // Init

		String[] nodeSplits=flameCacheletListStr.split("#");
		int agentIndex=0;
		for(String item:nodeSplits){
			String[] splits=item.split("_");
			if(splits.length==3){
				AgentBean agent=new AgentBean();
				agent.setAgentName(splits[0]);
				agent.setIp(splits[1]);
				agent.setPort(Integer.parseInt(splits[2]));
				agent.setAgentIndex(agentIndex);
				agentList.add(agent);
				agentIndex++;
				System.out.println("Repository.initCacheletAgentList(): agent found "+agent.toString());
			}
		} 
		System.out.println("Repository.initCacheletAgentList(): agentList size="+agentList.size());

		stepSizes[0]=1;
		stepSizes[1]=3;
		stepSizes[2]=5;
		stepSizes[3]=7;

		System.out.println("Repository.initCacheletAgentList(): funcLevelOutputFile= "+funcLevelOutputFile);

		//		try {
		//			fileWriter=new FileWriter("D:\\cacheResearch\\workspace\\faascache\\rehash\\log_overall_memory.txt");
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
	}


	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private static long lastScheduleTimestamp=0;
	public static int dispatchRequest(RequestBean request,String url,String jsonObjectStr,String requestType) throws IOException{
		int[] response=new int[3];

		String funcName=request.getFuncMetadata().getFuncName();

		lock.writeLock().lock(); 
		try{
			registerFunc(funcName);//register function and add its invocation counter
			checkHotspotCounterUpdate(request.getArrivalTime());
			totalRequestNum++;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.writeLock().unlock(); //释放锁
		}

		if(Repository.loadBalanceRule.equals("round")){
			response=roundBobinDispatcher(request, url, jsonObjectStr, requestType);
		}else if(Repository.loadBalanceRule.equals("rehash")){
			response=rehashDispatcher(request, url, jsonObjectStr, requestType);
		}else if(Repository.loadBalanceRule.equals("random")){
			response=randomDispatcher(request, url, jsonObjectStr, requestType);
		}else{
			response=new int[]{502,-1,-1};
		}

		if(totalRequestNum%10000==0){
			//runtimeDisplay(); //faascache memory size runtime
		}

		if(response[0]==200){ //latency
			return response[1];
		}else if(response[0]==500){ // scale up
			if (System.currentTimeMillis()-lastScheduleTimestamp>5000){ // 调度冷却时间
				funcScaleAPI=funcScaleAPI.replace("{func}",funcName);
				funcScaleAPI=funcScaleAPI.replace("{namespace}",funcNamespace);
				System.out.println();
				System.out.println();
				System.out.println("repository.dispatchRequest(): scheduling start memory requirement="+request.getFuncMetadata().getMemoryConsume());
				int result=scheduleFunc(funcName,request.getFuncMetadata().getMemoryConsume(),funcScaleAPI,response[2]);
				System.out.println("repository.dispatchRequest(): scheduling finished");
				
				if(result==-1){ //只有调度失败才更新冷却时间戳，防止短时间内再次尝试调度
					lastScheduleTimestamp=System.currentTimeMillis();
					System.out.println("repository.dispatchRequest(): scheduling failed, update lastScheduleTimestamp="+lastScheduleTimestamp);
				}
			}else{
				System.out.println("repository.dispatchRequest(): scheduling failed, wait for 5s ...");
			}
			return 65536;
		}else if(response[0]==502){ // function does not exist
			return -1; 
		}else {
			return -1;
		}
	}
	/***
	 * hotspot计数器注册函数
	 * @param funcName
	 */
	private static int hotspotTimer=1; //must be 1
	private static void registerFunc(String funcName){
		if(!funcInvocCountTimerMap.containsKey(funcName)){
			ArrayList<Integer> list=new ArrayList<>();
			for(int i=0;i<hotspotTimer;i++){
				list.add(0);
			}
			funcInvocCountTimerMap.put(funcName,list);
		}
		updateLastHotspotCounter(funcName);
	}

	private static void updateLastHotspotCounter(String funcName){
		ArrayList<Integer> list=funcInvocCountTimerMap.get(funcName);
		int size=list.size();
		if(size==0){
			System.err.println("Repository.updateLastHotspotCounter(): error---------list is null");
			System.exit(0);
		}else{
			list.set(size-1,list.get(size-1)+1);
			//System.out.println("Repository.updateLastHotspotCounter(): funcName invocations="+list.get(size-1));
		}
	}

	private static long firstReqArrivalTime=-1;
	private static long offset=HOTSPOT_STATISTICS_INTERVAL*hotspotTimer;
	private static long lastHotspotFuncUpdateTimestamp=0L;
	private static void checkHotspotCounterUpdate(long arrivalTime){
		/**
		 * 检查更新计数器时间范围
		 */
		if(firstReqArrivalTime==-1){
			firstReqArrivalTime=arrivalTime;
		}else if(arrivalTime-firstReqArrivalTime>=offset){
			addNewHotspotCounter(funcInvocCountTimerMap);
			calculateHotspot(); //计算热点函数得分 这里面会更新hotspotFuncMap的元素

			lastHotspotFuncUpdateTimestamp=firstReqArrivalTime+offset;
			hotspotTimer++;
			offset=HOTSPOT_STATISTICS_INTERVAL*hotspotTimer;
		}
	}

	/**
	 * 检查Hotspot计数器是否需要新增一个计数器
	 * @param arrivalTime
	 */ 
	private static void addNewHotspotCounter(Map<String,ArrayList<Integer>> map){
		Set<String> keySet=map.keySet();
		for(String key:keySet){
			map.get(key).add(0);
		}
	}

	/**
	 * 计算热点函数计数器得分,采用衰减计数方法进行计算 func-level
	 */
	private static void calculateHotspot(){
		//System.out.println("Repository.calculateHotspot(): start------------------");

		hotspotScoreSortedList.clear(); //初始化
		hotspotScoreSortedList.add(new TwoTuple<String,Integer>("virtual", -1));//添加一个虚拟元素
		int sum=0;
		/**
		 * 衰减计数法 计算热点得分
		 */
		Set<String> funcNameSet=funcInvocCountTimerMap.keySet();
		for(String funcName:funcNameSet){
			int score=0;
			//System.out.println("Repository.calculateHotspot(): funcName="+funcName);
			int size=funcInvocCountTimerMap.get(funcName).size();
			for(int item:funcInvocCountTimerMap.get(funcName)){
				size--;
				score=score+(item>>size);
				//System.out.println("Repository.calculateHotspot(): size="+size+" item="+item+" score="+score);
			}
			/**
			 * score排序并插入到linkedList
			 */
			sum+=score;
			for(int i=0;i<hotspotScoreSortedList.size();i++){
				if(score>=hotspotScoreSortedList.get(i).second){ //第一个元素是虚拟元素，一定比真实得分小
					hotspotScoreSortedList.add(i, new TwoTuple<String,Integer>(funcName,score)); //从大到小排列 插入元素
					break;
				}
			}
		}

		/**
		 * 从高到底排序
		 */
		hotspotFuncMap.clear();
		int threshold=(int)(sum*1.0f*hotspotThreshold);
		sum=0;
		for(TwoTuple<String,Integer> item:hotspotScoreSortedList){
			hotspotFuncMap.put(item.first,new TwoTuple<Integer,Boolean>(item.second,true)); //true代表是热点函数<funcName,<score,true/false>>
			sum+=item.second;
			if(sum<=threshold){ //函数按照调用量从大到小，累积贡献请求数超过总体的x%,这个列表里的函数就定义为热点函数
				//System.out.println("Repository.calculateHotspot(): "+item.first+","+item.second+",true"+"++++++++++");
				continue;
			}else{
				break;
			}
		}
		/**
		 * 处理剩余的
		 */
		for(TwoTuple<String,Integer> item:hotspotScoreSortedList){
			if(!hotspotFuncMap.containsKey(item.first)){
				hotspotFuncMap.put(item.first,new TwoTuple<Integer,Boolean>(item.second,false)); //false代表不是热点函数
				//System.out.println("Repository.calculateHotspot(): "+item.first+","+item.second+",false"+"--------");
			}
		}
	}

	/**
	 * 热度最小原则进行调度
	 * @param funcMemory
	 * @param arrivalTime
	 * @return
	 */
	private static int scheduleFunc(String funcName,int funcMemory,String funcScaleAPI,int coldstartOccurNode){
		//先判断节点是否有可用实例，没有可用实例的话，触发调度：
		//                     （1）看本地是否有足够空闲资源(不驱逐)，
		//                           如果有则创建。如果本地没有足够空闲资源，则（2）尝试全局查找有空闲资源的得分最大节点调度（不驱逐）。
		//		                                                                               如果找到节点则创建。否则，（3）本地尝试创建（可驱逐）,如果有足够资源则创建；否则（4）全局搜索（可驱逐）
		Map<Integer,ServerStatusBean> statusMap=new HashMap<Integer,ServerStatusBean>();;
		ServerStatusBean bean=null;
		int candidator=-1;
		int maxScore=0;
		if(agentList.size()>coldstartOccurNode){
			bean=agentList.get(coldstartOccurNode).getServerHotspotStatus(serverStatusReaderAPI,"GET"); 
			statusMap.put(coldstartOccurNode,bean);
			if(bean.getAvailMemory()>=funcMemory){ //看本地是否有足够空闲资源(不驱逐)，
				System.out.println("Repository.scheduleFunc(): (1)local has available memory, agentIndex=" +coldstartOccurNode+" memory="+statusMap.get(coldstartOccurNode).getAvailMemory()+" funcMem="+funcMemory);
				agentList.get(coldstartOccurNode).coldstartScale(funcScaleAPI,"GET");
				candidator=coldstartOccurNode;
			}else{
				

				//如果有则创建。如果本地没有足够空闲资源，则（2）尝试全局查找有空闲资源的得分最大节点调度（不驱逐）  如果找到节点则创建
				maxScore=0;
				candidator=-1;
				for(AgentBean agent:agentList){
					if(agent.getAgentIndex()==coldstartOccurNode){
						continue;
					}
					bean=agent.getServerHotspotStatus(serverStatusReaderAPI,"GET"); 
					statusMap.put(agent.getAgentIndex(),bean);
					if(bean.getAvailMemory()>=funcMemory){
						if(bean.getServerHotscore()>maxScore){
							maxScore=bean.getServerHotscore();
							candidator=agent.getAgentIndex();
							System.out.println("(2) maxScore="+maxScore + "candidator="+candidator);
						}
					} 
				}
				if(candidator!=-1){ //如果找到节点则创建
					System.out.println("Repository.scheduleFunc(): (2)remote has available memory, agentIndex=" +candidator+" memory="+statusMap.get(candidator).getAvailMemory()+" funcMem="+funcMemory);
					agentList.get(candidator).coldstartScale(funcScaleAPI,"GET");
				}else{ //否则，（3）本地尝试创建（可驱逐）,如果有足够资源则创建；
					if(statusMap.get(coldstartOccurNode).getEvictionMemory()>=funcMemory){ 
						System.out.println("Repository.scheduleFunc(): (3)local has available eviction memory, agentIndex=" +coldstartOccurNode+" memory="+statusMap.get(coldstartOccurNode).getEvictionMemory()+" funcMem="+funcMemory);
						agentList.get(coldstartOccurNode).coldstartScale(funcScaleAPI,"GET");
						candidator=coldstartOccurNode;
					}else{
						//否则（4）全局搜索（可驱逐）
						maxScore=0;
						candidator=-1;
						for(AgentBean agent:agentList){
							if(agent.getAgentIndex()==coldstartOccurNode){
								continue;
							}
							bean=statusMap.get(agent.getAgentIndex()); 
							if(bean.getEvictionMemory()>=funcMemory){
								if(bean.getServerHotscoreEviction()>maxScore){
									maxScore=bean.getServerHotscoreEviction();
									candidator=agent.getAgentIndex();
									System.out.println("(4) maxScore="+maxScore + "candidator="+candidator);
								}
							} 
						}
						if(candidator!=-1){
							System.out.println("Repository.scheduleFunc(): (4)remote has available eviction memory, agentIndex=" +candidator+" memory="+statusMap.get(candidator).getEvictionMemory()+" funcMem="+funcMemory);
							agentList.get(candidator).coldstartScale(funcScaleAPI,"GET");
						}
					}
				}
			}
		}

		if(candidator==-1){
			System.out.println("Repository.scheduleFunc(): unable to find server to schedule function "+funcName);
		}else{
			System.out.println("Repository.scheduleFunc(): find server "+agentList.get(candidator).toString()+" to schedule function "+funcName);
		}
		return candidator;
	}

	/**
	 * 获取热点函数的列表，更新时间戳，封装成对象
	 * @return HotspotBean
	 */
	public static HotspotBean getHotspotFuncMetadta(){
		HotspotBean bean=null;
		List<HotspotMetadata> list=new ArrayList<HotspotMetadata>();
		synchronized (Repository.hotspotFuncMap){ //<funcName,<score,true/false>>
			Set<String> keyset=hotspotFuncMap.keySet();
			for(String key:keyset){
				if(hotspotFuncMap.get(key).second==true){ // is hotspot function
					HotspotMetadata data=new HotspotMetadata(key, hotspotFuncMap.get(key).first);
					list.add(data);
				}
			}
			bean=new HotspotBean(list,lastHotspotFuncUpdateTimestamp);
		}
		return bean;
	}

	/**
	 * Adds a new data to the window array
	 * Loop assignment in Repository.windowSize
	 * @param data
	 */
	public void addWindowOnlineDataList(QueryData data, int serviceId){
		latestOnlineData[serviceId]=data;
		realQueryIntensity[serviceId]=data.getRealQps();
		synchronized (windowOnlineDataList.get(serviceId)) {
			if(windowOnlineDataList.get(serviceId).size()<windowSize){
				windowOnlineDataList.get(serviceId).add(data);
			}else{
				windowOnlineDataList.get(serviceId).set(windowOnLineDataListCount[serviceId]%windowSize,data);
				windowOnLineDataListCount[serviceId]++;
			}
		}
	}

	/**
	 * Calculate the mean of query time
	 * @return 
	 */
	public float[] getOnlineWindowAvgQueryTime(int serviceId){
		while (windowOnlineDataList.get(serviceId).isEmpty()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		tempWindowOnlineDataList.get(serviceId).clear();
		synchronized (windowOnlineDataList.get(serviceId)) {
			tempWindowOnlineDataList.get(serviceId).addAll(windowOnlineDataList.get(serviceId));
		}
		int size=tempWindowOnlineDataList.get(serviceId).size();
		float avg99thQueryTime=0;
		float avgAvgQueryTime=0;
		for(QueryData item:tempWindowOnlineDataList.get(serviceId)){
			avg99thQueryTime+=item.getQueryTime99th();
			avgAvgQueryTime+=item.getQueryTimeAvg();
		} 
		avg99thQueryTime=avg99thQueryTime/size; 
		avgAvgQueryTime=avgAvgQueryTime/size;
		windowAvgPerSec99thQueryTime[serviceId]=avg99thQueryTime;
		windowAvgPerSecAvgQueryTime[serviceId]=avgAvgQueryTime;

		return new float[]{avg99thQueryTime,avgAvgQueryTime};
	}
	/**
	 * maps the loaderIndex with the loaderDriver instance
	 * @param loaderIndex
	 * @return
	 */


	/**
	 * 
	 * @param request
	 * @param pattern
	 * @return latency
	 * @throws RemoteException
	 */
	/*private static void runtimeDisplay() throws IOException{
		System.out.println(totalRequestNum);
		for(int i=0;i<agentList.size();i++){
				int memory=agentList.get(i).serverMemDisplay();
				fileWriter.write(memory+",");
				//System.out.println(bean.toString());
			}
			fileWriter.write("\n");
			fileWriter.flush();
	}*/
	/***
	 * 
	 * @param filePath 输出文件路径
	 * @param childAgentCount 子节点的数量 our work是中心式 为8
	 * @return [sloViolationRate,overallMemoryUsageRate]
	 * @throws IOException
	 */
	/*public static float[] display(String filePath, int childAgentCount) throws IOException{
		float sloViolationRate=1.0f;
		float overallMemoryUsage=0;
		ArrayList<Float> agentMemoryUsageList=new ArrayList<Float>();

		List<StatisticsBean> agentResultList=new ArrayList<StatisticsBean>();
		for(int i=0;i<agentList.size();i++){
			for(int j=0;j<childAgentCount;j++){
				StatisticsBean bean=agentList.get(i).display(j);
				agentResultList.add(bean);
				//System.out.println(bean.toString());
			}
		}


		// func level (per function)
		FileWriter file2=null;
		if(filePath!=null&&!filePath.equals("")){
			file2=new FileWriter(funcLevelOutputFile);
		}
		Map<String, FuncInstBean> map=new HashMap<String, FuncInstBean>();
		for(StatisticsBean bean:agentResultList){
			float tempAgentMemUsage=0;
			Map<String, FuncInstBean> tempMap=bean.getFuncLevelStatistics();
			Set<String> keyset=tempMap.keySet();
			for(String key:keyset){
				if(!map.containsKey(key)){
					map.put(key, tempMap.get(key));
				}else{
					map.get(key).addReceivedReqCount(tempMap.get(key).getReceivedReqCount());// =warm+cold+drop
					map.get(key).addWarmStartCount(tempMap.get(key).getWarmStartCount());
					map.get(key).addColdStartCount(tempMap.get(key).getColdStartCount());
					map.get(key).addDropCount(tempMap.get(key).getDropCount());
					map.get(key).addRedirectedCount(tempMap.get(key).getRedirectedCount());
					map.get(key).addEvictionCount(tempMap.get(key).getEvictionCount());
					map.get(key).addReleaseCount(tempMap.get(key).getReleaseCount());
					map.get(key).addMemoryTimespanCost(tempMap.get(key).getMemoryTimespanCost());
				}
				tempAgentMemUsage+=tempMap.get(key).getMemoryTimespanCost();
			}
			agentMemoryUsageList.add(tempAgentMemUsage);
		}

		// memoryTime
		//System.out.println("function level statistics");
		StringBuilder result=new StringBuilder();
		result.append("funcName,receivedReqCount,funcWarmCount,funcColdCount,funcDropCount,funcRedirectedCount,funcEvictionCount,funcReleaseCount,totalMemoryCost\n");
		Set<String> keySet=map.keySet();
		for(String funcName:keySet){
			result.append(funcName).append(",");
			result.append(map.get(funcName).getReceivedReqCount()).append(",");
			result.append(map.get(funcName).getWarmStartCount()).append(",");
			result.append(map.get(funcName).getColdStartCount()).append(",");
			result.append(map.get(funcName).getDropCount()).append(",");
			result.append(map.get(funcName).getRedirectedCount()).append(",");
			result.append(map.get(funcName).getEvictionCount()).append(",");
			result.append(map.get(funcName).getReleaseCount()).append(",");
			result.append(map.get(funcName).getMemoryTimespanCost()); 
			overallMemoryUsage+=map.get(funcName).getMemoryTimespanCost();
			result.append("\n");
		}  
		//System.out.println(result.toString());
		file2.write(result.toString());
		file2.flush();
		file2.close();


		// overall level (per node)
		FileWriter file=null;
		if(filePath!=null&&!filePath.equals("")){
			file=new FileWriter(filePath);
		}

		//System.out.println();
		String title="serverName"+
				","+"FinalDeployedFunCount"+
				","+"FinalDeployedInstanceCount"+
				","+"TotalEvictionCount"+
				","+"TotalReleaseCount"+
				","+"MemorySize"+
				","+"MemoryUsageRate"+
				","+"ReceivedRequestCount"+ //=warm+cold+drop+redirected
				","+"RedirectedCount"+
				","+"WarmStartCount"+
				","+"ColdStartCount"+
				","+"DropCount"+
				","+"SloViolationRate";
		//System.out.println(title);
		if(file!=null){
			file.write(title+"\r\n");
		}
		int totalDeployedFuncCount=0;
		int totalDeployedInstanceCount=0;
		int totalColdstartCount=0;
		int totalWarmstartCount=0;
		int totalEvictionCount=0;
		int totalReleaseCount=0;
		int totalReceivedRequestCount=0; 
		int totalRedirectedCount=0;
		int totalDropCount=0;
		int totalAvailMemoryUsage=0;
		int totalMemorySize=0;
		int i=0;
		for(StatisticsBean bean:agentResultList){
			totalDeployedFuncCount+=bean.getDeployedFunCount();
			totalDeployedInstanceCount+=bean.getDeployedInstanceCount();
			totalEvictionCount+=bean.getTotalEvictionCount();
			totalReleaseCount+=bean.getTotalReleaseCount();
			totalWarmstartCount+=bean.getTotalWarmstartCount();
			totalColdstartCount+=bean.getTotalColdstartCount();
			totalReceivedRequestCount+=bean.getTotalRequestCount();
			totalRedirectedCount+=bean.getTotalRedirectedCount();
			totalDropCount+=bean.getTotalDropCount();
			//totalAvailMemoryUsage+=bean.getAvailMemorySize();
			totalMemorySize+=bean.getMemorySize();
			String row=
					bean.getAgentName()+
					","+bean.getDeployedFunCount()+
					","+bean.getDeployedInstanceCount()+
					","+bean.getTotalEvictionCount()+
					","+bean.getTotalReleaseCount()+
					","+bean.getMemorySize()+
					//","+bean.getMemoryUsageRate()+
					","+agentMemoryUsageList.get(i)+
					","+bean.getTotalRequestCount()+
					","+bean.getTotalRedirectedCount()+
					","+bean.getTotalWarmstartCount()+
					","+bean.getTotalColdstartCount()+
					","+bean.getTotalDropCount()+
					","+bean.getSloViolationRate();
			i++;
			//System.out.println(row);
			if(file!=null){
				file.write(row+"\r\n");
			}
		}

		sloViolationRate=1-1.0f*totalWarmstartCount/(totalWarmstartCount+totalColdstartCount+totalDropCount);

		String bottom=
				"overall"+
						","+totalDeployedFuncCount+
						","+totalDeployedInstanceCount+
						","+totalEvictionCount+
						","+totalReleaseCount+
						","+totalMemorySize+
						","+overallMemoryUsage+
						","+totalReceivedRequestCount+ 
						","+totalRedirectedCount+
						","+totalWarmstartCount+
						","+totalColdstartCount+
						","+totalDropCount+
						","+sloViolationRate;

		//System.out.println(bottom);
		if(file!=null){
			file.write(bottom+"\r\n");
			file.flush();
			file.close();
		}

		float[] res=new float[2];
		res[0]=sloViolationRate;
		res[1]=overallMemoryUsage;
		return res;
	}
	 */



	/*private static int roundDispatcher(){
		int agentIndex = 0;
		if(!agentList.isEmpty()){
			agentIndex=totalRequestNum%agentList.size();
		}else{
			System.out.println("repository.roundDispatcher(): reqDispatch error");
		}
		totalRequestNum++;
		return agentIndex;
	}

	private static int hashDispatcher(String funcName){
		int agentIndex = 0;
		if(!agentList.isEmpty()){
			//System.out.println((funcName.hashCode()&Integer.MAX_VALUE)%arraySize);
			agentIndex=(funcName.hashCode()&Integer.MAX_VALUE)%agentList.size();
		}else{
			System.out.println("repository.hashDispatcher(): reqDispatch error");
		}
		totalRequestNum++;
		return agentIndex;
	}*/

	/**
	 * 
	 * @param request
	 * @param url
	 * @param jsonObjectStr
	 * @param requestType
	 * @return int[3]{status code, latency, agentIndex} 转发请求 502函数不存在，500冷启动，200正常处理
	 * @throws RemoteException
	 */
	private static Random random=new Random(189984971L);
	private static int[] randomDispatcher(RequestBean request, String url, String jsonObjectStr, String requestType) throws RemoteException{
		int agentIndex = -1;
		int tryTimes=1;
		//ThreeTuple<String, Integer, Integer> response = new ThreeTuple<String, Integer, Integer>("",-1,-1);
		if(!agentList.isEmpty()){
			int offset=1;
			agentIndex=random.nextInt(agentList.size());
			int[] result=Repository.agentList.get(agentIndex).probe(url,jsonObjectStr,requestType);
			//System.out.println("repository.reRoundDispatcher(): funcName="+funcName+" firstAgentIndex="+agentIndex+" redirectFlag="+redirectFlag+"  ----------------------------");
			while(result[0]==500&&tryTimes>0){
				agentIndex=(agentIndex+offset)%agentList.size();
				result=Repository.agentList.get(agentIndex).probe(url,jsonObjectStr,requestType);
				tryTimes--;
				//System.out.println("repository.reRoundDispatcher(): tryTimes="+tryTimes+" redirectFlag="+redirectFlag+" new agentIndex="+agentIndex);
			}
			/*if(tryTimes==0){
				System.out.println("repository.reRoundDispatcher(): tryTimes out ++++++++++++++++");
			}else{
				System.out.println("repository.reRoundDispatcher(): fount it ++++++++++++++++");
			}*/
			return new int[]{result[0],result[1],agentIndex};
		}else{
			System.out.println("repository.randomDispatcher(): reqDispatch rehash error");
			return new int[]{502,65535,agentIndex}; 
		}
	}
	/**
	 * 
	 * @param request
	 * @param url
	 * @param jsonObjectStr
	 * @param requestType
	 * @return int[3]{status code, latency, agentIndex} 转发请求 502函数不存在，500冷启动，200正常处理
	 * @throws RemoteException
	 */
	private static int[] roundBobinDispatcher(RequestBean request, String url, String jsonObjectStr, String requestType) throws RemoteException{
		int agentIndex = -1;
		int tryTimes=1;

		//TwoTuple<Integer, Integer> response = new TwoTuple<Integer, Integer>(-1,-1);
		if(!agentList.isEmpty()){
			int offset=1;
			lock.readLock().lock(); 
			try{
				agentIndex=totalRequestNum%agentList.size();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				lock.readLock().unlock();   //释放锁
			}

			int[] result=Repository.agentList.get(agentIndex).probe(url,jsonObjectStr,requestType);
			//System.out.println("repository.reRoundDispatcher(): funcName="+funcName+" firstAgentIndex="+agentIndex+" redirectFlag="+redirectFlag+"  ----------------------------");
			while(result[0]==500&&tryTimes>0){
				agentIndex=(agentIndex+offset)%agentList.size();
				result=Repository.agentList.get(agentIndex).probe(url,jsonObjectStr,requestType);
				tryTimes--;
				//System.out.println("repository.reRoundDispatcher(): tryTimes="+tryTimes+" redirectFlag="+redirectFlag+" new agentIndex="+agentIndex);
			}
			/*if(tryTimes==0){
				System.out.println("repository.reRoundDispatcher(): tryTimes out ++++++++++++++++");
			}else{
				System.out.println("repository.reRoundDispatcher(): fount it ++++++++++++++++");
			}*/

			return new int[]{result[0],result[1],agentIndex};
		}else{
			System.out.println("repository.roundBobinDispatcher(): reqDispatch rehash error");
			return new int[]{502,65535,agentIndex}; 
		}
	}

	/**
	 * 
	 * @param request
	 * @param url
	 * @param jsonObjectStr
	 * @param requestType
	 * @return int[3]{status code, latency, agentIndex} 转发请求 502函数不存在，500冷启动，200正常处理
	 * @throws RemoteException
	 */
	private static int[] rehashDispatcher(RequestBean request, String url, String jsonObjectStr, String requestType) throws RemoteException{
		int agentIndex = -1;
		int tryTimes=1;
		//TwoTuple<Integer, Integer> response = new TwoTuple<Integer, Integer>(-1,-1);
		String funcName=request.getFuncMetadata().getFuncName();

		if(!agentList.isEmpty()){
			//System.out.println("stepSizes.length="+stepSizes.length);
			int hashIndex=(funcName.hashCode()&Integer.MAX_VALUE)%stepSizes.length;
			//System.out.println("hashIndex "+hashIndex);
			int offset=stepSizes[hashIndex]; //each function has an offset

			agentIndex=(funcName.hashCode()&Integer.MAX_VALUE)%agentList.size();
			int[] result=Repository.agentList.get(agentIndex).probe(url,jsonObjectStr,requestType);
			//System.out.println("repository.rehashDispatcher(): funcName="+funcName+" firstAgentIndex="+agentIndex+" redirectFlag="+redirectFlag+"  ----------------------------");
			while(result[0]==500&&tryTimes>0){
				agentIndex=(agentIndex+offset)%agentList.size();
				result=Repository.agentList.get(agentIndex).probe(url,jsonObjectStr,requestType);
				tryTimes--;
				//System.out.println("repository.rehashDispatcher(): tryTimes="+tryTimes+" redirectFlag="+redirectFlag+" new agentIndex="+agentIndex);
			}
			/*if(tryTimes==0){
				System.out.println("repository.rehashDispatcher(): tryTimes out ++++++++++++++++");
			}else{
				System.out.println("repository.rehashDispatcher(): fount it ++++++++++++++++");
			}*/
			return new int[]{result[0],result[1],agentIndex};
		}else{
			System.out.println("repository.rehashDispatcher(): reqDispatch rehash error");
			return new int[]{502,65535,agentIndex}; 
		}
	}

	/*final static float load_upper_bound=0.6f;
	final static float max_load_upper_bound=0.9f;
	final static int max_chain_len=3;
	private static int CH_RLU_Dispatcher(RequestBean request) throws RemoteException {
		int agentIndex = -1;
		boolean redirectFlag=true;
		int tryTimes=max_chain_len;

		int minServerLoadAgentIndex=-1;
		float minServerLoad=1.0f;

		String funcName=request.getFuncMetadata().getFuncName();
		if(!agentList.isEmpty()){
			int offset=1;
			agentIndex=(funcName.hashCode()&Integer.MAX_VALUE)%agentList.size();
			//System.out.println(agentIndex);
			TwoTuple<Integer,Float> serverLoad=Repository.agentList.get(agentIndex).calServerLoad(request.getArrivalTime());
			float coldstartPenalty=1+request.getFuncMetadata().getStartUpTime()*1.0f/request.getFuncMetadata().getExecutionTime();
			float minThreshold=coldstartPenalty*load_upper_bound<max_load_upper_bound?coldstartPenalty*load_upper_bound:max_load_upper_bound;
			if(serverLoad.second<minThreshold){
				redirectFlag=false;
			}else{
				if(serverLoad.second<minServerLoad){
					minServerLoad=serverLoad.second;
					minServerLoadAgentIndex=agentIndex;
				}
			}

			while(redirectFlag&&tryTimes>0){
				agentIndex=(agentIndex+offset)%agentList.size();
				serverLoad=Repository.agentList.get(agentIndex).calServerLoad(request.getArrivalTime());
				System.out.println("redict->"+agentIndex);
				if(serverLoad.second<minThreshold){
					redirectFlag=false;
				}else{
					if(serverLoad.second<minServerLoad){
						minServerLoad=serverLoad.second;
						minServerLoadAgentIndex=agentIndex;
					}
					tryTimes--;
				}
			}
		}else{
			System.out.println("repository.CHRLU-Dispatcher(): reqDispatch error, agentList is null");
		}
		agentIndex=agentIndex==-1?minServerLoadAgentIndex:agentIndex; // return least-loaded server
		return agentIndex;
	} */

}

