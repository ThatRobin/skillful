package io.github.thatrobin.skillful.screen;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.networking.SkillTabModPackets;
import io.github.thatrobin.skillful.skill_trees.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class SkillScreen extends Screen implements ClientSkillManager.Listener {
    private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
    private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int PAGE_OFFSET_X = 9;
    private static final int PAGE_OFFSET_Y = 18;
    public static final int PAGE_WIDTH = 234;
    public static final int PAGE_HEIGHT = 113;
    private static final int TITLE_OFFSET_X = 8;
    private static final int TITLE_OFFSET_Y = 6;
    public static final int field_32302 = 16;
    public static final int field_32303 = 16;
    public static final int field_32304 = 14;
    public static final int field_32305 = 7;
    private static final Text SAD_LABEL_TEXT = new TranslatableText("skills.sad_label");
    private static final Text EMPTY_TEXT = new TranslatableText("skills.empty");
    private static final Text SKILLS_TEXT = new TranslatableText("gui.skills");
    private final ClientSkillManager skillManager;
    public Map<Skill, SkillTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private SkillTab selectedTab;
    private boolean movingTab;

    public SkillScreen(ClientSkillManager skillManager) {
        super(NarratorManager.EMPTY);
        this.skillManager = skillManager;
    }

    protected void init() {
        this.tabs.clear();
        this.selectedTab = null;
        this.skillManager.setListener(this);
        if (this.selectedTab == null && !this.tabs.isEmpty()) {
            this.skillManager.selectTab((this.tabs.values().iterator().next()).getRoot(), true);
        } else {
            this.skillManager.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot(), true);
        }

    }

    public void removed() {
        this.skillManager.setListener(null);
        //ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
        //if (clientPlayNetworkHandler != null) {
        //    clientPlayNetworkHandler.sendPacket(AdvancementTabC2SPacket.close());
        //}

    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int i = (this.width - WINDOW_WIDTH) / 2;
            int j = (this.height - WINDOW_HEIGHT) / 2;
            for (SkillTab skillTab : this.tabs.values()) {
                if (!skillTab.isClickOnTab(i, j, mouseX, mouseY)) continue;
                this.skillManager.selectTab(skillTab.getRoot(), true);
                break;
            }
            for (Skill advancement : this.skillManager.getManager().getAdvancements()) {
                if(this.selectedTab != null) {
                    if (this.selectedTab.containsWidget(advancement)) {
                        SkillWidget widget = this.selectedTab.getWidget(advancement);
                        if(widget != null) {
                            if (!widget.isClickOnTab(i + (float)this.selectedTab.originX, j + (float)this.selectedTab.originY, mouseX, mouseY)) continue;
                            Skillful.LOGGER.info(widget.getSkill().getId());
                            buyWidgetPower(widget);
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client.options.keyAdvancements.matchesKey(keyCode, scanCode)) {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;
        this.renderBackground(matrices);
        this.drawAdvancementTree(matrices, mouseX, mouseY, i, j);
        this.drawWindow(matrices, i, j);
        this.drawWidgetTooltip(matrices, mouseX, mouseY, i, j);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0) {
            this.movingTab = false;
            return false;
        } else {
            if (!this.movingTab) {
                this.movingTab = true;
            } else if (this.selectedTab != null) {
                this.selectedTab.move(deltaX, deltaY);
            }

            return true;
        }
    }

    private void drawAdvancementTree(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
        SkillTab advancementTab = this.selectedTab;
        if (advancementTab == null) {
            AdvancementsScreen.fill(matrices, x + 9, y + 18, x + 9 + 234, y + 18 + 113, -16777216);
            int i = x + 9 + 117;
            AdvancementsScreen.drawCenteredText(matrices, this.textRenderer, EMPTY_TEXT, i, y + 18 + 56 - this.textRenderer.fontHeight / 2, -1);
            AdvancementsScreen.drawCenteredText(matrices, this.textRenderer, SAD_LABEL_TEXT, i, y + 18 + 113 - this.textRenderer.fontHeight, -1);
            return;
        }
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x + 9, y + 18, 0.0);
        RenderSystem.applyModelViewMatrix();
        advancementTab.render(matrices);
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
    }

    public void drawWindow(MatrixStack matrices, int x, int y) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
        this.drawTexture(matrices, x, y, 0, 0, 252, 140);
        if (this.tabs.size() > 1) {
            RenderSystem.setShaderTexture(0, TABS_TEXTURE);
            for (SkillTab skillTab : this.tabs.values()) {
                skillTab.drawBackground(matrices, x, y, skillTab == this.selectedTab);
            }
            RenderSystem.defaultBlendFunc();
            for (SkillTab skillTab : this.tabs.values()) {
                skillTab.drawIcon(x, y, this.itemRenderer);
            }
            RenderSystem.disableBlend();
        }
        this.textRenderer.draw(matrices, SKILLS_TEXT, (float)(x + 8), (float)(y + 6), 0x404040);
    }

    private void buyWidgetPower(SkillWidget skillWidget) {
        Identifier powerId = skillWidget.getSkill().getPowerId();
        if(powerId != null) {
            if (PowerTypeRegistry.contains(powerId)) {
                PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
                packetByteBuf.writeIdentifier(powerId);
                ClientPlayNetworking.send(SkillTabModPackets.APPLY_POWERS, packetByteBuf);
            }
        }
    }

    private void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab != null) {
            MatrixStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.push();
            matrixStack.translate((x + 9), (y + 18), 400.0D);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            this.selectedTab.drawWidgetTooltip(matrices, mouseX - x - 9, mouseY - y - 18, x, y);
            RenderSystem.disableDepthTest();
            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();
        }

        if (this.tabs.size() > 1) {
            Iterator var8 = this.tabs.values().iterator();

            while(var8.hasNext()) {
                SkillTab advancementTab = (SkillTab)var8.next();
                if (advancementTab.isClickOnTab(x, y, (double)mouseX, (double)mouseY)) {
                    this.renderTooltip(matrices, advancementTab.getTitle(), mouseX, mouseY);
                }
            }
        }

    }

    public void onRootAdded(Skill root) {
        SkillTab advancementTab = SkillTab.create(this.client, this, this.tabs.size(), root);
        if (advancementTab != null) {
            this.tabs.put(root, advancementTab);
        }
    }

    @Override
    public void onRootRemoved(Skill root) {
    }

    public void onDependentAdded(Skill dependent) {
        SkillTab advancementTab = this.getTab(dependent);
        if (advancementTab != null) {
            advancementTab.addSkill(dependent);
        }

    }

    @Override
    public void onDependentRemoved(Skill dependent) {
    }

    public void selectTab(@Nullable Skill advancement) {
        this.selectedTab = (SkillTab)this.tabs.get(advancement);
    }

    public void onClear() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Nullable
    public SkillWidget getAdvancementWidget(Skill advancement) {
        SkillTab advancementTab = this.getTab(advancement);
        return advancementTab == null ? null : advancementTab.getWidget(advancement);
    }

    @Nullable
    private SkillTab getTab(Skill advancement) {
        while(advancement.getParent() != null) {
            advancement = advancement.getParent();
        }

        return (SkillTab)this.tabs.get(advancement);
    }
}