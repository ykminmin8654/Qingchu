package qingchu.parse.nodes;

import qingchu.parse.Node;

/**
 * A parsed "code_" statement.
 *
 * Source form:
 *     code_python
 *         print("hello")
 *     code_end
 *
 * A CodeNode captures raw text in a foreign language.
 * It doesn't parse the language — it just holds the text.
 *
 * The "language" field is the part after "code_":
 *   code_python → "python"
 *   code_latex  → "latex"
 *   code_java   → "java"
 *   code_html   → "html"
 *   code_css    → "css"
 *
 * The "content" field is the raw captured text.
 */
public record CodeNode(
    String language,
    String content,
    int line
) implements Node {

    @Override
    public String statementName() {
        return "code statement";
    }
}