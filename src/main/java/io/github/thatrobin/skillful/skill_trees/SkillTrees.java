package io.github.thatrobin.skillful.skill_trees;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.thatrobin.skillful.Skillful;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

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
        SkillTreeRegistry.clear();
        prepared.forEach((id, jel) -> {
            for (JsonElement je : jel) {
                try {
                    JsonObject jo = je.getAsJsonObject();
                    String name = jo.get("name").getAsString();
                    Identifier itemId = Identifier.tryParse(jo.get("icon").getAsString());
                    Identifier display = Identifier.tryParse(jo.get("root").getAsString());
                    ItemStack stack = Registry.ITEM.get(itemId).getDefaultStack();
                    SkillTree skillTree = new SkillTree(id, name, stack);
                    if(SkillDisplayRegistry.contains(display)) {
                        skillTree.addChild(SkillDisplayRegistry.get(display));
                    } else {
                        Skillful.LOGGER.warn("Skill Display '" + display + "' did not exist in Registry");
                    }
                    SkillTreeRegistry.register(id, skillTree);
                } catch (Exception e) {
                    Skillful.LOGGER.error("There was a problem reading skill tree file " + id.toString() + " (skipping): " + e.getMessage());
                }
            }
        });
        Skillful.LOGGER.info("Finished Loading Skill Trees. number loaded: "+ SkillTreeRegistry.size());
    }
}
