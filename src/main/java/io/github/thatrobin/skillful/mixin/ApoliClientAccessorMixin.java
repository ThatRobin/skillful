package io.github.thatrobin.skillful.mixin;

import io.github.apace100.apoli.ApoliClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(value = ApoliClient.class, remap = false)
public interface ApoliClientAccessorMixin {

    @Accessor
    static HashMap<String, KeyBinding> getIdToKeyBindingMap() {
        throw new AssertionError();
    }

    @Accessor
    static void setIdToKeyBindingMap(HashMap<String, KeyBinding> ignoredMap) {
        throw new AssertionError();
    }
}
