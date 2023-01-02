package io.github.thatrobin.skillful.networking;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.components.SkillPointInterface;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;

import java.util.LinkedList;
import java.util.List;

public class SkillTabC2S {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SkillTabModPackets.APPLY_POWERS, SkillTabC2S::applyPowers);
    }

    private static void applyPowers(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        boolean hasParent = packetByteBuf.readBoolean();
        if(hasParent) {
            List<Identifier> parentPowerIds = new LinkedList<>();
            int parentSize = packetByteBuf.readInt();
            for (int i = 0; i < parentSize; i++) {
                parentPowerIds.add(packetByteBuf.readIdentifier());
            }
            List<Identifier> powerIds = new LinkedList<>();
            int powerSize = packetByteBuf.readInt();
            for (int i = 0; i < powerSize; i++) {
                powerIds.add(packetByteBuf.readIdentifier());
            }
            Identifier sourceId = packetByteBuf.readIdentifier();
            int cost = packetByteBuf.readInt();
            Identifier rootId = packetByteBuf.readIdentifier();
            minecraftServer.execute(() -> {
                if (sourceId != null) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(playerEntity);
                    if (parentPowerIds.stream().allMatch((identifier) -> component.hasPower(PowerTypeRegistry.get(identifier))) && powerIds.stream().noneMatch((identifier) -> component.hasPower(PowerTypeRegistry.get(identifier)))) {
                        SkillPointInterface skillPointInterface = SkillPointInterface.INSTANCE.get(playerEntity);
                        if(skillPointInterface.getSkillPoints(rootId) != null) {
                            if (cost <= skillPointInterface.getSkillPoints(rootId)) {
                                skillPointInterface.removeSkillPoints(rootId, cost);
                                skillPointInterface.sync();
                                powerIds.forEach((powerId) -> component.addPower(PowerTypeRegistry.get(powerId), sourceId));
                            }
                            component.sync();
                        }
                    }
                }
            });
        } else {
            List<Identifier> powerIds = new LinkedList<>();
            int powerSize = packetByteBuf.readInt();
            for (int i = 0; i < powerSize; i++) {
                powerIds.add(packetByteBuf.readIdentifier());
            }
            Identifier sourceId = packetByteBuf.readIdentifier();
            int cost = packetByteBuf.readInt();
            Identifier rootId = packetByteBuf.readIdentifier();
            minecraftServer.execute(() -> {
                if (sourceId != null) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(playerEntity);
                    if (powerIds.stream().noneMatch((identifier) -> component.hasPower(PowerTypeRegistry.get(identifier)))) {
                        SkillPointInterface skillPointInterface = SkillPointInterface.INSTANCE.get(playerEntity);
                        if(cost < skillPointInterface.getSkillPoints(rootId)) {
                            skillPointInterface.removeSkillPoints(rootId, cost);
                            skillPointInterface.sync();
                        }
                        powerIds.forEach((powerId) -> component.addPower(PowerTypeRegistry.get(powerId), sourceId));
                        component.sync();
                    }
                }
            });
        }
    }
}
