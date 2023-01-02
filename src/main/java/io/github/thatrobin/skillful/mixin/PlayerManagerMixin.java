package io.github.thatrobin.skillful.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.thatrobin.skillful.networking.SkillTabModPackets;
import io.github.thatrobin.skillful.skill_trees.Skill;
import io.github.thatrobin.skillful.skill_trees.SkillPowerRegistry;
import io.github.thatrobin.skillful.skill_trees.SkillTreeRegistry;
import io.github.thatrobin.skillful.utils.KeybindRegistry;
import io.github.thatrobin.skillful.utils.KeybindingData;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = PlayerManager.class, priority = 1001)
public class PlayerManagerMixin {

    @Inject(at = @At("TAIL"), method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
    private void onConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        PacketByteBuf skillData = new PacketByteBuf(Unpooled.buffer());
        Map<Identifier, Skill.Task> map = new HashMap<>();
        SkillTreeRegistry.entries().forEach(identifierTaskEntry -> map.put(identifierTaskEntry.getKey(), identifierTaskEntry.getValue()));
        skillData.writeMap(map, PacketByteBuf::writeIdentifier, ((packetByteBuf, task) -> task.toPacket(packetByteBuf)));
        ServerPlayNetworking.send(player, SkillTabModPackets.SKILL_DATA, skillData);

        PacketByteBuf keybindData = new PacketByteBuf(Unpooled.buffer());
        keybindData.writeInt(KeybindRegistry.size());
        KeybindRegistry.entries().forEach((bindingEntry) -> {
            Identifier identifier = bindingEntry.getKey();
            KeybindingData data = bindingEntry.getValue();
            keybindData.writeString(identifier.toString());
            data.toBuffer(keybindData, identifier);
        });

        ServerPlayNetworking.send(player, SkillTabModPackets.SEND_KEYBINDS, keybindData);

        PowerHolderComponent component = PowerHolderComponent.KEY.get(player);
        SkillPowerRegistry.entries().forEach(identifierPowerTypeEntry -> {
            PowerType<?> power = identifierPowerTypeEntry.getValue();
            if(!component.hasPower(power, identifierPowerTypeEntry.getKey())) {
                component.addPower(power, identifierPowerTypeEntry.getKey());
            }
        });
        component.sync();
    }
    
}
