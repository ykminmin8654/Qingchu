package qingchu.parse.nodes;

import qingchu.parse.Node;

/**
 * A parsed "new" statement — reassigns an existing binding.
 */
public record NewNode(
    String target,
    String expression,
    String context,
    int line
) implements Node {

    @Override
    public String statementName() {
        return "new statement";
    }
}