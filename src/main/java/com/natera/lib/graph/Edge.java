package com.natera.lib.graph;

import java.util.Objects;

public class Edge<T> {
    private final Vertex<T> from;
    private final Vertex<T> to;
    private final Integer cost;

    public Edge(Vertex<T> from, Vertex<T> to, Integer cost) {
        this.from = from;
        this.to = to;
        this.cost = cost;
    }

    public Edge(Vertex<T> from, Vertex<T> to) {
        this(from, to, 1);
    }

    public Vertex<T> getFrom() {
        return from;
    }

    public Vertex<T> getTo() {
        return to;
    }

    public Integer getCost() {
        return cost;
    }

    public boolean isToVertex(Vertex<T> to) {
        return this.to == to;
    }

    public boolean isFromVertex(Vertex<T> from) {
        return this.from == from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge<?> edge = (Edge<?>) o;
        return Objects.equals(from, edge.from) &&
                Objects.equals(to, edge.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%s)", from, to, cost);
    }
}
