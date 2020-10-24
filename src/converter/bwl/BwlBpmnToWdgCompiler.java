package converter.bwl;

import java.io.File;
import java.util.List;
import java.util.Map;

public class BwlBpmnToWdgCompiler {
	
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
			
			BwlBpmnParser bpmnParser = new BwlBpmnParser(inputFile);
			
			String startId = bpmnParser.getStartId();
			
			Map<String, List<String>> functionList = FunctionConverter.generateWDGFunctions(bpmnParser, startId);

			Map<String, List<String>> codeMap = CodeConverter.generateWDGFunctionCode(startId, bpmnParser);

			WalWriter.writeWDGFile(functionList, codeMap);
			
			System.out.println("Code generated in ./data/generated.txt");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
