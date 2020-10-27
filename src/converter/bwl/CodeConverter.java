package converter.bwl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import converter.common.BpmnTask;
import converter.common.StringUtils;

public class CodeConverter {
	
	private static Map<String, List<String>> generatedCode = new HashMap<String, List<String>>();
	private static Set<String> generatedSet = new HashSet<String>();
	
	public static void reset() {
		generatedCode = new HashMap<String, List<String>>();;
		generatedSet = new HashSet<String>();
	}
	
	public static Map<String, List<String>> generateWDGFunctionCode(String startId, BwlBpmnParser bpmnParser) {
		try {		
			generateCode(bpmnParser, startId, "root");					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return generatedCode;
	}

	private static void generateCode(BwlBpmnParser bpmnParser, String taskId, String parentName) throws IOException {
		
		BpmnTask task = bpmnParser.getTask(taskId);
		if (task != null) {
			
			System.out.println("**GENERATING CODE FOR " + task.getName());
			
			switch (task.getType()) {
			case START:
				generateTaskCode(bpmnParser, bpmnParser.getTask(task.getOutgoingId(0)), "root");
				break;
			case SUBPROCESS:				
				generateSubprocessCode(bpmnParser, task, parentName);
				task = bpmnParser.getTask(task.getOutgoingId(0));
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

	private static void generateTaskCode(BwlBpmnParser bpmnParser, BpmnTask task, String parentName) throws IOException {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());

		String gosubStr = "goSub --label " + name;
		addCode(parentName, gosubStr);

		String outgoingId = task.getOutgoingId(0);

		String id = task.getId();

		if (generatedSet.contains(id)) {
			return;
		} else {
			generatedSet.add(id);
		}
	    generateCode(bpmnParser, outgoingId, name);

	}
	
	private static void generateSubprocessCode(BwlBpmnParser bpmnParser, BpmnTask task, String parentName) throws IOException {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());

		String execScript = "executeScript --isfromfile  --filename " + name + ".wal";
		addCode(parentName, execScript);

		String outgoingId = task.getOutgoingId(0);

		String id = task.getId();

		if (generatedSet.contains(id)) {
			return;
		} else {
			generatedSet.add(id);
		}
		
	    generateCode(bpmnParser, outgoingId, name);

	}

	private static void addCode(String parentName, String gosubStr) {
		List<String> functionCode = generatedCode.get(parentName);
		
		if (functionCode == null) {
			functionCode = new ArrayList<String>();
			generatedCode.put(parentName, functionCode);
		}
		functionCode.add(gosubStr);	
	}
	
	private static void generateGatewayCode(BwlBpmnParser bpmnParser, BpmnTask task, String parentName) throws IOException {
		
		
		BpmnTask taskA = bpmnParser.getTask(task.getOutgoingId(0));
		BpmnTask taskB = bpmnParser.getTask(task.getOutgoingId(1));
		
		Map<String, String> sequenceMap = bpmnParser.getSequenceMap();
		boolean successPathA = (taskA != null) ? sequenceMap.containsKey(taskA.getIncomingId(0)) : false;
		boolean successPathB = (taskB != null) ? sequenceMap.containsKey(taskB.getIncomingId(0)) : false;
		
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

	private static void generateGosubCode(BwlBpmnParser bpmnParser, BpmnTask task, String parentName, boolean successPath ) throws IOException {
		
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
