package scs.pojo;

public class HotspotMetadata {
	private String funcName;
	private int hotScore;
	
	public HotspotMetadata(String funcName, int hotScore) {
		super();
		this.funcName = funcName;
		this.hotScore = hotScore;
	}
	
	public String getFuncName() {
		return funcName;
	}

	public int getHotScore() {
		return hotScore;
	}

	@Override
	public String toString() {
		return String.format("Metadata [funcName=%s, hotScore=%s]", funcName, hotScore);
	}
}
