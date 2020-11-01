package converter.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class BpmnTask {
	String id;
	String startId;
	TaskType type;
	String name;
	String documentation;
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
	
	public String getDocumentation() {
		return documentation;
	}
	
	public String getStartId() {
		return startId;
	}

	public void setStartId(String startId) {
		this.startId = startId;
	}

	public void setDocumentation(String doc) {	

		doc = doc.replace("<br />", "#nl;");
		System.out.println("***HTML DOC:" + doc);	
		
		StringReader in = new StringReader(doc);

		Html2Text parser = new Html2Text();
		try {
			parser.parse(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		in.close();
		

		// documentation = parser.getText().replace("#nl;", "\n").replace("&#64;", "@").replace("&#34;", "\"").replace(Character.toString((char)0xc2), " ").replace(Character.toString((char)0xa0), " ").replace("&#61;", "=").replace("<br />", "\n")
		documentation = parser.getText().replace("#nl;", "\n").replace(Character.toString((char)0xc2), " ").replace(Character.toString((char)0xa0), " ");
		System.out.println("***DOC:" + documentation);		
	}

	public String getIncomingId(int index) {
		return incomingIds.get(index);
	}	

	public void addIncomingId(String incomingId) {
		this.incomingIds.add(incomingId);
	}

	public List<String> getOutgoingIds() {
		return outgoingIds;
	}

	public String getOutgoingId(int index) {
		if (index > outgoingIds.size()-1) {
			return null;
		} else {
			return outgoingIds.get(index);
		}
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
