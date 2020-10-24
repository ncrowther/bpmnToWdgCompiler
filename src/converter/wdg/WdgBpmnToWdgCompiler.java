package converter.wdg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import converter.common.BpmnTask;
import converter.common.StringUtils;

public class WdgBpmnToWdgCompiler {

	private static Map<String, List<String>> functionList = new HashMap<String, List<String>>();
	private static Map<String, List<String>> codeMap;
	private static Set<String> generatedCode = new HashSet<String>();
	
	public static void main(String[] args) {

		String inputFileName= "";
		
		try {
			
			if (args.length > 0) {
				inputFileName = args[0];
			} else {
				System.err.println("Please supply a bpmn file");
				System.exit(1);
			}

			System.out.println("Processing " + inputFileName);
			
			File inputFile = new File(inputFileName);
			
			WdgBpmnParser bpmnParser = new WdgBpmnParser(inputFile);

			String startId = bpmnParser.getStartId();
			generateWDGFunctions(bpmnParser, startId);
			
			codeMap = CodeConverter.generateWDGCode(startId, bpmnParser);

			writeWDGFile(functionList, codeMap);
			
			System.out.println("Code generated in ./data/generated.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateWDGFunctions(WdgBpmnParser bpmnParser, String taskId) throws IOException {

		BpmnTask task = bpmnParser.getTask(taskId);

		if (task != null) {
			
			if (generatedCode.contains(task.getId())) {
				return;
			} else {
				generatedCode.add(task.getId());
			}
			
			switch (task.getType()) {
			case TASK:
				generateTaskCode(bpmnParser, task);
				break;
			case GATEWAY:
				generateGatewayCode(bpmnParser,task);
				break;
			default:
				// code block
			}
		}
	}

	private static void generateTaskCode(WdgBpmnParser bpmnParser, BpmnTask task) throws IOException {
		String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());

		StringBuilder beginSubStr = new StringBuilder();

		beginSubStr.append("beginSub --name " + name);
		beginSubStr.append("\n");
		beginSubStr.append("logMessage --message " + name + " --type \"Info\"");
		beginSubStr.append("\n");
		addFunction(name, beginSubStr.toString());

		generateWDGFunctions(bpmnParser, task.getOutgoingId(0));
	}

	private static void addFunction(String parentName, String functionStr) {
		List<String> functionCode = functionList.get(parentName);

		if (functionCode == null) {
			functionCode = new ArrayList<String>();
			functionList.put(parentName, functionCode);
		}
		functionCode.add(functionStr);
	}

	private static void generateGatewayCode(WdgBpmnParser bpmnParser, BpmnTask task) throws IOException {

		BpmnTask taskA = bpmnParser.getTask(task.getOutgoingId(0));

		BpmnTask taskB = bpmnParser.getTask(task.getOutgoingId(1));

		generateSubCode(bpmnParser, taskA);

		generateSubCode(bpmnParser, taskB);

	}

	private static void generateSubCode(WdgBpmnParser bpmnParser, BpmnTask task) throws IOException {

		if (task != null) {
			StringBuilder beginSubStr = new StringBuilder();
			String name = StringUtils.convertToTitleCaseIteratingChars(task.getName());
			beginSubStr.append("beginSub --name " + name);
			beginSubStr.append("\n");
			beginSubStr.append("logMessage --message " + name + " --type \"Info\"");
			beginSubStr.append("\n");
			
			generateWDGFunctions(bpmnParser, task.getOutgoingId(0));
	
			addFunction(name, beginSubStr.toString());
		}
	}


	public static void writeWDGFile(Map<String, List<String>> functionList, Map<String, List<String>> generatedCode)
			throws IOException {
		String filename = "data/generated.txt";
		FileOutputStream outputStream = new FileOutputStream(filename);

		StringBuilder strBuilder = new StringBuilder();

		/*
		 * String code1 = Character.toString((char)18); String code2 =
		 * Character.toString((char)38 ); String code3 = Character.toString((char)35 );
		 * String code4 = Character.toString((char)54 ); String code5 =
		 * Character.toString((char)53 ); String code6 = Character.toString((char)53 );
		 * String code7 = Character.toString((char)51 ); String code8 =
		 * Character.toString( (char)51); String code9 = Character.toString( (char)59);
		 * String code10 = Character.toString((char)2); strBuilder.append(code1 + code2
		 * + code3 + code4 + code5 + code6 + code7 + code8 + code9 + code10);
		 */

		strBuilder.append("defVar --name result --type Boolean --value True");
		strBuilder.append('\n');

		String code = getCode(generatedCode, "root");
		strBuilder.append('\n');
		strBuilder.append(code);

		for (String key : functionList.keySet()) {
			List<String> functions = functionList.get(key);

			for (String beginSub : functions) {
				strBuilder.append('\n');

				code = getCode(generatedCode, key);

				strBuilder.append(beginSub);
				strBuilder.append(code);
				strBuilder.append('\n');
				strBuilder.append("endSub");
				strBuilder.append('\n');
			}

			// String code11 = Character.toString((char)8);
			// strBuilder.append('*' + code11 + "20.5.2.0");
		}

		byte[] strToBytes = strBuilder.toString().getBytes();
		outputStream.write(strToBytes);

		outputStream.close();
	}

	private static String getCode(Map<String, List<String>> generatedCode, String key) {

		List<String> functionCode = generatedCode.get(key);

		StringBuilder strBuilder = new StringBuilder();

		// strBuilder.append('\n');
		// strBuilder.append("// " + key + '\n');

		if (functionCode != null) {
			for (String codeLine : functionCode) {
				strBuilder.append(codeLine);
				strBuilder.append('\n');
			}
		}

		return strBuilder.toString();
	}
}
