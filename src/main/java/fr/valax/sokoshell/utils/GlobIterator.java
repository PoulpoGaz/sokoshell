package fr.valax.sokoshell.utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.stream.Stream;

public class GlobIterator implements AutoCloseable {

    private final Stream<Path> stream;
    private final Iterator<Path> iterator;

    private int limit = 200;
    private int nNext = 0;

    public GlobIterator(String glob) throws IOException {
        this(glob, false);
    }

    public GlobIterator(String glob, boolean regex) throws IOException {
        stream = createIterator(glob, regex);
        iterator = stream.iterator();
    }

    private Stream<Path> createIterator(String glob, boolean regex) throws IOException {
        if (glob.startsWith("~")) {
            glob = glob.replaceFirst("~", System.getProperty("user.home"));
        }

        Path normalizedGlob = Path.of(glob).normalize();

        int maxDepth = 0;
        if (regex) {
            glob = "regex:" + normalizedGlob;

            if (glob.contains(".*") && !glob.contains("\\.*")) {
                maxDepth = Integer.MAX_VALUE;
            }
        } else {
            glob = "glob:" + normalizedGlob;

            if (glob.contains("**") && !glob.contains("\\**")) {
                maxDepth = Integer.MAX_VALUE;
            }
        }

        // find root
        Path root = normalizedGlob;
        while (root != null && !Files.exists(root)) {
            root = root.getParent();

            if (maxDepth != Integer.MAX_VALUE) {
                maxDepth++;
            }
        }


        PathMatcher matcher = getFileSystem(root).getPathMatcher(glob);

        return Files.walk(root, maxDepth)
                .filter(matcher::matches);
    }

    private FileSystem getFileSystem(Path path) {
        if (path == null) {
            return FileSystems.getDefault();
        } else {
            return path.getFileSystem();
        }
    }

    public boolean hasNext() {
        if (limit >= 0 && nNext >= limit) {
            return false;
        }

        return iterator.hasNext();
    }

    public Path next() {
        nNext++;
        return iterator.next();
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public void close() {
        stream.close();
    }
}
