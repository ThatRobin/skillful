package io.github.thatrobin.skillful.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.components.SkillPointInterface;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SkillTabC2S {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SkillTabModPackets.APPLY_POWERS, SkillTabC2S::applyPowers);
    }

    private static void applyPowers(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        boolean hasParent = packetByteBuf.readBoolean();
        if(hasParent) {
            Identifier parentPowerId = packetByteBuf.readIdentifier();
            Identifier powerId = packetByteBuf.readIdentifier();
            Identifier sourceId = packetByteBuf.readIdentifier();
            int cost = packetByteBuf.readInt();
            Identifier rootId = packetByteBuf.readIdentifier();
            minecraftServer.execute(() -> {
                if (powerId != null && parentPowerId != null && sourceId != null) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(playerEntity);
                    PowerType<?> powerType = PowerTypeRegistry.get(powerId);
                    PowerType<?> parentPowerType = PowerTypeRegistry.get(parentPowerId);
                    if (component.hasPower(parentPowerType) && !component.hasPower(powerType)) {
                        SkillPointInterface skillPointInterface = SkillPointInterface.INSTANCE.get(playerEntity);
                        if(cost < skillPointInterface.getSkillPoints(rootId)) {
                            skillPointInterface.removeSkillPoints(rootId, cost);
                            skillPointInterface.sync();
                        }
                        component.addPower(powerType, sourceId);
                        component.sync();
                    }
                }
            });
        } else {
            Identifier powerId = packetByteBuf.readIdentifier();
            Identifier sourceId = packetByteBuf.readIdentifier();
            int cost = packetByteBuf.readInt();
            Identifier rootId = packetByteBuf.readIdentifier();
            minecraftServer.execute(() -> {
                if (powerId != null && sourceId != null) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(playerEntity);
                    PowerType<?> powerType = PowerTypeRegistry.get(powerId);
                    if(!component.hasPower(powerType)) {
                        SkillPointInterface skillPointInterface = SkillPointInterface.INSTANCE.get(playerEntity);
                        if(cost < skillPointInterface.getSkillPoints(rootId)) {
                            skillPointInterface.removeSkillPoints(rootId, cost);
                            skillPointInterface.sync();
                        }
                        component.addPower(powerType, sourceId);
                        component.sync();
                    }
                }
            });
        }
    }
}
