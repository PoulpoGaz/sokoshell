package fr.valax.args.jline;

import org.jline.builtins.Styles;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.OSUtils;
import org.jline.utils.StyleResolver;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileNameCompleter {

    public static final FileNameCompleter INSTANCE = new FileNameCompleter();

    protected static StyleResolver resolver = Styles.lsStyle();

    public void complete(LineReader reader, String buffer, final List<Candidate> candidates) {
        assert buffer != null;
        assert candidates != null;

        Path current;
        String curBuf;
        String sep = getSeparator(reader.isSet(LineReader.Option.USE_FORWARD_SLASH));
        int lastSep = buffer.lastIndexOf(sep);
        try {
            if (lastSep >= 0) {
                curBuf = buffer.substring(0, lastSep + 1);
                if (curBuf.startsWith("~")) {
                    if (curBuf.startsWith("~" + sep)) {
                        current = getUserHome().resolve(curBuf.substring(2));
                    } else {
                        current = getUserHome().getParent().resolve(curBuf.substring(1));
                    }
                } else {
                    current = getUserDir().resolve(curBuf);
                }
            } else {
                curBuf = "";
                current = getUserDir();
            }
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(current, this::accept)) {
                directory.forEach(p -> {
                    String value = curBuf + p.getFileName().toString();
                    if (Files.isDirectory(p)) {
                        candidates.add(
                                new Candidate(value + (reader.isSet(LineReader.Option.AUTO_PARAM_SLASH) ? sep : ""),
                                        getDisplay(reader.getTerminal(), p, resolver, sep), null, null,
                                        reader.isSet(LineReader.Option.AUTO_REMOVE_SLASH) ? sep : null, null, false));
                    } else {
                        candidates.add(new Candidate(value, getDisplay(reader.getTerminal(), p, resolver, sep), null, null, null, null,
                                true));
                    }
                });
            } catch (IOException e) {
                // Ignore
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    protected boolean accept(Path path) {
        return true;
    }

    protected Path getUserDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

    protected Path getUserHome() {
        return Paths.get(System.getProperty("user.home"));
    }

    protected String getSeparator(boolean useForwardSlash) {
        return useForwardSlash ? "/" : getUserDir().getFileSystem().getSeparator();
    }

    protected String getDisplay(Terminal terminal, Path p, StyleResolver resolver, String separator) {
        AttributedStringBuilder sb = new AttributedStringBuilder();
        String name = p.getFileName().toString();
        int idx = name.lastIndexOf(".");
        String type = idx != -1 ? ".*" + name.substring(idx): null;
        if (Files.isSymbolicLink(p)) {
            sb.styled(resolver.resolve(".ln"), name).append("@");
        } else if (Files.isDirectory(p)) {
            sb.styled(resolver.resolve(".di"), name).append(separator);
        } else if (Files.isExecutable(p) && !OSUtils.IS_WINDOWS) {
            sb.styled(resolver.resolve(".ex"), name).append("*");
        } else if (type != null && resolver.resolve(type).getStyle() != 0) {
            sb.styled(resolver.resolve(type), name);
        } else if (Files.isRegularFile(p)) {
            sb.styled(resolver.resolve(".fi"), name);
        } else {
            sb.append(name);
        }
        return sb.toAnsi(terminal);
    }

}