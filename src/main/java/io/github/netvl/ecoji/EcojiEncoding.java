package io.github.netvl.ecoji;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

class EcojiEncoding {
    static int encode(InputStream source, Writer destination) throws IOException {
        byte[] buf = new byte[5];
        int charsWritten = 0;

        while (true) {
            int n = source.read(buf);

            if (n < 0) {
                break;
            }

            charsWritten += encodeChunk(buf, n, destination);
        }

        return charsWritten;
    }

    private static int encodeChunk(byte[] chunk, int len, Writer out) throws IOException {
        if (len <= 0 || len > 5 || len > chunk.length) {
            throw new IllegalArgumentException("Unexpected chunk length: " + chunk.length);
        }

        int b0 = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0;
        switch (len) {
            case 5:
                b4 = Byte.toUnsignedInt(chunk[4]);
            case 4:
                b3 = Byte.toUnsignedInt(chunk[3]);
            case 3:
                b2 = Byte.toUnsignedInt(chunk[2]);
            case 2:
                b1 = Byte.toUnsignedInt(chunk[1]);
            case 1:
                b0 = Byte.toUnsignedInt(chunk[0]);
            default:
        }

        int[] chars = new int[] {
            Emojis.MAPPING[b0 << 2 | b1 >> 6],
            Emojis.PADDING,
            Emojis.PADDING,
            Emojis.PADDING
        };

        switch (len) {
            case 5:
            case 4:
            case 3:
                chars[2] = Emojis.MAPPING[(b2 & 0x0f) << 6 | b3 >> 2];
            case 2:
                chars[1] = Emojis.MAPPING[(b1 & 0x3f) << 4 | b2 >> 4];
            case 1:
            default:
        }

        if (len == 4) {
            switch (b3 & 0x03) {
                case 0:
                    chars[3] = Emojis.PADDING_40;
                    break;
                case 1:
                    chars[3] = Emojis.PADDING_41;
                    break;
                case 2:
                    chars[3] = Emojis.PADDING_42;
                    break;
                case 3:
                    chars[3] = Emojis.PADDING_43;
                    break;
                default:
            }
        } else if (len == 5) {
            chars[3] = Emojis.MAPPING[(b3 & 0x03) << 8 | b4];
        }

        char[] buf = new char[2];
        int charsWritten = 0;
        for (int c : chars) {
            int n = Character.toChars(c, buf, 0);
            out.write(buf, 0, n);
            charsWritten += n;
        }

        return charsWritten;
    }
}
