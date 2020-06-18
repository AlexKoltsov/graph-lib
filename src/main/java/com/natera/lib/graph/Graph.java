package com.natera.lib.graph;

import java.util.List;
import java.util.function.Consumer;

public interface Graph<T> {
    /**
     * Adds vertex to the graph
     */
    void addVertex(Vertex<T> vertex);

    /**
     * Adds edge to the graph
     */
    void addEdge(Vertex<T> from, Vertex<T> to, boolean bidirectional, int cost);

    /**
     * Returns a list of edges between 2 vertices (path doesn’t have to be optimal)
     */
    List<Edge<T>> getPath(Vertex<T> from, Vertex<T> to);

    /**
     * Returns a number of vertices
     */
    int getNumberOfVertices();

    /**
     * Returns a number of edges
     */
    int getNumberOfEdges();

    /**
     * A user defined function to apply it on every vertex of the graph
     */
    void traverseApply(Consumer<Vertex<T>> acceptFunction);
}
