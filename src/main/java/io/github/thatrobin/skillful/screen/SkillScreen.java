package io.github.thatrobin.skillful.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.skill_trees.SkillDisplay;
import io.github.thatrobin.skillful.skill_trees.SkillTree;
import io.github.thatrobin.skillful.skill_trees.SkillTreeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SkillScreen extends Screen {

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
    private boolean movingTab;
    private SkillTree selectedTab;

    public SkillScreen() {
        super(NarratorManager.EMPTY);
    }

    @Override
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

    private void drawEmptyUI(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
        matrices.push();
        fill(matrices, x + 9, y + 18, x + 9 + 234, y + 18 + 113, -16777216);
        int i = x + 9 + 117;
        TextRenderer var10001 = this.textRenderer;
        Text var10002 = EMPTY_TEXT;
        int var10004 = y + 18 + 56;
        Objects.requireNonNull(this.textRenderer);
        drawCenteredText(matrices, var10001, var10002, i, var10004 - 9 / 2, -1);
        var10001 = this.textRenderer;
        var10002 = SAD_LABEL_TEXT;
        var10004 = y + 18 + 113;
        Objects.requireNonNull(this.textRenderer);
        drawCenteredText(matrices, var10001, var10002, i, var10004 - 9, -1);
        matrices.pop();
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
        guiLeft = (this.width - WINDOW_WIDTH) / 2;
        guiTop = (this.height - WINDOW_HEIGHT) / 2;
        List<SkillTree> skillTrees = SkillTreeRegistry.values();
        this.renderBackground(matrices);
        if(!skillTrees.isEmpty()) {
            this.selectedTab = skillTrees.get(0);
        }
        if(skillTrees.size() != 0) {
            this.selectedTab = skillTrees.get(0);
            for (SkillTree skillTree : skillTrees) {
                if (skillTree != selectedTab) {
                    skillTree.renderTab(matrices, guiLeft, guiTop);
                }
            }
        }
        if(this.selectedTab != null) {
            this.selectedTab.renderBackground(matrices, guiLeft, guiTop);
        } else {
            this.drawEmptyUI(matrices, mouseX, mouseY, guiLeft, guiTop);
        }
        this.drawWindow(matrices, guiLeft, guiTop);
        if(this.selectedTab != null) {
            this.selectedTab.getChildren().forEach(skillDisplay -> {
                skillDisplay.render(matrices, guiLeft, guiTop);
                skillDisplay.renderChildrenRecursive(matrices, guiLeft, guiTop);
            });
            this.selectedTab.renderTab(matrices, guiLeft, guiTop);
        }
        super.render(matrices,mouseX,mouseY,delta);
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        this.renderBackground(matrices, 0);
    }

    public void drawWindow(MatrixStack matrices, int x, int y) {
        matrices.push();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
        this.drawTexture(matrices, x, y, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        Text text = new LiteralText("Skill Tree");
        if(this.selectedTab != null) {
            text = this.selectedTab.getName();
        }
        this.textRenderer.draw(matrices, text, (float)(x + TITLE_OFFSET_X), (float)(y + TITLE_OFFSET_Y), 0x404040);
        matrices.pop();
    }

}