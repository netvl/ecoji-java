package io.github.netvl.ecoji;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Date: 18.03.18
 * Time: 21:59
 *
 * @author Vladimir Matveev
 */
class EncodingTest {
    @Test
    void testRandom() throws IOException {
        assertEquals(
            "👖📸🎈☕",
            Ecoji.encoder().readFrom("abc").writeToString()
        );
    }

    @Test
    void testOneByte() throws IOException {
        check((int) 'k').isEncodedAs(
            Emojis.MAPPING[((int) 'k') << 2],
            Emojis.PADDING,
            Emojis.PADDING,
            Emojis.PADDING
        );
    }

    @Test
    void testTwoBytes() throws IOException {
        check(0, 1).isEncodedAs(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.PADDING,
            Emojis.PADDING
        );
    }

    @Test
    void testThreeBytes() throws IOException {
        check(0, 1, 2).isEncodedAs(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING
        );
    }

    @Test
    void testFourBytes() throws IOException {
        check(0, 1, 2, 0).isEncodedAs(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING_40
        );

        check(0, 1, 2, 1).isEncodedAs(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING_41
        );

        check(0, 1, 2, 2).isEncodedAs(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING_42
        );

        check(0, 1, 2, 3).isEncodedAs(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING_43
        );
    }

    @Test
    void testFiveBytes() throws IOException {
        check(0xAB, 0xCD, 0xEF, 0x01, 0x23).isEncodedAs(
            Emojis.MAPPING[687],
            Emojis.MAPPING[222],
            Emojis.MAPPING[960],
            Emojis.MAPPING[291]
        );
    }

    private Checker check(int... ints) {
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < ints.length; ++i) {
            bytes[i] = (byte) ints[i];
        }
        return new Checker(bytes);
    }

    private static class Checker {

        private final byte[] bytes;

        private Checker(byte[] bytes) {
            this.bytes = bytes;
        }

        void isEncodedAs(int... codePoints) throws IOException {
            String encoded = Ecoji.encoder().readFrom(bytes).writeToString();
            String expected = Arrays.stream(codePoints)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
            assertEquals(expected, encoded);
        }
    }
}
