package cz.klecansky.indexsequancefile.files;

import cz.klecansky.indexsequancefile.blocks.DataBlock;
import cz.klecansky.indexsequancefile.blocks.DataControlBlock;
import cz.klecansky.indexsequancefile.records.Record;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class DataFile implements AutoCloseable {

    private static final int RECORDS_PER_DATA_BLOCK = 100;
    private static final int RECORD_SIZE = 200;
    private static final int DATA_BLOCK_SIZE = 5500;

    private final DataControlBlock controlBlock;
    private final RandomAccessFile file;
    private int dataBlocksCount = 0;
    private int currentBlock = 1;

    public DataFile(String filename) throws IOException {
        this.file = new RandomAccessFile(filename, "rw");
        this.controlBlock = new DataControlBlock(RECORD_SIZE, RECORDS_PER_DATA_BLOCK, DATA_BLOCK_SIZE);
        writeControlBlock(this.controlBlock);
    }

    public DataControlBlock readControlBlock() throws IOException {
        file.seek(0);
        byte[] bytes = new byte[controlBlock.controlBlockSize()];
        file.read(bytes, 0, controlBlock.controlBlockSize());
        return deserializeDataControlBlock(bytes);
    }


    public void writeDataBlock(DataBlock<Record> block) throws IOException {
        file.seek(blockOffSet(currentBlock));
        byte[] bytes = new byte[controlBlock.dataBlockSize()];
        byte[] serialize = serialize(block);
        System.arraycopy(serialize, 0, bytes, 0, serialize.length);
        file.write(bytes);
        currentBlock++;
        dataBlocksCount++;
    }

    public DataBlock<Record> readBlock(int blockIndex) throws IOException {
        long offSet = blockOffSet(blockIndex);
        System.out.println("Offset: " + offSet + " | Index:" + blockIndex);
        file.seek(offSet);
        byte[] bytes = new byte[controlBlock.dataBlockSize()];
        file.read(bytes);
        return deserializeDataBlock(bytes);
    }

    public int getRecordsPerDataBlock() {
        return RECORDS_PER_DATA_BLOCK;
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

    private DataBlock<Record> deserializeDataBlock(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }


}
