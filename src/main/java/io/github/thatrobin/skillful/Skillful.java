package io.github.thatrobin.skillful;

import io.github.apace100.apoli.power.PowerTypes;
import io.github.thatrobin.skillful.screen.SkillScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class Skillful implements ModInitializer {

    public static KeyBinding key = new KeyBinding("key.skillful.open_screen", GLFW.GLFW_KEY_J, "skillful");

    @Override
    public void onInitialize() {
        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.START_CLIENT_TICK.register(tick -> {
            while(key.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new SkillScreen());
            }
        });
    }

}
