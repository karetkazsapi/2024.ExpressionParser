public class Evaluator {

	protected static final double[] EMPTY_ARGS = {};

	double value(Parser.Node node) throws Error {
		try {
			return Double.parseDouble(node.getText());
		} catch (NumberFormatException e) {
			throw new Error("Invalid value", node, e);
		}
	}

	double function(Parser.Node node, double[] arguments) throws Error {
		switch (node.getText()) {
			case "min": {
				if (arguments.length < 1) {
					throw new Error("At least one argument expected", node);
				}
				double min = arguments[0];
				for (int i = 1; i < arguments.length; i++) {
					min = Math.min(min, arguments[i]);
				}
				return min;
			}

			case "abs":
				if (arguments.length != 1) {
					throw new Error("Single argument expected", node);
				}
				return Math.abs(arguments[0]);

			case "sign":
				if (arguments.length != 1) {
					throw new Error("Single argument expected", node);
				}
				return Math.signum(arguments[0]);

			case "mix": {
				if (arguments.length != 3) {
					throw new Error("Linear interpolation requires three arguments", node);
				}
				// Returns the linear interpolation between two values
				double min = arguments[0];
				double max = arguments[1];
				double t = arguments[2];
				return min + t * (max - min);
			}

			case "smoothstep": {
				if (arguments.length != 3) {
					throw new Error("Hermite interpolation requires three arguments", node);
				}

				// Returns the Hermite interpolation between two values
				double min = arguments[0];
				double max = arguments[1];
				double t = (arguments[2] - min) / (max - min);
				if (t < 0) {
					return 0;
				}
				if (t > 1) {
					return 1;
				}
				return t * t * (3 - 2 * t);
			}

			case "sqrt":
				if (arguments.length != 1) {
					throw new Error("Single argument expected", node);
				}
				return Math.sqrt(arguments[0]);

			case "sin":
				if (arguments.length != 1) {
					throw new Error("Single argument expected", node);
				}
				return Math.sin(arguments[0]);

			case "cos":
				if (arguments.length != 1) {
					throw new Error("Single argument expected", node);
				}
				return Math.cos(arguments[0]);
		}
		throw new Error("Invalid function", node);
	}

	public double evaluate(Parser.Node node) throws Error {
		double left, right;
		switch (node.getToken()) {
			case Value:
				return value(node);

			case Fun:
				if (node.getLeft() == null && node.getRight() == null) {
					// ()
					throw new Error("Invalid function call", node);
				}

				if (node.getLeft() == null) {
					// (3 + 2)
					return evaluate(node.getRight());
				}

				double[] args = EMPTY_ARGS;
				if (node.getRight() != null) {
					int length = 1;
					for (Parser.Node arg = node.getRight(); arg.getToken() == Lexer.Token.Coma; arg = arg.getLeft()) {
						length += 1;
					}
					args = new double[length];
					Parser.Node arg = node.getRight();
					while (arg.getToken() == Lexer.Token.Coma) {
						args[length -= 1] = evaluate(arg.getRight());
						arg = arg.getLeft();
					}
					args[0] = evaluate(arg);
				}
				return function(node.getLeft(), args);

			case Mul:
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left * right;

			case Div:
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left / right;

			case Rem:
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left % right;

			case Add:
				if (node.getLeft() == null) {
					// unary +
					return +evaluate(node.getRight());
				}
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left + right;

			case Sub:
				if (node.getLeft() == null) {
					// unary -
					return -evaluate(node.getRight());
				}
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left - right;

			case Lt:
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left < right ? 1 : 0;

			case Leq:
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left <= right ? 1 : 0;

			case Gt:
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left > right ? 1 : 0;

			case Geq:
				left = evaluate(node.getLeft());
				right = evaluate(node.getRight());
				return left >= right ? 1 : 0;

			case All:
				left = evaluate(node.getLeft());
				if (left == 0) {
					return left;
				}
				return evaluate(node.getRight());

			case Any:
				left = evaluate(node.getLeft());
				if (left != 0) {
					return left;
				}
				return evaluate(node.getRight());
		}
		throw new Error("Invalid operation", node);
	}
}
