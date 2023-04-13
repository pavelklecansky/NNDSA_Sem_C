package cz.klecansky.indexsequancefile;


import cz.klecansky.indexsequancefile.blocks.DataControlBlock;
import cz.klecansky.indexsequancefile.files.IndexSequenceFile;
import cz.klecansky.indexsequancefile.records.Record;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        IndexSequenceFile indexSequenceFile = new IndexSequenceFile("data");
        DataControlBlock dataControlBlock = new DataControlBlock(74, 7, 3000);
        int sizeof = sizeof(dataControlBlock);
        System.out.println(sizeof);
        System.out.println(UUID.randomUUID().toString().length());

//        Record test = new Record(1, "Test");
//        System.out.println(test.getValue());
//
//        int sizeof1 = sizeof(test);
//        System.out.println(sizeof1);

        List<Record> generate = generate();
        indexSequenceFile.build(generate);

        List<Integer> integers = indexSequenceFile.listOfKeys();

//        int records = 0;
//        for (int i = 1; i < dataFile.dataBlocksCount; i++) {
//            DataBlock<Record> dataBlock = dataFile.readBlock(i);
//            records += dataBlock.getRecordList().size();
//            System.out.println(dataBlock.getRecordList());
//        }
//        System.out.println("Records: " + records);

    }

    public static int sizeof(Serializable obj) throws IOException {
        return SerializationUtils.serialize(obj).length;
    }

    private static List<Record> generate() throws IOException {
        Random random = new Random();
        Set<Record> set = new HashSet<>();

        while (set.size() < 10000) {
            int randomNumber = random.nextInt(1, 40001);
            Record record = new Record(randomNumber, UUID.randomUUID().toString());
            set.add(new Record(randomNumber, UUID.randomUUID().toString()));
            System.out.println(sizeof(record));
        }
        return set.stream().toList();
    }
}
