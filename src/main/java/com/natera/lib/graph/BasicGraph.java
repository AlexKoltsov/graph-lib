package com.natera.lib.graph;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class BasicGraph<T> implements Graph<T> {

    private final Map<Vertex<T>, Set<Edge<T>>> vertexToEdges = new ConcurrentHashMap<>();

    @Override
    public void addVertex(Vertex<T> vertex) {
        vertexToEdges.putIfAbsent(vertex, new CopyOnWriteArraySet<>());
    }

    @Override
    public void addEdge(Vertex<T> from, Vertex<T> to, boolean bidirectional, int cost) {
        addVertex(from);
        addVertex(to);
        vertexToEdges.get(from).add(new Edge<>(from, to, cost));
        if (bidirectional) {
            vertexToEdges.get(to).add(new Edge<>(to, from, cost));
        }
    }

    @Override
    public List<Edge<T>> getPath(Vertex<T> from, Vertex<T> to) {
        List<List<Vertex<T>>> paths = new ArrayList<>();
        List<Vertex<T>> visited = new ArrayList<>();

        visited.add(from);

        getPathRecursively(from, to, visited, paths);

        return paths.stream()
                .map(this::verticesToEdges)
                .min(Comparator.comparingInt(edges -> edges.stream()
                        .mapToInt(Edge::getCost)
                        .sum()))
                .orElseThrow(() -> new IllegalStateException(String.format("Path from %s to %s not found in:\n%s",
                        from, to, this)));
    }

    private void getPathRecursively(Vertex<T> from, Vertex<T> to, List<Vertex<T>> visited,
                                    List<List<Vertex<T>>> paths) {
        if (from.equals(to)) {
            paths.add(new ArrayList<>(visited));
            visited.remove(to);
            return;
        }

        for (Edge<T> edge : vertexToEdges.get(from)) {
            Vertex<T> current = edge.getTo();
            if (!visited.contains(current)) {
                visited.add(current);
                getPathRecursively(current, to, visited, paths);
            }
        }
        visited.remove(from);
    }

    private List<Edge<T>> verticesToEdges(List<Vertex<T>> vertices) {
        List<Edge<T>> edges = new ArrayList<>();
        for (int i = 1; i < vertices.size(); i++) {
            Vertex<T> from = vertices.get(i - 1);
            Vertex<T> to = vertices.get(i);
            Edge<T> edge = vertexToEdges.get(from).stream()
                    .filter(e -> e.isToVertex(to))
                    .findAny()
                    .orElseThrow(() ->
                            new IllegalStateException(String.format("No edge in graph between %s and %s", from, to)));
            edges.add(edge);
        }
        return edges;
    }

    @Override
    public String toString() {
        return vertexToEdges.entrySet().stream()
                .map(entry -> entry.getKey().toString() + " -> " + entry.getValue().toString())
                .collect(Collectors.joining("\n", "BasicGraph:\n", ""));
    }

    @Override
    public int getNumberOfVertices() {
        return vertexToEdges.keySet().size();
    }

    @Override
    public int getNumberOfEdges() {
        return vertexToEdges.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    @Override
    public void traverseApply(Consumer<Vertex<T>> acceptFunction) {
        List<Runnable> actions = new ArrayList<>();
        Vertex<T> root = vertexToEdges.keySet()
                .stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("There is no any vertices in graph"));
        Set<Vertex<T>> visited = new LinkedHashSet<>();
        Queue<Vertex<T>> queue = new LinkedList<>();
        queue.add(root);
        visited.add(root);
        actions.add(() -> acceptFunction.accept(root)); // action for root
        while (!queue.isEmpty()) {
            Vertex<T> current = queue.poll();
            for (Edge<T> edge : vertexToEdges.get(current)) {
                Vertex<T> toVertex = edge.getTo();
                if (!visited.contains(toVertex)) {
                    visited.add(toVertex);
                    queue.add(toVertex);
                    actions.add(() -> acceptFunction.accept(toVertex)); // action for node
                }
            }
        }
        actions.forEach(Runnable::run);
    }
}
