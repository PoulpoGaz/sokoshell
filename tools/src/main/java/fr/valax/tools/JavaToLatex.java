package fr.valax.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class JavaToLatex {

    public static void main(String[] args) {
        Path root = Path.of("sokoshell/src/main/java/fr/valax/sokoshell/solver");

        TreeCreator treeCreator = new TreeCreator();
        try {
            Files.walkFileTree(root, treeCreator);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Tree tree = treeCreator.getRoot();

        try (BufferedWriter bw = Files.newBufferedWriter(Path.of("tools/out.tex"))) {
            writePreamble(bw);
            write(bw, tree, 0);
            bw.write("\\end{document}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writePreamble(BufferedWriter bw) throws IOException {
        bw.write(
                """
                \\documentclass[a4paper]{article}

                \\usepackage{color}
                \\usepackage{textcomp}
                \\usepackage{minted}
                \\usepackage{titling}% the wheel somebody else kindly made for us earlier
                \\usepackage{fancyhdr}
                \\usepackage{amsmath}
                \\usepackage{amssymb}
                \\usepackage{fontenc}
                \\usepackage[french]{babel}
                \\usepackage[a4paper, left=1cm, right=1cm, top=1.5cm, bottom=1.5cm]{geometry}

                \\usemintedstyle{colorful}

                \\setminted[java]{linenos, numbersep=5pt, autogobble, frame=lines, framesep=2mm, breaklines}

                \\begin{document}
                    \\tableofcontents
                """
        );
    }

    private static void write(BufferedWriter bw, Tree tree, int depth) throws IOException {
        newSection(bw, tree.directory, depth);

        for (Tree subDir : tree.subDirectories) {
            write(bw, subDir, depth + 1);
        }

        for (Path path : tree.files) {
            bw.write("    \\inputminted{java}{");
            bw.write(path.toAbsolutePath().toString());
            bw.write("}");
            bw.newLine();
        }
    }

    private static void newSection(BufferedWriter bw, String name, int depth) throws IOException {
        for (int i = 0; i < depth + 1; i++) {
            bw.write("    ");
        }

        if (depth == 0) {
            bw.write("\\section{");
            bw.write(name);
            bw.write("}");
        } else if (depth == 1) {
            bw.write("\\subsection{");
            bw.write(name);
            bw.write("}");
        }

        bw.newLine();
    }


    private static class TreeCreator implements FileVisitor<Path> {

        private Tree root;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Tree child = new Tree();
            child.directory = dir.getFileName().toString();
            child.parent = root;

            if (root != null) {
                root.subDirectories.add(child);
            }

            root = child;

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            root.files.add(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (root.parent != null) {
                root = root.parent;
            }
            return FileVisitResult.CONTINUE;
        }

        public Tree getRoot() {
            return root;
        }
    }

    public static class Tree {

        public Tree parent;
        public String directory;
        public List<Tree> subDirectories = new ArrayList<>();
        public List<Path> files = new ArrayList<>();
    }
}
