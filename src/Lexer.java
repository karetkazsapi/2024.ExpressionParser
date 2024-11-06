public class Lexer {
	private final boolean[] delimiters;

	private final String input;

	private int previous = -1;
	private int start = 0;
	private int end = 0;

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

	public void backToken() throws Error {
		if (previous < 0) {
			throw new Error("can not push back: ", Token.Undefined, this);
		}
		end = start;
		start = previous;
		previous = -1;
	}

	public int getPosition() {
		return start;
	}

	public String getText() {
		if (end > input.length()) {
			return "";
		}
		return input.substring(start, end);
	}

	private boolean isWhite(char c) {
		return Character.isWhitespace(c);
	}

	private boolean isDelimiter(char c) {
		return c < delimiters.length && delimiters[c];
	}

	public enum Token {
		Fun(15, false, "("),

		Mul(13, false, "*"),
		Div(13, false, "/"),
		Rem(13, false, "%"),

		Add(12, false, "+"),
		Sub(12, false, "-"),

		Lt(10, false, "<"),
		Leq(10, false, "<="),
		Gt(10, false, ">"),
		Geq(10, false, ">="),

		All(5, false, "&&"),
		Any(4, false, "||"),

		Set(2, true, "="),

		Coma(1, false, ","),

		Value(0, false, null),
		RParen(0, false, ")"),
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
			// allow some operators to behave as unary, the left branch will be null
			switch (this) {
				case Fun: // (3 + x) * min(x, y)
				case Add: // +9
				case Sub: // -9
					return true;
			}
			return false;
		}

		public boolean isBinary() {
			// disallow some tokens to be used as (binary) operators
			switch (this) {
				case Value:
				case RParen:
				case Undefined:
					return false;
			}
			return true;
		}
	}
}
