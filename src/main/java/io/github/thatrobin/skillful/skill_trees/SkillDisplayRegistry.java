package io.github.thatrobin.skillful.skill_trees;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SkillDisplayRegistry {
    private static HashMap<Identifier, SkillDisplay> idToSkillDis = new HashMap<>();

    public static SkillDisplay register(Identifier id, SkillDisplay skillDisplay) {
        if(idToSkillDis.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate skill display id tried to register: '" + id.toString() + "'");
        }
        idToSkillDis.put(id, skillDisplay);
        return skillDisplay;
    }

    protected static SkillDisplay update(Identifier id, SkillDisplay skillDisplay) {
        if(idToSkillDis.containsKey(id)) {
            SkillDisplay old = idToSkillDis.get(id);
            idToSkillDis.remove(id);
        }
        return register(id, skillDisplay);
    }

    public static int size() {
        return idToSkillDis.size();
    }

    public static Stream<Identifier> identifiers() {
        return idToSkillDis.keySet().stream();
    }

    public static Iterable<Map.Entry<Identifier, SkillDisplay>> entries() {
        return idToSkillDis.entrySet();
    }

    public static int getIndexOf(Identifier id){
        return idToSkillDis.keySet().stream().toList().indexOf(id);
    }

    public static Iterable<SkillDisplay> values() {
        return idToSkillDis.values();
    }

    public static SkillDisplay get(Identifier id) {
        if(!idToSkillDis.containsKey(id)) {
            throw new IllegalArgumentException("Could not get skill display from id '" + id.toString() + "', as it was not registered!");
        }
        SkillDisplay skillDisplay = idToSkillDis.get(id);
        return skillDisplay;
    }

    public static Identifier getId(SkillDisplay skillDisplay) {
        return skillDisplay.getIdentifier();
    }

    public static boolean contains(Identifier id) {
        return idToSkillDis.containsKey(id);
    }

    public static void clear() {
        idToSkillDis.clear();
    }

    public static void reset() {
        clear();
    }
}
