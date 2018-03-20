package io.github.netvl.ecoji;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Ecoji {
    private Ecoji() {
    }

    public static Encoder getEncoder() {
        return new Encoder();
    }

    public static class Encoder {
        private Encoder() {
        }

        public Target readFrom(InputStream inputStream) {
            return new Target(inputStream);
        }

        public Target readFrom(byte[] bytes) {
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

            private Target(InputStream source) {
                this.source = source;
            }

            public int writeTo(Writer writer) throws IOException {
                return EcojiEncoding.encode(source, writer);
            }

            public String writeToString() throws IOException {
                StringWriter sw = new StringWriter();
                writeTo(sw);
                return sw.toString();
            }
        }
    }

    public static Decoder getDecoder() {
        return new Decoder();
    }

    public static class Decoder {
        private Decoder() {
        }

        public Target readFrom(Reader reader) {
            return new Target(reader);
        }

        public Target readFrom(String string) {
            return readFrom(new StringReader(string));
        }

        public static class Target {
            private final Reader source;

            private Target(Reader source) {
                this.source = source;
            }

            public int writeTo(OutputStream outputStream) throws IOException {
                return EcojiDecoding.decode(source, outputStream);
            }

            public byte[] writeToBytes() throws IOException {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                writeTo(outputStream);
                return outputStream.toByteArray();
            }

            public String writeToString(Charset charset) throws IOException {
                return new String(writeToBytes(), charset);
            }

            public String writeToString() throws IOException {
                return writeToString(StandardCharsets.UTF_8);
            }
        }
    }
}
