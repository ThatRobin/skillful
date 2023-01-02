package io.github.thatrobin.skillful.networking;

import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.skill_trees.ClientSkillManager;
import io.github.thatrobin.skillful.skill_trees.Skill;
import io.github.thatrobin.skillful.skill_trees.SkillPositioner;
import io.github.thatrobin.skillful.utils.KeybindRegistry;
import io.github.thatrobin.skillful.utils.KeybindingData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SkillTabS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(SkillTabModPackets.SKILL_DATA, SkillTabS2C::recieveSkillData);
            ClientPlayNetworking.registerReceiver(SkillTabModPackets.SEND_KEYBINDS, SkillTabS2C::sendKeyBinds);
        }));
    }

    @Environment(EnvType.CLIENT)
    private static void recieveSkillData(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        Map<Identifier, Skill.Task> map = packetByteBuf.readMap(PacketByteBuf::readIdentifier, Skill.Task::fromPacket);
        Skillful.skillManager.getManager().load(map);

        for (Skill root : Skillful.skillManager.getManager().getRoots()) {
            if (root.getDisplay() == null) continue;
            SkillPositioner.arrangeForTree(root);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void sendKeyBinds(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        try {
            int amount = packetByteBuf.readInt();
            Map<Identifier, KeybindingData> map = new HashMap<>();
            for(int i = 0; i < amount; i++) {
                Identifier id = Identifier.tryParse(packetByteBuf.readString());
                KeybindingData key = KeybindingData.fromBuffer(packetByteBuf);
                map.put(id, key);
            }
            minecraftClient.execute(() -> {
                KeybindRegistry.reset();
                map.forEach(((identifier, keyBinding) -> {
                    if (!KeybindRegistry.contains(identifier)) {
                        KeybindRegistry.registerClient(identifier, keyBinding);
                    }
                }));
                minecraftClient.options.allKeys = KeyBindingRegistryImpl.process(minecraftClient.options.allKeys);
            });
            Skillful.LOGGER.info("Finished loading client KeyBinding from data files. Registry contains " + KeybindRegistry.size() + " KeyBinding files.");
        } catch (Exception e) {
            Skillful.LOGGER.error(e.getStackTrace());
        }

    }

}
