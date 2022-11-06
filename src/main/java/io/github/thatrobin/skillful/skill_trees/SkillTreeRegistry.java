package io.github.thatrobin.skillful.skill_trees;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SkillTreeRegistry {
    private static HashMap<Identifier, Skill.Task> idToSkill = new HashMap<>();

    public static Skill.Task register(Identifier id, Skill.Task skillTree) {
        if(idToSkill.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate skill tree id tried to register: '" + id.toString() + "'");
        }
        idToSkill.put(id, skillTree);
        return skillTree;
    }

    protected static Skill.Task update(Identifier id, Skill.Task skillTree) {
        if(idToSkill.containsKey(id)) {
            Skill.Task old = idToSkill.get(id);
            idToSkill.remove(id);
        }
        return register(id, skillTree);
    }

    public static int size() {
        return idToSkill.size();
    }

    public static Stream<Identifier> identifiers() {
        return idToSkill.keySet().stream();
    }

    public static Iterable<Map.Entry<Identifier, Skill.Task>> entries() {
        return idToSkill.entrySet();
    }

    public static int getIndexOf(Identifier id){
        return idToSkill.keySet().stream().toList().indexOf(id);
    }

    public static List<Skill.Task> values() {
        return idToSkill.values().stream().toList();
    }

    public static Skill.Task get(Identifier id) {
        if(!idToSkill.containsKey(id)) {
            throw new IllegalArgumentException("Could not get skill tree from id '" + id.toString() + "', as it was not registered!");
        }
        return idToSkill.get(id);
    }

    public static Identifier getId(Skill.Task skillTree) {
        return idToSkill.keySet().stream().toList().get(idToSkill.values().stream().toList().indexOf(skillTree));
    }

    public static boolean contains(Identifier id) {
        return idToSkill.containsKey(id);
    }

    public static void clear() {
        idToSkill.clear();
    }

    public static void reset() {
        clear();
    }
}
