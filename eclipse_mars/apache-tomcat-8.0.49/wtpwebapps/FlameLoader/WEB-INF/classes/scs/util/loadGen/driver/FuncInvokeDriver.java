package scs.util.loadGen.driver;
  
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import scs.pojo.RequestBean;
import scs.util.loadGen.driver.AbstractJobDriver;
import scs.util.loadGen.threads.LoadExecThread;
import scs.util.repository.Repository;
/**
 * Image recognition service request class
 * GPU inference
 * @author Yanan Yang
 *
 */
public class FuncInvokeDriver extends AbstractJobDriver{
	/**
	 * Singleton code block
	 */
	private Random random=new Random();
	private int serviceId;
	private String funcName;
	private int funcMemory;
	
	
	public FuncInvokeDriver(int serviceId){
		this.serviceId=serviceId;
		initVariables();
	}
 
	@Override
	protected void initVariables(){
		funcName=Repository.funcMetadataMap.get(serviceId).getFuncName();
		funcMemory=Repository.funcMetadataMap.get(serviceId).getMemoryConsume();
		queryItemsStr=Repository.flameFunctionBaseAPI;
		queryItemsStr=queryItemsStr.replace("{func}",funcName);
		jsonParmStr=Repository.flameFunctionParm;
		
		System.out.println("FuncInvokeDriver.initVariables(): queryItemsStr="+queryItemsStr+" jsonParmStr="+jsonParmStr+" funcName="+funcName+" funMemory="+funcMemory);
	}

	/**
	 * using countDown to send requests in open-loop
	 */
	public void executeJob(int serviceId){
		ExecutorService executor = Executors.newCachedThreadPool();
		Repository.onlineQueryThreadRunning[serviceId]=true;
		Repository.sendFlag[serviceId]=true;
		while(Repository.onlineDataFlag[serviceId]==true){
			if(Repository.sendFlag[serviceId]==true&&Repository.realRequestIntensity[serviceId]>0){
				CountDownLatch begin=new CountDownLatch(1);
				long currentTimestamp=System.currentTimeMillis();
				int sleepUnit=1000/Repository.realRequestIntensity[serviceId];
				for (int i=0;i<Repository.realRequestIntensity[serviceId];i++){
					RequestBean request=new RequestBean(funcName,currentTimestamp+sleepUnit*i,funcMemory,-1,-1);
					executor.execute(new LoadExecThread(request,queryItemsStr,begin,serviceId,jsonParmStr.replace("{parm}",Integer.toString(random.nextInt(99999))),sleepUnit*i,"POST"));
				}
				Repository.sendFlag[serviceId]=false;
				Repository.totalRequestCount[serviceId]+=Repository.realRequestIntensity[serviceId];
				begin.countDown();
			}else{
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//System.out.println("loader watting "+TestRepository.list.size());
			}
		}
		executor.shutdown();
		while(!executor.isTerminated()){
			try {
				Thread.sleep(2000);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}  
		Repository.onlineQueryThreadRunning[serviceId]=false; 
	}
}