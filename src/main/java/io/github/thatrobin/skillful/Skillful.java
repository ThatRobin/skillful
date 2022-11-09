package io.github.thatrobin.skillful;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
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
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class Skillful implements ModInitializer, EntityComponentInitializer {

    public static KeyBinding key = new KeyBinding("key.skillful.open_screen", GLFW.GLFW_KEY_J, "skillful");

    public static String MODID = "skillful";

    public static final Logger LOGGER = LogManager.getLogger(Skillful.class);

    public static ClientSkillManager skillManager;

    @Override
    public void onInitialize() {
        EntityActions.register();
        SkillTabS2C.register();

        /*
            int max = 1;
            Map<Identifier, Skill.Task> map = new HashMap<>();
            for (int i = 0; i < max; i++) {
                SkillDisplay display = new SkillDisplay(Registry.ITEM.stream().toList().get(i + 1).getDefaultStack(), new Identifier("example_pack", "test" + i), new LiteralText("title" + i), new LiteralText("description" + i), null, AdvancementFrame.TASK, false, false, false);
                map.put(new Identifier("example_pack", "testmap" + i), Skill.Task.create().display(display));
            }
            this.skillManager.getManager().load(map);

            Map<Identifier, Skill.Task> map2 = new HashMap<>();
            for (int i = 0; i < this.skillManager.getManager().getRoots().size(); i++) {
                Skill skill = this.skillManager.getManager().getRoots().get(i);
                SkillDisplay display = new SkillDisplay(Registry.ITEM.stream().toList().get(i + 2).getDefaultStack(), new Identifier("example_pack", "test" + i), new LiteralText("title2 " + i), new LiteralText("description2 " + i), null, AdvancementFrame.TASK, false, false, false);
                map2.put(new Identifier("example_pack", "testmap2" + i), Skill.Task.create().display(display).parent(skill));
            }
            this.skillManager.getManager().load(map2);
            for (Skill root : this.skillManager.getManager().getRoots()) {
                if (root.getDisplay() == null) continue;
                SkillPositioner.arrangeForTree(root);
            }
        });
        */

        OrderedResourceListeners.register(new SkillTrees()).complete();

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
