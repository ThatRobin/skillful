package io.github.thatrobin.skillful.skill_trees;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SkillTreeRegistry {
    private static HashMap<Identifier, SkillTree> idToSkillTree = new HashMap<>();

    public static SkillTree register(Identifier id, SkillTree skillTree) {
        if(idToSkillTree.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate skill tree id tried to register: '" + id.toString() + "'");
        }
        idToSkillTree.put(id, skillTree);
        return skillTree;
    }

    protected static SkillTree update(Identifier id, SkillTree skillTree) {
        if(idToSkillTree.containsKey(id)) {
            SkillTree old = idToSkillTree.get(id);
            idToSkillTree.remove(id);
        }
        return register(id, skillTree);
    }

    public static int size() {
        return idToSkillTree.size();
    }

    public static Stream<Identifier> identifiers() {
        return idToSkillTree.keySet().stream();
    }

    public static Iterable<Map.Entry<Identifier, SkillTree>> entries() {
        return idToSkillTree.entrySet();
    }

    public static int getIndexOf(Identifier id){
        return idToSkillTree.keySet().stream().toList().indexOf(id);
    }

    public static List<SkillTree> values() {
        return idToSkillTree.values().stream().toList();
    }

    public static SkillTree get(Identifier id) {
        if(!idToSkillTree.containsKey(id)) {
            throw new IllegalArgumentException("Could not get skill tree from id '" + id.toString() + "', as it was not registered!");
        }
        SkillTree skillTree = idToSkillTree.get(id);
        return skillTree;
    }

    public static Identifier getId(SkillTree skillTree) {
        return skillTree.getIdentifier();
    }

    public static boolean contains(Identifier id) {
        return idToSkillTree.containsKey(id);
    }

    public static void clear() {
        idToSkillTree.clear();
    }

    public static void reset() {
        clear();
    }
}
