package qingchu.lexer;

/**
 * Every kind of token in Qingchu source code.
 *
 * A token is the smallest unit of meaning the lexer produces.
 * The parser consumes a stream of these and builds an AST.
 */
public enum TokenType {

    // === Literals ===
    WORD,        // a bare word: Hello, Bob, name_string
    NUMBER,      // 5, 3.14, -5

    // === Punctuation ===
    TERMINATOR,  // " ."  (space + period) — ends content
    COMMA,       // ,      — separates list items
    UNDERSCORE,  // _      — marks a reference
    BACKSLASH,   // \      — escapes the next character
    PAREN_OPEN,  // (      — starts a function call
    PAREN_CLOSE, // )      — ends a function call

    // === Comments ===
    COMMENT,     // !! ... or !!! ... !!!

    // === Structure ===
    INDENT,      // indentation increased
    DEDENT,      // indentation decreased
    NEWLINE,     // end of a line
    EOF          // end of file
}