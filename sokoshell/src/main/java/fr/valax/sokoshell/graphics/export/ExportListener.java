package fr.valax.sokoshell.graphics.export;

import java.nio.file.Path;
import java.util.EventListener;

public interface ExportListener extends EventListener {

    void exportCanceled();

    void exportDone(Path out);
}