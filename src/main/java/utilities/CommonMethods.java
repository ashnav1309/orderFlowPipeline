package utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommonMethods {

	public static List<Object> removeDuplicateRows(List<String> list) {
		Set<String> dataSet = new HashSet<>(list);
		System.out.printf("%d total record(s)\n", list.size());
		System.out.printf("%d unique record(s)\n", dataSet.size());
		return dataSet.stream().collect(Collectors.toList());
	}
}
