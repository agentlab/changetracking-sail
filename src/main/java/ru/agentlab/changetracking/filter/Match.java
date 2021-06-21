package ru.agentlab.changetracking.filter;

public class Match<T> {
    private final Filtering filteredFrom;
    private final T data;

    public Match(Filtering filteredFrom, T data) {
        this.filteredFrom = filteredFrom;
        this.data = data;
    }

    public Filtering getFilteredFrom() {
        return filteredFrom;
    }

    public T getData() {
        return data;
    }
}
