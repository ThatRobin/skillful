package io.github.thatrobin.skillful;

import com.google.gson.JsonObject;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.calio.util.OrderedResourceListeners;
import io.github.thatrobin.skillful.components.SkillPointImpl;
import io.github.thatrobin.skillful.components.SkillPointInterface;
import io.github.thatrobin.skillful.factories.EntityActions;
import io.github.thatrobin.skillful.networking.SkillTabS2C;
import io.github.thatrobin.skillful.screen.SkillScreen;
import io.github.thatrobin.skillful.skill_trees.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class Skillful implements ModInitializer, EntityComponentInitializer {

    public static KeyBinding key = new KeyBinding("key.skillful.open_screen", GLFW.GLFW_KEY_J, "skillful");

    public static String MODID = "skillful";

    public static final Logger LOGGER = LogManager.getLogger(Skillful.class);

    public static ClientSkillManager skillManager;

    public static PowerSkillManager powerSkillManager = new PowerSkillManager();

    @Override
    public void onInitialize() {
        EntityActions.register();
        SkillTabS2C.register();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SkillTrees());

        KeyBindingHelper.registerKeyBinding(key);
        ClientTickEvents.START_CLIENT_TICK.register(tick -> {
            while(key.wasPressed()) {
                tick.setScreen(new SkillScreen(skillManager));
            }
        });
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(PlayerEntity.class, SkillPointInterface.INSTANCE)
                .impl(SkillPointImpl.class)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .end(SkillPointImpl::new);
    }

    public static Identifier identifier(String path) {
        return new Identifier(MODID, path);
    }
}
