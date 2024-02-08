package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.thatrobin.skillful.screen.SkillScreen;
import io.github.thatrobin.skillful.screen.SkillTreeTabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
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

public class SkillTab {
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

    public void drawBackground(DrawContext context, int x, int y, boolean selected) {
        this.type.drawBackground(context, x, y, selected, this.index);
    }

    public void drawIcon(DrawContext context, int x, int y) {
        this.type.drawIcon(context, x, y, this.index, this.icon);
    }

    public void render(DrawContext context, int x, int y) {
        if (!this.initialized) {
            this.originX = 117 - (this.maxPanX + this.minPanX) / 2f;
            this.originY = 56 - (this.maxPanY + this.minPanY) / 2f;
            this.initialized = true;
        }

        context.enableScissor(x, y, x + 234, y + 113);
        context.getMatrices().push();
        context.getMatrices().translate((float)x, (float)y, 0.0F);
        Identifier identifier = (Identifier)Objects.requireNonNullElse(this.display.getBackground(), TextureManager.MISSING_IDENTIFIER);
        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        int k = i % 16;
        int l = j % 16;

        for(int m = -1; m <= 15; ++m) {
            for(int n = -1; n <= 8; ++n) {
                context.drawTexture(identifier, k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        this.rootWidget.renderLines(context, i, j, true);
        this.rootWidget.renderLines(context, i, j, false);
        this.rootWidget.renderWidgets(context, i, j);
        context.getMatrices().pop();
        context.disableScissor();
    }

    public void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int x, int y) {
        context.getMatrices().push();
        context.getMatrices().translate(0.0D, 0.0D, -200.0D);
        context.fill(0, 0, 234, 113, MathHelper.floor(this.alpha * 255.0F) << 24);
        boolean bl = false;
        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
            for (SkillWidget skillWidget : this.widgets.values()) {
                if (skillWidget.shouldRender(i, j, mouseX, mouseY)) {
                    bl = true;
                    skillWidget.drawTooltip(context, i, j, this.alpha, x, y);
                    break;
                }
            }
        }

        context.getMatrices().pop();
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
