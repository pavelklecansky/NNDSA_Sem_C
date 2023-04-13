package cz.klecansky.indexsequancefile.files;

import cz.klecansky.indexsequancefile.blocks.DataBlock;
import cz.klecansky.indexsequancefile.records.IndexRecord;
import cz.klecansky.indexsequancefile.records.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IndexSequenceFile {
    private final IndexFile indexFile;
    private final DataFile dataFile;

    public IndexSequenceFile(String filename) throws IOException {
        this.dataFile = new DataFile(filename + ".dat");
        this.indexFile = new IndexFile(filename + ".index");
    }

    public void build(List<Record> recordList) throws IOException {
        List<IndexRecord> indexRecordList = buildDataFile(new ArrayList<>(recordList.stream().sorted().toList()));
        buildIndexFile(indexRecordList);
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
