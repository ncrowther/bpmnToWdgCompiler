package converter.wdg.ibm.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

public class CodeConverter {
	
	private static Map<String, BpmnTask> taskMap = new HashMap<String, BpmnTask>();	
	private static Map<String, List<String>> generatedCode = new HashMap<String, List<String>>();
	private static Map<String, String> sequenceMap = new HashMap<String, String>();
	
	private static String startId = "";
	
	public static Map<String, List<String>> generateCode(Document doc) {

		try {
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			
			// Get the BPMN Start Id
			startId = BpmnParser.getStartId(doc);
			
			sequenceMap = BpmnParser.getSequenceFlow(doc);
			
			taskMap = BpmnParser.getNodes(doc);
			
			String taskId = startId;
			
			parse(taskId, "root");
						
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return generatedCode;
	}

	private static void parse(String taskId, String parentName) throws IOException {
		
		BpmnTask task = taskMap.get(taskId);
		System.out.println(task);

		if (task != null) {
			switch (task.getType()) {
			case TASK:
				generateTaskCode(task, parentName);
				break;
			case GATEWAY:
				generateGatewayCode(task, parentName);
				break;
			default:
				// code block
			}
			
		}
	}

	private static void generateTaskCode(BpmnTask task, String parentName) throws IOException {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.name);

		String gosubStr = "goSub --label " + name;
		addCode(parentName, gosubStr);

	    parse(task.getOutgoingId(0), name);
		
	}

	private static void addCode(String parentName, String gosubStr) {
		List<String> functionCode = generatedCode.get(parentName);
		
		if (functionCode == null) {
			functionCode = new ArrayList<String>();
			generatedCode.put(parentName, functionCode);
		}
		functionCode.add(gosubStr);	
	}
	
	private static void generateGatewayCode(BpmnTask task, String parentName) throws IOException {
		
		String ifStr = "if --left \"${result}\" --operator \"Is_True\"";
		addCode(parentName, ifStr);
		
		BpmnTask taskA = taskMap.get(task.getOutgoingId(0));
		generateGosubCode(taskA, parentName);
		
		String elseStr = "else";
		addCode(parentName, elseStr);
		
		BpmnTask taskB = taskMap.get(task.getOutgoingId(1));
		generateGosubCode(taskB, parentName);	

		String endifStr = "endif";
		addCode(parentName, endifStr);

	}

	private static void generateGosubCode(BpmnTask task, String parentName) throws IOException {
		
		if (task != null) {
			
			boolean successPath = sequenceMap.containsKey(task.incomingId);
			
			if (successPath) {
				String commentStr = "// Success path ";
				addCode(parentName, commentStr);
			} 
			
			String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
				
			String gosubStr = "goSub --label " + name;
			addCode(parentName, gosubStr);
			
			parse(task.getOutgoingId(0), name);
		}
	}	
}
