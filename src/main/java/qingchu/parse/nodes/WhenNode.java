package qingchu.parse.nodes;

import qingchu.parse.Node;

import java.util.List;

/**
 * A parsed "when" loop.
 */
public record WhenNode(
    String conditionLeft,
    String conditionOperator,
    String conditionRight,
    List<Node> body,
    String context,
    int line
) implements Node {

    @Override
    public String statementName() {
        return "when statement";
    }
}