public class Main {
	public static void main(String[] args) {
		/*String expression = "1 - x * x - y * y / 2";
		Lexer lexer = new Lexer(expression);
		while (lexer.hasNext()) {
			Lexer.Token token = lexer.nextToken();
			System.out.println("token: " + token + ", position" + lexer.getPosition() + ", text: " +lexer.getText());
		}*/
		String expression = "1 - x * x - y * y / 2";
		Parser.Node root = Parser.parse(expression);
		System.out.println(root);
	}
}
