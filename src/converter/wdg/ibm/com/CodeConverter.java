package converter.wdg.ibm.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

public class CodeConverter {
	
	//private static Map<String, BpmnTask> taskMap = new HashMap<String, BpmnTask>();	
	private static Map<String, List<String>> generatedCode = new HashMap<String, List<String>>();
	//private static Map<String, String> sequenceMap = new HashMap<String, String>();
	
	
	public static Map<String, List<String>> generateWDGCode(String startId, BpmnParser bpmnParser) {
		try {		
			generateCode(bpmnParser, startId, "root");					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return generatedCode;
	}

	private static void generateCode(BpmnParser bpmnParser, String taskId, String parentName) throws IOException {
		
		BpmnTask task = bpmnParser.getTask(taskId);
		if (task != null) {
			switch (task.getType()) {
			case TASK:
				generateTaskCode(bpmnParser, task, parentName);
				break;
			case GATEWAY:
				generateGatewayCode(bpmnParser, task, parentName);
				break;
			default:
				// code block
			}
			
		}
	}

	private static void generateTaskCode(BpmnParser bpmnParser, BpmnTask task, String parentName) throws IOException {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.name);

		String gosubStr = "goSub --label " + name;
		addCode(parentName, gosubStr);

	    generateCode(bpmnParser, task.getOutgoingId(0), name);
		
	}

	private static void addCode(String parentName, String gosubStr) {
		List<String> functionCode = generatedCode.get(parentName);
		
		if (functionCode == null) {
			functionCode = new ArrayList<String>();
			generatedCode.put(parentName, functionCode);
		}
		functionCode.add(gosubStr);	
	}
	
	private static void generateGatewayCode(BpmnParser bpmnParser, BpmnTask task, String parentName) throws IOException {
		
		
		BpmnTask taskA = bpmnParser.getTask(task.getOutgoingId(0));
		BpmnTask taskB = bpmnParser.getTask(task.getOutgoingId(1));
		
		Map<String, String> sequenceMap = bpmnParser.getSequenceMap();
		boolean successPathA = sequenceMap.containsKey(taskA.incomingId);
		boolean successPathB = sequenceMap.containsKey(taskB.incomingId);
		
		if (successPathA) {
			generateGosubCode(bpmnParser, taskA, parentName, true);		
			generateGosubCode(bpmnParser, taskB, parentName, false);	
		} else if (successPathB) {
			generateGosubCode(bpmnParser, taskB, parentName, true);		
			generateGosubCode(bpmnParser, taskA, parentName, false);
		} else {
			// No success path defined, so just make one up
			generateGosubCode(bpmnParser, taskA, parentName, true);		
			generateGosubCode(bpmnParser, taskB, parentName, false);	
		}
		
		String endifStr = "endif";
		addCode(parentName, endifStr);

	}

	private static void generateGosubCode(BpmnParser bpmnParser, BpmnTask task, String parentName, boolean successPath ) throws IOException {
		
		if (task != null) {
			
			if (successPath) {
				String ifStr = "if --left \"${result}\" --operator \"Is_True\"";
				addCode(parentName, ifStr);
				String commentStr = "// Success path ";
				addCode(parentName, commentStr);
			} else {
				String elseStr = "else";
				addCode(parentName, elseStr);
				String commentStr = "// Failure path ";
				addCode(parentName, commentStr);
			}
			
			String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
				
			String gosubStr = "goSub --label " + name;
			addCode(parentName, gosubStr);
			
			generateCode(bpmnParser, task.getOutgoingId(0), name);
		}
	}	
}
