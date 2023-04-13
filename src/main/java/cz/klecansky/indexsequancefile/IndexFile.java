package cz.klecansky.indexsequancefile;

import javafx.scene.control.Alert;
import org.apache.commons.lang3.SerializationUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IndexFile implements AutoCloseable {

    private static final int RECORDS_PER_DATA_BLOCK = 10;
    private static final int RECORD_SIZE = 100;
    private static final int DATA_BLOCK_SIZE = 500;

    private RandomAccessFile file;

    private final DataControlBlock controlBlock;

    public int dataBlocksCount = 0;
    private int currentBlock = 1;


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

    public void build(List<IndexRecord> recordList) throws IOException {
        System.out.println("---- Index file -----");
        System.out.println(recordList);
        List<IndexRecord> list = new ArrayList<>(recordList.stream().sorted().toList());
        dataBlocksCount = 1;
        while (!list.isEmpty()) {
            List<IndexRecord> blockList = new ArrayList<>(list.subList(0, Math.min(list.size(), RECORDS_PER_DATA_BLOCK)));
            System.out.println(blockList);
            System.out.println("Size: " + blockList.size());
            list.removeAll(blockList);
            DataBlock<IndexRecord> dataBlock = new DataBlock<>(dataBlocksCount, blockList);
            writeDataBlock(dataBlock);
            dataBlocksCount++;
        }
        file.seek(0);
    }

    public void writeDataBlock(DataBlock<IndexRecord> block) throws IOException {
        file.seek(blockOffSet(currentBlock));
        byte[] bytes = new byte[controlBlock.dataBlockSize()];
        byte[] serialize = serialize(block);
        System.arraycopy(serialize, 0, bytes, 0, serialize.length);
        System.out.println(controlBlock.dataBlockSize() + "->" + serialize.length);
        file.write(bytes);
        currentBlock++;
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
