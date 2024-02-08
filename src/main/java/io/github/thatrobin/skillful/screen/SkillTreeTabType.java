package io.github.thatrobin.skillful.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public
enum SkillTreeTabType {
    ABOVE(new SkillTreeTabType.Textures(new Identifier("advancements/tab_above_left_selected"), new Identifier("advancements/tab_above_middle_selected"), new Identifier("advancements/tab_above_right_selected")), new SkillTreeTabType.Textures(new Identifier("advancements/tab_above_left"), new Identifier("advancements/tab_above_middle"), new Identifier("advancements/tab_above_right")), 28, 32, 8),
    BELOW(new SkillTreeTabType.Textures(new Identifier("advancements/tab_below_left_selected"), new Identifier("advancements/tab_below_middle_selected"), new Identifier("advancements/tab_below_right_selected")), new SkillTreeTabType.Textures(new Identifier("advancements/tab_below_left"), new Identifier("advancements/tab_below_middle"), new Identifier("advancements/tab_below_right")), 28, 32, 8),
    LEFT(new SkillTreeTabType.Textures(new Identifier("advancements/tab_left_top_selected"), new Identifier("advancements/tab_left_middle_selected"), new Identifier("advancements/tab_left_bottom_selected")), new SkillTreeTabType.Textures(new Identifier("advancements/tab_left_top"), new Identifier("advancements/tab_left_middle"), new Identifier("advancements/tab_left_bottom")), 32, 28, 5),
    RIGHT(new SkillTreeTabType.Textures(new Identifier("advancements/tab_right_top_selected"), new Identifier("advancements/tab_right_middle_selected"), new Identifier("advancements/tab_right_bottom_selected")), new SkillTreeTabType.Textures(new Identifier("advancements/tab_right_top"), new Identifier("advancements/tab_right_middle"), new Identifier("advancements/tab_right_bottom")), 32, 28, 5);

    private final SkillTreeTabType.Textures selectedTextures;
    private final SkillTreeTabType.Textures unselectedTextures;

    private final int width;
    private final int height;
    private final int tabCount;

    SkillTreeTabType(SkillTreeTabType.Textures selectedTextures, SkillTreeTabType.Textures unselectedTextures, int width, int height, int tabCount) {
        this.selectedTextures = selectedTextures;
        this.unselectedTextures = unselectedTextures;
        this.width = width;
        this.height = height;
        this.tabCount = tabCount;
    }

    public int getTabCount() {
        return this.tabCount;
    }

    public void drawBackground(DrawContext context, int x, int y, boolean selected, int index) {
        SkillTreeTabType.Textures textures = selected ? this.selectedTextures : this.unselectedTextures;
        Identifier identifier;
        if (index == 0) {
            identifier = textures.first();
        } else if (index == this.tabCount - 1) {
            identifier = textures.last();
        } else {
            identifier = textures.middle();
        }
        context.drawGuiTexture(identifier, x + this.getTabX(index), y + this.getTabY(index), this.width, this.height);
    }

    public void drawIcon(DrawContext context, int x, int y, int index, ItemStack stack) {
        int i = x + this.getTabX(index);
        int j = y + this.getTabY(index);
        switch (this) {
            case ABOVE -> {
                i += 6;
                j += 9;
            }
            case BELOW -> {
                i += 6;
                j += 6;
            }
            case LEFT -> {
                i += 10;
                j += 5;
            }
            case RIGHT -> {
                i += 6;
                j += 5;
            }
        }

        context.drawItemWithoutEntity(stack, i, j);
    }

    public int getTabX(int index) {
        return switch (this) {
            case ABOVE, BELOW -> (this.width + 4) * index;
            case LEFT -> -this.width + 4;
            case RIGHT -> 248;
        };
    }

    public int getTabY(int index) {
        return switch (this) {
            case ABOVE -> -this.height + 4;
            case BELOW -> 136;
            case LEFT, RIGHT -> this.height * index;
        };
    }

    public boolean isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
        int i = screenX + this.getTabX(index);
        int j = screenY + this.getTabY(index);
        return mouseX > (double)i && mouseX < (double)(i + this.width) && mouseY > (double)j && mouseY < (double)(j + this.height);
    }

    @Environment(EnvType.CLIENT)
    private static record Textures(Identifier first, Identifier middle, Identifier last) {
        Textures(Identifier first, Identifier middle, Identifier last) {
            this.first = first;
            this.middle = middle;
            this.last = last;
        }

        public Identifier first() {
            return this.first;
        }

        public Identifier middle() {
            return this.middle;
        }

        public Identifier last() {
            return this.last;
        }
    }
}
