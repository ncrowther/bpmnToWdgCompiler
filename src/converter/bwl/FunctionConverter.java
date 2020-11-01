package converter.bwl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import converter.common.BpmnTask;
import converter.common.StringUtils;

public class FunctionConverter {

	private static Map<String, List<String>> functionList = new HashMap<String, List<String>>();
	private static Set<String> generatedCode = new HashSet<String>();
		
	public static void reset() {
		generatedCode = new HashSet<String>();
		functionList = new HashMap<String, List<String>>();
	}
	public static Map<String, List<String>> generateWDGFunctions(BwlBpmnParser bpmnParser, String taskId)  {

		BpmnTask task = bpmnParser.getTask(taskId);

		if (task != null) {
			
			if (generatedCode.contains(task.getId())) {
				return functionList;
			} else {
				generatedCode.add(task.getId());
			}
			
			switch (task.getType()) {
			case START:
				generateMainCode(bpmnParser, task);
				break;
			case TASK:
				generateTaskCode(bpmnParser, task);
				break;
			case SUBPROCESS:
				generateSubprocessCode(bpmnParser, task);
				break;
			case GATEWAY:
				generateGatewayCode(bpmnParser,task);
				break;
			default:
				// code block
			}
		}
		
		return functionList;
	}

	private static void generateMainCode(BwlBpmnParser bpmnParser, BpmnTask task)  {
		
		task = bpmnParser.getTask(task.getOutgoingId(0));
		generateWDGFunctions(bpmnParser, task.getId());
	}
	
	private static void generateTaskCode(BwlBpmnParser bpmnParser, BpmnTask task)  {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());

		StringBuilder beginSubStr = new StringBuilder();
		
		beginSubStr.append("beginSub --name " + name);
		beginSubStr.append("\n");
		
		addFunction(name, beginSubStr.toString());

		List<String> outgoingIds  = task.getOutgoingIds();	
		outgoingIds.forEach((outgoingId)  -> {
			generateWDGFunctions(bpmnParser, outgoingId);
        });
		
	}
	
	private static void generateSubprocessCode(BwlBpmnParser bpmnParser, BpmnTask task)  {

		List<String> outgoingIds  = task.getOutgoingIds();	
		outgoingIds.forEach((outgoingId)  -> {
			generateWDGFunctions(bpmnParser, outgoingId);
        });
		
	}	

	private static void addFunction(String parentName, String functionStr) {
		List<String> functionCode = functionList.get(parentName);

		if (functionCode == null) {
			functionCode = new ArrayList<String>();
			functionList.put(parentName, functionCode);
		}
		functionCode.add(functionStr);
	}

	private static void generateGatewayCode(BwlBpmnParser bpmnParser, BpmnTask task)  {

		List<String> outgoingIds  = task.getOutgoingIds();	
		outgoingIds.forEach((outgoingId)  -> {
			getChildTask(bpmnParser, outgoingId);
        });
	}
	
	private static void getChildTask(BwlBpmnParser bpmnParser, String childId)  {
		BpmnTask childTask = bpmnParser.getTask(childId);
		if (childTask != null) {
			generateSubCode(bpmnParser, childTask);
		}
	}

	private static void generateSubCode(BwlBpmnParser bpmnParser, BpmnTask task)  {

		if (task != null) {
			StringBuilder beginSubStr = new StringBuilder();
			String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
			beginSubStr.append("beginSub --name " + name);
			beginSubStr.append("\n");
			
			List<String> outgoingIds  = task.getOutgoingIds();	
			outgoingIds.forEach((outgoingId)  -> {
				generateWDGFunctions(bpmnParser, outgoingId);
	        });
			
			addFunction(name, beginSubStr.toString());
		}
	}
}
