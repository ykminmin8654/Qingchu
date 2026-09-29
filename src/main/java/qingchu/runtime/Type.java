package qingchu.runtime;

/**
 * Every type a Qingchu value can have at runtime.
 *
 * For Tier 1, there are only three types:
 *   NUMBER, STRING, BOOLEAN
 *
 * Later tiers add more:
 *   LATEX, PYTHON, JAVA, HTML, CSS, LIST, MAP
 */
public enum Type {

    NUMBER,
    STRING,
    BOOLEAN;

    /**
     * Is this type a foreign-code type (from a code_* mode)?
     * For Tier 1, always false.
     */
    public boolean isForeign() {
        switch (this) {
            case NUMBER:
            case STRING:
            case BOOLEAN:
                return false;
            default:
                return true;
        }
    }
}