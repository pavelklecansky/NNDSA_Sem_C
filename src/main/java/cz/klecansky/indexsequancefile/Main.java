package cz.klecansky.indexsequancefile;


import cz.klecansky.indexsequancefile.blocks.DataControlBlock;
import cz.klecansky.indexsequancefile.files.IndexSequenceFile;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
//        IndexSequenceFile indexSequenceFile = new IndexSequenceFile("data", loggingService);
//        DataControlBlock dataControlBlock = new DataControlBlock(74, 7, 3000);
//        int sizeof = sizeof(dataControlBlock);
//        System.out.println(sizeof);
//        System.out.println(UUID.randomUUID().toString().length());

//        Record test = new Record(1, "Test");
//        System.out.println(test.getValue());
//
//        int sizeof1 = sizeof(test);
//        System.out.println(sizeof1);


//        indexSequenceFile.build(generate);
//
//        List<Integer> integers = indexSequenceFile.listOfKeys();
//
//        String skey = indexSequenceFile.find(15);
//        System.out.println(skey);
    }

    public static int sizeof(Serializable obj) throws IOException {
        return SerializationUtils.serialize(obj).length;
    }


}
