package ru.agentlab.changetracking.filter;

public class Match<T> {
    private final ChangetrackingFilter.Filtering filteredFrom;
    private final T data;

    public Match(ChangetrackingFilter.Filtering filteredFrom, T data) {
        this.filteredFrom = filteredFrom;
        this.data = data;
    }

    public ChangetrackingFilter.Filtering getFilteredFrom() {
        return filteredFrom;
    }

    public T getData() {
        return data;
    }
}
