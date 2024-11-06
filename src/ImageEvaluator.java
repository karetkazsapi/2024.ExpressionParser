import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

class ImageEvaluator extends Evaluator {
	private final double[] vars;

	public ImageEvaluator() {
		this.vars = new double[128];
		for (int i = '0'; i <= '9'; i++) {
			vars[i] = i - '0';
		}
	}

	@Override
	public double value(Parser.Node node) throws Error {
		if (node.getText().length() == 1) {
			return vars[node.getText().charAt(0)];
		}
		return super.value(node);
	}

	@Override
	public double evaluate(Parser.Node node) throws Error {
		switch (node.getToken()) {
			case Set:
				if (node.getLeft().getToken() != Lexer.Token.Value) {
					throw new Error("set can only modify variables", node);
				}
				if (node.getLeft().getText().length() != 1) {
					throw new Error("variables can be single characters", node.getLeft());
				}
				char chr = node.getLeft().getText().charAt(0);
				if (chr >= '0' && chr <= '9') {
					throw new Error("variables can not be numbers", node.getLeft());
				}
				return vars[chr] = evaluate(node.getRight());

			case Coma:
				// enable chain of expressions, returning the value of the last one
				evaluate(node.getLeft());
				return evaluate(node.getRight());
		}

		return super.evaluate(node);
	}

	public static void main(String[] args) throws IOException, Error {
		int width = 1024 / 2;
		int height = width;

		String expression = "h = x * x + y * y, h > 1 || (" +
				"d = abs(y) - h," +
				"a = d - 0.23," +
				"b = h - 1," +
				"c = sign(a * b * (y + x + (y - x) * sign(d)))," +
				"c = mix(c, 0.0f, smoothstep(1.00f, h, 0.98f))," +
				"c = mix(c, 1.0f, smoothstep(1.02f, h, 1.00f))," +
				"c)";

		Lexer lexer = new Lexer(expression);
		ImageEvaluator evaluator = new ImageEvaluator();

		long parseTime = System.nanoTime();
		Parser.Node root = Parser.parseAll(lexer);
		parseTime = System.nanoTime() - parseTime;

		// evaluate first time (probably bytecode will be executed, no jit yet)
		long eval0Time = System.nanoTime();
		evaluator.vars['x'] =  0;
		evaluator.vars['y'] =  0;
		evaluator.evaluate(root);
		eval0Time = System.nanoTime() - eval0Time;

		// write the value of each evaluation to file
		long execTime = System.nanoTime();
		try (FileWriter img = new FileWriter("test.ppm")) {
			img.append("P2\n")
				.append(String.valueOf(width))
				.append(" ")
				.append(String.valueOf(height))
				.append(" 255\n");

			for (int y = 0; y < height; y += 1) {
				for (int x = 0; x < width; x += 1) {
					evaluator.vars['x'] = 2 * x / (double) width - 1;
					evaluator.vars['y'] = 2 * y / (double) height - 1;
					double value = 256 * evaluator.evaluate(root);
					value = Math.min(Math.max(value, 0), 255);
					if (x > 0) {
						img.append(" ");
					}
					img.append(String.valueOf((int) value));
				}
				img.append("\n");
			}
		}
		execTime = System.nanoTime() - execTime;

		// evaluate a single time (this time the evaluate function should be jit compiled)
		long eval1Time = System.nanoTime();
		evaluator.vars['x'] =  0;
		evaluator.vars['y'] =  0;
		evaluator.evaluate(root);
		eval1Time = System.nanoTime() - eval1Time;

		double unit = TimeUnit.MILLISECONDS.toNanos(1);
		System.out.println("parseTime: " + parseTime / unit);
		System.out.println("eval0Time: " + eval0Time / unit);
		System.out.println("eval1Time: " + eval1Time / unit);
		System.out.println("execTime: " + execTime / unit);
	}
}
