package io.github.thatrobin.skillful.screen;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.client.SkillfulClient;
import io.github.thatrobin.skillful.components.SkillPointInterface;
import io.github.thatrobin.skillful.networking.SkillTabModPackets;
import io.github.thatrobin.skillful.skill_trees.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SkillScreen extends Screen implements ClientSkillManager.Listener {
    private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
    private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final Text SAD_LABEL_TEXT = Text.translatable("skills.sad_label").setStyle(Text.translatable("skills.sad_label").getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://skillful-docs.readthedocs.io/en/latest/")));;
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
            this.skillManager.selectTab((this.tabs.values().iterator().next()).getRoot());
        } else {
            this.skillManager.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot());
        }
    }

    public void removed() {
        this.skillManager.setListener(null);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int i = (this.width - WINDOW_WIDTH) / 2;
            int j = (this.height - WINDOW_HEIGHT) / 2;
            for (SkillTab skillTab : this.tabs.values()) {
                if (!skillTab.isClickOnTab(i, j, mouseX, mouseY)) continue;
                this.skillManager.selectTab(skillTab.getRoot());
                break;
            }
            for (Skill skill : this.skillManager.getManager().getSkills()) {
                if (this.selectedTab != null) {
                    if (this.selectedTab.containsWidget(skill)) {
                        SkillWidget widget = this.selectedTab.getWidget(skill);
                        if (widget != null) {
                            if (!widget.isClickOnTab(i + (float) this.selectedTab.originX, j + (float) this.selectedTab.originY, mouseX, mouseY))
                                continue;
                            if(widget.getSkill().getPowers() != null) {
                                if (widget.getSkill().getParent() != null) {
                                    List<Identifier> powerTypes = widget.getSkill().getParent().getPowers();
                                    if (powerTypes != null) {
                                        if (powerTypes.stream().allMatch(PowerTypeRegistry::contains)) {
                                            if(widget.getSkill().getCondition() == null || widget.getSkill().getCondition().test(MinecraftClient.getInstance().player)) {
                                                buyWidgetPower(widget);
                                            }
                                        }
                                    }
                                } else {
                                    List<Identifier> powerTypes = widget.getSkill().getPowers();
                                    if(powerTypes != null) {
                                        if (powerTypes.stream().allMatch(PowerTypeRegistry::contains)) {
                                            if (widget.getSkill().getCondition() == null || widget.getSkill().getCondition().test(MinecraftClient.getInstance().player)) {
                                                buyWidgetPower(widget);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.client != null) {
            if (SkillfulClient.key.matchesKey(keyCode, scanCode)) {
                this.client.setScreen(null);
                this.client.mouse.lockCursor();
                return true;
            } else {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;
        this.renderBackground(context, mouseX, mouseY, delta);
        this.drawSkillTree(context, mouseX, mouseY, i, j);
        this.drawWindow(context, i, j);
        this.drawWidgetTooltip(context, mouseX, mouseY, i, j);
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

    private void drawSkillTree(DrawContext context, int mouseX, int mouseY, int x, int y) {
        SkillTab skillTab = this.selectedTab;
        if (skillTab == null) {
            context.fill(x + 9, y + 18, x + 9 + 234, y + 18 + 113, -16777216);
            int i = x + 9 + 117;
            context.drawCenteredTextWithShadow(this.textRenderer, EMPTY_TEXT, i, y + 18 + 56 - this.textRenderer.fontHeight / 2, -1);
            context.drawCenteredTextWithShadow(this.textRenderer, SAD_LABEL_TEXT, i, y + 18 + 113 - this.textRenderer.fontHeight, -1);
            return;
        }
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x + 9, y + 18, 0.0);
        RenderSystem.applyModelViewMatrix();
        skillTab.render(context, x, y);
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
    }

    public void drawWindow(DrawContext context, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
        context.drawTexture(WINDOW_TEXTURE, x, y, 0, 0, 252, 140);
        if (this.tabs.size() > 1) {
            RenderSystem.setShaderTexture(0, TABS_TEXTURE);
            for (SkillTab skillTab : this.tabs.values()) {
                skillTab.drawBackground(context, x, y, skillTab == this.selectedTab);
            }
            RenderSystem.defaultBlendFunc();
            for (SkillTab skillTab : this.tabs.values()) {
                skillTab.drawIcon(context, x, y);
            }
            RenderSystem.disableBlend();
        }
        context.drawText(this.textRenderer, SKILLS_TEXT, (x + 8), (y + 6), 0x404040, false);

        if(MinecraftClient.getInstance().player != null) {
            if (this.selectedTab != null) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                SkillPointInterface skillPointInterface = SkillPointInterface.INSTANCE.get(player);
                Integer points = skillPointInterface.getSkillPoints(this.selectedTab.getRoot().getId());
                if (points != null) {
                    Text SKILL_POINT_AMOUNT = Text.translatable("gui.skill_point_amount", points);

                    int textWidth = this.textRenderer.getWidth(SKILL_POINT_AMOUNT);
                    context.drawText(this.textRenderer, SKILL_POINT_AMOUNT, (x + (WINDOW_WIDTH - (textWidth + 8))), (y + 6), 0x404040, false);
                }
            }
        }
    }

    private void buyWidgetPower(SkillWidget skillWidget) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        List<Identifier> powerTypes = skillWidget.getSkill().getPowers();
        if(skillWidget.getSkill().getParent() != null) {
            List<Identifier> parentPowerTypes = skillWidget.getSkill().getParent().getPowers();
            if(parentPowerTypes != null) {
                if (parentPowerTypes.stream().allMatch((powerType) -> PowerTypeRegistry.contains(powerType))) {
                    if (powerTypes != null) {
                        if (powerTypes.stream().allMatch((powerType) -> PowerTypeRegistry.contains(powerType))) {
                            packetByteBuf.writeBoolean(true);
                            packetByteBuf.writeInt(parentPowerTypes.size());
                            for(Identifier parentPowerType : parentPowerTypes) {
                                packetByteBuf.writeIdentifier(parentPowerType);
                            }
                            packetByteBuf.writeInt(powerTypes.size());
                            for(Identifier powerType : powerTypes) {
                                packetByteBuf.writeIdentifier(powerType);
                            }
                        }
                    }
                }
            }
        } else {
            if(powerTypes != null) {
                if (powerTypes.stream().allMatch((powerType) -> PowerTypeRegistry.contains(powerType))) {
                    packetByteBuf.writeBoolean(false);
                    packetByteBuf.writeInt(powerTypes.size());
                    for (Identifier powerType : powerTypes) {
                        packetByteBuf.writeIdentifier(powerType);
                    }
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

    private void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.selectedTab != null) {
            MatrixStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.push();
            matrixStack.translate((x + 9), (y + 18), 400.0D);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            this.selectedTab.drawWidgetTooltip(context, mouseX - x - 9, mouseY - y - 18, x, y);
            RenderSystem.disableDepthTest();
            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();
        }

        if (this.tabs.size() > 1) {
            for (SkillTab skillTab : this.tabs.values()) {
                if (skillTab.isClickOnTab(x, y, mouseX, mouseY)) {
                    context.drawTooltip(this.textRenderer, skillTab.getTitle(), mouseX, mouseY);
                }
            }
        }

    }

    public void onRootAdded(Skill root) {
        SkillTab skillTab = SkillTab.create(this.client, this, this.tabs.size(), root);
        if (skillTab != null) {
            if(skillTab.getRoot().getCondition() == null || skillTab.getRoot().getCondition().test(MinecraftClient.getInstance().player)) {
                this.tabs.put(root, skillTab);
            }
        }
    }

    @Override
    public void onRootRemoved(Skill root) {
    }

    public void onDependentAdded(Skill dependent) {
        SkillTab skillTab = this.getTab(dependent);
        if (skillTab != null) {
            skillTab.addSkill(dependent);
        }

    }

    @Override
    public void onDependentRemoved(Skill dependent) {
    }

    public void selectTab(@Nullable Skill skill) {
        this.selectedTab = this.tabs.get(skill);
    }

    public void onClear() {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @SuppressWarnings("unused")
    public SkillWidget getSkillWidget(Skill skill) {
        SkillTab skillTab = this.getTab(skill);
        return skillTab == null ? null : skillTab.getWidget(skill);
    }

    @Nullable
    private SkillTab getTab(Skill skill) {
        while(skill.getParent() != null) {
            skill = skill.getParent();
        }

        return this.tabs.get(skill);
    }
}