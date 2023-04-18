package cz.klecansky.indexsequancefile.files;

import cz.klecansky.indexsequancefile.blocks.DataBlock;
import cz.klecansky.indexsequancefile.records.IndexRecord;
import cz.klecansky.indexsequancefile.records.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IndexSequenceFile implements AutoCloseable {
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
        DataBlock<IndexRecord> currentIndexBlock;
        Optional<DataBlock<IndexRecord>> bufferIndexBlock = Optional.empty();
        for (int blockIndex = 1; blockIndex <= indexFile.getDataBlocksCount(); blockIndex++) {
            currentIndexBlock = readBlockOrGetFromBuffer(blockIndex, bufferIndexBlock);

            for (int indexRecordIndex = 0; indexRecordIndex < currentIndexBlock.recordList().size(); indexRecordIndex++) {
                IndexRecord current = currentIndexBlock.recordList().get(indexRecordIndex);
                IndexRecord next = getNextIndexRecord(indexRecordIndex, currentIndexBlock.recordList());

                if (next == null) {
                    bufferIndexBlock = Optional.ofNullable((isLastIndexBlock(blockIndex)) ? null : indexFile.readBlock(blockIndex + 1));
                    if (bufferIndexBlock.isPresent()) {
                        next = bufferIndexBlock.get().recordList().get(0);
                    }
                }

                if (key >= current.key() && (isLastBlock(current.block()) || key < next.key())) {
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

    private IndexRecord getNextIndexRecord(int recordIndex, List<IndexRecord> indexRecords) {
        IndexRecord next = null;
        if (recordIndex + 1 < indexRecords.size()) {
            next = indexRecords.get(recordIndex + 1);
        }
        return next;
    }


    public List<Integer> listOfKeys() throws IOException {
        List<Integer> listOfKeys = new ArrayList<>();
        for (int i = 1; i <= dataFile.getDataBlocksCount(); i++) {
            DataBlock<Record> recordDataBlock = dataFile.readBlock(i);
            recordDataBlock.recordList().forEach(record -> listOfKeys.add(record.key()));
        }
        return listOfKeys;
    }

    private DataBlock<IndexRecord> readBlockOrGetFromBuffer(int currentBlockIndex, Optional<DataBlock<IndexRecord>> bufferBlock) throws IOException {
        DataBlock<IndexRecord> currentBlock;
        if (bufferBlock.isPresent()) {
            currentBlock = bufferBlock.get();
        } else {
            currentBlock = indexFile.readBlock(currentBlockIndex);
        }
        return currentBlock;
    }


    private boolean isLastBlock(int block) {
        return dataFile.getDataBlocksCount() <= block;
    }

    private boolean isLastIndexBlock(int block) {
        return indexFile.getDataBlocksCount() <= block;
    }

    private void buildIndexFile(List<IndexRecord> recordList) throws IOException {
        List<IndexRecord> list = new ArrayList<>(recordList.stream().sorted().toList());
        int dataBlocksCount = 1;
        while (!list.isEmpty()) {
            List<IndexRecord> blockList = new ArrayList<>(list.subList(0, Math.min(list.size(), indexFile.getRecordsPerDataBlock())));
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


    @Override
    public void close() throws Exception {
        indexFile.close();
        dataFile.close();
    }

    private boolean shouldBeInBlock(Record record, Integer blockKey) {
        return record.key() >= (blockKey - 1) * dataFile.getRecordsPerDataBlock() && record.key() <= blockKey * dataFile.getRecordsPerDataBlock();
    }
}
