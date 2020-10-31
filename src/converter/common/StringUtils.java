package converter.common;

import java.util.Optional;

public class StringUtils {
	
	private static String deleteWhiteSpace(String str) {
		String s = "";
		char[] arr = str.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			int temp = arr[i];
			if (temp != 32 && temp != 9 && temp != Character.getNumericValue('\\') && temp != Character.getNumericValue('/')) { // 32 ASCII for space and 9 is for Tab
				s += arr[i];
			}
		}
		return s;
	}
	
	private static String removeInvalidCharacters(String str) {
		String s = "";
		char[] arr = str.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			char temp = arr[i];
			if (temp != '/' && temp != '\\') { //Invalid chars
				s += arr[i];
			}
		}
		return deleteWhiteSpace(s);
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

		return removeInvalidCharacters(converted.toString());
	}
	
	public static String getExtensionByStringHandling(String filename) {
	    return Optional.ofNullable(filename)
	      .filter(f -> f.contains("."))
	      .map(f -> f.substring(filename.lastIndexOf(".") + 1)).get();
	}

}
