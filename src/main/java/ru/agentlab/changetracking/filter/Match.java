package ru.agentlab.changetracking.filter;

public record Match<T>(Filtering filteredFrom, T data) { }
