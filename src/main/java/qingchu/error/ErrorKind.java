package qingchu.error;

/**
 * Every kind of error Qingchu can produce.
 *
 * Each error kind has a message — the words that appear
 * in the error output, like:
 *
 *   error missing terminator on line 1(print statement)
 *
 * The message is the part between "error " and " on line N(...)".
 */
public enum ErrorKind {

    // === Lexer errors ===
    MISSING_TERMINATOR("missing terminator"),
    ESCAPE_AT_END("escape at end of line"),
    UNKNOWN_CHARACTER("unknown character"),
    BAD_INDENT("bad indentation"),

    // === Parser errors ===
    UNEXPECTED_INDENT("unexpected indent"),
    EXPECTED_TYPE("expected type declaration"),
    EXPECTED_BLOCK("expected block"),
    UNEXPECTED_TOKEN("unexpected token"),

    // === Interpreter errors ===
    UNDEFINED_VARIABLE("undefined variable"),
    CANNOT_REASSIGN_CONSTANT("cannot reassign constant"),
    TYPE_MISMATCH("type mismatch"),
    INVALID_INPUT("invalid input"),

    // === Later ===
    UNDEFINED_FUNCTION("undefined function"),
    WRONG_ARGUMENT_COUNT("wrong number of arguments");

    private final String message;

    ErrorKind(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}