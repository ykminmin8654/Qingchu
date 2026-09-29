package qingchu.lexer;

import qingchu.error.ErrorKind;
import qingchu.error.QingchuError;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Turns Qingchu source text into a list of Tokens.
 *
 * RAW CONTENT MODE:
 *   After emitting WORD(print) or WORD(ask), the lexer switches
 *   to raw content mode. It captures everything until " ." as a
 *   single CONTENT token. No tokenization inside.
 */
public class Lexer {

    private final CharStream chars;
    private final List<Token> tokens = new ArrayList<>();
    private final Stack<Integer> indents = new Stack<>();
    private boolean atLineStart = true;
    private boolean rawContent = false;

    public Lexer(String source) {
        this.chars = new CharStream(source);
        this.indents.push(0);
    }

    public List<Token> lex() {
        while (!chars.atEnd()) {
            if (atLineStart) {
                handleIndentation();
                if (chars.atEnd()) break;
            }

            if (rawContent) {
                captureRawContent();
                continue;
            }

            char c = chars.peek();

            if (c == '\n' || c == '\r') {
                chars.advance();
                if (c == '\r' && !chars.atEnd() && chars.peek() == '\n') {
                    chars.advance();
                }
                addToken(TokenType.NEWLINE, "\\n");
                atLineStart = true;

            } else if (c == ' ') {
                handleSpace();

            } else if (c == '\t') {
                chars.advance();
                addToken(TokenType.SPACE, " ");

            } else if (c == '\u00A0') {
                chars.advance();
                addToken(TokenType.SPACE, " ");

            } else if (c == '\u200B' || c == '\u200C'
                    || c == '\u200D' || c == '\uFEFF') {
                chars.advance();

            } else if (c == ',') {
                chars.advance();
                addToken(TokenType.COMMA, ",");

            } else if (c == '(') {
                chars.advance();
                addToken(TokenType.PAREN_OPEN, "(");

            } else if (c == ')') {
                chars.advance();
                addToken(TokenType.PAREN_CLOSE, ")");

            } else if (c == '!') {
                lexComment();

            } else if (c == '\\') {
                lexEscape();

            } else if (isDigit(c)) {
                lexNumber();

            } else if (isWordChar(c)) {
                lexWord();

            } else if (c == '.') {
                lexWord();

            } else if (isOperator(c)) {
                chars.advance();
                addToken(TokenType.WORD, String.valueOf(c));

            } else {
                throw new QingchuError(
                    ErrorKind.UNKNOWN_CHARACTER,
                    chars.line(),
                    "unknown statement"
                );
            }
        }

        while (indents.peek() > 0) {
            indents.pop();
            addToken(TokenType.DEDENT, "");
        }

        addToken(TokenType.EOF, "");
        return tokens;
    }

    private void captureRawContent() {
        int startLine = chars.line();
        int startCol = chars.column();
        StringBuilder sb = new StringBuilder();

        while (!chars.atEnd()) {
            char c = chars.peek();

            if (c == ' ' && chars.peekNext() == '.') {
                break;
            }

            if (c == '\n' || c == '\r') {
                break;
            }

            sb.append(chars.advance());
        }

        tokens.add(new Token(TokenType.CONTENT, sb.toString(), startLine, startCol));
        rawContent = false;
    }

    private void handleIndentation() {
        int spaces = 0;
        while (!chars.atEnd()) {
            char c = chars.peek();
            if (c == ' ') {
                spaces++;
                chars.advance();
            } else if (c == '\t') {
                spaces += 4;
                chars.advance();
            } else if (c == '\u00A0') {
                spaces++;
                chars.advance();
            } else {
                break;
            }
        }

        if (chars.atEnd() || chars.peek() == '\n' || chars.peek() == '\r') {
            return;
        }

        int current = indents.peek();

        if (spaces > current) {
            indents.push(spaces);
            addToken(TokenType.INDENT, "");
        } else if (spaces < current) {
            while (indents.peek() > spaces) {
                indents.pop();
                addToken(TokenType.DEDENT, "");
            }
            if (indents.peek() != spaces) {
                throw new QingchuError(
                    ErrorKind.BAD_INDENT, chars.line(), "block");
            }
        }

        atLineStart = false;
    }

    private void handleSpace() {
        if (chars.peekNext() == '.') {
            chars.advance();
            chars.advance();
            addToken(TokenType.TERMINATOR, " .");
        } else {
            chars.advance();
            addToken(TokenType.SPACE, " ");
        }
    }

    private void lexEscape() {
        chars.advance();
        if (chars.atEnd() || chars.peek() == '\n' || chars.peek() == '\r') {
            throw new QingchuError(
                ErrorKind.ESCAPE_AT_END, chars.line(), "unknown statement");
        }
        char escaped = chars.advance();
        addToken(TokenType.WORD, String.valueOf(escaped));
    }

    private void lexComment() {
        chars.advance();
        if (!chars.match('!')) {
            throw new QingchuError(
                ErrorKind.UNKNOWN_CHARACTER, chars.line(), "comment");
        }
        if (chars.match('!')) {
            lexMultiLineComment();
        } else {
            while (!chars.atEnd()
                    && chars.peek() != '\n'
                    && chars.peek() != '\r') {
                chars.advance();
            }
        }
    }

    private void lexMultiLineComment() {
        while (!chars.atEnd()) {
            if (chars.peek() == '!' && chars.peekNext() == '!') {
                chars.advance();
                chars.advance();
                if (chars.match('!')) return;
            } else {
                chars.advance();
            }
        }
        throw new QingchuError(
            ErrorKind.UNKNOWN_CHARACTER, chars.line(), "comment");
    }

    private void lexNumber() {
        StringBuilder sb = new StringBuilder();
        int startLine = chars.line();
        int startCol = chars.column();

        while (!chars.atEnd() && isDigit(chars.peek())) {
            sb.append(chars.advance());
        }

        if (!chars.atEnd()
                && chars.peek() == '.'
                && isDigit(chars.peekNext())) {
            sb.append(chars.advance());
            while (!chars.atEnd() && isDigit(chars.peek())) {
                sb.append(chars.advance());
            }
        }

        tokens.add(new Token(TokenType.NUMBER, sb.toString(), startLine, startCol));
    }

    private void lexWord() {
        StringBuilder sb = new StringBuilder();
        int startLine = chars.line();
        int startCol = chars.column();

        while (!chars.atEnd()) {
            char c = chars.peek();
            if (isWordChar(c) || c == '.') {
                sb.append(chars.advance());
            } else if (c == '\\') {
                chars.advance();
                if (chars.atEnd() || chars.peek() == '\n' || chars.peek() == '\r') {
                    throw new QingchuError(
                        ErrorKind.ESCAPE_AT_END, chars.line(), "word");
                }
                sb.append(chars.advance());
            } else {
                break;
            }
        }

        Token wordTok = new Token(TokenType.WORD, sb.toString(),
                startLine, startCol);
        tokens.add(wordTok);

        if (wordTok.text.equals("print") || wordTok.text.equals("ask")) {
            if (!chars.atEnd() && chars.peek() == ' ') {
                chars.advance();
            }
            rawContent = true;
        }
    }

    private void addToken(TokenType type, String text) {
        tokens.add(new Token(type, text, chars.line(), chars.column()));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private boolean isOperator(char c) {
        return c == '=' || c == '>'
            || c == '<' || c == '+'
            || c == '-' || c == '*'
            || c == '/';
    }
}