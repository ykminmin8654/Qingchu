package qingchu.parse.nodes;

import qingchu.parse.Node;

import java.util.List;

/**
 * A parsed "define" statement.
 *
 * Source form:
 *     define double(x) as x * 2
 *         x ts number
 *         double ts number
 *
 * A function has:
 *   - a name
 *   - a list of parameters (names)
 *   - type declarations for parameters and the return type
 *   - a body (list of statements), or null if there's no "as"
 */
public record DefineNode(
    String name,
    List<String> parameters,
    List<TypeDecl> types,
    List<Node> body,
    int line
) implements Node {

    @Override
    public String statementName() {
        return "define statement";
    }

    /**
     * One "ts" declaration inside a define block.
     * For example: "x ts number" → TypeDecl("x", "number")
     */
    public record TypeDecl(String name, String typeName) {}
}