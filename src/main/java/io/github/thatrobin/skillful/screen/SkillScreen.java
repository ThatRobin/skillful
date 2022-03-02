package io.github.thatrobin.skillful.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.thatrobin.skillful.Skill;
import io.github.thatrobin.skillful.Skillful;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SkillScreen extends Screen {

    private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int PAGE_OFFSET_X = 9;
    private static final int PAGE_OFFSET_Y = 18;
    public static final int PAGE_WIDTH = 234;
    public static final int PAGE_HEIGHT = 113;
    private static final int TITLE_OFFSET_X = 8;
    private static final int TITLE_OFFSET_Y = 6;
    private static final Text SAD_LABEL_TEXT = new TranslatableText("skillful.sad_label");
    private static final Text EMPTY_TEXT = new TranslatableText("skillful.empty");
    private static final Text SKILLTREE_TEXT = new TranslatableText("gui.skillful");
    private int guiLeft;
    private int guiTop;

    public List<ButtonWidget> buttons = Lists.newArrayList();

    public SkillScreen() {
        super(NarratorManager.EMPTY);
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (this.width - WINDOW_WIDTH) / 2;
        guiTop = (this.height - WINDOW_HEIGHT) / 2;
        renderTabs();
    }

    private void renderTabs() {
        List<String> strings = Lists.newArrayList();

        for(int i = 0; i < strings.size()+1; i++) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TABS_TEXTURE);
            boolean bl2 = true;
            int j = i * 28;
            int k = 0;
            int l = guiLeft + 28 * i;
            int m = guiTop;
            int n = 32;
            if (i > 0) {
                l += i;
            }
            if(bl2) {
                m-=28;
            } else {
                k += 64;
                m += this.WINDOW_HEIGHT - 4;
            }
            buttons.add(new SkillWidget(l, m, 28, 32, 0, 0, new LiteralText("test"), (widget) -> {
                Apoli.LOGGER.info("TEST LOG BY SKILLFUL");
            }));
        }

        buttons.forEach(this::addDrawableChild);

    }

    @Override
    public void removed() {
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
        if (clientPlayNetworkHandler != null) {
            clientPlayNetworkHandler.sendPacket(AdvancementTabC2SPacket.close());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Skillful.key.matchesKey(keyCode, scanCode)) {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawWindow(matrices, guiLeft, guiTop);
        super.render(matrices,mouseX,mouseY,delta);
    }

    public void drawWindow(MatrixStack matrices, int x, int y) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
        this.drawTexture(matrices, x, y, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        this.textRenderer.draw(matrices, SKILLTREE_TEXT, (float)(x + TITLE_OFFSET_X), (float)(y + TITLE_OFFSET_Y), 0x404040);
    }

}