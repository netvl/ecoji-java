package io.github.netvl.ecoji;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

class EcojiDecoding {
    static int decode(Reader source, OutputStream destination) throws IOException {
        int bytesWritten = 0;

        int[] chars = new int[4];
        byte[] out = new byte[5];

        while (true) {
            chars[0] = checkCodePoint(readCodePoint(source));
            if (chars[0] == -1) {
                break;
            }

            for (int i = 1; i < 4; ++i) {
                chars[i] = checkCodePoint(readCodePoint(source));
                if (chars[i] == -1) {
                    throw new IOException("Unexpected end of data, the number of input Unicode code points " +
                                          "is not a multiple of 4");
                }
            }

            int bits1 = Emojis.revMapGetOrZero(chars[0]),
                bits2 = Emojis.revMapGetOrZero(chars[1]),
                bits3 = Emojis.revMapGetOrZero(chars[2]),
                bits4;
            switch (chars[3]) {
                case Emojis.PADDING_40:
                    bits4 = 0;
                    break;
                case Emojis.PADDING_41:
                    bits4 = 1 << 8;
                    break;
                case Emojis.PADDING_42:
                    bits4 = 2 << 8;
                    break;
                case Emojis.PADDING_43:
                    bits4 = 3 << 8;
                    break;
                default:
                    bits4 = Emojis.revMapGetOrZero(chars[3]);
            }

            out[0] = (byte) (bits1 >> 2);
            out[1] = (byte) (((bits1 & 0x3) << 6) | (bits2 >> 4));
            out[2] = (byte) (((bits2 & 0xf) << 4) | (bits3 >> 6));
            out[3] = (byte) (((bits3 & 0x3f) << 2) | (bits4 >> 8));
            out[4] = (byte) (bits4 & 0xff);

            int len;
            if (chars[1] == Emojis.PADDING) {
                len = 1;
            } else if (chars[2] == Emojis.PADDING) {
                len = 2;
            } else if (chars[3] == Emojis.PADDING) {
                len = 3;
            } else if (chars[3] == Emojis.PADDING_40 || chars[3] == Emojis.PADDING_41 ||
                       chars[3] == Emojis.PADDING_42 || chars[3] == Emojis.PADDING_43) {
                len = 4;
            } else {
                len = 5;
            }

            destination.write(out, 0, len);
            bytesWritten += len;
        }

        return bytesWritten;
    }

    private static int checkCodePoint(int c) throws IOException {
        if (Emojis.isValidAlphabetChar(c) || c == -1) {  // pass EOF through
            return c;
        } else {
            throw new IOException("Input code point " + c + " is not a part of the Ecoji alphabet");
        }
    }

    private static int readCodePoint(Reader source) throws IOException {
        int high = source.read();
        if (high == -1) {
            return -1;
        }
        char highChar = (char) high;
        if (Character.isHighSurrogate(highChar)) {
            int low = source.read();
            if (low == -1) {
                throw new IOException("Failed to read low surrogae");
            }
            char lowChar = (char) low;
            return Character.toCodePoint(highChar, lowChar);
        } else {
            return high;
        }
    }
}
