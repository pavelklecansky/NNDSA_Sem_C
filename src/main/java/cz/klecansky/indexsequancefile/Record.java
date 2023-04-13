package cz.klecansky.indexsequancefile;

import java.io.Serializable;

public class Record implements IntKeyed, Serializable, Comparable<Record> {

    private static final int VALUE_LENGTH = 36;

    private final int key;
    private final String value;

    public Record(Integer key, String value) {
        this.key = key;
        this.value = normalizeValue(value);

    }

    @Override
    public int key() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Record record = (Record) o;

        return key == record.key;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(key);
    }

    @Override
    public String toString() {
        return key +
                " -> " + value;
    }

    private String normalizeValue(String value) {
        if (value.length() == VALUE_LENGTH) {
            return value;
        } else if (value.length() > VALUE_LENGTH) {
            return value.substring(0, VALUE_LENGTH);
        } else {
            return padRightSpace(value);
        }
    }

    private String padRightSpace(String inputString) {
        StringBuilder sb = new StringBuilder();
        sb.append(inputString);
        while (sb.length() < VALUE_LENGTH) {
            sb.append(' ');
        }
        return sb.toString();
    }


    @Override
    public int compareTo(Record o) {
        return Integer.compare(key, o.key);
    }
}
