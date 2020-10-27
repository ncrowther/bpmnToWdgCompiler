package converter.bwl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import converter.common.BpmnTask;
import converter.common.StringUtils;
import converter.common.WalWriter;

public class BwlBpmnToWdgCompiler {
	
	public static void main(String[] args) {

		String inputFileName= "";
		String outputFileName = null;
		
		try {
			
			if (args.length > 0) {
				inputFileName = args[0];
				String extension =  StringUtils.getExtensionByStringHandling(inputFileName);
				
				if (extension.equals("bpmn")) {
				    outputFileName = inputFileName.replace(extension, "txt");
				    outputFileName = outputFileName.replace("data", "generated");
				} else {
					System.err.println("Please supply a bpmn file");
					System.exit(1);					
				}
			} else {
				System.err.println("No file specified");
				System.exit(1);
			}

			System.out.println("Processing " + inputFileName);
			
			File inputFile = new File(inputFileName);
			
			BwlBpmnParser bpmnParser = new BwlBpmnParser(inputFile, outputFileName);
			
			Stack<BpmnTask> startIds = bpmnParser.getStartIds();
			
			while (!startIds.empty()) {
				BpmnTask startNode = startIds.pop();
				
				FunctionConverter.reset();
				CodeConverter.reset();

				Map<String, List<String>> functionList = FunctionConverter.generateWDGFunctions(bpmnParser, startNode.getId());

				Map<String, List<String>> codeMap = CodeConverter.generateWDGFunctionCode(startNode.getId(), bpmnParser);

				outputFileName =  startNode.getName();
				WalWriter.writeWDGFile(outputFileName, functionList, codeMap);
				
				System.out.println("Code generated in " + outputFileName);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
