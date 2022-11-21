package io.github.thatrobin.skillful.client;

import io.github.thatrobin.skillful.networking.SkillTabC2S;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkillfulClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SkillTabC2S.register();
    }

}
