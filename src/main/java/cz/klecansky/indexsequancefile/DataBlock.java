package cz.klecansky.indexsequancefile;

import java.io.Serializable;
import java.util.List;

public class DataBlock implements IntKeyed, Serializable {

    private final Integer key;
    private final List<Record> recordList;

    public DataBlock(Integer key, List<Record> recordList) {
        this.key = key;
        this.recordList = recordList;
    }

    @Override
    public Integer getKey() {
        return key;
    }

    public List<Record> getRecordList() {
        return recordList;
    }
}
