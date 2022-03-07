package io.github.thatrobin.skillful.skill_trees;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.thatrobin.skillful.Skillful;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class SkillDisplay extends DrawableHelper {

    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
    private final Text title;
    private final Text description;
    private final ItemStack icon;
    private final Identifier identifier;
    private final AdvancementFrame frame;
    private final boolean hidden;
    private List<SkillDisplay> children = Lists.newArrayList();

    public SkillDisplay(Identifier id, ItemStack icon, Text title, Text description, AdvancementFrame frame, boolean hidden) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.identifier = id;
        this.frame = frame;
        this.hidden = hidden;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public boolean addChild(SkillDisplay skillDisplay) {
        return this.children.add(skillDisplay);
    }

    public void render(MatrixStack matrices, int x, int y) {
        matrices.push();
        if (!this.isHidden()) {
            x += 26;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            int z = 26;
            if (PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player).hasPower(PowerTypeRegistry.get(this.getIdentifier()))) {
                z -= 26;
            }
            this.drawTexture(matrices, x + 50, y + 50, this.getFrame().getTextureV(), 128 + z, 26, 26);
            MinecraftClient.getInstance().getItemRenderer().renderInGui(this.getIcon(), x + 55, y + 55);
        }
        matrices.pop();
        this.renderChildrenRecursive(matrices, x, y);
    }

    public void renderChildrenRecursive(MatrixStack matrices, int x, int y) {
        for (SkillDisplay child : this.children) {
            matrices.push();
            if (!child.isHidden()) {
                x+=26;
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
                int z = 26;
                if(PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player).hasPower(PowerTypeRegistry.get(child.getIdentifier()))) {
                    z -= 26;
                }
                this.drawTexture(matrices, x + 50, y + 50, child.getFrame().getTextureV(), 128 + z, 26, 26);
                MinecraftClient.getInstance().getItemRenderer().renderInGui(child.getIcon(), x + 55, y + 55);
            }
            matrices.pop();
            child.renderChildrenRecursive(matrices, x, y);
        }
    }

    public Text getTitle() {
        return this.title;
    }

    public Text getDescription() {
        return this.description;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public AdvancementFrame getFrame() {
        return this.frame;
    }

    public boolean isHidden() {
        return this.hidden;
    }


}
