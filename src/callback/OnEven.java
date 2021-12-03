package callback;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface OnEven {
    public void callback(SelectionKey key);
}
