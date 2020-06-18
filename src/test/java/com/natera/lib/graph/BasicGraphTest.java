package com.natera.lib.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class BasicGraphTest {

    private BasicGraph<Integer> graph;

    @BeforeEach
    void setUp() {
        graph = new BasicGraph<>();
    }

    @Test
    void addVertexTest() {
        assertEquals(0, graph.getNumberOfVertices());
        graph.addVertex(new Vertex<>(1));
        assertEquals(1, graph.getNumberOfVertices());
    }

    @Test
    void addEdgeTest() {
        Vertex<Integer> from = new Vertex<>(1);
        Vertex<Integer> to = new Vertex<>(2);

        assertEquals(0, graph.getNumberOfVertices());
        graph.addEdge(from, to, true, 1);
        assertEquals(2, graph.getNumberOfVertices());
        assertEquals(2, graph.getNumberOfEdges());

        graph.addEdge(from, to, false, 1);
        assertEquals(2, graph.getNumberOfEdges());
    }

    @Test
    void getPathTest() {
        Vertex<Integer> one = new Vertex<>(1);
        Vertex<Integer> two = new Vertex<>(2);
        Vertex<Integer> three = new Vertex<>(3);
        Vertex<Integer> four = new Vertex<>(4);
        Vertex<Integer> five = new Vertex<>(5);
        Vertex<Integer> six = new Vertex<>(6);

        // add vertexes
        Stream.of(one, two, three, four, five, six)
                .forEach(graph::addVertex);

        // add edges
        graph.addEdge(one, two, false, 3);
        graph.addEdge(one, five, false, 10);

        graph.addEdge(two, three, false, 3);
        graph.addEdge(two, four, false, 3);

        graph.addEdge(four, six, false, 3);

        graph.addEdge(five, six, false, 10);

        List<Edge<Integer>> actualPath = graph.getPath(one, six);
        List<Edge<Integer>> expectedPath = List.of(
                new Edge<>(one, two),
                new Edge<>(two, four),
                new Edge<>(four, six)
        );
        assertIterableEquals(expectedPath, actualPath);
    }

    @Test
    void traverseApplyTest() {
        Vertex<Integer> one = new Vertex<>(1);
        Vertex<Integer> two = new Vertex<>(2);
        Vertex<Integer> three = new Vertex<>(3);
        Vertex<Integer> four = new Vertex<>(4);
        Vertex<Integer> five = new Vertex<>(5);
        Vertex<Integer> six = new Vertex<>(6);

        // add vertexes
        Stream.of(one, two, three, four, five, six)
                .forEach(graph::addVertex);

        // add edges
        graph.addEdge(one, two, false, 3);
        graph.addEdge(one, five, false, 10);

        graph.addEdge(two, three, false, 3);
        graph.addEdge(two, four, false, 3);

        graph.addEdge(four, six, false, 3);

        graph.addEdge(five, six, false, 10);

        System.out.println(graph.toString());
        graph.traverseApply(vertex -> vertex.setData(vertex.getData() + 10));
        System.out.println(graph.toString());

        assertAll(
                () -> assertEquals(new Vertex<>(11), one),
                () -> assertEquals(new Vertex<>(12), two),
                () -> assertEquals(new Vertex<>(13), three),
                () -> assertEquals(new Vertex<>(14), four),
                () -> assertEquals(new Vertex<>(15), five),
                () -> assertEquals(new Vertex<>(16), six)
        );
    }

    @Test
    void multiThreadTest() throws InterruptedException {
        List<Vertex<Integer>> vertices = new CopyOnWriteArrayList<>();

        Runnable vertexAdder = () -> {
            while (true) {
                int data = ThreadLocalRandom.current().nextInt(100);
                Vertex<Integer> vertex = new Vertex<>(data);
                graph.addVertex(vertex);
                vertices.add(vertex);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable edgesAdder = () -> {
            while (true) {
                int size = vertices.size();
                if (size > 0) {
                    Supplier<Vertex<Integer>> vertexSupplier = () ->
                            vertices.get(ThreadLocalRandom.current().nextInt(size));
                    Vertex<Integer> from = vertexSupplier.get();
                    Vertex<Integer> to = vertexSupplier.get();
                    graph.addEdge(from, to, ThreadLocalRandom.current().nextBoolean(),
                            ThreadLocalRandom.current().nextInt(10) + 1);
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable graphPrinter = () -> {
            while (true) {
                System.out.println(graph);
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable pathFinder = () -> {
            while (true) {
                int size = vertices.size();
                if (size > 0) {
                    Supplier<Vertex<Integer>> vertexSupplier = () -> vertices.get(ThreadLocalRandom.current().nextInt(size));
                    Vertex<Integer> from = vertexSupplier.get();
                    Vertex<Integer> to = vertexSupplier.get();
                    try {
                        List<Edge<Integer>> path = graph.getPath(from, to);
                        System.out.println(path);
                    } catch (IllegalStateException e) {

                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(vertexAdder).start();
        new Thread(edgesAdder).start();
        new Thread(graphPrinter).start();
        new Thread(pathFinder).start();

        Thread.sleep(5_000L);
    }
}