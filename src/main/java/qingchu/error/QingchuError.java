package qingchu.error;

/**
 * The exception Qingchu throws when something goes wrong.
 *
 * It carries:
 *   - an ErrorKind   (what went wrong)
 *   - a line number  (where it went wrong)
 *   - a statement    (which statement it was in)
 *
 * And it formats them into the Qingchu error format:
 *
 *   error missing terminator on line 1(print statement)
 */
public class QingchuError extends RuntimeException {

    public final ErrorKind kind;
    public final int line;
    public final String statement;

    public QingchuError(ErrorKind kind, int line, String statement) {
        super(kind.message() + " on line " + line + "(" + statement + ")");
        this.kind = kind;
        this.line = line;
        this.statement = statement;
    }

    /**
     * Formats the full error message, including the leading "error ".
     */
    public String fullMessage() {
        return "error " + kind.message() + " on line " + line + "(" + statement + ")";
    }
}