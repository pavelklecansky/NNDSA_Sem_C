package cz.klecansky.indexsequancefile;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataFile implements AutoCloseable {

    private static final int RECORDS_PER_DATA_BLOCK = 100;
    private static final int RECORD_SIZE = 231;
    private final IndexFile indexFile;
    private final DataControlBlock controlBlock;
    private final RandomAccessFile file;

    public int dataBlocksCount = 0;

    private int currentBlock = 1;

    public DataFile(String filename) throws IOException {
        this.indexFile = new IndexFile(filename + ".index");
        this.file = new RandomAccessFile(filename + ".dat", "rw");
        this.controlBlock = new DataControlBlock(RECORD_SIZE, RECORDS_PER_DATA_BLOCK);
        writeControlBlock(this.controlBlock);
    }

    public DataControlBlock readControlBlock() throws IOException {
        file.seek(0);
        byte[] bytes = new byte[controlBlock.controlBlockSize()];
        file.read(bytes, 0, controlBlock.controlBlockSize());
        return deserializeDataControlBlock(bytes);
    }

    public void build(List<Record> recordList) throws IOException {
        List<Record> list = new ArrayList<>(recordList.stream().sorted().toList());
        dataBlocksCount = 1;
        while (!list.isEmpty()) {
            int finalBlock = dataBlocksCount;
            List<Record> blockList = list.stream().filter(record -> shouldBeInBlock(record, finalBlock)).toList();
            System.out.println(blockList);
            System.out.println("Size: " + blockList.size());
            list.removeAll(blockList);
            DataBlock dataBlock = new DataBlock(dataBlocksCount, blockList);
            writeDataBlock(dataBlock);
            dataBlocksCount++;
        }
         file.seek(0);
    }

    public boolean writeDataBlock(DataBlock block) throws IOException {
        file.seek(blockOffSet(currentBlock));
        byte[] serialize = serialize(block);
        System.out.println(controlBlock.dataBlockSize() + "->" + serialize.length);
        file.write(serialize);
        currentBlock++;

        return true;
    }

    public DataBlock readBlock(int blockIndex) throws IOException {
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
        indexFile.close();
    }

    private byte[] serialize(Serializable object) {
        return SerializationUtils.serialize(object);
    }

    private byte[] serialize(DataBlock object) {
        return SerializationUtils.serialize(object);
    }

    private DataControlBlock deserializeDataControlBlock(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    private DataBlock deserializeDataBlock(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    private boolean shouldBeInBlock(Record record, Integer blockKey) {
        return record.getKey() >= (blockKey - 1) * RECORDS_PER_DATA_BLOCK && record.getKey() <= blockKey * RECORDS_PER_DATA_BLOCK;
    }
}
