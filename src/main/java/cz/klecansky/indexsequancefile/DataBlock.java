package cz.klecansky.indexsequancefile;

import java.io.Serializable;
import java.util.List;

public record DataBlock<T>(int key, List<T> recordList) implements IntKeyed, Serializable {

}
