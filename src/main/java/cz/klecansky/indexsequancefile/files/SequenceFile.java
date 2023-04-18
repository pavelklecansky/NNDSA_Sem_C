package cz.klecansky.indexsequancefile.files;

import cz.klecansky.indexsequancefile.blocks.DataBlock;
import cz.klecansky.indexsequancefile.blocks.DataControlBlock;
import cz.klecansky.indexsequancefile.logging.LogManager;
import cz.klecansky.indexsequancefile.logging.Logger;
import cz.klecansky.indexsequancefile.records.Record;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class SequenceFile<T> implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger();

    private final FileType type;

    private final DataControlBlock controlBlock;
    private final RandomAccessFile file;
    private int dataBlocksCount = 0;
    private int currentBlock = 1;

    public SequenceFile(String filename, int recordSize, int recordsPerDataBlock, int dataBlockSize, FileType type) throws IOException {
        this.file = new RandomAccessFile(filename, "rw");
        this.controlBlock = new DataControlBlock(recordSize, recordsPerDataBlock, dataBlockSize);
        writeControlBlock(this.controlBlock);
        this.type = type;
    }

    public DataControlBlock readControlBlock() throws IOException {
        file.seek(0);
        byte[] bytes = new byte[controlBlock.controlBlockSize()];
        file.read(bytes, 0, controlBlock.controlBlockSize());
        return deserializeDataControlBlock(bytes);
    }


    public void writeDataBlock(DataBlock<T> block) throws IOException {
        file.seek(blockOffSet(currentBlock));
        byte[] bytes = new byte[controlBlock.dataBlockSize()];
        byte[] serialize = serialize(block);
        System.arraycopy(serialize, 0, bytes, 0, serialize.length);
        file.write(bytes);
        currentBlock++;
        dataBlocksCount++;
    }

    public DataBlock<T> readBlock(int blockIndex) throws IOException {
        long offSet = blockOffSet(blockIndex);
        logger.readBlock(blockIndex, type);
        file.seek(offSet);
        byte[] bytes = new byte[controlBlock.dataBlockSize()];
        file.read(bytes);
        return deserializeDataBlock(bytes);
    }

    public int getRecordsPerDataBlock() {
        return controlBlock.recordsPerDataBlock();
    }

    public int getDataBlocksCount() {
        return dataBlocksCount;
    }

    private long blockOffSet(Integer offset) {
        return controlBlock.controlBlockSize() + (offset) * controlBlock.dataBlockSize();
    }

    private void writeControlBlock(DataControlBlock block) {
        try {
            file.seek(0);
            byte[] bytes = serialize(block);
            file.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    public void resetPointer() throws IOException {
        file.seek(0);
    }

    private byte[] serialize(Serializable object) {
        return SerializationUtils.serialize(object);
    }

    private DataControlBlock deserializeDataControlBlock(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    private DataBlock<T> deserializeDataBlock(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }


}
