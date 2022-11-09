package io.github.thatrobin.skillful.screen;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.components.SkillPointInterface;
import io.github.thatrobin.skillful.networking.SkillTabModPackets;
import io.github.thatrobin.skillful.skill_trees.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

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
    private static final Text SAD_LABEL_TEXT = Text.translatable("skills.sad_label");
    private static final Text EMPTY_TEXT = Text.translatable("skills.empty");
    private static final Text SKILLS_TEXT = Text.translatable("gui.skills");
    private final ClientSkillManager skillManager;
    public Map<Skill, SkillTab> tabs = Maps.newLinkedHashMap();
    @Nullable
    private SkillTab selectedTab;
    private boolean movingTab;

    public SkillScreen(ClientSkillManager skillManager) {
        super(NarratorManager.EMPTY);
        this.skillManager = skillManager;
    }

    @Override
    protected void init() {
        super.init();
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
                if (this.selectedTab != null) {
                    if (this.selectedTab.containsWidget(advancement)) {
                        SkillWidget widget = this.selectedTab.getWidget(advancement);
                        if (widget != null) {
                            if (!widget.isClickOnTab(i + (float) this.selectedTab.originX, j + (float) this.selectedTab.originY, mouseX, mouseY))
                                continue;
                            if (widget.getSkill().getParent() != null) {
                                Identifier powerId = widget.getSkill().getParent().getPowerId();
                                if (powerId != null) {
                                    if (PowerTypeRegistry.contains(powerId)) {
                                        buyWidgetPower(widget);
                                    }
                                }
                            } else {
                                buyWidgetPower(widget);
                            }
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client.options.advancementsKey.matchesKey(keyCode, scanCode)) {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
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

        if(MinecraftClient.getInstance().player != null) {
            if (this.selectedTab != null) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                SkillPointInterface skillPointInterface = SkillPointInterface.INSTANCE.get(player);
                Integer points = skillPointInterface.getSkillPoints(this.selectedTab.getRoot().getId());
                if (points != null) {
                    Text SKILL_POINT_AMOUNT = Text.translatable("gui.skill_point_amount", points);

                    int textWidth = this.textRenderer.getWidth(SKILL_POINT_AMOUNT);
                    this.textRenderer.draw(matrices, SKILL_POINT_AMOUNT, (float) (x + (WINDOW_WIDTH - (textWidth + 8))), (float) (y + 6), 0x404040);
                }
            }
        }
    }

    private void buyWidgetPower(SkillWidget skillWidget) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        Identifier powerId = skillWidget.getSkill().getPowerId();
        if(skillWidget.getSkill().getParent() != null) {
            Identifier parentPowerId = skillWidget.getSkill().getParent().getPowerId();
            if(parentPowerId != null) {
                if (PowerTypeRegistry.contains(parentPowerId)) {
                    if (powerId != null) {
                        if (PowerTypeRegistry.contains(powerId)) {
                            packetByteBuf.writeBoolean(true);
                            packetByteBuf.writeIdentifier(parentPowerId);
                            packetByteBuf.writeIdentifier(powerId);
                        }
                    }
                }
            }
        } else {
            if(powerId != null) {
                if (PowerTypeRegistry.contains(powerId)) {
                    packetByteBuf.writeBoolean(false);
                    packetByteBuf.writeIdentifier(powerId);
                }
            }
        }
        packetByteBuf.writeIdentifier(skillWidget.getSkill().getId());
        packetByteBuf.writeInt(skillWidget.getSkill().getCost());
        if(this.selectedTab != null) {
            packetByteBuf.writeIdentifier(this.selectedTab.getRoot().getId());
        }
        ClientPlayNetworking.send(SkillTabModPackets.APPLY_POWERS, packetByteBuf);
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