package io.github.netvl.ecoji;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Ecoji {
    private Ecoji() {
    }

    public static Encoder encoder() {
        return new Encoder();
    }

    public static class Encoder {
        private Encoder() {
        }

        public Target readFrom(InputStream inputStream) {
            return new Target(inputStream);
        }

        public Target readFrom(byte[] bytes){
            return readFrom(new ByteArrayInputStream(bytes));
        }

        public Target readFrom(String string) {
            return readFrom(string, StandardCharsets.UTF_8);
        }

        public Target readFrom(String string, Charset charset) {
            return readFrom(new ByteArrayInputStream(string.getBytes(charset)));
        }

        public static class Target {
            private final InputStream source;

            Target(InputStream source) {
                this.source = source;
            }

            public void writeTo(Writer writer) throws IOException {
                EcojiEncoding.encode(source, writer);
            }

            public String writeToString() throws IOException {
                StringWriter sw = new StringWriter();
                writeTo(sw);
                return sw.toString();
            }
        }
    }
}
