package io.github.thatrobin.skillful.client;

import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.networking.SkillTabC2S;
import io.github.thatrobin.skillful.networking.SkillTabS2C;
import io.github.thatrobin.skillful.screen.SkillScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SkillfulClient implements ClientModInitializer {

    public static KeyBinding key = new KeyBinding("key.skillful.open_screen", GLFW.GLFW_KEY_J, "skillful");

    @Override
    public void onInitializeClient() {
        SkillTabS2C.register();
        ClientTickEvents.START_CLIENT_TICK.register(tick -> {
            while(key.wasPressed()) {
                tick.setScreen(new SkillScreen(Skillful.skillManager));
            }
        });

        KeyBindingHelper.registerKeyBinding(key);
    }

}
