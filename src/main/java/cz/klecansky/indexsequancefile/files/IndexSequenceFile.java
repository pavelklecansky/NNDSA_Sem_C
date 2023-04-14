package cz.klecansky.indexsequancefile.files;

import cz.klecansky.indexsequancefile.blocks.DataBlock;
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
        this.dataFile = new SequenceFile<>(filename + ".dat", FileConfig.DATA_FILE_RECORD_SIZE, FileConfig.DATA_FILE_RECORDS_PER_DATA_BLOCK, FileConfig.DATA_FILE_DATA_BLOCK_SIZE);
        this.indexFile = new SequenceFile<>(filename + ".index", FileConfig.INDEX_FILE_RECORD_SIZE, FileConfig.INDEX_FILE_RECORDS_PER_DATA_BLOCK, FileConfig.INDEX_FILE_DATA_BLOCK_SIZE);
    }

    public void build(List<Record> recordList) throws IOException {
        List<IndexRecord> indexRecordList = buildDataFile(new ArrayList<>(recordList.stream().sorted().toList()));
        buildIndexFile(indexRecordList);
    }

    public String find(Integer key) throws IOException {
        DataBlock<IndexRecord> indexBlock = null;
        DataBlock<IndexRecord> bufferBlock = null;
        for (int i = 1; i <= indexFile.getDataBlocksCount(); i++) {
            indexBlock = indexFile.readBlock(i);
            if (i < indexFile.getDataBlocksCount()) {
                bufferBlock = indexFile.readBlock(i + 1);
            }

            if (isKeyInIndexBlockRange(key, indexBlock, bufferBlock)) {
                for (int j = 0; j < indexBlock.recordList().size(); j++) {
                    IndexRecord current = indexBlock.recordList().get(j);
                    IndexRecord next = indexBlock.recordList().get(j + 1);
                    if (key >= current.key() && key <= next.key()) {
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
        }
        return "";
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
