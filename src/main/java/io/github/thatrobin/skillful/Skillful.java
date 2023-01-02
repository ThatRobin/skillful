package io.github.thatrobin.skillful;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.components.SkillPointImpl;
import io.github.thatrobin.skillful.components.SkillPointInterface;
import io.github.thatrobin.skillful.factories.EntityActions;
import io.github.thatrobin.skillful.networking.SkillTabC2S;
import io.github.thatrobin.skillful.networking.SkillTabModPackets;
import io.github.thatrobin.skillful.networking.SkillTabS2C;
import io.github.thatrobin.skillful.screen.SkillScreen;
import io.github.thatrobin.skillful.skill_trees.*;
import io.github.thatrobin.skillful.utils.KeybindManager;
import io.github.thatrobin.skillful.utils.KeybindRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class Skillful implements ModInitializer, EntityComponentInitializer {

    public static String MODID = "skillful";

    public static final Logger LOGGER = LogManager.getLogger(Skillful.class);

    public static ClientSkillManager skillManager = new ClientSkillManager();

    @Override
    public void onInitialize() {
        PowerSkillManager.initializeSkillData();
        EntityActions.register();
        SkillTabC2S.register();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SkillTrees());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new KeybindManager());

        ServerWorldEvents.UNLOAD.register(((server, world) -> {
            Skillful.clearRegistries();
            KeybindRegistry.clear();
        }));

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(((server, resourceManager) -> Skillful.clearRegistries()));


        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, success) -> {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                PacketByteBuf skillData = new PacketByteBuf(Unpooled.buffer());
                Map<Identifier, Skill.Task> map = new HashMap<>();
                SkillTreeRegistry.entries().forEach(identifierTaskEntry -> map.put(identifierTaskEntry.getKey(), identifierTaskEntry.getValue()));
                skillData.writeMap(map, PacketByteBuf::writeIdentifier, ((packetByteBuf, task) -> task.toPacket(packetByteBuf)));
                ServerPlayNetworking.send(serverPlayerEntity, SkillTabModPackets.SKILL_DATA, skillData);

                PowerHolderComponent component = PowerHolderComponent.KEY.get(serverPlayerEntity);
                SkillPowerRegistry.entries().forEach(identifierPowerTypeEntry -> {
                    PowerType<?> power = identifierPowerTypeEntry.getValue();
                    if(!component.hasPower(power, identifierPowerTypeEntry.getKey())) {
                        component.addPower(power, identifierPowerTypeEntry.getKey());
                    }
                });
                component.sync();
            }
        }));
    }

    public static void clearRegistries() {
        SkillTreeRegistry.clear();
        SkillPowerRegistry.clear();
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
