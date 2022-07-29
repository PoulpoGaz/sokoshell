package fr.valax.sokoshell.readers;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Tile;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SLCReader implements Reader {

    private static final SAXReader reader = new SAXReader();


    @Override
    public Pack read(InputStream is) throws IOException, JsonException {
        Document document;
        try {
            document = reader.read(is);
        } catch (DocumentException e) {
            throw new IOException(e);
        }

        String name = document.selectSingleNode("//Title").getText();
        String author = document.selectSingleNode("//@Copyright").getText();

        List<Node> levelNodes = document.selectNodes("//Level");
        List<Level> levels = new ArrayList<>(levelNodes.size());

        for (int i = 0; i < levelNodes.size(); i++) {
            Node node = levelNodes.get(i);

            Element element = (Element) node;
            int width = Integer.parseInt(element.attribute("Width").getValue());
            int height = Integer.parseInt(element.attribute("Height").getValue());

            levels.add(getLevel(i, width, height, element.elementIterator()));
        }

        return new Pack(name, author, levels);
    }

    private Level getLevel(int index, int width, int height, Iterator<Element> lines) {
        Level.Builder builder = new Level.Builder();
        builder.setSize(width, height);

        int x;
        int y = 0;
        while (lines.hasNext()) {
            Element e = lines.next();

            char[] array = e.getText().toCharArray();
            x = 0;
            for (char c : array) {
                ReaderUtils.set(c, builder, x, y);

                x++;
            }

            if (x != width) {
                for (; x < width; x++) {
                    builder.set(Tile.FLOOR, x, y);
                }
            }

            y++;
        }

        builder.setIndex(index);

        return builder.build();
    }
}
