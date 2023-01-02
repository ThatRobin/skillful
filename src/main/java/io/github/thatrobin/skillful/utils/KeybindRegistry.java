package io.github.thatrobin.skillful.utils;

import io.github.apace100.apoli.ApoliClient;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.mixin.ApoliClientAccessorMixin;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KeybindRegistry {

    private static final HashMap<Identifier, KeybindingData> idToUP = new HashMap<>();
    private static final List<Identifier> idList = new LinkedList<>();

    public static KeybindingData registerServer(Identifier id, KeybindingData binding) {
        if (idToUP.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate universal power id tried to register: '" + id.toString() + "'");
        }
        idToUP.put(id, binding);
        return binding;
    }

    public static KeyBinding registerClient(Identifier id, KeybindingData binding) {
        if (idToUP.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate universal power id tried to register: '" + id.toString() + "'");
        }
        idToUP.put(id, binding);
        InputUtil.Key key = InputUtil.Type.KEYSYM.map.values().stream().filter((akey -> akey.getTranslationKey().equals(binding.getKeyKey()))).toList().get(0);
        KeyBinding keyBinding = new KeyBinding(binding.getTranslationKey(), InputUtil.Type.KEYSYM, key.getCode(), binding.getCategory());
        KeyBindingHelper.registerKeyBinding(keyBinding);
        ApoliClient.registerPowerKeybinding(binding.getTranslationKey(), keyBinding);
        return keyBinding;
    }

    public static List<Identifier> getList() {
        return idList;
    }

    public static int size() {
        return idToUP.size();
    }

    public static Iterable<Map.Entry<Identifier, KeybindingData>> entries() {
        return idToUP.entrySet();
    }

    public static KeybindingData get(Identifier id) {
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

        List<KeyBinding> keybinds = new LinkedList<>();
        for (KeybindingData value : idToUP.values()) {
            InputUtil.Key key = InputUtil.Type.KEYSYM.map.values().stream().filter((akey -> akey.getTranslationKey().equals(value.getKeyKey()))).toList().get(0);
            keybinds.add(new KeyBinding(value.getTranslationKey(), InputUtil.Type.KEYSYM, key.getCode(), value.getCategory()));
        }

        MinecraftClient.getInstance().options.allKeys = KeyBindingRegistryImplExtention.removeAndProcess(MinecraftClient.getInstance().options.allKeys, keybinds.toArray(new KeyBinding[]{}));
        idToUP.clear();
    }

    public static void reset() {
        clear();
    }
}
