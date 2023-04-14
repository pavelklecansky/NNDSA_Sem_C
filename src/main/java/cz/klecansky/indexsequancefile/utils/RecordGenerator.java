package cz.klecansky.indexsequancefile.utils;

import cz.klecansky.indexsequancefile.records.Record;

import java.io.IOException;
import java.util.*;

public class RecordGenerator {
    public List<Record> generate() throws IOException {
        Random random = new Random();
        Set<Record> set = new HashSet<>();
        while (set.size() < 10000) {
            int randomNumber = random.nextInt(1, 20001);
            set.add(new Record(randomNumber, UUID.randomUUID().toString()));
        }
        return set.stream().toList();
    }
}
