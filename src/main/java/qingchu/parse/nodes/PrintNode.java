package qingchu.parse.nodes;

import qingchu.parse.Node;

import java.util.List;

/**
 * A parsed "print" statement.
 *
 * Content is a list of pieces:
 *   WORD  — a word (the interpreter decides if it's a binding or text)
 *   SPACE — whitespace
 */
public record PrintNode(
    List<Piece> pieces,
    String context,
    int line
) implements Node {

    @Override
    public String statementName() {
        return "print statement";
    }

    public record Piece(String text, Kind kind) {
        public enum Kind {
            WORD,
            SPACE
        }

        public static Piece word(String text) {
            return new Piece(text, Kind.WORD);
        }

        public static Piece space() {
            return new Piece(" ", Kind.SPACE);
        }
    }
}