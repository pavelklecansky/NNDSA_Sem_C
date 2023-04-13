package cz.klecansky.indexsequancefile;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataFile implements AutoCloseable {

    private static final int RECORDS_PER_DATA_BLOCK = 100;
    private static final int RECORD_SIZE = 200;
    private static final int DATA_BLOCK_SIZE = 5500;
    private final IndexFile indexFile;
    private final DataControlBlock controlBlock;
    private final RandomAccessFile file;
    public int dataBlocksCount = 0;
    private int currentBlock = 1;

    public DataFile(String filename) throws IOException {
        this.indexFile = new IndexFile(filename + ".index");
        this.file = new RandomAccessFile(filename + ".dat", "rw");
        this.controlBlock = new DataControlBlock(RECORD_SIZE, RECORDS_PER_DATA_BLOCK, DATA_BLOCK_SIZE);
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
        List<IndexRecord> indexRecordList = new ArrayList<>();
        while (!list.isEmpty()) {
            int finalBlock = dataBlocksCount;
            List<Record> blockList = list.stream().filter(record -> shouldBeInBlock(record, finalBlock)).toList();
            System.out.println(blockList);
            System.out.println("Size: " + blockList.size());
            list.removeAll(blockList);
            DataBlock<Record> dataBlock = new DataBlock<>(dataBlocksCount, blockList);
            Optional<Record> first = blockList.stream().findFirst();
            first.ifPresent(record -> indexRecordList.add(new IndexRecord(record.key(), finalBlock)));
            writeDataBlock(dataBlock);
            dataBlocksCount++;
        }
        indexFile.build(indexRecordList);
        file.seek(0);
    }

    public void writeDataBlock(DataBlock<Record> block) throws IOException {
        file.seek(blockOffSet(currentBlock));
        byte[] bytes = new byte[controlBlock.dataBlockSize()];
        byte[] serialize = serialize(block);
        System.arraycopy(serialize, 0, bytes, 0, serialize.length);
        file.write(bytes);
        currentBlock++;
    }

    public DataBlock<Record> readBlock(int blockIndex) throws IOException {
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

    private DataControlBlock deserializeDataControlBlock(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    private DataBlock<Record> deserializeDataBlock(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    private boolean shouldBeInBlock(Record record, Integer blockKey) {
        return record.key() >= (blockKey - 1) * RECORDS_PER_DATA_BLOCK && record.key() <= blockKey * RECORDS_PER_DATA_BLOCK;
    }
}
