package io.github.thatrobin.skillful.utils;

import io.github.apace100.apoli.ApoliClient;
import io.github.thatrobin.skillful.mixin.ApoliClientAccessorMixin;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeybindRegistry {

    private static final HashMap<Identifier, KeyBinding> idToUP = new HashMap<>();
    private static final List<Identifier> idList = Lists.newArrayList();

    public static KeyBinding register(Identifier id, KeyBinding binding) {
        if (idToUP.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate universal power id tried to register: '" + id.toString() + "'");
        }
        idToUP.put(id, binding);
        KeyBindingHelper.registerKeyBinding(binding);
        ApoliClient.registerPowerKeybinding(binding.getTranslationKey(), binding);
        return binding;
    }

    public static List<Identifier> getList() {
        return idList;
    }

    public static int size() {
        return idToUP.size();
    }

    public static Iterable<Map.Entry<Identifier, KeyBinding>> entries() {
        return idToUP.entrySet();
    }

    public static KeyBinding get(Identifier id) {
        if(!idToUP.containsKey(id)) {
            throw new IllegalArgumentException("Could not get power from id '" + id.toString() + "', as it was not registered!");
        }
        return idToUP.get(id);
    }

    public static boolean contains(Identifier id) {
        return idToUP.containsKey(id);
    }

    public static void clear() {
        HashMap<String, KeyBinding> map = ApoliClientAccessorMixin.getIdToKeyBindingMap();
        idToUP.values().forEach((keyBinding -> {
            map.remove(keyBinding.getTranslationKey());
        }));
        ApoliClientAccessorMixin.setIdToKeyBindingMap(map);

        MinecraftClient.getInstance().options.allKeys = KeyBindingRegistryImplExtention.removeAndProcess(MinecraftClient.getInstance().options.allKeys, idToUP.values().toArray(new KeyBinding[]{}));
        idToUP.clear();
    }

    public static void reset() {
        clear();
    }
}
