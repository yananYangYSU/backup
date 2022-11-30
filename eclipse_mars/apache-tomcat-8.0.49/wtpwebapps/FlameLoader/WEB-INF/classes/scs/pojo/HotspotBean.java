package scs.pojo;

import java.util.List;

public class HotspotBean {
	private List<HotspotMetadata> result;
	private long lastUpdateTimestamp;
	
	public HotspotBean(List<HotspotMetadata> result, long lastUpdateTimestamp) {
		super();
		this.result = result;
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	public List<HotspotMetadata> getResult() {
		return result;
	}

	public long getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HotspotBean [result=").append(result).append(", lastUpdateTimestamp=")
				.append(lastUpdateTimestamp).append("]");
		return builder.toString();
	}
}

