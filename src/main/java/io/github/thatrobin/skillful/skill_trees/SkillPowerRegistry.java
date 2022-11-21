package io.github.thatrobin.skillful.skill_trees;

import io.github.apace100.apoli.power.PowerType;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SkillPowerRegistry {
    private static final HashMap<Identifier, PowerType<?>> idToUP = new HashMap<>();

    public static PowerType<?> register(Identifier id, PowerType<?> power) {
        if(idToUP.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate universal power id tried to register: '" + id.toString() + "'");
        }
        idToUP.put(id, power);
        return power;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static PowerType<?> update(Identifier id, PowerType<?> skillTree) {
        idToUP.remove(id);
        return register(id, skillTree);
    }

    @SuppressWarnings("unused")
    public static int size() {
        return idToUP.size();
    }

    public static Iterable<Map.Entry<Identifier, PowerType<?>>> entries() {
        return idToUP.entrySet();
    }

    public static PowerType<?> get(Identifier id) {
        if(!idToUP.containsKey(id)) {
            throw new IllegalArgumentException("Could not get power from id '" + id.toString() + "', as it was not registered!");
        }
        return idToUP.get(id);
    }

    public static boolean contains(Identifier id) {
        return idToUP.containsKey(id);
    }

    public static void clear() {
        idToUP.clear();
    }

    @SuppressWarnings("unused")
    public static void reset() {
        clear();
    }
}
