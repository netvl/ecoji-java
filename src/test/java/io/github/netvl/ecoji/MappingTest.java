package io.github.netvl.ecoji;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MappingTest {
    @Test
    void testMapping() {
        assertEquals(1024, Emojis.MAPPING.length);
        assertEquals(1024, Emojis.MAPPING_REV.size());
        for (int i = 0; i < Emojis.MAPPING.length; ++i) {
            assertEquals(i, (int) Emojis.MAPPING_REV.get(Emojis.MAPPING[i]));
        }
    }
}
