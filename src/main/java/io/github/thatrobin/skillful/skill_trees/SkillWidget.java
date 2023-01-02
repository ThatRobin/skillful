package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.Skillful;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkillWidget extends DrawableHelper {
    private static final Identifier WIDGETS_TEXTURE = Skillful.identifier("textures/gui/skills/widgets.png");
    private static final int[] SPLIT_OFFSET_CANDIDATES = new int[]{0, 10, -10, 25, -25};
    private final SkillTab tab;
    private final Skill skill;
    private final SkillDisplay display;
    private final OrderedText title;
    private final int width;
    private final List<OrderedText> description;
    private final MinecraftClient client;
    @Nullable
    private SkillWidget parent;
    private final List<SkillWidget> children = Lists.newArrayList();
    private final int x;
    private final int y;

    public SkillWidget(SkillTab tab, MinecraftClient client, Skill skill, SkillDisplay display) {
        this.tab = tab;
        this.skill = skill;
        this.display = display;
        this.client = client;
        this.title = Language.getInstance().reorder(client.textRenderer.trimToWidth(display.getTitle(), 163));
        this.x = MathHelper.floor(display.getX() * 28.0F);
        this.y = MathHelper.floor(display.getY() * 27.0F);
        int k = 0;
        int l = 29 + client.textRenderer.getWidth(this.title) + k;
        this.description = Language.getInstance().reorder(this.wrapDescription(Texts.setStyleIfAbsent(display.getDescription().copy(), Style.EMPTY.withColor(display.getFrame().getTitleFormat())), l));
        OrderedText orderedText;
        for(Iterator<OrderedText> var9 = this.description.iterator(); var9.hasNext(); l = Math.max(l, client.textRenderer.getWidth(orderedText))) {
            orderedText = var9.next();
        }

        this.width = l + 3 + 5;
    }

    public boolean isClickOnTab(float screenX, float screenY, double mouseX, double mouseY) {
        float i = screenX + this.getX() + 15;
        float j = screenY + this.getY() + 20;
        return mouseX > i && mouseX < (i + 26) && mouseY > j && mouseY < (j + 26);
    }

    public Skill getSkill() {
        return this.skill;
    }

    private static float getMaxWidth(TextHandler textHandler, List<StringVisitable> lines) {
        Optional<StringVisitable> longestO = lines.stream().max(Comparator.comparingInt((test) -> test.getString().length()));
        if(longestO.isPresent()) {
            StringVisitable longest = longestO.get();
            Objects.requireNonNull(textHandler);
            return textHandler.getWidth(longest);
        }
        return 0;
    }

    private List<StringVisitable> wrapDescription(Text text, int width) {
        TextHandler textHandler = this.client.textRenderer.getTextHandler();
        List<StringVisitable> list = null;
        float f = 3.4028235E38F;
        for (int i : SPLIT_OFFSET_CANDIDATES) {
            List<StringVisitable> list2 = textHandler.wrapLines(text, width - i, Style.EMPTY);
            if (!(this.skill.getCost() == 0 && this.skill.getParent() == null)) {
                list2.add(Text.literal("Cost: " + this.skill.getCost()));
            }
            float g = Math.abs(getMaxWidth(textHandler, list2) - (float) width);
            if (g <= 10.0F) {
                return list2;
            }

            if (g < f) {
                f = g;
                list = list2;
            }
        }

        return list;
    }

    @Nullable
    private SkillWidget getParent(Skill skill) {
        do {
            skill = skill.getParent();
        } while(skill != null && skill.getDisplay() == null);

        if (skill != null && skill.getDisplay() != null) {
            return this.tab.getWidget(skill);
        } else {
            return null;
        }
    }

    public void renderLines(MatrixStack matrices, int x, int y, boolean border) {
        if (this.parent != null) {
            int i = x + this.parent.x + 13;
            int j = x + this.parent.x + 26 + 4;
            int k = y + this.parent.y + 13;
            int l = x + this.x + 13;
            int m = y + this.y + 13;
            int n = border ? -16777216 : -1;
            if (border) {
                this.drawHorizontalLine(matrices, j, i, k - 1, n);
                this.drawHorizontalLine(matrices, j + 1, i, k, n);
                this.drawHorizontalLine(matrices, j, i, k + 1, n);
                this.drawHorizontalLine(matrices, l, j - 1, m - 1, n);
                this.drawHorizontalLine(matrices, l, j - 1, m, n);
                this.drawHorizontalLine(matrices, l, j - 1, m + 1, n);
                this.drawVerticalLine(matrices, j - 1, m, k, n);
                this.drawVerticalLine(matrices, j + 1, m, k, n);
            } else {
                this.drawHorizontalLine(matrices, j, i, k, n);
                this.drawHorizontalLine(matrices, l, j, m, n);
                this.drawVerticalLine(matrices, j, m, k, n);
            }
        }

        for (SkillWidget skillWidget : this.children) {
            skillWidget.renderLines(matrices, x, y, border);
        }
    }

    public void renderWidgets(MatrixStack matrices, int x, int y) {
        if (!this.display.isHidden()) {
            if (MinecraftClient.getInstance().player != null) {
                SkillObtainedStatus skillObtainedStatus = SkillObtainedStatus.LOCKED;
                List<Identifier> powerTypes = this.skill.getPowers();
                PowerHolderComponent component = PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player);
                if ((this.skill.getParent() == null || this.skill.getParent().getPowers().stream().allMatch((id) -> component.hasPower(PowerTypeRegistry.get(id))) && (this.skill.getCondition() == null || this.skill.getCondition().test(MinecraftClient.getInstance().player)))) {
                    skillObtainedStatus = SkillObtainedStatus.UNOBTAINED;
                }
                if (powerTypes != null) {
                    if(powerTypes.stream().allMatch((id) -> component.hasPower(PowerTypeRegistry.get(id)))) {
                        skillObtainedStatus = SkillObtainedStatus.OBTAINED;
                    }
                }
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
                this.drawTexture(matrices, x + this.x + 3, y + this.y, this.display.getFrame().getTextureV(), 128 + skillObtainedStatus.getSpriteIndex() * 26, 26, 26);
                this.client.getItemRenderer().renderInGui(this.display.getIcon(), x + this.x + 8, y + this.y + 5);
            }
        }
        for (SkillWidget widget : this.children) {
            widget.renderWidgets(matrices, x, y);
        }
    }

    @SuppressWarnings("unused")
    public int getWidth() {
        return this.width;
    }

    public void addChild(SkillWidget widget) {
        this.children.add(widget);
    }

    @SuppressWarnings("ConstantConditions")
    public void drawTooltip(MatrixStack matrices, int originX, int originY, int x) {
        boolean bl = x + originX + this.x + this.width + 26 >= this.tab.getScreen().width;
        boolean bl2 = 113 - originY - this.y - 26 <= 6 + this.description.size() * this.client.textRenderer.fontHeight;
        SkillObtainedStatus skillObtainedStatus = SkillObtainedStatus.LOCKED;
        float f = 1.0f;
        int j = MathHelper.floor(f * (float)this.width);
        if(f >= 1.0f) {
            j = this.width / 2;
        }
        PowerHolderComponent component = PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player);
        if((this.skill.getParent() == null || this.skill.getParent().getPowers().stream().allMatch((id) -> component.hasPower(PowerTypeRegistry.get(id)))) && (this.skill.getCondition() == null ||this.skill.getCondition().test(MinecraftClient.getInstance().player))) {
            skillObtainedStatus = SkillObtainedStatus.UNOBTAINED;
        }
        if(this.skill.getPowers() != null) {
            if (MinecraftClient.getInstance().player != null && this.skill.getPowers().stream().allMatch((id) -> component.hasPower(PowerTypeRegistry.get(id)))) {
                skillObtainedStatus = SkillObtainedStatus.OBTAINED;
            }
        }
        int k = this.width - j;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        int l = originY + this.y;
        int m;
        if (bl) {
            m = originX + this.x - this.width + 26 + 6;
        } else {
            m = originX + this.x;
        }

        int var10001 = this.description.size();
        Objects.requireNonNull(this.client.textRenderer);
        int n = 32 + var10001 * 9;
        if (!this.description.isEmpty()) {
            if (bl2) {
                this.renderDescriptionBackground(matrices, m, l + 26 - n, this.width, n);
            } else {
                this.renderDescriptionBackground(matrices, m, l, this.width, n);
            }
        }
        this.drawTexture(matrices, m, l, 0, skillObtainedStatus.getSpriteIndex() * 26, j, 26);
        this.drawTexture(matrices, m + j, l, 200 - k, skillObtainedStatus.getSpriteIndex() * 26, k, 26);
        this.drawTexture(matrices, originX + this.x + 3, originY + this.y, this.display.getFrame().getTextureV(), 128 + skillObtainedStatus.getSpriteIndex() * 26, 26, 26);
        if (bl) {
            this.client.textRenderer.drawWithShadow(matrices, this.title, (float)(m + 5), (float)(originY + this.y + 9), -1);
        } else {
            this.client.textRenderer.drawWithShadow(matrices, this.title, (float)(originX + this.x + 32), (float)(originY + this.y + 9), -1);
        }

        float var10003;
        int o;
        int var10004;
        TextRenderer var21;
        OrderedText var22;
        if (bl2) {
            for(o = 0; o < this.description.size(); ++o) {
                var21 = this.client.textRenderer;
                var22 = this.description.get(o);
                var10003 = (float)(m + 5);
                var10004 = l + 26 - n + 7;
                Objects.requireNonNull(this.client.textRenderer);
                var21.draw(matrices, var22, var10003, (float)(var10004 + o * 9), -5592406);
            }
        } else {
            for(o = 0; o < this.description.size(); ++o) {
                var21 = this.client.textRenderer;
                var22 = this.description.get(o);
                var10003 = (float)(m + 5);
                var10004 = originY + this.y + 9 + 17;
                Objects.requireNonNull(this.client.textRenderer);
                var21.draw(matrices, var22, var10003, (float)(var10004 + o * 9), -5592406);
            }
        }

        this.client.getItemRenderer().renderInGui(this.display.getIcon(), originX + this.x + 8, originY + this.y + 5);
    }

    protected void renderDescriptionBackground(MatrixStack matrices, int x, int y, int width, int height) {
        this.drawTexture(matrices, x, y, 0, 52, 10, 10);
        this.drawTextureRepeatedly(matrices, x + 10, y, width - 10 - 10, 10, 10, 52, 200 - 10 - 10, 26);
        this.drawTexture(matrices, x + width - 10, y, 200 - 10, 52, 10, 10);
        this.drawTexture(matrices, x, y + height - 10, 0, 52 + 26 - 10, 10, 10);
        this.drawTextureRepeatedly(matrices, x + 10, y + height - 10, width - 10 - 10, 10, 10, 52 + 26 - 10, 200 - 10 - 10, 26);
        this.drawTexture(matrices, x + width - 10, y + height - 10, 200 - 10, 52 + 26 - 10, 10, 10);
        this.drawTextureRepeatedly(matrices, x, y + 10, 10, height - 10 - 10, 0, 52 + 10, 200, 26 - 10 - 10);
        this.drawTextureRepeatedly(matrices, x + 10, y + 10, width - 10 - 10, height - 10 - 10, 10, 52 + 10, 200 - 10 - 10, 26 - 10 - 10);
        this.drawTextureRepeatedly(matrices, x + width - 10, y + 10, 10, height - 10 - 10, 200 - 10, 52 + 10, 200, 26 - 10 - 10);
    }

    protected void drawTextureRepeatedly(MatrixStack matrices, int x, int y, int width, int height, int u, int v, int textureWidth, int textureHeight) {
        for(int i = 0; i < width; i += textureWidth) {
            int j = x + i;
            int k = Math.min(textureWidth, width - i);

            for(int l = 0; l < height; l += textureHeight) {
                int m = y + l;
                int n = Math.min(textureHeight, height - l);
                this.drawTexture(matrices, j, m, u, v, k, n);
            }
        }

    }

    public boolean shouldRender(int originX, int originY, int mouseX, int mouseY) {
        if (!this.display.isHidden()) {
            int i = originX + this.x;
            int j = i + 26;
            int k = originY + this.y;
            int l = k + 26;
            return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
        } else {
            return false;
        }
    }

    public void addToTree() {
        if (this.parent == null && this.skill.getParent() != null) {
            this.parent = this.getParent(this.skill);
            if (this.parent != null) {
                this.parent.addChild(this);
            }
        }

    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }

}
