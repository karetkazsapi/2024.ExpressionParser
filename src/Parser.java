public class Parser {

	public static Node parseAll(Lexer lexer) throws Error {
		Node node = parseBinary(lexer, 0);
		if (lexer.hasNext()) {
			throw new Error("End of input expected, got", lexer.nextToken(), lexer);
		}
		return node;
	}

	private static Node parseUnary(Lexer lexer) throws Error {
		Lexer.Token token = lexer.nextToken();
		if (token == Lexer.Token.Value) {
			return new Node(token, lexer.getPosition(), lexer.getText());
		}

		if (!token.isUnary()) {
			throw new Error("Unary operator expected", token, lexer);
		}

		if (token == Lexer.Token.Fun) {
			if (lexer.nextToken() == Lexer.Token.RParen) {
				// allow empty list arguments: '(' ')'
				return new Node(token, lexer.getPosition(), lexer.getText());
			}

			lexer.backToken();
			Node fun = new Node(token, lexer.getPosition(), lexer.getText());
			fun.right = parseBinary(lexer, 0);
			if (lexer.nextToken() != Lexer.Token.RParen) {
				throw new Error("Right parenthesis expected, got", token, lexer);
			}
			return fun;
		}

		Node root = new Node(token, lexer.getPosition(), lexer.getText());
		root.right = parseUnary(lexer);
		return root;
	}

	private static Node parseBinary(Lexer lexer, int minPrecedence) throws Error {
		Node root = parseUnary(lexer);
		while (lexer.hasNext()) {
			Lexer.Token token = lexer.nextToken();
			switch (token) {
				case Undefined:
				case RParen:
					lexer.backToken();
					return root;
			}

			if (!token.isBinary()) {
				throw new Error("Binary operator expected, got", token, lexer);
			}

			if (token.precedence <= minPrecedence) {
				if (token.precedence < minPrecedence) {
					lexer.backToken();
					break;
				}
				if (!token.right2left) {
					lexer.backToken();
					break;
				}
			}

			if (token == Lexer.Token.Fun) {
				lexer.backToken();
				Node node = parseUnary(lexer);
				node.left = root;
				root = node;
			} else {
				Node node = new Node(token, lexer.getPosition(), lexer.getText());
				node.right = parseBinary(lexer, token.precedence);
				node.left = root;
				root = node;
			}
		}

		return root;
	}

	public static class Node {
		private final Lexer.Token token;
		private final int position;
		private final String text;

		private Node left;
		private Node right;

		public Node(Lexer.Token token, int position, String text) {
			this.token = token;
			this.position = position;
			this.text = text;
		}

		public Lexer.Token getToken() {
			return token;
		}

		public int getPosition() {
			return position;
		}

		public String getText() {
			return text;
		}

		public Node getLeft() {
			return left;
		}

		public Node getRight() {
			return right;
		}
	}
}
