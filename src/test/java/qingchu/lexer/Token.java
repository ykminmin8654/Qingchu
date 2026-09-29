package qingchu.lexer;

/**
 * One token in Qingchu source code.
 *
 * A token has:
 *   - a type  (what kind of token it is)
 *   - text    (the actual characters it came from)
 *   - line    (which line in the source it appeared on)
 *   - column  (which column in the line it appeared at)
 *
 * The line and column are used for error messages, like:
 *   error missing terminator on line 1(print statement)
 */
public class Token {

    public final TokenType type;
    public final String text;
    public final int line;
    public final int column;

    public Token(TokenType type, String text, int line, int column) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return type + "(" + text + ") @" + line + ":" + column;
    }
}