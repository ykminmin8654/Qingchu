package qingchu.runtime;

/**
 * A runtime value in Qingchu.
 *
 * A value has:
 *   - a Type   (NUMBER, STRING, BOOLEAN)
 *   - data     (the actual number, string, or boolean)
 *
 * Values are immutable — once created, they don't change.
 * To "change" a binding, you create a new Value and replace it.
 */
public class Value {

    public final Type type;
    private final Object data;

    private Value(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    // === Constructors ===

    public static Value number(double n) {
        return new Value(Type.NUMBER, n);
    }

    public static Value string(String s) {
        return new Value(Type.STRING, s);
    }

    public static Value bool(boolean b) {
        return new Value(Type.BOOLEAN, b);
    }

    // === Accessors ===

    public double asNumber() {
        if (type != Type.NUMBER) {
            throw new IllegalStateException("not a number: " + this);
        }
        return (double) data;
    }

    public String asString() {
        if (type != Type.STRING) {
            throw new IllegalStateException("not a string: " + this);
        }
        return (String) data;
    }

    public boolean asBoolean() {
        if (type != Type.BOOLEAN) {
            throw new IllegalStateException("not a boolean: " + this);
        }
        return (boolean) data;
    }

    // === Display ===

    /**
     * How this value looks when printed.
     * Numbers print without ".0" if whole.
     * Strings print as-is.
     * Booleans print as "true" or "false".
     */
    public String display() {
        switch (type) {
            case NUMBER:
                double n = (double) data;
                if (n == Math.floor(n) && !Double.isInfinite(n)) {
                    return String.valueOf((long) n);
                }
                return String.valueOf(n);
            case STRING:
                return (String) data;
            case BOOLEAN:
                return ((boolean) data) ? "true" : "false";
            default:
                return data.toString();
        }
    }

    @Override
    public String toString() {
        return type + "(" + display() + ")";
    }
}