package io.github.thatrobin.skillful.skill_trees;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SkillDisplay {
    private Text title;
    private Text description;
    private final ItemStack icon;
    private final Identifier background;
    private final AdvancementFrame frame;
    private Identifier identifier;
    private final boolean hidden;
    private float x;
    private float y;

    public SkillDisplay(ItemStack icon, Text title, Text description, Identifier background, AdvancementFrame frame, boolean hidden) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.background = background;
        this.frame = frame;
        this.hidden = hidden;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public SkillDisplay setIdentifier(Identifier identifier) {
        this.identifier = identifier;
        return this;
    }

    public void setName(Text name) {
        this.title = name;
    }

    public void setDescription(Text description) {
        this.description = description;
    }

    public Identifier getIdentifier() {
        return identifier;
    }


    public Text getDescription() {
        return this.description;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public Text getTitle() {
        return this.title;
    }

    public Identifier getBackground() {
        return this.background;
    }

    public AdvancementFrame getFrame() {
        return this.frame;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isHidden() {
        return this.hidden;
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeText(this.title);
        buf.writeText(this.description);
        buf.writeItemStack(this.icon);
        buf.writeEnumConstant(this.frame);
        int i = 0;
        if (this.background != null) {
            i |= 1;
        }

        if (this.hidden) {
            i |= 4;
        }

        buf.writeInt(i);
        if (this.background != null) {
            buf.writeIdentifier(this.background);
        }

        buf.writeFloat(this.x);
        buf.writeFloat(this.y);
    }

    public static SkillDisplay fromPacket(PacketByteBuf buf) {
        Text text = buf.readText();
        Text text2 = buf.readText();
        ItemStack itemStack = buf.readItemStack();
        AdvancementFrame skillFrame = buf.readEnumConstant(AdvancementFrame.class);
        int i = buf.readInt();
        Identifier identifier = (i & 1) != 0 ? buf.readIdentifier() : null;
        boolean bl = (i & 2) != 0;
        SkillDisplay skillDisplay = new SkillDisplay(itemStack, text, text2, identifier, skillFrame, bl);
        skillDisplay.setPos(buf.readFloat(), buf.readFloat());
        return skillDisplay;
    }
}
