package io.github.thatrobin.skillful.skill_trees;

import com.google.gson.*;
import io.github.apace100.apoli.integration.PowerReloadCallback;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.thatrobin.skillful.Skillful;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import org.apache.commons.compress.utils.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SkillTrees extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    private SkillManager manager = new SkillManager();
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
                    JsonObject jo = je.getAsJsonObject();
                    String name = jo.get("name").getAsString();
                    String description = jo.get("description").getAsString();
                    Identifier itemId = Identifier.tryParse(jo.get("icon").getAsString());
                    List<Identifier> powerIds = Lists.newArrayList();
                    if(jo.has("power")) {
                        powerIds.add(Identifier.tryParse(jo.get("power").getAsString()));
                    }
                    if(jo.has("powers")) {
                        for(JsonElement elememt : jo.getAsJsonArray("powers")) {
                            powerIds.add(Identifier.tryParse(elememt.getAsString()));
                        }
                    }
                    Identifier parent = null;
                    if(jo.has("parent")) {
                        parent = Identifier.tryParse(jo.get("parent").getAsString());
                    } else {
                        if(jo.has("default_powers")) {
                            JsonArray array = jo.getAsJsonArray("default_powers");
                            array.forEach((jsonElement -> {
                                Identifier powerID = Identifier.tryParse(jsonElement.getAsString());
                                PowerType<?> power = PowerTypeRegistry.get(powerID);
                                if(SkillPowerRegistry.contains(powerID)) {
                                    SkillPowerRegistry.update(powerID, power);
                                } else {
                                    SkillPowerRegistry.register(powerID, power);
                                }
                            }));
                        }
                    }
                    Identifier background = new Identifier("textures/block/stone.png");
                    if(jo.has("background")) {
                        background = Identifier.tryParse(jo.get("background").getAsString());
                    }
                    int cost = 0;
                    if(jo.has("cost")) {
                        cost = jo.get("cost").getAsInt();
                    }
                    ItemStack stack = Registry.ITEM.get(itemId).getDefaultStack();
                    SkillDisplay display = new SkillDisplay(stack, id, Text.literal(name), Text.literal(description), background, AdvancementFrame.TASK, false, false, false);
                    Skill.Task task = Skill.Task.create().display(display).cost(cost);
                    if(parent != null) {
                        task.parent(parent);
                    } if (!powerIds.isEmpty()) {
                        task.powers(powerIds);
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
        Skillful.LOGGER.info("Finished Loading Skill Trees. number loaded: " + SkillTreeRegistry.size());
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        List<Identifier> requirements = Lists.newArrayList();
        requirements.add(new Identifier("apoli", "powers"));
        return requirements;
    }
}
