package converter.wdg.ibm.com;

public class StringUtils {
	
	private static String deleteWhiteSpace(String str) {
		String s = "";
		char[] arr = str.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			int temp = arr[i];
			if (temp != 32 && temp != 9) { // 32 ASCII for space and 9 is for Tab
				s += arr[i];
			}
		}
		return s;
	}

	public static String convertToTitleCaseIteratingChars(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		StringBuilder converted = new StringBuilder();

		boolean convertNext = true;
		for (char ch : text.toCharArray()) {
			if (Character.isSpaceChar(ch)) {
				convertNext = true;
			} else if (convertNext) {
				ch = Character.toTitleCase(ch);
				convertNext = false;
			} else {
				ch = Character.toLowerCase(ch);
			}
			converted.append(ch);
		}

		return deleteWhiteSpace(converted.toString());
	}

}
