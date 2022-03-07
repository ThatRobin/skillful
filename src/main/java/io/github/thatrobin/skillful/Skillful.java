package io.github.thatrobin.skillful;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.AdditionalPowerDataCallback;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.util.OrderedResourceListeners;
import io.github.thatrobin.skillful.screen.SkillScreen;
import io.github.thatrobin.skillful.skill_trees.SkillDisplay;
import io.github.thatrobin.skillful.skill_trees.SkillDisplayRegistry;
import io.github.thatrobin.skillful.skill_trees.SkillTrees;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
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
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class Skillful implements ModInitializer {

    public static KeyBinding key = new KeyBinding("key.skillful.open_screen", GLFW.GLFW_KEY_J, "skillful");

    public static final Logger LOGGER = LogManager.getLogger(Skillful.class);

    @Override
    public void onInitialize() {
        PowerTypes.registerAdditionalData("skill", (powerId, factoryId, isSubPower, data, powerType) -> {
            JsonObject jo = data.getAsJsonObject();
            ItemStack itemStack = Registry.ITEM.get(Identifier.tryParse(jo.get("icon").getAsString())).getDefaultStack();
            LiteralText title = new LiteralText(jo.get("title").getAsString());
            Identifier parent = null;
            if(jo.has("parent")) {
                 parent = Identifier.tryParse(jo.get("parent").getAsString());
            }
            LiteralText desc = new LiteralText(jo.get("description").getAsString());
            if(!SkillDisplayRegistry.contains(powerId)) {
                SkillDisplay skillDisplay = new SkillDisplay(powerId, itemStack, title, desc, AdvancementFrame.TASK, false);
                SkillDisplayRegistry.register(powerId, skillDisplay);
                if(SkillDisplayRegistry.get(parent) != null) {
                    SkillDisplayRegistry.get(parent).addChild(skillDisplay);
                }
            }
        });


        OrderedResourceListeners.register(new SkillTrees()).after(Apoli.identifier( "powers")).complete();

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.START_CLIENT_TICK.register(tick -> {
            while(key.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new SkillScreen());
            }
        });
    }

}
