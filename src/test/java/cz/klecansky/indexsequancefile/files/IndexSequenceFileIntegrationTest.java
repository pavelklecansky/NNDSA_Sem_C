package cz.klecansky.indexsequancefile.files;

import cz.klecansky.indexsequancefile.logging.LogManager;
import cz.klecansky.indexsequancefile.logging.Logger;
import cz.klecansky.indexsequancefile.utils.RecordGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndexSequenceFileIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

    private IndexSequenceFile file;

    @BeforeEach
    void setUp() throws IOException {
        RecordGenerator recordGenerator = new RecordGenerator();
        file = new IndexSequenceFile("test");
        file.build(recordGenerator.generate(10000, 10));
    }

    @AfterEach
    void tearDown() throws Exception {
        file.close();
        File dataFile = new File("test.dat");
        File indexFile = new File("test.index");
        dataFile.delete();
        indexFile.delete();
    }

    @Test
    public void findShouldFindAllKeys() throws IOException {
        List<Integer> listOfKeys = file.listOfKeys();
        List<String> listOfValues = new ArrayList<>();

        assertDoesNotThrow(() -> {
            listOfKeys.forEach(key -> {
                try {
                    String value = file.find(key);
                    listOfValues.add(value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        assertEquals(listOfKeys.size(), listOfValues.size());
    }

}