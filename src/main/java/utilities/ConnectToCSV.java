package utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public final class ConnectToCSV {

	public static List<String> readCSV(String fileName, String columnName) {
		Reader in;
		List<String> rowsData = new ArrayList<>();
		try {
			in = new FileReader(fileName);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return rowsData;
		}
		Iterable<CSVRecord> records;
		try {
			records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return rowsData;
		}
		for (CSVRecord record : records) {
			String data = record.get(columnName);
			if(!data.isEmpty())
				rowsData.add(data);
		}
		return rowsData;
	}

	public static List<Object> removeDuplicateRows(List<String> list) {
		Set<String> dataSet = new HashSet<>(list);
		System.out.printf("%d total record(s)\n", list.size());
		System.out.printf("%d unique record(s)\n", dataSet.size());
		return dataSet.stream().collect(Collectors.toList());
	}
}
