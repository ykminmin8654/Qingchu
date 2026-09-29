package qingchu.parse;

import qingchu.error.ErrorKind;
import qingchu.error.QingchuError;
import qingchu.lexer.Token;
import qingchu.lexer.TokenType;
import qingchu.parse.nodes.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a list of Tokens into a list of Nodes (the AST).
 *
 * CONTEXT RULES:
 *   "math"   — bare names resolve to _number
 *   "string" — bare names resolve to _string
 *   ""       — no context; only explicit _name references work
 *
 * TERMINATOR RULES:
 *   let (plain)             — block; ends with indentation
 *   math/string let         — flat; ends with " ."
 *   new                     — flat; ends with " ."
 *   print                   — flat; ends with " ."
 *   ask                     — flat; ends with " ."
 *   calculate               — flat; ends with " ."
 *   if / when / define      — block; ends with indentation
 *
 * PRINT / ASK CONTENT:
 *   The lexer produces a single CONTENT token with the raw text.
 *   This parser splits it into WORD and SPACE pieces.
 *   The interpreter decides whether each WORD is a binding or text.
 */
public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Node> parse() {
        return parseStatements("");
    }

    // === Statement dispatch ===

    private List<Node> parseStatements(String context) {
        List<Node> nodes = new ArrayList<>();
        while (!atEnd()) {
            skipNewlines();
            if (atEnd()) break;
            Node node = parseStatement(context);
            if (node != null) nodes.add(node);
        }
        return nodes;
    }

    private Node parseStatement(String context) {
        Token t = peek();
        if (t.type != TokenType.WORD) {
            throw new QingchuError(
                ErrorKind.UNEXPECTED_TOKEN, t.line, "unknown statement");
        }

        // Context prefix?
        if (t.text.equals("math") || t.text.equals("string")) {
            String newContext = t.text;
            advance();
            return parseStatement(newContext);
        }

        switch (t.text) {
            case "let":        return parseLet(context);
            case "new":        return parseNew(context);
            case "print":      return parsePrint(context);
            case "if":         return parseIf(context);
            case "when":       return parseWhen(context);
            case "define":     return parseDefine(context);
            case "ask":        return parseAsk(context);
            case "calculate":  return parseCalculate(context);
        }
        throw new QingchuError(
            ErrorKind.UNEXPECTED_TOKEN, t.line, "unknown statement");
    }

    // === let ===

    private Node parseLet(String context) {
        Token letTok = advance();
        int line = letTok.line;

        String name = expectWord("let statement").text;
        expectWord("let statement"); // "="

        String expression = readExpression();

        if (!context.isEmpty()) {
            // Context let — flat statement, no ts block.
            if (!match(TokenType.TERMINATOR)) {
                throw new QingchuError(
                    ErrorKind.MISSING_TERMINATOR, line, "let statement");
            }
            String typeName = context.equals("math") ? "number" : "string";
            return new LetNode(name, expression, typeName, "", 1, context, line);
        }

        // Plain let — block; requires a ts block.
        skipNewlines();
        if (!match(TokenType.INDENT)) {
            throw new QingchuError(
                ErrorKind.EXPECTED_TYPE, line, "let statement");
        }
        expectWord("let statement"); // variable name
        Token tsTok = expectWord("let statement");
        if (!tsTok.text.equals("ts") && !tsTok.text.equals("type_is")) {
            throw new QingchuError(
                ErrorKind.EXPECTED_TYPE, tsTok.line, "let statement");
        }
        Token modOrType = expectWord("let statement");
        String modifier = "";
        String typeName;
        if (modOrType.text.equals("constant") || modOrType.text.equals("const")) {
            modifier = "constant";
            typeName = expectWord("let statement").text;
        } else {
            typeName = modOrType.text;
        }
        skipNewlines();
        match(TokenType.DEDENT);

        return new LetNode(name, expression, typeName, modifier, 1, context, line);
    }

    // === new ===

    private Node parseNew(String context) {
        Token newTok = advance();
        int line = newTok.line;

        if (context.isEmpty()) {
            throw new QingchuError(
                ErrorKind.UNEXPECTED_TOKEN, line, "new statement");
        }

        String target = expectWord("new statement").text;
        expectWord("new statement"); // "="
        String expression = readExpression();

        if (!match(TokenType.TERMINATOR)) {
            throw new QingchuError(
                ErrorKind.MISSING_TERMINATOR, line, "new statement");
        }
        return new NewNode(target, expression, context, line);
    }

    // === print ===

    private Node parsePrint(String context) {
        Token printTok = advance();
        int line = printTok.line;

        List<PrintNode.Piece> pieces = new ArrayList<>();

        if (peek().type == TokenType.CONTENT) {
            Token content = advance();
            pieces = splitContent(content.text);
        }

        if (!match(TokenType.TERMINATOR)) {
            throw new QingchuError(
                ErrorKind.MISSING_TERMINATOR, line, "print statement");
        }
        return new PrintNode(pieces, context, line);
    }

    // === if ===

    private Node parseIf(String context) {
        Token ifTok = advance();
        int line = ifTok.line;

        String left = expectWord("if statement").text;
        String operator = expectWord("if statement").text;
        String right = expectWord("if statement").text;

        skipNewlines();
        List<Node> thenBlock = parseBlock("if statement", context);

        List<Node> elseBlock = null;
        skipNewlines();
        if (checkWord("else")) {
            advance();
            skipNewlines();
            elseBlock = parseBlock("if statement", context);
        }
        return new IfNode(left, operator, right, thenBlock, elseBlock, context, line);
    }

    // === when ===

    private Node parseWhen(String context) {
        Token whenTok = advance();
        int line = whenTok.line;

        String left = expectWord("when statement").text;
        String operator = expectWord("when statement").text;
        String right = expectWord("when statement").text;

        skipNewlines();
        List<Node> body = parseBlock("when statement", context);
        return new WhenNode(left, operator, right, body, context, line);
    }

    // === define ===

    private Node parseDefine(String context) {
        Token defineTok = advance();
        int line = defineTok.line;

        String name = expectWord("define statement").text;
        List<String> parameters = new ArrayList<>();

        if (match(TokenType.PAREN_OPEN)) {
            while (!atEnd() && peek().type != TokenType.PAREN_CLOSE) {
                Token p = advance();
                if (p.type == TokenType.WORD) parameters.add(p.text);
            }
            match(TokenType.PAREN_CLOSE);
        }

        boolean hasAs = false;
        String inlineExpression = null;

        if (checkWord("as")) {
            advance(); // "as"
            hasAs = true;

            // Read the rest of the line — inline expression, if any.
            String expr = readExpression();
            if (!expr.isEmpty()) {
                inlineExpression = expr;
            }
        }

        skipNewlines();

        List<Node> body = null;
        List<DefineNode.TypeDecl> types = new ArrayList<>();

        if (hasAs) {
            if (match(TokenType.INDENT)) {
                body = new ArrayList<>();
                while (!atEnd() && peek().type != TokenType.DEDENT) {
                    skipNewlines();
                    if (atEnd() || peek().type == TokenType.DEDENT) break;

                    if (peek().type == TokenType.WORD && peekAheadIsTs()) {
                        String n = advance().text;
                        advance(); // "ts"
                        String type = expectWord("define statement").text;
                        types.add(new DefineNode.TypeDecl(n, type));
                    } else {
                        Node stmt = parseStatement(context);
                        if (stmt != null) body.add(stmt);
                    }
                }
                match(TokenType.DEDENT);
            }
        }

        // If inline expression, add it as a "calculate" node at the end.
        if (inlineExpression != null) {
            if (body == null) body = new ArrayList<>();
            body.add(new CalculateNode(inlineExpression, line));
        }

        return new DefineNode(name, parameters, types, body, line);
    }

    // === ask ===

    private Node parseAsk(String context) {
        Token askTok = advance();
        int line = askTok.line;

        String question = "";
        if (peek().type == TokenType.CONTENT) {
            question = advance().text;
        }

        if (!match(TokenType.TERMINATOR)) {
            throw new QingchuError(
                ErrorKind.MISSING_TERMINATOR, line, "ask statement");
        }

        skipNewlines();
        String target = "";
        String typeName = "string";

        if (match(TokenType.INDENT)) {
            if (checkWord("response")) {
                advance();
                expectWord("ask statement"); // "="
                target = expectWord("ask statement").text;
            }
            skipNewlines();
            if (match(TokenType.INDENT)) {
                expectWord("ask statement");
                Token tsTok = expectWord("ask statement");
                if (!tsTok.text.equals("ts") && !tsTok.text.equals("type_is")) {
                    throw new QingchuError(
                        ErrorKind.EXPECTED_TYPE, tsTok.line, "ask statement");
                }
                typeName = expectWord("ask statement").text;
                skipNewlines();
                match(TokenType.DEDENT);
            }
            skipNewlines();
            match(TokenType.DEDENT);
        }
        return new AskNode(question, target, typeName, line);
    }

    // === calculate ===

    private Node parseCalculate(String context) {
        Token calcTok = advance();
        int line = calcTok.line;

        String expression = readExpression();

        if (!match(TokenType.TERMINATOR)) {
            throw new QingchuError(
                ErrorKind.MISSING_TERMINATOR, line, "calculate statement");
        }
        return new CalculateNode(expression, line);
    }

    // === Blocks ===

    private List<Node> parseBlock(String stmtName, String context) {
        List<Node> block = new ArrayList<>();
        if (!match(TokenType.INDENT)) {
            throw new QingchuError(
                ErrorKind.EXPECTED_BLOCK, peek().line, stmtName);
        }
        while (!atEnd() && peek().type != TokenType.DEDENT) {
            skipNewlines();
            if (atEnd() || peek().type == TokenType.DEDENT) break;
            Node stmt = parseStatement(context);
            if (stmt != null) block.add(stmt);
        }
        match(TokenType.DEDENT);
        return block;
    }

    // === Expression reading ===

    private String readExpression() {
        StringBuilder sb = new StringBuilder();
        while (!atEnd()
                && peek().type != TokenType.NEWLINE
                && peek().type != TokenType.DEDENT
                && peek().type != TokenType.INDENT
                && peek().type != TokenType.TERMINATOR) {
            Token tk = advance();
            if (tk.type == TokenType.SPACE) {
                if (sb.length() > 0 && !sb.toString().endsWith(" ")) {
                    sb.append(" ");
                }
            } else {
                if (sb.length() > 0
                        && !sb.toString().endsWith(" ")
                        && !tk.text.equals("=")) {
                    sb.append(" ");
                }
                sb.append(tk.text);
            }
        }
        return sb.toString().trim();
    }

    // === Content splitting ===

    /**
     * Split raw content into WORD and SPACE pieces.
     * The interpreter decides whether each WORD is a binding or text.
     */
    private List<PrintNode.Piece> splitContent(String content) {
        List<PrintNode.Piece> pieces = new ArrayList<>();
        if (content.isEmpty()) return pieces;

        StringBuilder word = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == ' ') {
                if (word.length() > 0) {
                    pieces.add(PrintNode.Piece.word(word.toString()));
                    word.setLength(0);
                }
                pieces.add(PrintNode.Piece.space());
            } else {
                word.append(c);
            }
        }
        if (word.length() > 0) {
            pieces.add(PrintNode.Piece.word(word.toString()));
        }
        return pieces;
    }

    // === Helpers ===

    private boolean atEnd() {
        skipSpaces();
        return rawPeek().type == TokenType.EOF;
    }

    private Token peek() {
        skipSpaces();
        return rawPeek();
    }

    private Token advance() {
        skipSpaces();
        return rawAdvance();
    }

    private Token rawPeek() {
        return tokens.get(pos);
    }

    private Token rawAdvance() {
        Token t = tokens.get(pos);
        if (t.type != TokenType.EOF) pos++;
        return t;
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private boolean match(TokenType type) {
        if (peek().type == type) {
            advance();
            return true;
        }
        return false;
    }

    private boolean checkWord(String word) {
        Token t = peek();
        return t.type == TokenType.WORD && t.text.equals(word);
    }

    private Token expectWord(String stmtName) {
        Token t = peek();
        if (t.type != TokenType.WORD && t.type != TokenType.NUMBER) {
            throw new QingchuError(
                ErrorKind.UNEXPECTED_TOKEN, t.line, stmtName);
        }
        return advance();
    }

    private void skipNewlines() {
        while (true) {
            skipSpaces();
            if (rawPeek().type == TokenType.NEWLINE) rawAdvance();
            else break;
        }
    }

    private void skipSpaces() {
        while (tokens.get(pos).type == TokenType.SPACE) pos++;
    }

    private boolean peekAheadIsTs() {
        int i = pos;
        while (i < tokens.size() && tokens.get(i).type == TokenType.SPACE) i++;
        i++;
        while (i < tokens.size() && tokens.get(i).type == TokenType.SPACE) i++;
        if (i >= tokens.size()) return false;
        Token t = tokens.get(i);
        return t.type == TokenType.WORD
            && (t.text.equals("ts") || t.text.equals("type_is"));
    }
}