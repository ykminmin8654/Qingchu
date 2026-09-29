package qingchu.lexer;

/**
 * Every kind of token in Qingchu source code.
 */
public enum TokenType {

    // === Literals ===
    WORD,        // a bare word: Hello, Bob, name_string
    NUMBER,      // 5, 3.14, -5
    CONTENT,     // raw content after "print"/"ask" until " ."

    // === Punctuation ===
    TERMINATOR,  // " ."  (space + period)
    COMMA,       // ,
    UNDERSCORE,  // _
    BACKSLASH,   // \
    PAREN_OPEN,  // (
    PAREN_CLOSE, // )

    // === Whitespace ===
    SPACE,       // a single space

    // === Comments ===
    COMMENT,     // !! ... or !!! ... !!!

    // === Structure ===
    INDENT,      // indentation increased
    DEDENT,      // indentation decreased
    NEWLINE,     // end of a line
    EOF          // end of file
}