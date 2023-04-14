package cz.klecansky.indexsequancefile.logging;

import cz.klecansky.indexsequancefile.files.FileType;

import java.util.ArrayList;
import java.util.List;

public class Logger {
    List<String> logs = new ArrayList<>();

    public void readBlock(int blockIndex, FileType type) {
        switch (type) {
            case DATA -> logs.add(String.format("Data block %d read", blockIndex));
            case INDEX -> logs.add(String.format("Index block %d read", blockIndex));
        }
    }

    public List<String> getLogs() {
        return logs;
    }
}
