package cz.klecansky.indexsequancefile.blocks;

import java.io.Serializable;
import java.util.Objects;

public final class DataControlBlock implements Serializable {
    private final int recordSize;
    private final int recordsPerDataBlock;
    private final int controlBlockSize = 300;
    private final int dataBlockSize;

    public DataControlBlock(int recordSize, int recordsPerDataBlock, int dataBlockSize) {
        this.recordSize = recordSize;
        this.recordsPerDataBlock = recordsPerDataBlock;
        this.dataBlockSize = dataBlockSize;
    }

    public int dataBlockSize() {
        return dataBlockSize;
    }

    public int recordSize() {
        return recordSize;
    }

    public int recordsPerDataBlock() {
        return recordsPerDataBlock;
    }

    public int controlBlockSize() {
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
                Objects.equals(this.recordsPerDataBlock, that.recordsPerDataBlock);
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

