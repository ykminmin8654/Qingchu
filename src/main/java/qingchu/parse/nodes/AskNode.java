package qingchu.parse.nodes;

import qingchu.parse.Node;

/**
 * A parsed "ask" statement.
 *
 * Source form:
 *     ask What is your name?.
 *         response = name
 *             name ts string
 *
 * An "ask" has:
 *   - a question (text to print as the prompt)
 *   - a target variable name (where the answer goes)
 *   - a type name (how to parse the answer)
 */
public record AskNode(
    String question,
    String target,
    String typeName,
    int line
) implements Node {

    @Override
    public String statementName() {
        return "ask statement";
    }
}