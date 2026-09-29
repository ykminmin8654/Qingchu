package qingchu.parse;

/**
 * The base type for every AST node.
 *
 * Every statement in Qingchu becomes a class that implements Node.
 *
 * The node types are:
 *   LetNode        — a "let" declaration
 *   NewNode        — a "new" reassignment
 *   PrintNode      — a "print" statement
 *   IfNode         — an "if" statement
 *   WhenNode       — a "when" loop
 *   DefineNode     — a "define" function
 *   CalculateNode  — a "calculate" return
 *   AskNode        — an "ask" input statement
 *   CodeNode       — a "code_*" embedded-language block
 */
public interface Node {

    /** The line number where this statement appears. */
    int line();

    /** A human name for this statement, used in error messages. */
    String statementName();
}