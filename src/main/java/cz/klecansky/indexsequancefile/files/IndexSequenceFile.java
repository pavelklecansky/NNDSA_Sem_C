package cz.klecansky.indexsequancefile.files;

import cz.klecansky.indexsequancefile.blocks.DataBlock;
import cz.klecansky.indexsequancefile.logging.LogManager;
import cz.klecansky.indexsequancefile.records.IndexRecord;
import cz.klecansky.indexsequancefile.records.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IndexSequenceFile {
    private final SequenceFile<IndexRecord> indexFile;
    private final SequenceFile<Record> dataFile;

    public IndexSequenceFile(String filename) throws IOException {
        this.dataFile = new SequenceFile<>(filename + ".dat", FileConfig.DATA_FILE_RECORD_SIZE, FileConfig.DATA_FILE_RECORDS_PER_DATA_BLOCK, FileConfig.DATA_FILE_DATA_BLOCK_SIZE, FileType.DATA);
        this.indexFile = new SequenceFile<>(filename + ".index",
                                            FileConfig.INDEX_FILE_RECORD_SIZE,
                                            FileConfig.INDEX_FILE_RECORDS_PER_DATA_BLOCK,
                                            FileConfig.INDEX_FILE_DATA_BLOCK_SIZE,
                                            FileType.INDEX);
    }

    public void build(List<Record> recordList) throws IOException {
        List<IndexRecord> indexRecordList = buildDataFile(new ArrayList<>(recordList.stream().sorted().toList()));
        buildIndexFile(indexRecordList);
    }

    public String find(Integer key) throws IOException {
        DataBlock<IndexRecord> currentBlock;
        Optional<DataBlock<IndexRecord>> bufferBlock = Optional.empty();
        for (int i = 1; i <= indexFile.getDataBlocksCount(); i++) {
            if (bufferBlock.isPresent()) {
                currentBlock = bufferBlock.get();
            } else {
                currentBlock = indexFile.readBlock(i);
            }

            for (int j = 0; j < currentBlock.recordList().size(); j++) {
                IndexRecord current = currentBlock.recordList().get(j);
                IndexRecord next = null;
                if (j + 1 < currentBlock.recordList().size()) {
                    next = currentBlock.recordList().get(j + 1);
                }
                if (next == null) {
                    bufferBlock = Optional.ofNullable((i < indexFile.getDataBlocksCount()) ? indexFile.readBlock(i + 1) : null);
                    if (bufferBlock.isPresent()) {
                        next = bufferBlock.get().recordList().get(0);
                    }
                }

                if (key >= current.key() && (isLastBlock(current.block()) || key <= next.key())) {
                    int block = current.block();
                    DataBlock<Record> recordDataBlock = dataFile.readBlock(block);
                    for (Record record : recordDataBlock.recordList()) {
                        if (record.key() == key) {
                            return record.value();
                        }
                    }
                    throw new RuntimeException("No key find");
                }
            }

        }
        throw new RuntimeException("No key find");
    }

    private boolean isLastBlock(int block) {
        return dataFile.getDataBlocksCount() <= block;
    }

    private boolean isKeyInIndexBlockRange(Integer key, DataBlock<IndexRecord> indexBlock, DataBlock<IndexRecord> bufferBlock) {
        IndexRecord firstIndexBlock = indexBlock.recordList().stream().findFirst().get();
        IndexRecord firstBufferBlock = bufferBlock.recordList().stream().findFirst().get();

        return key >= firstIndexBlock.key() && key <= firstBufferBlock.key();
    }

    public List<Integer> listOfKeys() throws IOException {
        List<Integer> listOfKeys = new ArrayList<>();
        for (int i = 1; i <= dataFile.getDataBlocksCount(); i++) {
            DataBlock<Record> recordDataBlock = dataFile.readBlock(i);
            recordDataBlock.recordList().forEach(record -> listOfKeys.add(record.key()));
        }
        System.out.println(listOfKeys.size());
        System.out.println(listOfKeys);
        return listOfKeys;
    }

    public void buildIndexFile(List<IndexRecord> recordList) throws IOException {
        System.out.println("---- Index file -----");
        System.out.println(recordList);
        List<IndexRecord> list = new ArrayList<>(recordList.stream().sorted().toList());
        int dataBlocksCount = 1;
        while (!list.isEmpty()) {
            List<IndexRecord> blockList = new ArrayList<>(list.subList(0, Math.min(list.size(), indexFile.getRecordsPerDataBlock())));
            System.out.println(blockList);
            System.out.println("Size: " + blockList.size());
            list.removeAll(blockList);
            DataBlock<IndexRecord> dataBlock = new DataBlock<>(dataBlocksCount, blockList);
            indexFile.writeDataBlock(dataBlock);
            dataBlocksCount++;
        }
        indexFile.resetPointer();
    }

    private List<IndexRecord> buildDataFile(List<Record> recordList) throws IOException {
        List<Record> list = new ArrayList<>(recordList.stream().sorted().toList());
        int dataBlocksCount = 1;
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
            dataFile.writeDataBlock(dataBlock);
            dataBlocksCount++;
        }
        dataFile.resetPointer();
        return indexRecordList;
    }

    private boolean shouldBeInBlock(Record record, Integer blockKey) {
        return record.key() >= (blockKey - 1) * dataFile.getRecordsPerDataBlock() && record.key() <= blockKey * dataFile.getRecordsPerDataBlock();
    }
}
