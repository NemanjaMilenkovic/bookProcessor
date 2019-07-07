package bookProcessor;

import java.sql.*;
import java.util.HashMap;

public class Dictionary {
	private final String konStr = "jdbc:sqlite:Dictionary.db";
	private HashMap<String, String> words;

	public Dictionary() {
		this.words = new HashMap<String, String>();
	}

	// Creating a HashMap from given .db file
	public void setWords() {
		String sql = "SELECT word, definition FROM entries";
		try (Connection con = DriverManager.getConnection(konStr);
				Statement stm = con.createStatement();
				ResultSet rezultat = stm.executeQuery(sql)) {
			while (rezultat.next()) {
				words.put(rezultat.getString("word").toLowerCase(), rezultat.getString("definition"));
			}

		} catch (SQLException e) {
			System.err.println("The dictionary initialization failed.");
			e.printStackTrace();
		}

	}
	
	public boolean containsWord(String s) {
		if (words.containsKey(s)) {
			return true;
		}
			return false;
	}

}