package com.carilt01.schematictonbt;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;
}