package converter.wdg.ibm.com;

import java.util.ArrayList;
import java.util.List;

public class BpmnTask {
	String id;
	TaskType type;
	String name;
    List<String> incomingIds = new ArrayList<String>();
    List<String> outgoingIds = new ArrayList<String>();
    
	public String getId() {
		return id;
	}
	
	public TaskType getType() {
		return type;
	}
	public void setType(TaskType type) {
		this.type = type;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getIncomingId(int index) {
		return incomingIds.get(index);
	}	

	public void addIncomingId(String incomingId) {
		this.incomingIds.add(incomingId);
	}

	public String getOutgoingId(int index) {
		return outgoingIds.get(index);
	}

	public void addOutgoingId(String outgoingId) {
		this.outgoingIds.add(outgoingId);
	}

	@Override
	public String toString() {
		return "BpmnTask [id=" + id + ", type=" + type + ", name=" + name + ", incomingIds=" + incomingIds
				+ ", outgoingIds=" + outgoingIds + "]";
	}


	
	
}
