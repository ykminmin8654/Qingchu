package qingchu.runtime;

import qingchu.error.ErrorKind;
import qingchu.error.QingchuError;
import qingchu.parse.Node;
import qingchu.parse.nodes.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Runs a Qingchu AST.
 *
 * PRINT CONTENT:
 *   Each WORD is checked against the scope.
 *   - If it's a declared binding -> print its value.
 *   - If not -> print the word as text.
 */
public class Interpreter {

    private final Scope scope = new Scope();
    private final Map<String, DefineNode> functions = new HashMap<>();
    private final Scanner input = new Scanner(System.in);

    public void run(List<Node> program) {
        for (Node node : program) {
            execute(node);
        }
    }

    private void execute(Node node) {
        if (node instanceof LetNode n)         executeLet(n);
        else if (node instanceof NewNode n)    executeNew(n);
        else if (node instanceof PrintNode n)  executePrint(n);
        else if (node instanceof IfNode n)     executeIf(n);
        else if (node instanceof WhenNode n)   executeWhen(n);
        else if (node instanceof DefineNode n) executeDefine(n);
        else if (node instanceof CalculateNode n) executeCalculate(n);
        else if (node instanceof AskNode n)    executeAsk(n);
        else throw new IllegalStateException("unknown node: " + node);
    }

    // === let ===

    private void executeLet(LetNode n) {
        Type type = typeFromName(n.typeName(), n.line());
        Value value = evaluateExpression(n.expression(), n.context(), type, n.line());

        Binding.Modifier mod = n.modifier().equals("constant")
            ? Binding.Modifier.CONSTANT : Binding.Modifier.NONE;

        Binding b = new Binding(n.name(), type, mod, n.instance(), value);

        if (scope.has(b.key())) {
            throw new QingchuError(
                ErrorKind.UNDEFINED_VARIABLE, n.line(), "let statement");
        }
        scope.define(b);
    }

    // === new ===

    private void executeNew(NewNode n) {
        String suffix = n.context().equals("math") ? "_number" : "_string";
        String targetKey = n.target().endsWith(suffix)
            ? n.target() : n.target() + suffix;

        Binding target = scope.get(targetKey);
        if (target == null) {
            throw new QingchuError(
                ErrorKind.UNDEFINED_VARIABLE, n.line(), "new statement");
        }
        if (target.isConstant()) {
            throw new QingchuError(
                ErrorKind.CANNOT_REASSIGN_CONSTANT, n.line(), "new statement");
        }

        Value result = evaluateExpression(
            n.expression(), n.context(), target.type, n.line());
        scope.set(targetKey, result);
    }

    // === print ===

    private void executePrint(PrintNode n) {
        StringBuilder sb = new StringBuilder();
        String context = n.context();
        String suffix = context.equals("math") ? "_number"
                      : context.equals("string") ? "_string" : "";

        for (PrintNode.Piece p : n.pieces()) {
            switch (p.kind()) {
                case SPACE:
                    sb.append(" ");
                    break;
                case WORD:
                    String word = p.text();
                    String key;
                    if (word.contains("_")) {
                        key = word;
                    } else if (!suffix.isEmpty()) {
                        key = word + suffix;
                    } else {
                        key = null;
                    }

                    Binding b = (key != null) ? scope.get(key) : null;
                    if (b != null) {
                        sb.append(b.value.display());
                    } else {
                        sb.append(word);
                    }
                    break;
            }
        }
        System.out.println(sb.toString());
    }

    // === if ===

    private void executeIf(IfNode n) {
        boolean result = evaluateCondition(
            n.conditionLeft(), n.conditionOperator(), n.conditionRight(),
            n.context(), n.line());

        List<Node> block = result ? n.thenBlock() : n.elseBlock();
        if (block != null) {
            for (Node stmt : block) execute(stmt);
        }
    }

    // === when ===

    private void executeWhen(WhenNode n) {
        while (true) {
            boolean result = evaluateCondition(
                n.conditionLeft(), n.conditionOperator(), n.conditionRight(),
                n.context(), n.line());
            if (!result) break;
            for (Node stmt : n.body()) execute(stmt);
        }
    }

    // === define ===

    private void executeDefine(DefineNode n) {
        functions.put(n.name(), n);
    }

    private void executeCalculate(CalculateNode n) {
        // Placeholder
    }

