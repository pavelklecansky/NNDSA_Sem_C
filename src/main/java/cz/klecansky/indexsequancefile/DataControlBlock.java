package cz.klecansky.indexsequancefile;

import java.io.Serializable;
import java.util.Objects;

public final class DataControlBlock implements Serializable {
    private final Integer recordSize;
    private final Integer recordsPerDataBlock;
    private final Integer controlBlockSize = 251;
    private final Integer dataBlockSize = 5837;

    public DataControlBlock(Integer recordSize, Integer recordsPerDataBlock) {
        this.recordSize = recordSize;
        this.recordsPerDataBlock = recordsPerDataBlock;
    }

    public Integer dataBlockSize() {
        return dataBlockSize;
    }

    public Integer recordSize() {
        return recordSize;
    }

    public Integer recordsPerDataBlock() {
        return recordsPerDataBlock;
    }

    public Integer controlBlockSize() {
        return controlBlockSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (DataControlBlock) obj;
        return Objects.equals(this.recordSize, that.recordSize) &&
                Objects.equals(this.recordsPerDataBlock, that.recordsPerDataBlock) &&
                Objects.equals(this.controlBlockSize, that.controlBlockSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordSize, recordsPerDataBlock, controlBlockSize);
    }

    @Override
    public String toString() {
        return "DataControlBlock[" +
                "dataBlockSize=" + recordSize + ", " +
                "recordsPerDataBlock=" + recordsPerDataBlock + ", " +
                "controlBlockSize=" + controlBlockSize + ']';
    }


}

