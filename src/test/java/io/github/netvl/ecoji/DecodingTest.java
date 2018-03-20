package io.github.netvl.ecoji;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DecodingTest {
    @Test
    void testRandom() throws IOException {
        assertEquals(
            "abc",
            Ecoji.getDecoder().readFrom("👖📸🎈☕").writeToString()
        );
    }

    @Test
    void testOneByte() throws IOException {
        checkThat(
            Emojis.MAPPING[((int) 'k') << 2],
            Emojis.PADDING,
            Emojis.PADDING,
            Emojis.PADDING
        ).isDecodedAs((int) 'k');
    }


    @Test
    void testTwoBytes() throws IOException {
        checkThat(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.PADDING,
            Emojis.PADDING
        ).isDecodedAs(0, 1);
    }

    @Test
    void testThreeBytes() throws IOException {
        checkThat(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING
        ).isDecodedAs(0, 1, 2);
    }

    @Test
    void testFourBytes() throws IOException {
        checkThat(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING_40
        ).isDecodedAs(0, 1, 2, 0);

        checkThat(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING_41
        ).isDecodedAs(0, 1, 2, 1);

        checkThat(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING_42
        ).isDecodedAs(0, 1, 2, 2);

        checkThat(
            Emojis.MAPPING[0],
            Emojis.MAPPING[16],
            Emojis.MAPPING[128],
            Emojis.PADDING_43
        ).isDecodedAs(0, 1, 2, 3);
    }

    @Test
    void testFiveBytes() throws IOException {
        checkThat(
            Emojis.MAPPING[687],
            Emojis.MAPPING[222],
            Emojis.MAPPING[960],
            Emojis.MAPPING[291]
        ).isDecodedAs(0xAB, 0xCD, 0xEF, 0x01, 0x23);
    }

    private Checker checkThat(int... codePoints) {
        StringBuilder sb = new StringBuilder();
        for (int c : codePoints) {
            sb.appendCodePoint(c);
        }
        return new Checker(sb.toString());
    }

    private static class Checker {
        private final String string;

        private Checker(String string) {
            this.string = string;
        }

        void isDecodedAs(int... ints) throws IOException {
            byte[] decoded = Ecoji.getDecoder().readFrom(string).writeToBytes();
            byte[] expected = new byte[ints.length];
            for (int i = 0; i < ints.length; ++i) {
                expected[i] = (byte) ints[i];
            }
            assertArrayEquals(expected, decoded);
        }
    }
}
