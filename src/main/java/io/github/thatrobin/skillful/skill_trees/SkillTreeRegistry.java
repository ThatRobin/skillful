package io.github.thatrobin.skillful.skill_trees;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SkillTreeRegistry {
    private static final HashMap<Identifier, Skill.Task> idToSkill = new HashMap<>();

    public static Skill.Task register(Identifier id, Skill.Task skillTree) {
        if(idToSkill.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate skill tree id tried to register: '" + id.toString() + "'");
        }
        idToSkill.put(id, skillTree);
        return skillTree;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected static Skill.Task update(Identifier id, Skill.Task skillTree) {
        idToSkill.remove(id);
        return register(id, skillTree);
    }

    public static int size() {
        return idToSkill.size();
    }

    @SuppressWarnings("unused")
    public static Stream<Identifier> identifiers() {
        return idToSkill.keySet().stream();
    }

    public static Iterable<Map.Entry<Identifier, Skill.Task>> entries() {
        return idToSkill.entrySet();
    }

    @SuppressWarnings("unused")
    public static int getIndexOf(Identifier id){
        return idToSkill.keySet().stream().toList().indexOf(id);
    }

    @SuppressWarnings("unused")
    public static List<Skill.Task> values() {
        return idToSkill.values().stream().toList();
    }

    public static Skill.Task get(Identifier id) {
        if(!idToSkill.containsKey(id)) {
            throw new IllegalArgumentException("Could not get skill tree from id '" + id.toString() + "', as it was not registered!");
        }
        return idToSkill.get(id);
    }

    @SuppressWarnings("unused")
    public static Identifier getId(Skill.Task skillTree) {
        return idToSkill.keySet().stream().toList().get(idToSkill.values().stream().toList().indexOf(skillTree));
    }

    public static boolean contains(Identifier id) {
        return idToSkill.containsKey(id);
    }

    public static void clear() {
        idToSkill.clear();
    }

    @SuppressWarnings("unused")
    public static void reset() {
        clear();
    }
}
