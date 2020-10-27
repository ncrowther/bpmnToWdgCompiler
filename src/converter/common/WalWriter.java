package converter.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WalWriter {

	public static void writeWDGFile(String filename, Map<String, List<String>> functionList, Map<String, List<String>> generatedCode)
			throws IOException {

		FileOutputStream outputStream = new FileOutputStream(filename);

		StringBuilder strBuilder = new StringBuilder();
        
		/*
		char  code12 = 0x12;
		strBuilder.append(code12);
		
		char len = 0x0C;
		strBuilder.append(len);		

		len = 0x04;
		strBuilder.append(len);	
		
        strBuilder.append("defVar --name result --type Boolean --value True");
		strBuilder.append('\n');
        */
		
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
		}
		
		/*
		char code2A = 0x2A;
		strBuilder.append(code2A);
		
		char code09 = 0x09;
		strBuilder.append(code09);
		
		strBuilder.append("20.10.0.0");
		*/

		
		byte[] strToBytes = strBuilder.toString().getBytes();
		outputStream.write(strToBytes);


		outputStream.close();
	}

	private static String getCode(Map<String, List<String>> generatedCode, String key) {

		List<String> functionCode = generatedCode.get(key);

		StringBuilder strBuilder = new StringBuilder();

		if (functionCode != null) {
			for (String codeLine : functionCode) {
				strBuilder.append(codeLine);
				strBuilder.append('\n');
			}
		}

		return strBuilder.toString();
	}
}
