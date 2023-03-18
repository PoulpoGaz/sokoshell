package fr.valax.tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class LatexWriter {

    private static final Path TMP = Path.of(System.getProperty("java.io.tmpdir")).resolve("xsb-to-png");

    public static void write(Path dest, BufferedImage sokoban, Data data) throws IOException {
        Files.createDirectories(TMP);
        Path tempImg = TMP.resolve("img.png");
        Path tempLatex = TMP.resolve("latex.tex");

        ImageIO.write(sokoban, "png", tempImg.toFile());
        writeLatex(tempImg, tempLatex, data);
        executeLatex(tempLatex);

        Files.move(TMP.resolve("latex.png"), dest, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void cleanup() {
        try {
            Files.walkFileTree(TMP, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {}
    }

    private static void writeLatex(Path tempImg, Path out, Data data) throws IOException {
        int width = data.board()[0].length;
        int height = data.board().length;

        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(out))) {
            Files.copy(Path.of("tools/template_tex.tex"), os);

            Writer w = new OutputStreamWriter(os);

            for (String line : data.latex()) {
                if (line.contains("$WIDTH$")) {
                    line = line.replace("$WIDTH$", "sok/width=" + width);
                }
                if (line.contains("$HEIGHT$")) {
                    line = line.replace("$HEIGHT$", "sok/height=" + height);
                }
                if (line.contains("$FILE$")) {
                    line = line.replace("$FILE$", tempImg.toAbsolutePath().toString());
                }

                w.write(line);
                w.write(System.lineSeparator());
            }

            w.close();
        }
    }

    private static void executeLatex(Path latex) throws IOException {
        String[] args = new String[] {
            "pdflatex", "-shell-escape", latex.toAbsolutePath().toString()
        };


        boolean error = false;
        Process process = new ProcessBuilder()
                .command(args)
                .directory(TMP.toFile())
                .start();
        try (BufferedReader br = process.inputReader()) {
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println("[pdflatex] " + line);
                if (line.startsWith("?") || line.startsWith("! LaTeX Error")) {
                    process.outputWriter().write("q\n");
                    error = true;
                }
            }
        }

        if (error) {
            throw new IOException("An error has been detected. Please check your files");
        }
    }
}
