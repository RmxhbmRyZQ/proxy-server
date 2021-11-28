package stream;

import java.io.IOException;

public class SystemBufferOverflowException extends IOException {
    private final int len;

    public SystemBufferOverflowException(int len) {
        this.len = len;
    }

    public int getLen() {
        return len;
    }
}
