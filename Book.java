package bookProcessor;

import java.io.*;
import java.sql.*;
import java.util.*;
import static java.util.stream.Collectors.*;

public class Book {
	private final String bookPath;
	private final String dictPath;
	private Dictionary dict;
	private HashMap<String, Integer> foundInDict;
	private TreeSet<String> newWords;
	private TreeSet<String> allWords;

	public Book() {
		bookPath = "knjiga.txt";
		dictPath = "jdbc:sqlite:Dictionary.db";
		dict = new Dictionary();
		foundInDict = new HashMap<String, Integer>();
		newWords = new TreeSet<String>();
		allWords = new TreeSet<String>();
		dict.setWords();
	}

	// Finding words that are used in the book, but not found in the dictionary
	// Writing all words used to file
	public void findUnknownWords() {
		try (BufferedReader br = new BufferedReader(new FileReader(bookPath));
				FileWriter fwAll = new FileWriter("allWords.txt");
				FileWriter fwNew = new FileWriter("newWords.txt")) {

			String line = br.readLine();

			while (line != null) {
				line = line.toLowerCase();
				for (int i = 0; i < line.length(); i++) {
					char ch = line.charAt(i);
					if (!(ch >= 'a' && ch <= 'z'))
						line = line.replace(ch, ' ');
				}

				String[] lineSplit = line.split("[\\p{Punct}\\s0123456789]+");
				for (int i = 0; i < lineSplit.length; i++) {
					allWords.add(lineSplit[i]);
					fwAll.write(lineSplit[i].toString() + "\n");
					if (!dict.containsWord(lineSplit[i])) {
						fwNew.write(lineSplit[i] + " \n");
						newWords.add(lineSplit[i]);
					} else {
						// Using Map value to count the number of times the word is used
						int count = foundInDict.containsKey(lineSplit[i]) ? foundInDict.get(lineSplit[i]) : 0;
						foundInDict.put(lineSplit[i], ++count);
					}
				}

				line = br.readLine();
			}
			fwAll.flush();
			fwNew.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void updateDBnewWords() {

		try (Connection con = DriverManager.getConnection(dictPath); Statement stm = con.createStatement()) {
			StringBuilder newWord = new StringBuilder();

			for (String s : newWords) {
				newWord.append("(\"").append(s).append("\"),");
			}
			newWord.deleteCharAt(newWord.length() - 1);
			stm.executeUpdate("DROP TABLE if EXISTS newWords; CREATE TABLE newWords (word VARCHAR(50))");
			stm.executeUpdate("INSERT INTO newWords VALUES " + newWord.toString());

		} catch (SQLException e) {
			System.err.println("Database update failed.");
		}

	}
	// Sorting and writing top 20 values with keys from a HashMap to a file
	public void mostUsedTop20() {
		try (BufferedReader br = new BufferedReader(new FileReader(bookPath));
				FileWriter top20 = new FileWriter("mostUsedTop20.txt")) {

			HashMap<String, Integer> sorted = foundInDict // Sorting by value in reverse order
			        .entrySet()
			        .stream()
			        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
			        .collect(
			            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
			                LinkedHashMap::new));
			
			String line = br.readLine();
			int counter = 1;
			StringBuilder sb = new StringBuilder();
			while (line != null) {
				for (String w : sorted.keySet()) {
					sb.append(counter).append(") '").append(w).append("' is used ").append(foundInDict.get(w))
							.append(" times\n");
					if (counter++ >= 20) {
						line = null;
						break;
					}
				}
				top20.write(sb.toString());
			}
			top20.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeAllWordsToFile() {
		try (FileWriter fw = new FileWriter("allWords.txt")){
			StringBuilder sb = new StringBuilder();
			for (String w : allWords) {
				sb.append(w + "\n");
			}
			fw.write(sb.toString());
			fw.flush();
		} catch (IOException e) {
			System.err.println("Writing to a file failed.");
			e.printStackTrace();
		}
	}
}