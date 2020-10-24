package converter.bwl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WalWriter {

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

		if (functionCode != null) {
			for (String codeLine : functionCode) {
				strBuilder.append(codeLine);
				strBuilder.append('\n');
			}
		}

		return strBuilder.toString();
	}
}
