package converter.bwl;

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

		generatedSet = new HashSet<String>();
	}

	public static Map<String, List<String>> generateWDGFunctionCode(String startId, BwlBpmnParser bpmnParser, boolean generateFromDoc) {
		
		try {
			generateCode(bpmnParser, startId, null, generateFromDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return generatedCode;
	}

	private static void generateCode(BwlBpmnParser bpmnParser, String taskId, BpmnTask parentTask, boolean generateFromDoc) {

		BpmnTask task = bpmnParser.getTask(taskId);
		if (task != null) {

			System.out.println("**GENERATING CODE FOR " + task.getName());

			switch (task.getType()) {
			case START:
				generateStartCode(bpmnParser, bpmnParser.getTask(task.getOutgoingId(0)), null, generateFromDoc);
				break;
			case SUBPROCESS:
				generateSubprocessCode(bpmnParser, task, parentTask, generateFromDoc);
				break;
			case TASK:
				generateTaskCode(bpmnParser, task, parentTask, generateFromDoc);
				break;
			case METABOT:
				generateMetabotCode(bpmnParser, task, parentTask, generateFromDoc);
				break;
			case GATEWAY:
				generateGatewayCode(bpmnParser, task, parentTask, generateFromDoc);
				break;
			default:
				// code block
			}

		}
	}

	private static void generateStartCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask, boolean generateFromDoc) {

		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
		String parentName = getParentName(parentTask);

		if (generateFromDoc) {

			String doc = getProcessDocumentation(task, bpmnParser);

			addCode(CodePlacement.MAIN.toString(), doc);
			addCode(CodePlacement.DEFVARS.toString(), null);

			doc = getDocumentation(task);
			addCode(name, doc);
		} else {
			String gosubStr = "goSub --label " + name;
			addCode(parentName, gosubStr);
		}

		generateCodeRecursive(bpmnParser, task, generateFromDoc);

	}

	private static void generateTaskCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask, boolean generateFromDoc) {

		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
		String parentName = getParentName(parentTask);

		if (generateFromDoc) {
			String doc = getDocumentation(task);
			addCode(name, doc);
		} else {
			String gosubStr = "goSub --label " + name;
			addCode(parentName, gosubStr);
		}

		generateCodeRecursive(bpmnParser, task, generateFromDoc);
	}

	private static void generateMetabotCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask, boolean generateFromDoc) {

		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
		String parentName = getParentName(parentTask);

		if (generateFromDoc) {
			String doc = getDocumentation(task);
			addCode(name, doc);
		} else {
			String execScript = "executeScript --name " + name;
			addCode(parentName, execScript);
			
			String gosubStr = "goSub --label " + name;
			addCode(parentName, gosubStr);
		}

		generateCodeRecursive(bpmnParser, task, generateFromDoc);
	}
	
	private static void generateSubprocessCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask, boolean generateFromDoc) {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
		String parentName = getParentName(parentTask);

		if (generateFromDoc) {
			String doc = getDocumentation(task);
			addCode(name, doc);
		} else {
			String execScript = "executeScript --isfromfile  --filename " + name + ".wal";
			addCode(parentName, execScript);
		}

		generateCodeRecursive(bpmnParser, task, parentTask, generateFromDoc);
	}

	private static void generateCodeRecursive(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask, boolean generateFromDoc) {
		String id = task.getId();

		if (generatedSet.contains(id)) {
			return;
		} else {
			generatedSet.add(id);
		}

		List<String> outgoingIds = task.getOutgoingIds();

		outgoingIds.forEach((outgoingId) -> {
			generateCode(bpmnParser, outgoingId, parentTask, generateFromDoc);
		});
	}

	private static void generateCodeRecursive(BwlBpmnParser bpmnParser, BpmnTask task, boolean generateFromDoc) {
		generateCodeRecursive(bpmnParser, task, task, generateFromDoc);
	}

	private static void addCode(String parentName, String codeStr) {
		List<String> functionCode = generatedCode.get(parentName);

		if (functionCode == null) {
			functionCode = new ArrayList<String>();
			generatedCode.put(parentName, functionCode);
		}
		functionCode.add(codeStr);
	}

	private static void generateGatewayCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask, boolean generateFromDoc) {

		String parentName = getParentName(parentTask);

		boolean first = true;
		List<String> outgoingIds = task.getOutgoingIds();

		for (String outgoingId : outgoingIds) {

			BpmnTask childTask = bpmnParser.getTask(outgoingId);
			if (first) {
				generateGatewayCode(bpmnParser, childTask, parentTask, true, generateFromDoc);
				first = false;
			} else {
				generateGatewayCode(bpmnParser, childTask, parentTask, false, generateFromDoc);
			}

		}

		if (!generateFromDoc) {
			String endifStr = "endif";
			addCode(parentName, endifStr);
		}

	}

	private static void generateGatewayCode(BwlBpmnParser bpmnParser, BpmnTask task, BpmnTask parentTask,
			boolean successPath, boolean generateFromDoc) {

		if (task != null) {

			String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
			String parentName = getParentName(parentTask);

			if (generateFromDoc) {		
			    String doc = getDocumentation(task);
				addCode(name, doc);
			} else {

				if (generatedCode.get(CodePlacement.DEFVARS.toString()) == null) {
					String defVar = "defVar --name result --type Boolean --value True";
					addCode(CodePlacement.DEFVARS.toString(), defVar);
				}

				if (successPath) {

					String ifStr = "if --left \"${result}\" --operator \"Is_True\"";
					addCode(parentName, ifStr);

				} else {
					String elseStr = "elseif --left \"${result}\" --operator \"Is_Null_Or_Empty\"";
					addCode(parentName, elseStr);
				}

				if (task.getType() == TaskType.TASK) {
					String gosubStr = "goSub --label " + name;
					addCode(parentName, gosubStr);
				}
			}

			addGotoChild(bpmnParser, task, name, parentName, parentTask, generateFromDoc);

			List<String> outgoingIds = task.getOutgoingIds();

			outgoingIds.forEach((outgoingId) -> {
				generateCode(bpmnParser, outgoingId, task, generateFromDoc);
			});
		}
	}

	private static void addGotoChild(BwlBpmnParser bpmnParser, BpmnTask task, String name, String parentName, BpmnTask parentTask, boolean generateFromDoc) {
		if (task.getType() == TaskType.SUBPROCESS) {
			String execScript = "executeScript --isfromfile  --filename " + name + ".wal";
			addCode(parentName, execScript);
			
			List<String> outgoingIds = parentTask.getOutgoingIds();

			outgoingIds.forEach((outgoingId) -> {
				generateCode(bpmnParser, outgoingId, task, generateFromDoc);
			});

		}
	}

	private static String getDocumentation(BpmnTask task) {
		if (task == null) {
			return "";
		} else {
			String doc = task.getDocumentation();
			if (doc == null || doc.length() == 0) {
				return "";
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
			return CodePlacement.MAIN.toString();
		} else {
			return StringUtils.convertToTitleCaseIteratingChars(parentTask.getName());
		}
	}
}
