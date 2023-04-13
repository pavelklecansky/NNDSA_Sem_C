package cz.klecansky.indexsequancefile.records;

import java.io.Serializable;

public record IndexRecord(int key, int block) implements Comparable<IndexRecord>, Serializable {
    @Override
    public int compareTo(IndexRecord o) {
        return Integer.compare(key, o.key);
    }
}
