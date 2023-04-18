package cz.klecansky.indexsequancefile.utils;

import cz.klecansky.indexsequancefile.records.Record;

import java.io.IOException;
import java.util.*;

public class RecordGenerator {

    public List<Record> generate(int values) {
        Random random = new Random();
        Set<Record> set = new HashSet<>();
        while (set.size() < values) {
            int randomNumber = random.nextInt(1, (values * 4 + 1));
            set.add(new Record(randomNumber, UUID.randomUUID().toString()));
        }
        return set.stream().toList();
    }

    public List<Record> generate(int values, long seed) {
        Random random = new Random(seed);
        Set<Record> set = new HashSet<>();
        while (set.size() < values) {
            int randomNumber = random.nextInt(1, (values * 4 + 1));
            set.add(new Record(randomNumber, UUID.randomUUID().toString()));
        }
        return set.stream().toList();
    }
}
