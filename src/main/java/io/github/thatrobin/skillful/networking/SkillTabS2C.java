package io.github.thatrobin.skillful.networking;

import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.skill_trees.ClientSkillManager;
import io.github.thatrobin.skillful.skill_trees.Skill;
import io.github.thatrobin.skillful.skill_trees.SkillPositioner;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;

public class SkillTabS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(SkillTabModPackets.SKILL_DATA, SkillTabS2C::recieveSkillData);
        }));
    }

    @Environment(EnvType.CLIENT)
    private static void recieveSkillData(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        Skillful.skillManager = new ClientSkillManager(minecraftClient);
        Map<Identifier, Skill.Task> map = packetByteBuf.readMap(PacketByteBuf::readIdentifier, Skill.Task::fromPacket);
        Skillful.skillManager.getManager().load(map);

        for (Skill root : Skillful.skillManager.getManager().getRoots()) {
            if (root.getDisplay() == null) continue;
            SkillPositioner.arrangeForTree(root);
        }
    }

}
