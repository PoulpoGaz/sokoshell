package fr.valax.sokoshell.commands;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.solver.State;
import org.openjdk.jol.info.ClassLayout;

import java.io.InputStream;
import java.io.PrintStream;

public class ObjectSizeCommand extends AbstractCommand {

    /**
     * @see org.openjdk.jol.vm.HotspotUnsafe
     */
    private static final String MAGIC_FIELD_OFFSET_OPTION = "jol.magicFieldOffset";
    private static final boolean MAGIC_FIELD_OFFSET = Boolean.parseBoolean(System.getProperty(MAGIC_FIELD_OFFSET_OPTION, "false"));;

    @Option(names = {"d", "detailed"}, description = "Print information about memory layout of the objects")
    private boolean detailed;

    @Option(names = {"f", "force"})
    private boolean force;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        if (!MAGIC_FIELD_OFFSET && !force) {
            err.println("You don't specify -D" + MAGIC_FIELD_OFFSET_OPTION + " to the jvm.");
            err.println("You can force with -f but it may make the program crash");

            return FAILURE;
        } else {
            ClassLayout state = ClassLayout.parseClass(State.class);
            ClassLayout intArray = ClassLayout.parseClass(int[].class);

            if (detailed) {
                out.println(state.toPrintable());
                out.println(intArray.toPrintable());
            } else {
                out.println("State size: " + state.instanceSize());
                out.println("Int array size: " + intArray.instanceSize());
            }

            return SUCCESS;
        }
    }

    @Override
    public String getName() {
        return "object-size";
    }

    @Override
    public String getShortDescription() {
        return "Returns the size of a State and an int array. Only works for Hotspot JVM and you will need to add -Djol.magicFieldOffset=true to the jvm";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
