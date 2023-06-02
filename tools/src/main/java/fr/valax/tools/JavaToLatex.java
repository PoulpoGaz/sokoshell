package fr.valax.tools;

import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.utils.Utils;

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
        Package sources = getSources(Path.of("sokoshell/src/main/java"), args);
        if (sources == null) {
            return;
        }

        try (BufferedWriter bw = Files.newBufferedWriter(Path.of("tools/out.tex"))) {
            writePreamble(bw, depth(sources));
            writeTree(bw, sources);
            write(bw, sources);
            bw.write("\\end{document}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writePreamble(BufferedWriter bw, int maxDepth) throws IOException {
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
                \\usepackage[a4paper, left=2cm, right=2cm, top=1.5cm, bottom=2cm]{geometry}
                \\usepackage{dirtree}
                \\usepackage{hyperref}

                \\usemintedstyle{colorful}

                \\setminted[java]{linenos, numbersep=5pt, autogobble, frame=single, breaklines}
                """);

        bw.write("\\setcounter{tocdepth}{");
        bw.write(Integer.toString(maxDepth - 1));
        bw.write("}");
        bw.newLine();

        bw.write("\\setcounter{secnumdepth}{");
        bw.write(Integer.toString(maxDepth - 1));
        bw.write("}");
        bw.newLine();

        bw.write("""
                \\begin{document}
                """
        );
    }

    private static void writeTree(BufferedWriter bw, Package p) throws IOException {
        writeIndent(bw, 0);
        bw.write("\\dirtree{%");
        bw.newLine();

        iter(p, new PackageVisitor() {
            @Override
            public void visitPackage(Package p, String fullyQualifiedName, int depth) throws IOException {
                writeIndent(bw, 1);
                bw.write('.');
                bw.write(Integer.toString(depth + 1));
                bw.write(" {");
                bw.write(p.directory);
                bw.write("}.");
                bw.newLine();
            }

            @Override
            public void visitFile(Package ancestor, Path file, String simpleName, String fullyQualifiedName, int depth) throws IOException {
                writeIndent(bw, 1);
                bw.write('.');
                bw.write(Integer.toString(depth + 1));
                bw.write(' ');
                bw.write("\\hyperref[package:%s]{%s}".formatted(fullyQualifiedName, simpleName));
                bw.write("\\DTcomment{\\pageref{package:" + fullyQualifiedName + "}}");
                bw.write('.');
                bw.newLine();
            }
        });
        bw.write("}");
        bw.newLine();
    }

    private static void write(BufferedWriter bw, Package p) throws IOException {
        iter(p, new PackageVisitor() {
            @Override
            public void visitPackage(Package p, String fullyQualifiedName, int depth) throws IOException {
                newSection(bw, p.directory, fullyQualifiedName, depth);
            }

            @Override
            public void visitFile(Package ancestor, Path file, String simpleName, String fullyQualifiedName, int depth) throws IOException {
                newSection(bw, simpleName, fullyQualifiedName, 3);

                writeIndent(bw, depth);
                bw.write("\\inputminted{java}{");
                bw.write(file.toAbsolutePath().toString());
                bw.write("}");
                bw.newLine();
            }
        });
    }

    private static void iter(Package aPackage, PackageVisitor visitor) throws IOException {
        iter_(aPackage, visitor, null, 0);
    }

    private static void iter_(Package p, PackageVisitor visitor, String fullyQualifiedName, int depth)
            throws IOException {
        for (Package subDir : p.innerPackages) {
            String childFully = getFullyQualifiedName(fullyQualifiedName, subDir.directory);
            visitor.visitPackage(subDir, childFully, depth);

            iter_(subDir, visitor, childFully, depth + 1);
        }

        for (Path path : p.files) {
            String fileName = path.getFileName().toString();
            String simple = fileName.replace(".java", "");
            String fully = getFullyQualifiedName(fullyQualifiedName, fileName);

            visitor.visitFile(p, path, simple, fully, depth + 1);
        }
    }

    private static void newSection(BufferedWriter bw, String name,
                                   String fullyQualifiedName, int depth) throws IOException {
        writeIndent(bw, depth);

        if (depth == 0) {
            bw.write("\\section{");
            bw.write(name);
            bw.write("}");
        } else if (depth == 1) {
            bw.write("\\subsection{");
            bw.write(name);
            bw.write("}");
        } else if (depth == 2) {
            bw.write("\\subsubsection{");
            bw.write(name);
            bw.write("}");
        } else if (depth == 3) {
            bw.write("\\paragraph{");
            bw.write(name);
            bw.write("}");
        } else if (depth == 4) {
            bw.write("\\subparagraph{");
            bw.write(name);
            bw.write("}");
        } else if (depth == 5) {
            bw.write("\\subsubparagraph{");
            bw.write(name);
            bw.write("}");
        }

        bw.write("\\label{package:");
        bw.write(fullyQualifiedName);
        bw.write("}");

        bw.newLine();
    }

    private static void writeIndent(BufferedWriter bw, int depth) throws IOException {
        for (int i = 0; i < depth + 1; i++) {
            bw.write("    ");
        }
    }

    private static String getFullyQualifiedName(String ancestorFully, String child) {
        if (ancestorFully == null) {
            return child;
        } else {
            return ancestorFully + "." + child;
        }
    }



    private static Package getSources(Path sourcePath, String[] exclude) {
        SourceExplorer sourceExplorer = new SourceExplorer(exclude);
        try {
            Files.walkFileTree(sourcePath, sourceExplorer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return sourceExplorer.getRoot();
    }

    private static int depth(Package p) {
        int d = 0;

        for (Package p2 : p.innerPackages) {
            d = Math.max(depth(p2), d);
        }

        return d + 1;
    }

    private static class SourceExplorer implements FileVisitor<Path> {

        private final String[] exclude;
        private Path root;

        private Package currentPackage;

        public SourceExplorer(String[] exclude) {
            this.exclude = exclude;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            boolean isRoot = false;
            if (root == null) {
                isRoot = true;
                this.root = dir;
            }

            if (isExcluded(dir)) {
                System.out.println("Excluding " + dir);
                return FileVisitResult.SKIP_SUBTREE;
            } else {
                Package child = new Package();
                if (!isRoot) {
                    child.directory = dir.getFileName().toString();
                    child.parent = currentPackage;
                }

                if (currentPackage != null) {
                    currentPackage.innerPackages.add(child);
                }

                currentPackage = child;

                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (!isExcluded(file)) {
                currentPackage.files.add(file);
            } else {
                System.out.println("Excluding " + file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            if (currentPackage.innerPackages.size() == 1 && currentPackage.files.isEmpty()) {
                Package child = currentPackage.innerPackages.get(0);

                if (currentPackage.directory != null) {
                    child.directory = currentPackage.directory + "." + child.directory;
                }
                child.parent = currentPackage.parent;

                if (currentPackage.parent != null) {
                    currentPackage.parent.innerPackages.remove(currentPackage);
                    currentPackage.parent.innerPackages.add(child);
                }
            }

            if (currentPackage.parent != null) {
                currentPackage = currentPackage.parent;
            }

            return FileVisitResult.CONTINUE;
        }

        private boolean isExcluded(Path path) {
            Path fromRoot = root.relativize(path);
            String dots = fromRoot.toString().replace("/", ".");

            if (Files.isDirectory(path)) {
                return ArgsUtils.contains(exclude, dots);
            } else if (Utils.getExtension(path).equals("java")) {
                dots = dots.substring(0, dots.length() - 5);
                String longest = longestPrefix(exclude, dots);

                if (longest == null) {
                    return false;
                }

                return longest.equals(dots) || countDots(dots) == countDots(longest);
            } else {
                return true;
            }
        }

        private String longestPrefix(String[] exclude, String file) {
            String longest = null;
            for (String str : exclude) {
                if (file.startsWith(str) && (longest == null || longest.length() < str.length())) {
                    longest = str;
                }
            }

            return longest;
        }

        private int countDots(String str) {
            int n = 0;

            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '.') {
                    n++;
                }
            }

            return n;
        }

        public Package getRoot() {
            return currentPackage;
        }
    }

    private static class Package {

        public Package parent;
        public String directory;
        public List<Package> innerPackages = new ArrayList<>();
        public List<Path> files = new ArrayList<>();
    }

    private interface PackageVisitor {

        void visitPackage(Package p, String fullyQualifiedName, int depth) throws IOException;

        void visitFile(Package ancestor, Path file, String simpleName, String fullyQualifiedName, int depth) throws IOException;
    }
}
