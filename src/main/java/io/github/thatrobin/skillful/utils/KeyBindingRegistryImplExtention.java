package io.github.thatrobin.skillful.utils;

import com.google.common.collect.Lists;
import io.github.thatrobin.skillful.mixin.KeyBindingRegistryImplAccessorMixin;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.mixin.client.keybinding.KeyBindingAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.List;
import java.util.Map;

public class KeyBindingRegistryImplExtention {

    private static Map<String, Integer> getCategoryMap() {
        return KeyBindingAccessor.fabric_getCategoryMap();
    }

    public static KeyBinding unRegisterKeyBinding(KeyBinding binding) {
        List<KeyBinding> moddedKeyBindings = KeyBindingRegistryImplAccessorMixin.getModdedKeyBindings();
        for (KeyBinding existingKeyBindings : moddedKeyBindings) {
            if (existingKeyBindings == binding) {
                return moddedKeyBindings.remove(binding) ? binding : null;
            } else if (existingKeyBindings.getTranslationKey().equals(binding.getTranslationKey())) {
                return moddedKeyBindings.remove(binding) ? binding : null;
            }
        }
        MinecraftClient.getInstance().options.allKeys = KeyBindingRegistryImpl.process(MinecraftClient.getInstance().options.allKeys);
        throw new RuntimeException("Attempted to register two key bindings with equal ID: " + binding.getTranslationKey() + "!");
    }

    public static KeyBinding[] removeAndProcess(KeyBinding[] keysAll, KeyBinding... keyBindings) {
        List<KeyBinding> moddedKeyBindings = KeyBindingRegistryImplAccessorMixin.getModdedKeyBindings();
        List<KeyBinding> newKeysAll = Lists.newArrayList(keysAll);
        newKeysAll.removeAll(moddedKeyBindings);
        for (KeyBinding keyBinding : keyBindings) {
            moddedKeyBindings.remove(keyBinding);
        }
        newKeysAll.addAll(moddedKeyBindings);
        return newKeysAll.toArray(new KeyBinding[0]);
    }

}
