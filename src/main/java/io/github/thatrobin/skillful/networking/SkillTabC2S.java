package io.github.thatrobin.skillful.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.skill_trees.ClientSkillManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Iterator;
import java.util.Map;

public class SkillTabC2S {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SkillTabModPackets.APPLY_POWERS, SkillTabC2S::applyPowers);
    }

    private static void applyPowers(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        Identifier powerId = packetByteBuf.readIdentifier();
        minecraftServer.execute(() -> {
            Skillful.LOGGER.info("player is not null");
            PowerHolderComponent component = PowerHolderComponent.KEY.get(playerEntity);
            PowerType<?> powerType = PowerTypeRegistry.get(powerId);
            component.addPower(powerType, Skillful.identifier("skill_redeemed"));
            component.sync();
            component.getPowers().forEach((power) -> {
                Skillful.LOGGER.info(power.getType().getIdentifier());
            });
            Skillful.LOGGER.info("added power and synced");
        });
    }
}
