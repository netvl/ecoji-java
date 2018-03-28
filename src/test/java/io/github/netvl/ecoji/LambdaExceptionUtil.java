package io.github.netvl.ecoji;

import java.util.function.Function;

/**
 * From https://stackoverflow.com/questions/27644361/how-can-i-throw-checked-exceptions-from-inside-java-8-streams
 */
final class LambdaExceptionUtil {

    @FunctionalInterface
    public interface Function_WithExceptions<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    /**
     * .map(rethrowFunction(name -> Class.forName(name))) or .map(rethrowFunction(Class::forName))
     */
    static <T, R, E extends Exception> Function<T, R> rethrowFunction(Function_WithExceptions<T, R, E> function) throws E {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E {
        throw (E) exception;
    }

}
