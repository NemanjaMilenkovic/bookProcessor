package bookProcessor;

public class Program {

	public static void main(String[] args) {
		
		Book b = new Book();
		b.findUnknownWords();
		b.updateDBnewWords();
		b.mostUsedTop20();
		b.writeAllWordsToFile();
	}

}
