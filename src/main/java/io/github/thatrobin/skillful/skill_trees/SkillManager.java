package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.thatrobin.skillful.Skillful;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkillManager {
    private final Map<Identifier, Skill> advancements = Maps.newHashMap();
    private final Set<Skill> roots = Sets.newLinkedHashSet();
    private final Set<Skill> dependents = Sets.newLinkedHashSet();
    @Nullable
    private SkillManager.Listener listener;

    public SkillManager() {
    }

    private void remove(Skill advancement) {
        for (Skill advancement2 : advancement.getChildren()) {
            this.remove(advancement2);
        }

        Skillful.LOGGER.info("Forgot about advancement {}", advancement.getId());
        this.advancements.remove(advancement.getId());
        if (advancement.getParent() == null) {
            this.roots.remove(advancement);
            if (this.listener != null) {
                this.listener.onRootRemoved(advancement);
            }
        } else {
            this.dependents.remove(advancement);
            if (this.listener != null) {
                this.listener.onDependentRemoved(advancement);
            }
        }

    }

    public void removeAll(Set<Identifier> advancements) {
        for (Identifier identifier : advancements) {
            Skill advancement = this.advancements.get(identifier);
            if (advancement == null) {
                Skillful.LOGGER.warn("Told to remove advancement {} but I don't know what that is", identifier);
            } else {
                this.remove(advancement);
            }
        }

    }

    public void load(Map<Identifier, Skill.Task> map) {
        HashMap<Identifier, Skill.Task> map2 = Maps.newHashMap(map);
        while (!map2.isEmpty()) {
            boolean bl = false;
            Iterator<Map.Entry<Identifier, Skill.Task>> iterator = map2.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Identifier, Skill.Task> entry = iterator.next();
                Identifier identifier = entry.getKey();
                Skill.Task task = entry.getValue();
                if (!task.findParent(this.advancements::get)) continue;
                Skill advancement = task.build(identifier);
                this.advancements.put(identifier, advancement);
                bl = true;
                iterator.remove();
                if (advancement.getParent() == null) {
                    this.roots.add(advancement);
                    if (this.listener == null) continue;
                    this.listener.onRootAdded(advancement);
                    continue;
                }
                this.dependents.add(advancement);
                if (this.listener == null) continue;
                this.listener.onDependentAdded(advancement);
            }
            if (bl) continue;
            for (Map.Entry<Identifier, Skill.Task> entry : map2.entrySet()) {
                Skillful.LOGGER.error("Couldn't load advancement {}: {}", entry.getKey(), entry.getValue());
            }
        }
        Skillful.LOGGER.info("Loaded {} advancements", this.advancements.size());
    }

    public void clear() {
        this.advancements.clear();
        this.roots.clear();
        this.dependents.clear();
        if (this.listener != null) {
            this.listener.onClear();
        }
    }

    public List<Skill> getRoots() {
        return this.roots.stream().toList();
    }

    public Collection<Skill> getAdvancements() {
        return this.advancements.values();
    }

    @Nullable
    public Skill get(Identifier id) {
        return this.advancements.get(id);
    }

    public void setListener(@Nullable SkillManager.Listener listener) {
        this.listener = listener;
        if (listener != null) {
            Iterator<Skill> var2 = this.roots.iterator();

            Skill advancement;
            while(var2.hasNext()) {
                advancement = var2.next();
                listener.onRootAdded(advancement);
            }

            var2 = this.dependents.iterator();

            while(var2.hasNext()) {
                advancement = var2.next();
                listener.onDependentAdded(advancement);
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
