package scs.pojo;

import org.apache.http.impl.client.CloseableHttpClient;
import scs.util.tools.HttpClientPool;

public class AgentBean {
	private String agentName;
	private int agentIndex;
	private String ip;
	private int port;
	private CloseableHttpClient httpClient;
	
	public AgentBean(String agentName, int agentIndex, String ip, int port, CloseableHttpClient httpClient) {
		super();
		this.agentName = agentName;
		this.agentIndex = agentIndex;
		this.ip = ip;
		this.port = port;
		this.httpClient = HttpClientPool.getInstance().getConnection();
	}
	public AgentBean() {
		this.httpClient = HttpClientPool.getInstance().getConnection();
	}
	
	public String getAgentName() {
		return agentName;
	}
	public int getAgentIndex() {
		return agentIndex;
	}
	public String getIp() {
		return ip;
	}
	public int getPort() {
		return port;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public void setAgentIndex(int agentIndex) {
		this.agentIndex = agentIndex;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public int serverMemDisplay(){
		//send http request to gateway, return the server's memory status
		return 0;
	}
	
	public StatisticsBean display(int agentIndex){
		//send http request to gateway, return the server's statistics data
		return new StatisticsBean();
	}
	
	public ServerStatusBean getServerHotspotStatus(String url,String requestType){
		//send http request to gateway, return the server's statistics data
		url=url.replace("{host}",ip+":"+port);
		ServerStatusBean bean=HttpClientPool.getRequestEntitySim(httpClient,url);
		System.out.println("AgentBean.java: agentIndex+"+agentIndex+" "+bean.toString());
		return bean;
	}
	
	/**
	 * 
	 * @param url
	 * @param jsonObjectStr
	 * @param requestType
	 * @return int[2]{status code, response time}
	 */
	public int[] probe(String url,String jsonObjectStr,String requestType){
		// send http request for execution, cache miss -1;
		url=url.replace("{host}",ip+":"+port);
		int[] result=HttpClientPool.sendRequest(httpClient,url,jsonObjectStr, requestType);
		//System.out.println("AgentBean: invoke function url "+ url +" status code="+result[0]+" time cost="+result[1]);
		return result;
	}
	
	public int[] coldstartScale(String url,String requestType){
		url=url.replace("{host}",ip+":"+port);
		int[] result=HttpClientPool.sendRequest(httpClient, url, "", requestType);
		//System.out.println("AgentBean: scale function url "+ url +" status code="+result[0]+" time cost="+result[1]);
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentBean [agentName=");
		builder.append(agentName);
		builder.append("AgentBean [agentIndex=");
		builder.append(agentIndex);
		builder.append(", ip=");
		builder.append(ip);
		builder.append(", port=");
		builder.append(port);
		builder.append("]");
		return builder.toString();
	}
	
}
