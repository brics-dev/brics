package gov.nih.tbi.repository.service.util;

import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

/**
 * Helper class that parses a full name string into separate components.
 * 
 * @author jim3
 */
public class FullNameParser {

	private String[] prefixes = {"dr", "mr", "ms", "atty", "prof", "miss", "mrs", "md"};
	private String[] suffixes = {"jr", "sr", "ii", "iii", "iv", "v", "vi", "esq", "2nd", "3rd", "jd", "phd", "md",
			"cpa", "rn", "mph", "pt", "atc"};

	private String firstName;
	private String lastName;
	private String mi;
	private String suffix;

	public FullNameParser() {}

	public void reset() {
		firstName = null;
		lastName = null;
		mi = null;
		suffix = null;
	}

	public static void main(String[] args) {
		FullNameParser parser = new FullNameParser();

		System.out.print("Please enter a full name: ");
		Scanner scanner = new Scanner(System.in);
		String name = scanner.nextLine();

		while (!name.equals("0")) {
			parser.parseFullName(name);

			System.out.println("First Name: " + parser.getFirstName());
			System.out.println("Last Name: " + parser.getLastName());
			System.out.println("MI: " + parser.getMi());
			System.out.println("Suffix: " + parser.getSuffix());
			System.out.println("----------------------------------------------------");

			System.out.print("Please enter a full name: ");
			name = scanner.nextLine();
		}

		scanner.close();
	}

	public void parseFullName(String fullName) {
		this.reset();

		if (StringUtils.isEmpty(fullName)) {
			System.out.println("Input name is empty.");
		}

		fullName = fullName.trim().replaceAll(" +", " ");  // remove extra white spaces in between the string
		String[] words = fullName.split(" ");
		int count = words.length;

		if (count == 1) {
			System.out.println("This is mostly a junk: " + fullName);
			firstName = fullName;

		} else if (count == 2) {
			parseTwoWords(words[0], words[1]);

		} else if (count == 3) {
			this.parseThreeWords(words[0], words[1], words[2]);

		} else if (count > 3) {
			if (this.countComma(fullName) > 1 && !words[0].endsWith(",")) {
				// Just pick the first one if there are more than one names entered
				String firstOne = fullName.substring(0, fullName.indexOf(","));
				this.parseFullName(firstOne);
				
			} else if (isPrefix(words[0])) {
				parseThreeWords(words[1], words[2], words[3]);

			} else if (words[1].endsWith(",") && isSuffix(words[2])) {
				parseThreeWords(words[0], words[1], words[2]);

			} else if (words[2].endsWith(",") && isSuffix(words[3])) {
				suffix = words[3];
				if (suffix.endsWith(",")) {
					suffix = suffix.substring(0, suffix.length() - 1);
				}
				parseThreeWords(words[0], words[1], words[2]);

			} else {
				if (fullName.contains(",")) {
					if (words[0].endsWith(",")) {
						lastName = words[0];
						String rest = fullName.substring(words[0].length() + 1);
						if (!rest.contains(",")) {
							firstName = rest;
						} else {
							firstName = rest.substring(0, rest.indexOf(","));
						}

					} else {
						// Just pick the first one if there are more than one names entered
						String firstOne = fullName.substring(0, fullName.indexOf(","));
						this.parseFullName(firstOne);
					}
				} else {
					int startIndex = words[0].length() + words[1].length() + 2;
					String newLastName = fullName.substring(startIndex);
					this.parseThreeWords(words[0], words[1], newLastName);
				}
			}
		}

		// Final clean up
		if (firstName != null) {
			firstName = firstName.replace(".", "").replace(",", "");
		}
		if (lastName != null) {
			lastName = lastName.replace(".", "").replace(",", "");
		}
		if (suffix != null) {
			suffix = suffix.replace(",", "");
		}
	}

	private void parseTwoWords(String word1, String word2) {
		if (isPrefix(word1)) {	// prefix FirstName
			lastName = word2;
		} else if (!word1.endsWith(",")) {	// Simplest case: FirstName LastName
			firstName = word1;
			lastName = word2;
		} else {				// LastName, FirstName
			lastName = word1;
			firstName = word2;
		}
	}

	private void parseThreeWords(String word1, String word2, String word3) {
		if (isPrefix(word1)) {    // Prefix + Full Name
			parseTwoWords(word2, word3);

		} else if (word1.endsWith(",")) {  // LastName, FistName MI
			lastName = word1;
			firstName = word2;
			mi = word3.substring(0, 1);

		} else if (word2.endsWith(".")) {  // FirstName MI. LastName
			firstName = word1;
			mi = word2;
			lastName = word3;

		} else if (word2.endsWith(",")) {  // FirstName LastName, suffix
			firstName = word1;
			lastName = word2;

			if (isSuffix(word3)) {
				suffix = word3;
			}

		} else {		// default is FirstName M LastName
			firstName = word1;
			mi = word2.substring(0, 1);
			lastName = word3;
		}
	}

	private boolean isPrefix(String str) {
		str = str.replace(".", "").replace(",", "");
		for (String prefix : prefixes) {
			if (prefix.equals(str.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private boolean isSuffix(String str) {
		str = str.replace(".", "").replace(",", "");
		for (String suffix : suffixes) {
			if (suffix.equals(str.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private int countComma(String text) {
		int counter = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == ',') {
				counter++;
			}
		}
		return counter;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getMi() {
		return mi;
	}

	public String getSuffix() {
		return suffix;
	}
}
