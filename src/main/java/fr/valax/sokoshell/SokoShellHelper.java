package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.Pack;

import java.util.Collection;
import java.util.HashMap;

public class SokoShellHelper {

    private final HashMap<String, Pack> packs;

    public SokoShellHelper() {
        packs = new HashMap<>();
    }

    /**
     * Add a pack if there is no pack with his name
     * @param pack the pack to add
     * @return true if the pack was added
     */
    public boolean addPack(Pack pack) {
        if (packs.containsKey(pack.name())) {
            return false;
        } else {
            packs.put(pack.name(), pack);

            return true;
        }
    }

    public void addPackReplace(Pack pack) {
        packs.put(pack.name(), pack);
    }

    public Pack getPack(String name) {
        return packs.get(name);
    }

    public Collection<Pack> getPacks() {
        return packs.values();
    }
}
