package cz.klecansky.indexsequancefile.files;

import cz.klecansky.indexsequancefile.blocks.DataBlock;
import cz.klecansky.indexsequancefile.blocks.DataControlBlock;
import cz.klecansky.indexsequancefile.records.IndexRecord;
import org.apache.commons.lang3.SerializationUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class IndexFile implements AutoCloseable {

    private static final int RECORDS_PER_DATA_BLOCK = 10;
    private static final int RECORD_SIZE = 100;
    private static final int DATA_BLOCK_SIZE = 500;

    private final RandomAccessFile file;

    private final DataControlBlock controlBlock;

    public int dataBlocksCount = 0;
    private int lastBlockIndex = 1;


    public IndexFile(String filename) throws FileNotFoundException {
        this.file = new RandomAccessFile(filename, "rw");
        this.controlBlock = new DataControlBlock(RECORD_SIZE, RECORDS_PER_DATA_BLOCK, DATA_BLOCK_SIZE);
    }

    public DataControlBlock readControlBlock() throws IOException {
        file.seek(0);
        byte[] bytes = new byte[controlBlock.controlBlockSize()];
        file.read(bytes, 0, controlBlock.controlBlockSize());
        return deserializeDataControlBlock(bytes);
    }

    public int getRecordsPerDataBlock() {
        return RECORDS_PER_DATA_BLOCK;
    }

    public void writeDataBlock(DataBlock<IndexRecord> block) throws IOException {
        file.seek(blockOffSet(lastBlockIndex));
        byte[] bytes = new byte[controlBlock.dataBlockSize()];
        byte[] serialize = serialize(block);
        System.arraycopy(serialize, 0, bytes, 0, serialize.length);
        System.out.println(controlBlock.dataBlockSize() + "->" + serialize.length);
        file.write(bytes);
        lastBlockIndex++;
        dataBlocksCount++;
    }

    public DataBlock<IndexRecord> readBlock(int blockIndex) throws IOException {
        long offSet = blockOffSet(blockIndex);
        System.out.println("Offset: " + offSet + " | Index:" + blockIndex);
        file.seek(offSet);
        byte[] bytes = new byte[controlBlock.dataBlockSize()];
        file.read(bytes);
        return deserializeDataBlock(bytes);
    }

    private long blockOffSet(Integer offset) {
        return controlBlock.controlBlockSize() + (offset) * controlBlock.dataBlockSize();
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

    private DataBlock<IndexRecord> deserializeDataBlock(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }
}
