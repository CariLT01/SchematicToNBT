package com.carilt01.schematictonbt.app.converter.helpers;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
    T get() throws E;
}