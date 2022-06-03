package fr.valax.sokoshell;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.solver.Pack;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ListCommand extends AbstractVoidCommand {

    private final ReentrantLock lock = new ReentrantLock();

    public ListCommand(SokoShellHelper helper) {
        super(helper);
    }

    @Override
    public void run() {
        lock.lock();

        try {
            for (Pack pack : helper.getPacks()) {
                System.out.println(pack.name());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
        helper.lock();

        try {
            for (Pack pack : helper.getPacks()) {
                System.out.println(pack.name());
            }
        } finally {
            helper.unlock();
        }
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "List all pack";
    }
}
