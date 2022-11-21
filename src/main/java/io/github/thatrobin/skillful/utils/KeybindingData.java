package io.github.thatrobin.skillful.utils;

import io.netty.buffer.Unpooled;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class KeybindingData {

    private KeyBinding keyBinding;

    public KeybindingData(KeyBinding keyBinding) {
        this.keyBinding = keyBinding;
    }

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }

    public String getTranslationKey() {
        return this.keyBinding.getTranslationKey();
    }

    public String getCategory() {
        return this.keyBinding.getCategory();
    }

    public PacketByteBuf toBuffer(PacketByteBuf buf, Identifier identifier) {
        String name = this.keyBinding.getTranslationKey();
        String category = this.keyBinding.getCategory();
        int code = this.keyBinding.getDefaultKey().getCode();
        buf.writeString(identifier.toString());
        buf.writeString(name);
        buf.writeString(category);
        buf.writeInt(code);
        return buf;
    }

    public static KeyBinding fromBuffer(PacketByteBuf buf) {
        Identifier id = Identifier.tryParse(buf.readString());
        String name = buf.readString();
        String category = buf.readString();
        int code = buf.readInt();
        if (id != null) {
            return new KeyBinding("key." + id.getNamespace() + "." + id.getPath(), InputUtil.Type.KEYSYM, code, category);
        } else {
            return new KeyBinding(name, InputUtil.Type.KEYSYM, code, category);
        }
    }
}
