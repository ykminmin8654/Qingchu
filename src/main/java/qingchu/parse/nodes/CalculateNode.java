package qingchu.parse.nodes;

import qingchu.parse.Node;

/**
 * A parsed "calculate" statement.
 *
 * Source form:
 *     calculate result .
 *
 * "calculate" is the Qingchu word for "return".
 * It ends the current function and gives back a value.
 */
public record CalculateNode(
    String expression,
    int line
) implements Node {

    @Override
    public String statementName() {
            return "calculate statement";
    }
}