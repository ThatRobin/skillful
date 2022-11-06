package io.github.thatrobin.skillful.client;

import io.github.thatrobin.skillful.networking.SkillTabC2S;
import io.github.thatrobin.skillful.skill_trees.ClientSkillManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientAdvancementManager;

@Environment(EnvType.CLIENT)
public class SkillfulClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkillTabC2S.register();
    }
}
