package io.github.netvl.ecoji;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The main entry point for Ecoji encoding/decoding.
 *
 * <p>Use the {@link #getEncoder()} and {@link #getDecoder()} methods to obtain an instance of {@link Encoder} and
 * {@link Decoder}, respectively. These classes can then be used to read encoded or decoded data and produce base-1024
 * decoded or encoded data.</p>
 *
 * <p>As a general rule, for {@link Encoder} the input is an {@link InputStream} and the output is a {@link Writer}.
 * For {@link Decoder} it's in the opposite way: the input is a {@link Reader} and the output is a {@link OutputStream}.
 * However, both provide convenice methods to encode from or decode to {@link String}s and {@code byte[]}s.</p>
 *
 * <p>For example:</p>
 *
 * <pre>
 *   String encoded = Ecoji.getEncoder().readFrom("hello world").writeToString();
 *   String decoded = Ecoji.getDecoder().readFrom(encoded).writeToString();
 *   assert decoded.equals("hello world");
 * </pre>
 */
public final class Ecoji {
    private Ecoji() {
    }

    private static final Encoder ENCODER = new Encoder();

    /**
     * Returns an instance of the {@link Encoder} class, which can then be used to encode any stream or sequence of
     * bytes into a base-1024 representation.
     *
     * @return An instance of {@link Encoder}.
     */
    public static Encoder getEncoder() {
        return ENCODER;
    }

    /**
     * Encodes byte sources into a base-1024 representation with an emoji alphabet.
     *
     * Use one of the {@code readFrom()} methods to start the encoding process.
     */
    public static class Encoder {
        private Encoder() {
        }

        /**
         * Uses the provided {@link InputStream} as a bytes source.
         *
         * <p>The provided stream will be read entirely, however, it won't be closed automatically. There are
         * no limitations on the contents of the stream.</p>
         *
         * @param inputStream A sequence of bytes which should be encoded as emojis.
         *
         * @return An intermediate object used to specify the destination of the encoding process.
         */
        public Target readFrom(InputStream inputStream) {
            return new Target(inputStream);
        }

        /**
         * Uses the provided byte array as bytes source.
         *
         * @param bytes A byte array which should be encoded as emojis.
         *
         * @return an intermediate object used to specify the destination of the encoding process.
         */
        public Target readFrom(byte[] bytes) {
            return readFrom(new ByteArrayInputStream(bytes));
        }

        /**
         * Uses the provided string encoded in UTF-8 as bytes source.
         *
         * @param string A string whose UTF-8 representation should be encoded as emojis.
         *
         * @return An intermediate object used to specify the destination of the encoding process.
         */
        public Target readFrom(String string) {
            return readFrom(string, StandardCharsets.UTF_8);
        }

        /**
         * Uses the provided string encoded in in the specified encoding as bytes source.
         *
         * @param string A string whose representation in the encoding defined by the {@code charset} parameter
         *               should be encoded as emoji.
         *
         * @param charset A character encoding which should be used to convert the provided string to bytes.
         *
         * @return An intermediate object used to specify the destination of the encoding process.
         */
        public Target readFrom(String string, Charset charset) {
            return readFrom(new ByteArrayInputStream(string.getBytes(charset)));
        }

        /**
         * An intermediate class, needed to implement the builder-like DSL for encoding.
         */
        public static class Target {
            private final InputStream source;

            private Target(InputStream source) {
                this.source = source;
            }

            /**
             * Writes the base-1024 representation of bytes contained in the previously specified source to the
             * provided {@link Writer}.
             *
             * <p>Note that most of the emoji used in the alphabet do not belong to the Basic Multilingual Plane,
             * therefore they are encoded as a pair of UTF-16 code units, i.e. as a surrogate pair.</p>
             *
             * <p>The passed {@link Writer} will not be closed after the operation finishes.</p>
             *
             * @param writer An {@link Writer} which will accept the Ecoji-encoded data.
             *
             * @return A number of {@code char}s written to the provided {@link Writer}.
             *
             * @throws IOException If an error has happened during a read or a write operation.
             */
            public int writeTo(Writer writer) throws IOException {
                return EcojiEncoding.encode(source, writer);
            }

            /**
             * Writes the base-1024 representation of bytes contained in the previously specified source as a
             * {@link String}.
             *
             * <p>Note that most of the emoji used in the alphabet do not belong to the Basic Multilingual Plane,
             * therefore they are encoded as a pair of UTF-16 code units, i.e. as a surrogate pair.</p>
             *
             * @return A {@link String} containing the base-1024 representation of the input.
             *
             * @throws IOException If an error has happened during a read operation.
             */
            public String writeToString() throws IOException {
                StringWriter sw = new StringWriter();
                writeTo(sw);
                return sw.toString();
            }
        }
    }

    private static final Decoder DECODER = new Decoder();

    /**
     * Returns an instance of the {@link Decoder} class, which can then be used to decode a stream or a sequence of
     * characters which is an Ecoji representation of a byte stream into the original sequence of bytes.
     *
     * @return An instance of {@link Decoder}.
     */
    public static Decoder getDecoder() {
        return DECODER;
    }

    /**
     * Decodes Unicode character sources from a base-1024 representation with an emoji alphabet into bytes.
     *
     * Use one of the {@code readFrom()} methods to start the decoding process.
     */
    public static class Decoder {
        private Decoder() {
        }

        /**
         * Uses the provided {@link Reader} as a source of emoji characters.
         *
         * <p>The provided stream will be read entirely, however, it won't be closed automatically. It is expected that
         * the entirety of the stream is a valid Ecoji-encoded data; if any of the code points read from the stream
         * do not belong to the Ecoji alphabet, an exception will be thrown. An exception will also be thrown if
         * the number of code points in the input is not a multiple of 4.</p>
         *
         * @param reader A {@link Reader} which contains Ecoji-encoded data.
         *
         * @return An intermediate object used to specify the destination of the decoding process.
         */
        public Target readFrom(Reader reader) {
            return new Target(reader);
        }

        /**
         * Uses the provided {@link String} as a source of emoji characters.
         *
         * <p>It is expected that the entire string contains valid Ecoji-encoded data; if any of the code points of
         * which the string consists of do not belong to the Ecoji alphabet, an exception will be thrown. An exception
         * will also be thrown if the number of code points in the string is not a multiple of 4.</p>
         *
         * @param string A {@link String} containing Ecoji-encoded data.
         *
         * @return An intermediate object used to specify the destination of the decoding process.
         */
        public Target readFrom(String string) {
            return readFrom(new StringReader(string));
        }

        /**
         * An intermediate class, needed to implement the builder-like DSL for decoding.
         */
        public static class Target {
            private final Reader source;

            private Target(Reader source) {
                this.source = source;
            }

            /**
             * Writes the original representation of the base-1024 encoded data contained in the previously specified
             * source to the provided {@link OutputStream}.
             *
             * <p>The provided {@link OutputStream} will not be closed when the operation finishes.</p>
             *
             * @param outputStream An {@link OutputStream} which accept the original representation of the base-1024
             *                     encoded data.
             *
             * @return A number of bytes written to the provided {@link OutputStream}.
             *
             * @throws IOException If an error has happened during a read or a write operation, or if the input cannot
             * be decoded properly.
             */
            public int writeTo(OutputStream outputStream) throws IOException {
                return EcojiDecoding.decode(source, outputStream);
            }

            /**
             * Returns the original representation of the base-1024 encoded data contained in the previously specified
             * source as a byte array.
             *
             * @return An array of bytes which contain the original representation of the base-1024 encoded data
             * contained in the provided source.
             *
             * @throws IOException If an error has happened during a read or a write operation, or if the input cannot
             * be decoded properly.
             */
            public byte[] writeToBytes() throws IOException {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                writeTo(outputStream);
                return outputStream.toByteArray();
            }

            /**
             * Returns the original representation of the base-1024 encoded data contained in the previously specified
             * source as a {@link String}, decoding it using the provided {@link Charset}.
             *
             * <p>If the decoded bytes do not form a valid string in the specified encoding, then according to the
             * contract of the {@link String#String(byte[], Charset)} constructor, invalid characters will be replaced
             * with the replacement characters for this encoding.</p>
             *
             * @param charset A character encoding which is used to decode the string.
             *
             * @return A {@link String} decoded using the provided {@link Charset} from the original representation
             * of the base-1024 encoded data contained in the provided source.
             *
             * @throws IOException If an error has happened during a read or a write operation, or if the input cannot
             * be decoded properly.
             */
            public String writeToString(Charset charset) throws IOException {
                return new String(writeToBytes(), charset);
            }

            /**
             * Returns the original representation of the base-1024 encoded data contained in the previously specified
             * source as a {@link String}, decoding it using the UTF-8 encoding.
             *
             * <p>If the decoded bytes do not form a valid UTF-8 string, then according to the contract of the
             * {@link String#String(byte[], Charset)} constructor, invalid characters will be replaced with the
             * UTF-8 replacement characters.</p>
             *
             * @return A {@link String} decoded using the UTF-8 encoding from the original representation
             * of the base-1024 encoded data contained in the provided source.
             *
             * @throws IOException If an error has happened during a read or a write operation, or if the input cannot
             * be decoded properly.
             */
            public String writeToString() throws IOException {
                return writeToString(StandardCharsets.UTF_8);
            }
        }
    }
}
