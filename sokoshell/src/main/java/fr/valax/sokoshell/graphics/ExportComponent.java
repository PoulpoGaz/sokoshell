package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.graphics.layout.BorderLayout;

import java.util.function.Supplier;

public class ExportComponent extends Component {

    private final Label exportLabel;
    private final Supplier<String> exporter;

    private long last;

    public ExportComponent(Supplier<String> exporter) {
        this.exporter = exporter;

        exportLabel = new Label();
        exportLabel.setHorizAlign(Label.WEST);

        setLayout(new BorderLayout());
        add(exportLabel, BorderLayout.CENTER);
    }

    @Override
    protected void updateComponent() {
        if (keyReleased(Key.CTRL_E)) {
            String out = exporter.get();

            if (out != null) {
                exportLabel.setText("Map exported to " + out);
                last = System.currentTimeMillis();
            }
        }

        if (last + 2000 < System.currentTimeMillis()) {
            exportLabel.setText("");
        }
    }
}
