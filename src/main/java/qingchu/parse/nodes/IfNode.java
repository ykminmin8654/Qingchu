package qingchu.parse.nodes;

import qingchu.parse.Node;

import java.util.List;

/**
 * A parsed "if" statement.
 */
public record IfNode(
    String conditionLeft,
    String conditionOperator,
    String conditionRight,
    List<Node> thenBlock,
    List<Node> elseBlock,
    String context,
    int line
) implements Node {

    @Override
    public String statementName() {
        return "if statement";
    }

    public boolean hasElse() {
        return elseBlock != null;
    }
}