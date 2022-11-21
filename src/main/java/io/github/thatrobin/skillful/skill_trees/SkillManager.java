package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.thatrobin.skillful.Skillful;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkillManager {
    private final Map<Identifier, Skill> skills = Maps.newHashMap();
    private final Set<Skill> roots = Sets.newLinkedHashSet();
    private final Set<Skill> dependents = Sets.newLinkedHashSet();
    @Nullable
    private SkillManager.Listener listener;

    public SkillManager() {
    }

    private void remove(Skill skill) {
        for (Skill skill2 : skill.getChildren()) {
            this.remove(skill2);
        }

        Skillful.LOGGER.info("Forgot about skill {}", skill.getId());
        this.skills.remove(skill.getId());
        if (skill.getParent() == null) {
            this.roots.remove(skill);
            if (this.listener != null) {
                this.listener.onRootRemoved(skill);
            }
        } else {
            this.dependents.remove(skill);
            if (this.listener != null) {
                this.listener.onDependentRemoved(skill);
            }
        }

    }

    @SuppressWarnings("unused")
    public void removeAll(Set<Identifier> skills) {
        for (Identifier identifier : skills) {
            Skill skill = this.skills.get(identifier);
            if (skill == null) {
                Skillful.LOGGER.warn("Told to remove skill {} but I don't know what that is", identifier);
            } else {
                this.remove(skill);
            }
        }

    }

    public void load(Map<Identifier, Skill.Task> map) {
        HashMap<Identifier, Skill.Task> map2 = Maps.newHashMap(map);
        for(Map.Entry<Identifier, Skill.Task> entry : map2.entrySet()) {
            try {
                Identifier identifier = entry.getKey();
                Skill.Task task = entry.getValue();
                if (!task.findParent(this.skills::get)) continue;
                Skill skill = task.build(identifier);
                this.skills.put(identifier, skill);
                if (skill.getParent() == null) {
                    this.roots.add(skill);
                    if (this.listener == null) continue;
                    this.listener.onRootAdded(skill);
                    continue;
                }
                this.dependents.add(skill);
                if (this.listener == null) continue;
                this.listener.onDependentAdded(skill);
            } catch (Exception e) {
                Skillful.LOGGER.error("Couldn't load skill {}: {}", entry.getKey(), e.getMessage());
            }
        }
        Skillful.LOGGER.info("Loaded {} skills", this.skills.size());
    }

    @SuppressWarnings("unused")
    public void clear() {
        this.skills.clear();
        this.roots.clear();
        this.dependents.clear();
        if (this.listener != null) {
            this.listener.onClear();
        }
    }

    public List<Skill> getRoots() {
        return this.roots.stream().toList();
    }

    public Collection<Skill> getSkills() {
        return this.skills.values();
    }

    @Nullable
    public Skill get(Identifier id) {
        return this.skills.get(id);
    }

    public void setListener(@Nullable SkillManager.Listener listener) {
        this.listener = listener;
        if (listener != null) {
            Iterator<Skill> var2 = this.roots.iterator();

            Skill skill;
            while(var2.hasNext()) {
                skill = var2.next();
                listener.onRootAdded(skill);
            }

            var2 = this.dependents.iterator();

            while(var2.hasNext()) {
                skill = var2.next();
                listener.onDependentAdded(skill);
            }
        }

    }

    public interface Listener {
        void onRootAdded(Skill root);

        void onRootRemoved(Skill root);

        void onDependentAdded(Skill dependent);

        void onDependentRemoved(Skill dependent);

        void onClear();
    }
}
