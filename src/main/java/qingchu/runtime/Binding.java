package qingchu.runtime;

/**
 * One binding in Qingchu.
 *
 * A binding is what "let" creates. It has:
 *   - a name       (the variable name, like "x" or "name")
 *   - a type       (NUMBER, STRING, BOOLEAN)
 *   - a modifier   (none, or CONSTANT)
 *   - an instance  (1, 2, 3... for duplicate types; 1 means "no number")
 *   - a value      (the actual data)
 *
 * The full reference is built from these parts:
 *   name + "_" + [modifier + "_"] + type + [number]
 *
 * For example:
 *   x_number            → name="x", type=NUMBER, instance=1
 *   x_constant_number   → name="x", type=NUMBER, modifier=CONSTANT
 *   x_number2           → name="x", type=NUMBER, instance=2
 */
public class Binding {

    public enum Modifier {
        NONE,
        CONSTANT
    }

    public final String name;
    public final Type type;
    public final Modifier modifier;
    public final int instance;
    public Value value;

    public Binding(String name, Type type, Modifier modifier, int instance, Value value) {
        this.name = name;
        this.type = type;
        this.modifier = modifier;
        this.instance = instance;
        this.value = value;
    }

    /** A convenience constructor for the common case: no modifier, no instance. */
    public Binding(String name, Type type, Value value) {
        this(name, type, Modifier.NONE, 1, value);
    }

    /** Is this binding constant (cannot be reassigned)? */
    public boolean isConstant() {
        return modifier == Modifier.CONSTANT;
    }

    /**
     * The reference key — how this binding is looked up.
     * For example: "x_number", "x_constant_number", "x_number2"
     */
    public String key() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("_");
        if (modifier == Modifier.CONSTANT) {
            sb.append("constant_");
        }
        sb.append(typeName());
        if (instance > 1) {
            sb.append(instance);
        }
        return sb.toString();
    }

    /** The lowercase name of the type: "number", "string", "boolean". */
    public String typeName() {
        switch (type) {
            case NUMBER:  return "number";
            case STRING:  return "string";
            case BOOLEAN: return "boolean";
            default:      return type.name().toLowerCase();
        }
    }

    @Override
    public String toString() {
        return key() + " = " + value.display();
    }
}