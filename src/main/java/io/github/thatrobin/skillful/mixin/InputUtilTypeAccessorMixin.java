package io.github.thatrobin.skillful.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = InputUtil.Type.class, remap = false)
public interface InputUtilTypeAccessorMixin {

    @Accessor("map")
    Int2ObjectMap<InputUtil.Key> getMap();

}
