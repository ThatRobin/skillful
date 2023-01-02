package io.github.thatrobin.skillful.utils;

import io.netty.buffer.Unpooled;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class KeybindingData {

    private String translationKey;
    private String keyKey;
    private String category;

    public KeybindingData(String translationKey, String keyKey, String category) {
        this.category = category;
        this.keyKey = keyKey;
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public String getKeyKey() {
        return this.keyKey;
    }

    public String getCategory() {
        return this.category;
    }

    public PacketByteBuf toBuffer(PacketByteBuf buf, Identifier identifier) {
        String name = this.translationKey;
        String key = this.keyKey;
        String category = this.category;
        buf.writeString(identifier.toString());
        buf.writeString(name);
        buf.writeString(key);
        buf.writeString(category);
        return buf;
    }

    public static KeybindingData fromBuffer(PacketByteBuf buf) {
        Identifier id = Identifier.tryParse(buf.readString());
        String name = buf.readString();
        String key = buf.readString();
        String category = buf.readString();
        if (id != null) {
            return new KeybindingData("key." + id.getNamespace() + "." + id.getPath(), key, category);
        } else {
            return new KeybindingData(name, key, category);
        }
    }
}
