package io.github.thatrobin.skillful.skill_trees;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class SkillTree extends DrawableHelper {

    private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");

    private ItemStack icon;
    private Identifier identifier;
    private Text name;
    private double originX;
    private double originY;
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private int minPanX = 2147483647;
    private int minPanY = 2147483647;
    private int maxPanX = -2147483648;
    private int maxPanY = -2147483648;
    private boolean initialized = false;

    private List<SkillDisplay> widgets = Lists.newArrayList();

    public SkillTree(Identifier identifier, String name, ItemStack icon) {
        this.icon = icon;
        this.name = new LiteralText(name);
        this.identifier = identifier;
    }

    public Text getName() {
        return this.name;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    private void addWidget(SkillDisplay widget) {
        this.widgets.add(widget);
    }

    public void renderTab(MatrixStack matrices, int x, int y) {
        matrices.push();
        int el = SkillTreeRegistry.getIndexOf(this.identifier);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TABS_TEXTURE);

        boolean bl2 = el < 9;
        int i = el % 9;
        int j = 0;
        int k = 0;
        int l = x + 27 * i;
        int m = (y + 51) / 2;
        int n = 32;
        if (i > 0) {
            l += i;
        }
        if (el == 0) {
            k += 32;
        }
        if (bl2) {
            m -= 28;
        } else {
            k += 64;
            m += 136;
        }
        this.drawTexture(matrices, l, m, j, k, 28, 32);
        int o = (bl2 ? 0 : -1);
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.zOffset = 100.0f;
        ItemStack itemStack = this.getIcon();
        itemRenderer.renderInGuiWithOverrides(itemStack, l + 6, m + 8 + o);
        itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, itemStack, l + o, m + o);
        itemRenderer.zOffset = 0.0f;
        matrices.pop();
    }

    public void renderBackground(MatrixStack matrices, int x, int y) {
        matrices.push();
        //matrices.translate(x, y, 950.0D);
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(false, false, false, false);
        fill(matrices, 4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        //matrices.translate(1.0D, 1.0D, -950.0D);
        RenderSystem.depthFunc(518);
        fill(matrices, x + 8, x + 16, WINDOW_WIDTH - 5, WINDOW_HEIGHT - 5, -16777216);
        RenderSystem.depthFunc(515);
        Identifier identifier = new Identifier("minecraft", "textures/gui/advancements/backgrounds/stone.png");
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, identifier);
        x += 5;
        y += 5;
        int i = WINDOW_WIDTH;
        int j = WINDOW_HEIGHT;
        int k = (i % 16) + x;
        int l = (j % 16) + y;

        for(int m = -1; m <= 14; ++m) {
            for(int n = -1; n <= 7; ++n) {
                if(n == 7 && m == 14) {
                    drawTexture(matrices, k + (16 * m), l + (16 * n), 0.0F, 0.0F, 8, 7, 8, 7);
                } else if (n == 7) {
                    drawTexture(matrices, k + (16 * m), l + (16 * n), 0.0F, 0.0F, 16, 7, 16, 7);
                } else if (m == 14) {
                    drawTexture(matrices, k + (16 * m), l + (16 * n), 0.0F, 0.0F, 8, 16, 8, 16);
                } else {
                    drawTexture(matrices, k + (16 * m), l + (16 * n), 0.0F, 0.0F, 16, 16, 16, 16);
                }
            }
        }
        RenderSystem.depthFunc(518);
        //matrices.translate(-(x+1), -(y+1), -950.0D);
        matrices.pop();
    }



    public void addChild(SkillDisplay widget) {
        this.addWidget(widget);
    }

    public List<SkillDisplay> getChildren() {
        return this.widgets;
    }

    public void move(double offsetX, double offsetY) {
        if (this.maxPanX - this.minPanX > 234) {
            this.originX = MathHelper.clamp(this.originX + offsetX, (double)(-(this.maxPanX - 234)), 0.0D);
        }

        if (this.maxPanY - this.minPanY > 113) {
            this.originY = MathHelper.clamp(this.originY + offsetY, (double)(-(this.maxPanY - 113)), 0.0D);
        }

    }
}
