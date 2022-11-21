package io.github.thatrobin.skillful.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.data.SkillfulDataTypes;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.List;
import java.util.Map;

public class KeybindManager extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public KeybindManager() {
        super(GSON, "keybinds");
    }

    @Override
    public Identifier getFabricId() {
        return Skillful.identifier("keybinds");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> prepared, ResourceManager manager, Profiler profiler) {
        KeybindRegistry.clear();
        prepared.forEach((id, jel) -> jel.forEach(je -> {
            try {
                KeybindingData key = SkillfulDataTypes.KEYBINDING.read(je);
                if(!KeybindRegistry.contains(id)) {
                    KeybindRegistry.register(id, key.getKeyBinding());
                }
            } catch(Exception e) {
                Skillful.LOGGER.error("There was a problem reading a KeyBinding file: " + id.toString() + " (skipping): " + e.getMessage());
            }
        }));
    }
}
