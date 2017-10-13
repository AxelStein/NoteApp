package com.axel_stein.domain.utils;

import android.support.annotation.NonNull;

public class ObjectUtil {

    /**
     * Verifies if the object is not null and returns it or throws a NullPointerException
     * with the given message.
     *
     * @param <T>     the value type
     * @param object  the object to verify
     * @param message the message to use with the NullPointerException
     * @return the object itself
     * @throws NullPointerException if object is null
     */
    @NonNull
    public static <T> T requireNonNull(final T object, final Object message) {
        if (object == null) {
            throw new NullPointerException(String.valueOf(message));
        }
        return object;
    }

    @NonNull
    public static <T> T requireNonNull(final T object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return object;
    }

}
