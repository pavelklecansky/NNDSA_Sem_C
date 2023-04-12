package cz.klecansky.indexsequancefile;

import javafx.scene.control.Alert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexFile implements AutoCloseable {

    private RandomAccessFile file;

    public IndexFile(String filename) throws FileNotFoundException {
        this.file = new RandomAccessFile(filename, "rw");
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
