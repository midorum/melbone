package midorum.melbone.model.exception;

import midorum.melbone.model.settings.key.StampKey;

public class StampHasNoValue extends RuntimeException {

    private final StampKey key;

    public StampHasNoValue(StampKey key) {
        this("Stamp " + key.internal().groupName() + "." + key.name() + " not provided yet", key);
    }

    public StampHasNoValue(String message, StampKey key) {
        super(message);
        this.key = key;
    }

    public StampKey getKey() {
        return key;
    }
}
