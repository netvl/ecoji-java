package io.github.netvl.ecoji;

import com.google.common.primitives.UnsignedBytes;
import org.junit.jupiter.api.Test;
import org.quicktheories.core.Gen;
import org.quicktheories.generators.Generate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.netvl.ecoji.LambdaExceptionUtil.rethrowFunction;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.lists;

class EcojiTest {
    private Gen<byte[]> byteArrays() {
        return Generate.byteArrays(
            integers().between(0, 1024),
            integers().between(Byte.MIN_VALUE, Byte.MAX_VALUE).map(Integer::byteValue)
        );
    }

    private interface IOPredicate<T> {

        boolean test(T value) throws IOException;
    }

    private <T> Predicate<T> io(IOPredicate<T> pred) {
        return value -> {
            try {
                return pred.test(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Test
    void testEncodeThenEncodeIdenitity() {
        qt()
            .forAll(byteArrays())
            .check(io(this::checkEncodeThenDecodeIdentity));
    }

    private boolean checkEncodeThenDecodeIdentity(byte[] input) throws IOException {
        String encoded = Ecoji.getEncoder().readFrom(input).writeToString();
        byte[] output = Ecoji.getDecoder().readFrom(encoded).writeToBytes();
        return Arrays.equals(input, output);
    }

    @Test
    void testEncodedDataHasTheSameSortOrder() {
        qt()
            .forAll(lists().of(byteArrays()).ofSizeBetween(0, 128))
            .check(io(this::checkEncodedDataHasTheSameSortOrder));
    }

    private boolean checkEncodedDataHasTheSameSortOrder(List<byte[]> input) throws IOException {
        // input         -----sort------>  inputSorted
        //
        // input         --encode+sort-->  outputSorted
        //
        // outputSorted  ----decode----->  input2Sorted
        //
        // inputSorted         ==          input2Sorted

        List<byte[]> inputSorted = new ArrayList<>(input);
        inputSorted.sort(UnsignedBytes.lexicographicalComparator());

        List<String> outputSorted = input.stream()
            .map(rethrowFunction((byte[] b) -> Ecoji.getEncoder().readFrom(b).writeToString()))
            .sorted(String::compareTo)
            .collect(Collectors.toList());

        List<byte[]> input2Sorted = outputSorted.stream()
            .map(rethrowFunction((String s) -> Ecoji.getDecoder().readFrom(s).writeToBytes()))
            .collect(Collectors.toList());

        return Arrays.deepEquals(inputSorted.toArray(), input2Sorted.toArray());
    }
}
