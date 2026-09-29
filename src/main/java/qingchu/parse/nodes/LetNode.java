package qingchu.parse.nodes;

import qingchu.parse.Node;

/**
 * A parsed "let" statement.
 *
 * The value is an expression string, like "5" or "price * quantity".
 * The interpreter evaluates it.
 */
public record LetNode(
    String name,
    String expression,
    String typeName,
    String modifier,
    int instance,
    String context,
    int line
) implements Node {

    @Override
    public String statementName() {
        return "let statement";
    }
}