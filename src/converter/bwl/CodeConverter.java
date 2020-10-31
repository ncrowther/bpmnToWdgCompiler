package converter.bwl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import converter.common.BpmnTask;
import converter.common.CodePlacement;
import converter.common.StringUtils;
import converter.common.TaskType;

public class CodeConverter {

	private static Map<String, List<String>> generatedCode = new HashMap<String, List<String>>();
	private static Set<String> generatedSet = new HashSet<String>();

	public static void reset() {
		generatedCode = new HashMap<String, List<String>>();
		;
		generatedSet = new HashSet<String>();
	}

	public static Map<String, List<String>> generateWDGFunctionCode(String startId, BwlBpmnParser bpmnParser) {
		try {
			generateCode(bpmnParser, startId, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return generatedCode;
	}

	private static void generateCode(BwlBpmnParser bpmnParser, String taskId, BpmnTask parentTask) throws IOException {

		BpmnTask task = bpmnParser.getTask(taskId);
		if (task != null) {

			System.out.println("**GENERATING CODE FOR " + task.getName());

			switch (task.getType()) {
			case START:
				generateStartCode(bpmnParser, bpmnParser.getTask(task.getOutgoingId(0)), null);
				break;
			case SUBPROCESS:
				generateSubprocessCode(bpmnParser, task, parentTask);
				task = bpmnParser.getTask(task.getOutgoingId(0));
			case TASK:
				generateTaskCode(bpmnParser, task, parentTask);
				break;
			case GATEWAY:
				generateGatewayCode(bpmnParser, task, parentTask);
				break;
			default:
				// code block
			}

		}
	}

	private static void generateStartCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask)
			throws IOException {

		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
		String parentName = getParentName(parentTask);

		String doc = getProcessDocumentation(task, bpmnParser);

		if (doc != null) {
			addCode(CodePlacement.ROOT.toString(), doc);
		}

		doc = getDocumentation(task);

		if (doc != null) {
			addCode(name, doc);
		} else {
			String gosubStr = "goSub --label " + name;
			addCode(parentName, gosubStr);
		}

		String outgoingId = task.getOutgoingId(0);

		String id = task.getId();

		if (generatedSet.contains(id)) {
			return;
		} else {
			generatedSet.add(id);
		}
		generateCode(bpmnParser, outgoingId, task);

	}

	private static void generateTaskCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask)
			throws IOException {

		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
		String parentName = getParentName(parentTask);

		String doc = getDocumentation(task);

		if (doc != null) {
			addCode(name, doc);
		} else {

			String logMessage = "logMessage --message " + parentName + " --type \"Info\"";
			addCode(parentName, logMessage);

			String gosubStr = "goSub --label " + name;
			addCode(parentName, gosubStr);
		}

		String outgoingId = task.getOutgoingId(0);

		String id = task.getId();

		if (generatedSet.contains(id)) {
			return;
		} else {
			generatedSet.add(id);
		}

		generateCode(bpmnParser, outgoingId, task);

	}

	private static void generateSubprocessCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask)
			throws IOException {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
		String parentName = getParentName(parentTask);

		String doc = getDocumentation(task);
		// doc = null;
		if (doc != null) {
			addCode(name, doc);
		} else {

			String logMessage = "logMessage --message " + parentName + " --type \"Info\"";
			addCode(parentName, logMessage);

			String execScript = "executeScript --isfromfile  --filename " + name + ".wal";
			addCode(parentName, execScript);
		}

		String outgoingId = task.getOutgoingId(0);

		String id = task.getId();

		if (generatedSet.contains(id)) {
			return;
		} else {
			generatedSet.add(id);
		}

		generateCode(bpmnParser, outgoingId, task);

	}

	private static void addCode(String parentName, String codeStr) {
		List<String> functionCode = generatedCode.get(parentName);

		if (functionCode == null) {
			functionCode = new ArrayList<String>();
			generatedCode.put(parentName, functionCode);
		}
		functionCode.add(codeStr);
	}

	private static void generateGatewayCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask)
			throws IOException {

		String parentName = getParentName(parentTask);
		
		String logMessage = "logMessage --message " + parentName + " --type \"Info\"";
		addCode(parentName, logMessage);

		BpmnTask taskA = bpmnParser.getTask(task.getOutgoingId(0));
		BpmnTask taskB = bpmnParser.getTask(task.getOutgoingId(1));

		Map<String, String> sequenceMap = bpmnParser.getSequenceMap();
		boolean successPathA = (taskA != null) ? sequenceMap.containsKey(taskA.getIncomingId(0)) : false;
		boolean successPathB = (taskB != null) ? sequenceMap.containsKey(taskB.getIncomingId(0)) : false;

		if (successPathA) {
			generateGatewayCode(bpmnParser, taskA, parentTask, true);
			generateGatewayCode(bpmnParser, taskB, parentTask, false);
		} else if (successPathB) {
			generateGatewayCode(bpmnParser, taskB, parentTask, true);
			generateGatewayCode(bpmnParser, taskA, parentTask, false);
		} else {
			// No success path defined, so just make one up
			generateGatewayCode(bpmnParser, taskA, parentTask, true);
			generateGatewayCode(bpmnParser, taskB, parentTask, false);
		}

		String endifStr = "endif";
		addCode(parentName, endifStr);

	}

	private static void generateGatewayCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask,
			boolean successPath) throws IOException {

		if (task != null) {

			String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
			String parentName = getParentName(parentTask);

			String doc = getDocumentation(task);

			if (doc != null) {
				addCode(name, doc);
			} else {

				if (generatedCode.get(CodePlacement.DEFVARS.toString()) == null) {
					String defVar = "defVar --name result --type Boolean --value True";
					addCode(CodePlacement.DEFVARS.toString(), defVar);
				}

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

				if (task.getType() == TaskType.TASK) {
					String gosubStr = "goSub --label " + name;
					addCode(parentName, gosubStr);
				}

				if (task.getType() == TaskType.SUBPROCESS) {
					String execScript = "executeScript --isfromfile  --filename " + name + ".wal";
					addCode(parentName, execScript);
				}
			}

			generateCode(bpmnParser, task.getOutgoingId(0), task);
		}
	}

	private static String getDocumentation(BpmnTask task) {
		if (task == null) {
			return null;
		} else {
			String doc = task.getDocumentation();
			if (doc == null || doc.length() == 0) {
				return null;
			} else {
				return doc;
			}
		}
	}

	private static String getProcessDocumentation(BpmnTask task, BwlBpmnParser bpmnParser) {
		if (task == null) {
			return null;
		} else {
			String startId = task.getIncomingId(0); // getStartId();

			if (startId != null) {
				BpmnTask process = bpmnParser.getAssociatedProcess(startId);

				if (process != null) {
					return getDocumentation(process);
				}
			}
		}

		return null;
	}

	private static String getParentName(BpmnTask parentTask) {
		if (parentTask == null) {
			return CodePlacement.ROOT.toString();
		} else {
			return StringUtils.convertToTitleCaseIteratingChars(parentTask.getName());
		}
	}
}
