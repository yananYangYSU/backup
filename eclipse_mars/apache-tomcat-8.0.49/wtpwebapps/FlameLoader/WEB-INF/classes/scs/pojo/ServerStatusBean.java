package scs.pojo;

public class ServerStatusBean {
	private int serverHotscore;
	private int availMemory;
	private int serverHotscoreEviction;
	private int evictionMemory;
	
	public int getServerHotscore() {
		return serverHotscore;
	}
	public int getAvailMemory() {
		return availMemory;
	}
	public int getServerHotscoreEviction() {
		return serverHotscoreEviction;
	}
	public int getEvictionMemory() {
		return evictionMemory;
	}
	public void setServerHotscore(int serverHotscore) {
		this.serverHotscore = serverHotscore;
	}
	public void setAvailMemory(int availMemory) {
		this.availMemory = availMemory;
	}
	public void setServerHotscoreEviction(int serverHotscoreEviction) {
		this.serverHotscoreEviction = serverHotscoreEviction;
	}
	public void setEvictionMemory(int evictionMemory) {
		this.evictionMemory = evictionMemory;
	}
	
	@Override
	public String toString() {
		return String.format(
				"ServerStatusBean [serverHotscore=%s, availMemory=%s, serverHotscoreEviction=%s, evictionMemory=%s]",
				serverHotscore, availMemory, serverHotscoreEviction, evictionMemory);
	}
}
