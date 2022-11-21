package io.github.thatrobin.skillful.skill_trees;

import com.google.gson.*;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.data.SkillfulDataTypes;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.compress.utils.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SkillTrees extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public SkillTrees() {
        super(GSON, "skill_trees");

    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("skillful", "skill_trees");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> prepared, ResourceManager manager, Profiler profiler) {
        prepared.forEach((id, jel) -> {
            for (JsonElement je : jel) {
                try {
                    Skill.Task task = SkillfulDataTypes.SKILL.read(je);

                    SkillDisplay display = task.getDisplay();
                    if(display != null) {
                        task = task.display(display.setIdentifier(id));
                    }
                    if(SkillTreeRegistry.contains(id)) {
                        SkillTreeRegistry.update(id, task);
                    } else {
                        SkillTreeRegistry.register(id, task);
                    }
                } catch (Exception e) {
                    Skillful.LOGGER.error("There was a problem reading skill tree file " + id.toString() + " (skipping): " + e.getMessage());
                }
            }
        });
        for (Map.Entry<Identifier, Skill.Task> value : SkillTreeRegistry.entries()) {
            Skill.Task task = value.getValue();
            if(task.getParent() != null && !SkillTreeRegistry.contains(task.getParent())) {
                Skillful.LOGGER.error("Skill " + value.getKey() + " contains parent " + task.getParent() + " which doesn't exist.");
            }
        }
        Skillful.LOGGER.info("Finished Loading Skill Trees. number loaded: " + SkillTreeRegistry.size());
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        List<Identifier> requirements = Lists.newArrayList();
        requirements.add(new Identifier("apoli", "powers"));
        return requirements;
    }
}