    // === ask ===

    private void executeAsk(AskNode n) {
        if (!n.question().isEmpty()) {
            System.out.print(n.question() + " ");
        }
        String answer = input.nextLine();

        Type type = typeFromName(n.typeName(), n.line());
        Value value;

        if (type == Type.NUMBER) {
            try {
                value = Value.number(Double.parseDouble(answer));
            } catch (NumberFormatException e) {
                throw new QingchuError(
                    ErrorKind.INVALID_INPUT, n.line(), "ask statement");
            }
        } else if (type == Type.BOOLEAN) {
            if (answer.equalsIgnoreCase("true")) value = Value.bool(true);
            else if (answer.equalsIgnoreCase("false")) value = Value.bool(false);
            else throw new QingchuError(
                ErrorKind.INVALID_INPUT, n.line(), "ask statement");
        } else {
            value = Value.string(answer);
        }

        Binding b = new Binding(n.target(), type, value);
        if (scope.has(b.key())) scope.set(b.key(), value);
        else scope.define(b);
    }

    // === Expression evaluation ===

    private Value evaluateExpression(String expr, String context, Type expectedType, int line) {
        expr = expr.trim();
        if (expr.isEmpty()) return Value.string("");

        try {
            double d = Double.parseDouble(expr);
            if (expectedType == Type.NUMBER) return Value.number(d);
            if (expectedType == Type.STRING) return Value.string(expr);
            return Value.number(d);
        } catch (NumberFormatException ignored) {}

        String[] ops = {" + ", " - ", " * ", " / "};
        for (String op : ops) {
            int idx = expr.indexOf(op);
            if (idx > 0) {
                String left = expr.substring(0, idx).trim();
                String right = expr.substring(idx + op.length()).trim();
                String operator = op.trim();
                double l = resolveNumber(left, context, line);
                double r = resolveNumber(right, context, line);
                double res;
                switch (operator) {
                    case "+": res = l + r; break;
                    case "-": res = l - r; break;
                    case "*": res = l * r; break;
                    case "/":
                        if (r == 0) throw new QingchuError(
                            ErrorKind.TYPE_MISMATCH, line, "statement");
                        res = l / r;
                        break;
                    default:
                        throw new QingchuError(
                            ErrorKind.UNEXPECTED_TOKEN, line, "statement");
                }
                return Value.number(res);
            }
        }

        String suffix = context.equals("math") ? "_number"
                      : context.equals("string") ? "_string" : "";
        String key = expr.contains("_") ? expr
                   : !suffix.isEmpty() ? expr + suffix : expr;

        Binding b = scope.get(key);
        if (b == null) {
            if (expectedType == Type.STRING) return Value.string(expr);
            throw new QingchuError(
                ErrorKind.UNDEFINED_VARIABLE, line, "statement");
        }
        return b.value;
    }

    private double resolveNumber(String text, String context, int line) {
        text = text.trim();
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ignored) {}

        String suffix = context.equals("math") ? "_number"
                      : context.equals("string") ? "_string" : "";
        String key = text.contains("_") ? text
                   : !suffix.isEmpty() ? text + suffix : text;

        Binding b = scope.get(key);
        if (b == null) throw new QingchuError(
            ErrorKind.UNDEFINED_VARIABLE, line, "statement");
        return b.value.asNumber();
    }

    private boolean evaluateCondition(String leftText, String op,
                                      String rightText, String context, int line) {
        double left = resolveNumber(leftText, context, line);
        double right = resolveNumber(rightText, context, line);
        switch (op) {
            case ">":  return left >  right;
            case "<":  return left <  right;
            case "=":  return left == right;
            case ">=": return left >= right;
            case "<=": return left <= right;
            case "!=": return left != right;
            case "\u2260": return left != right;
            default:
                throw new QingchuError(
                    ErrorKind.UNEXPECTED_TOKEN, line, "statement");
        }
    }

    private Type typeFromName(String name, int line) {
        switch (name) {
            case "number": case "num": case "n": return Type.NUMBER;
            case "string": case "str": case "s": return Type.STRING;
            case "boolean": case "bool": case "b": return Type.BOOLEAN;
            default:
                throw new QingchuError(
                    ErrorKind.TYPE_MISMATCH, line, "statement");
        }
    }
}