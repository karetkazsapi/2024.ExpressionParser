import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException, Error {
//		String expression = "1 - x * x - y * y / 2";
//		String expression = "min(4, 2, 2, 1)";
//		String expression = "5 + 5 * 2";
//		String expression = "1 / 0";
		String expression = "min(0)";

		Lexer lexer = new Lexer(expression);
		/*while (lexer.hasNext()) {
			Lexer.Token token = lexer.nextToken();
			System.out.println("token: " + token + ", position" + lexer.getPosition() + ", text: " +lexer.getText());
		}*/

		Parser.Node root = Parser.parseAll(lexer);
		System.out.print("abstract syntax tree: ");
		print(System.out, root);
		System.out.println();

		Evaluator evaluator = new ImageEvaluator();
//		evaluator.evaluate(Parser.parseAll(new Lexer("x = 3")));
//		evaluator.evaluate(Parser.parseAll(new Lexer("y = 3")));
		System.out.println("expression: " + expression);
		System.out.println("result: " + evaluator.evaluate(root));
	}

	public static void print(Appendable out, Parser.Node node) throws IOException {
		if (node == null) {
			return;
		}
		if (node.getLeft() == null && node.getRight() == null) {
			out.append(node.getText());
			return;
		}
		out.append('(');
		if (node.getLeft() != null) {
			print(out, node.getLeft());
			out.append(' ');
		}
		switch (node.getToken()) {
			case Fun:
				out.append("()");
				break;

			default:
				out.append(node.getText());
				break;
		}
		if (node.getRight() != null) {
			out.append(' ');
			print(out, node.getRight());
		}
		out.append(')');
	}
}
