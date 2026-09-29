package qingchu.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * The binding table.
 *
 * A Scope holds every binding the program has created, keyed by
 * the binding's reference string (like "x_number" or "name_string").
 *
 * Looking up a binding:
 *     scope.get("x_number")   → the Binding, or null
 *
 * Adding a binding:
 *     scope.define(binding)   → throws if the key already exists
 *
 * Changing a binding:
 *     scope.set("x_number", newValue)  → throws if not found or if constant
 */
public class Scope {

    private final Map<String, Binding> bindings = new HashMap<>();

    /**
     * Adds a new binding.
     * Throws if a binding with the same key already exists.
     */
    public void define(Binding binding) {
        String key = binding.key();
        if (bindings.containsKey(key)) {
            throw new IllegalStateException("binding already exists: " + key);
        }
        bindings.put(key, binding);
    }

    /**
     * Finds a binding by its reference key.
     * Returns null if not found.
     */
    public Binding get(String key) {
        return bindings.get(key);
    }

    /**
     * Checks if a binding exists.
     */
    public boolean has(String key) {
        return bindings.containsKey(key);
    }

    /**
     * Changes the value of an existing binding.
     * Throws if the binding doesn't exist or if it's constant.
     */
    public void set(String key, Value value) {
        Binding b = bindings.get(key);
        if (b == null) {
            throw new IllegalStateException("no such binding: " + key);
        }
        if (b.isConstant()) {
            throw new IllegalStateException("cannot reassign constant: " + key);
        }
        b.value = value;
    }

    /**
     * How many bindings are in this scope.
     */
    public int size() {
        return bindings.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Scope(");
        sb.append(bindings.size());
        sb.append(" bindings)\n");
        for (Binding b : bindings.values()) {
            sb.append("  ");
            sb.append(b);
            sb.append("\n");
        }
        return sb.toString();
    }
}