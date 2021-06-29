package ru.agentlab.changetracking.filter;

public record Match<T>(Filtering filteredFrom, T data) {
    public static <T> Match<T> added(T data) {
        return new Match<>(Filtering.ADDED, data);
    }

    public static <T> Match<T> removed(T data) {
        return new Match<>(Filtering.REMOVED, data);
    }
}
