package gov.nih.tbi.download.util;

import java.util.Comparator;


/**
 * This class has been moved to its own class so that it is usable
 * from both the macro view and the micro view. -Victor Wang
 * 
 * @author wangvg
 *
 */
public class ByteComparator implements Comparator<String> {

	@Override
	public int compare(String string1, String string2) {
		int index = string1.indexOf("/");
		Float float1 =
				(index < 0) ? Float.parseFloat(string1.split(" ")[0]) : Float.parseFloat(string1.substring(index + 1)
						.split(" ")[0]);
		index = string2.indexOf("/");
		Float float2 =
				(index < 0) ? Float.parseFloat(string2.split(" ")[0]) : Float.parseFloat(string2.substring(index + 1)
						.split(" ")[0]);

		String bytes1 = string1.split(" ")[1];
		Bytes byte1enum = Bytes.valueOf(bytes1);
		String bytes2 = string2.split(" ")[1];
		Bytes byte2enum = Bytes.valueOf(bytes2);

		if (byte2enum.ordinal() < byte1enum.ordinal())
			return 1;
		else if (byte2enum.ordinal() == byte1enum.ordinal())
			return float1.compareTo(float2);
		else
			return -1;
	}
	
	public enum Bytes {
		B, KB, MB, GB, TB
	}
}
