public class Parser {

	public static Node parse(String text) {
		Lexer lexer = new Lexer(text);
		Node node = parseBinary(lexer, 0);
		if (lexer.hasNext()) {
			throw new Error("End of input expected");
		}
		return node;
	}

	public static Node parseUnary(Lexer lexer) {
		Lexer.Token token = lexer.nextToken();
		if (token == Lexer.Token.Value) {
			return new Node(token, lexer.getPosition(), lexer.getText());
		}

		if (!token.isUnary()) {
			throw new Error("Unary operator expected");
		}

		Node root = new Node(token, lexer.getPosition(), lexer.getText());
		root.right = parseUnary(lexer);
		return root;
	}

	public static Node parseBinary(Lexer lexer, int minPrecedence) {
		Node root = parseUnary(lexer);
		while (lexer.hasNext()) {
			Lexer.Token token = lexer.nextToken();
			if (!token.isBinary()) {
				throw new Error("Binary operator expected, got: " + token);
			}
			if (token.precedence <= minPrecedence) {
				if (token.precedence < minPrecedence) {
					lexer.back();
					break;
				}
				if (!token.right2left) {
					lexer.back();
					break;
				}
			}
			Node node = new Node(token, lexer.getPosition(), lexer.getText());
			node.right = parseBinary(lexer, token.precedence);
			node.left = root;
			root = node;
		}

		return root;
	}

	public static class Node {
		public final Lexer.Token token;
		public final int position;
		public final String text;

		public Node left;
		public Node right;

		public Node(Lexer.Token token, int position, String text) {
			this.token = token;
			this.position = position;
			this.text = text;
		}
	}
}
