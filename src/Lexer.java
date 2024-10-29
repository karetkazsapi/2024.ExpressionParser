public class Lexer {
	private final boolean[] delimiters;

	private final String input;

	private int previous = -1;
	private int start;
	private int end;

	public Lexer(String input) {
		this.input = input;
		this.delimiters = new boolean[256];

		for (int i = 0; i < delimiters.length; i++) {
			delimiters[i] = isWhite((char) i);
		}

		for (Token token : Token.values()) {
			if (token.text != null) {
				delimiters[token.text.charAt(0)] = true;
			}
		}
	}

	public boolean hasNext() {
		return end < input.length();
	}

	public Token nextToken() {
		while (end < input.length()) {
			if (!isWhite(input.charAt(end))) {
				break;
			}
			end ++;
		}

		previous = start;
		start = end;
		while (end < input.length()) {
			if (isDelimiter(input.charAt(end))) {
				break;
			}
			end ++;
		}

		if (start < end) {
			return Token.Value;
		}

		Token match = null;
		for (Token token : Token.values()) {
			if (token.text == null || !input.startsWith(token.text, start)) {
				continue;
			}
			if (match == null || match.text.length() < token.text.length()) {
				match = token;
			}
		}

		if (match != null) {
			end += match.text.length();
			return match;
		}

		end += 1;
		return Token.Undefined;
	}

	public int getPosition() {
		return start;
	}

	public String getText() {
		return input.substring(start, end);
	}

	private boolean isWhite(char c) {
		return Character.isWhitespace(c);
	}

	private boolean isDelimiter(char c) {
		return c < delimiters.length && delimiters[c];
	}

	public void back() {
		if (previous < 0) {
			throw new Error("can not go back");
		}
		end = start;
		start = previous;
		previous = -1;
	}

	public enum Token {
		Pow(14, true, "**"),
		Mul(13, false, "*"),
		Div(13, false,"/"),
		Add(12, false,"+"),
		Sub(12, false,"-"),
		Value(0, false, null),
		Undefined(0, false, null);

		public final int precedence;
		public final boolean right2left;
		public final String text;

		Token(int precedence, boolean right2left, String text) {
			this.precedence = precedence;
			this.right2left = right2left;
			this.text = text;
		}

		public boolean isUnary() {
			return this == Add || this == Sub;
		}

		public boolean isBinary() {
			return this != Value && this != Undefined;
		}
	}
}
