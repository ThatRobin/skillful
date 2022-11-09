package io.github.thatrobin.skillful.skill_trees;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

public class ClientSkillManager {
    private final MinecraftClient client;
    private final SkillManager manager = new SkillManager();
    @Nullable
    private ClientSkillManager.Listener listener;
    @Nullable
    private Skill selectedTab;

    public ClientSkillManager(MinecraftClient client) {
        this.client = client;
    }

    public SkillManager getManager() {
        return this.manager;
    }

    public void selectTab(@Nullable Skill tab, boolean local) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeEnumConstant(Action.OPENED_TAB);
        buf.writeIdentifier(tab.getId());
        if (this.selectedTab != tab) {
            this.selectedTab = tab;
            if (this.listener != null) {
                this.listener.selectTab(tab);
            }
        }
    }

    public void setListener(@Nullable ClientSkillManager.Listener listener) {
        this.listener = listener;
        this.manager.setListener(listener);
        if (listener != null) {
            listener.selectTab(this.selectedTab);
        }

    }

    @Environment(EnvType.CLIENT)
    public interface Listener extends SkillManager.Listener {
        void selectTab(@Nullable Skill advancement);
    }

    public static enum Action {
        OPENED_TAB,
        CLOSED_SCREEN;

        private Action() {
        }
    }
}
