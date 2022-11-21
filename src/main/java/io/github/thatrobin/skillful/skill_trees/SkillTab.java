package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.thatrobin.skillful.screen.SkillScreen;
import io.github.thatrobin.skillful.screen.SkillTreeTabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.Objects;

public class SkillTab extends DrawableHelper {
    private final MinecraftClient client;
    private final SkillScreen screen;
    private final SkillTreeTabType type;
    private final int index;
    private final Skill root;
    private final SkillDisplay display;
    private final ItemStack icon;
    private final Text title;
    private final SkillWidget rootWidget;
    private final Map<Skill, SkillWidget> widgets = Maps.newLinkedHashMap();
    public double originX;
    public double originY;
    private int minPanX = 2147483647;
    private int minPanY = 2147483647;
    private int maxPanX = -2147483648;
    private int maxPanY = -2147483648;
    private float alpha;
    private boolean initialized;

    public SkillTab(MinecraftClient client, SkillScreen screen, SkillTreeTabType type, int index, Skill root, SkillDisplay display) {
        this.client = client;
        this.screen = screen;
        this.type = type;
        this.index = index;
        this.root = root;
        this.display = display;
        this.icon = display.getIcon();
        this.title = display.getTitle();
        this.rootWidget = new SkillWidget(this, client, root, display);
        this.addWidget(this.rootWidget, root);
    }

    @SuppressWarnings("unused")
    public SkillTreeTabType getType() {
        return this.type;
    }

    @SuppressWarnings("unused")
    public int getIndex() {
        return this.index;
    }

    public Skill getRoot() {
        return this.root;
    }

    public Text getTitle() {
        return this.title;
    }

    @SuppressWarnings("unused")
    public SkillDisplay getDisplay() {
        return this.display;
    }

    public void drawBackground(MatrixStack matrices, int x, int y, boolean selected) {
        this.type.drawBackground(matrices, this, x, y, selected, this.index);
    }

    public void drawIcon(int x, int y, ItemRenderer itemRenderer) {
        this.type.drawIcon(x, y, this.index, itemRenderer, this.icon);
    }

    public void render(MatrixStack matrices) {
        if (!this.initialized) {
            this.originX = 117 - (this.maxPanX + this.minPanX) / 2f;
            this.originY = 56 - (this.maxPanY + this.minPanY) / 2f;
            this.initialized = true;
        }

        matrices.push();
        matrices.translate(0.0D, 0.0D, 950.0D);
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(false, false, false, false);
        fill(matrices, 4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        matrices.translate(0.0D, 0.0D, -950.0D);
        RenderSystem.depthFunc(518);
        fill(matrices, 234, 113, 0, 0, -16777216);
        RenderSystem.depthFunc(515);
        Identifier identifier = this.display.getBackground();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Objects.requireNonNullElse(identifier, TextureManager.MISSING_IDENTIFIER));

        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        int k = i % 16;
        int l = j % 16;

        for(int m = -1; m <= 15; ++m) {
            for(int n = -1; n <= 8; ++n) {
                drawTexture(matrices, k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        this.rootWidget.renderLines(matrices, i, j, true);
        this.rootWidget.renderLines(matrices, i, j, false);
        this.rootWidget.renderWidgets(matrices, i, j);
        RenderSystem.depthFunc(518);
        matrices.translate(0.0D, 0.0D, -950.0D);
        RenderSystem.colorMask(false, false, false, false);
        fill(matrices, 4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(515);
        matrices.pop();
    }

    public void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int x) {
        matrices.push();
        matrices.translate(0.0D, 0.0D, -200.0D);
        fill(matrices, 0, 0, 234, 113, MathHelper.floor(this.alpha * 255.0F) << 24);
        boolean bl = false;
        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
            for (SkillWidget skillWidget : this.widgets.values()) {
                if (skillWidget.shouldRender(i, j, mouseX, mouseY)) {
                    bl = true;
                    skillWidget.drawTooltip(matrices, i, j, x);
                    break;
                }
            }
        }

        matrices.pop();
        if (bl) {
            this.alpha = MathHelper.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
        } else {
            this.alpha = MathHelper.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
        }

    }

    public boolean isClickOnTab(int screenX, int screenY, double mouseX, double mouseY) {
        return this.type.isClickOnTab(screenX, screenY, this.index, mouseX, mouseY);
    }

    @Nullable
    public static SkillTab create(MinecraftClient client, SkillScreen screen, int index, Skill root) {
        if (root.getDisplay() != null) {
            SkillTreeTabType[] var4 = SkillTreeTabType.values();
            for (SkillTreeTabType skillTabType : var4) {
                if (index < skillTabType.getTabCount()) {
                    return new SkillTab(client, screen, skillTabType, index, root, root.getDisplay());
                }

                index -= skillTabType.getTabCount();
            }

        }
        return null;
    }

    public void move(double offsetX, double offsetY) {
        if (this.maxPanX - this.minPanX > 234) {
            this.originX = MathHelper.clamp(this.originX + offsetX, -(this.maxPanX - 234), 0.0D);
        }

        if (this.maxPanY - this.minPanY > 113) {
            this.originY = MathHelper.clamp(this.originY + offsetY, -(this.maxPanY - 113), 0.0D);
        }

    }

    public void addSkill(Skill skill) {
        if (skill.getDisplay() != null) {
            SkillWidget advancementWidget = new SkillWidget(this, this.client, skill, skill.getDisplay());
            this.addWidget(advancementWidget, skill);
        }
    }

    private void addWidget(SkillWidget widget, Skill skill) {
        this.widgets.put(skill, widget);
        int i = widget.getX();
        int j = i + 28;
        int k = widget.getY();
        int l = k + 27;
        this.minPanX = Math.min(this.minPanX, i);
        this.maxPanX = Math.max(this.maxPanX, j);
        this.minPanY = Math.min(this.minPanY, k);
        this.maxPanY = Math.max(this.maxPanY, l);
        for (SkillWidget skillWidget : this.widgets.values()) {
            skillWidget.addToTree();
        }

    }

    @Nullable
    public SkillWidget getWidget(Skill skill) {
        return this.widgets.get(skill);
    }

    public boolean containsWidget(Skill skill) {
        return this.widgets.containsKey(skill);
    }

    public SkillScreen getScreen() {
        return this.screen;
    }
}
