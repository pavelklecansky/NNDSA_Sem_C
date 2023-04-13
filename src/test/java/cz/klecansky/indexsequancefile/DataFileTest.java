package cz.klecansky.indexsequancefile;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFileTest {
    @Test
    public void whenDataFileCreated_thenDataControlBlockIsCreated() throws IOException {
        DataFile dataFile = new DataFile("data");
        DataControlBlock dataControlBlock = dataFile.readControlBlock();

        assertEquals(new DataControlBlock(800, 100, 3000), dataControlBlock);
    }
}