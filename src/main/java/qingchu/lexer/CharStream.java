package qingchu.lexer;

/**
 * Walks through Qingchu source code character by character.
 *
 * The Lexer uses this to read the source without worrying about
 * indices, line numbers, or bounds checking.
 *
 * Key operations:
 *   peek()      — look at the current char without moving
 *   peekNext()  — look at the next char
 *   advance()   — move forward one char, return the one we left
 *   match(c)    — if the current char is c, advance and return true
 *   atEnd()     — are we at the end of the source?
 */
public class CharStream {

    private final String source;
    private int index = 0;
    private int line = 1;
    private int column = 1;

    public CharStream(String source) {
        this.source = source;
    }

    /** The character at the current position, or '\0' if at end. */
    public char peek() {
        if (atEnd()) return '\0';
        return source.charAt(index);
    }

    /** The character after the current one, or '\0' if at end. */
    public char peekNext() {
        if (index + 1 >= source.length()) return '\0';
        return source.charAt(index + 1);
    }

    /** Move forward one character. Returns the character we left. */
    public char advance() {
        char c = source.charAt(index);
        index++;
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    /** If the current character is c, advance past it and return true. */
    public boolean match(char c) {
        if (atEnd()) return false;
        if (source.charAt(index) != c) return false;
        advance();
        return true;
    }

    /** Are we at the end of the source? */
    public boolean atEnd() {
        return index >= source.length();
    }

    /** The current line number (1-based). */
    public int line() {
        return line;
    }

    /** The current column (1-based). */
    public int column() {
        return column;
    }
}