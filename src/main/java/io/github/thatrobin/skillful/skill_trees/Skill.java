package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.thatrobin.skillful.Skillful;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Skill {
    @Nullable
    private final Skill parent;
    @Nullable
    private final SkillDisplay display;
    private final Identifier id;
    @Nullable
    private final List<Identifier> powerIds;
    private final int cost;
    private final Set<Skill> children = Sets.newLinkedHashSet();
    private final Text text;

    public Skill(Identifier id, @Nullable Skill parent, @Nullable SkillDisplay display, @Nullable List<Identifier> powerIds, int cost) {
        this.id = id;
        this.cost = cost;
        this.powerIds = powerIds;
        this.display = display;
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }

        if (display == null) {
            this.text = Text.literal(id.toString());
        } else {
            Text text = display.getTitle();
            Formatting formatting = display.getFrame().getTitleFormat();
            Text text2 = Texts.setStyleIfAbsent(text.copy(), Style.EMPTY.withColor(formatting)).append("\n").append(display.getDescription());
            Text text3 = text.copy().styled((style) -> {
                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2));
            });
            this.text = Texts.bracketed(text3).formatted(formatting);
        }

    }

    @Nullable
    public Skill getParent() {
        return this.parent;
    }

    @Nullable
    public SkillDisplay getDisplay() {
        return this.display;
    }

    public String toString() {
        Identifier var10000 = this.getId();
        return "SimpleSkill{id=" + var10000 + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + "}";
    }

    public Iterable<Skill> getChildren() {
        return this.children;
    }

    public void addChild(Skill child) {
        this.children.add(child);
    }

    public Identifier getId() {
        return this.id;
    }

    public List<Identifier> getPowerIds() {
        return this.powerIds;
    }

    public int getCost() {
        return this.cost;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Skill)) {
            return false;
        } else {
            Skill Skill = (Skill)o;
            return this.id.equals(Skill.id);
        }
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public Text toHoverableText() {
        return this.text;
    }

    public static class Task {
        @Nullable
        private Identifier parentId;
        private List<Identifier> powerIds;
        @Nullable
        private Skill parentObj;
        @Nullable
        private SkillDisplay display;
        private int cost;

        Task(@Nullable Identifier parentId, @Nullable SkillDisplay display, @Nullable List<Identifier> powerIds, int cost) {
            this.parentId = parentId;
            this.display = display;
            this.powerIds = powerIds;
            this.cost = cost;
        }

        private Task() {
        }

        public static Skill.Task create() {
            return new Skill.Task();
        }

        public Skill.Task parent(Skill parent) {
            this.parentObj = parent;
            return this;
        }

        public Skill.Task parent(Identifier parentId) {
            this.parentId = parentId;
            return this;
        }

        public Skill.Task cost(int cost) {
            this.cost = cost;
            return this;
        }

        public Skill.Task display(ItemStack icon, Identifier identifier, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return this.display(new SkillDisplay(icon, identifier, title, description, background, frame, showToast, announceToChat, hidden));
        }

        public Skill.Task display(ItemConvertible icon, Identifier identifier, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return this.display(new SkillDisplay(new ItemStack(icon.asItem()), identifier, title, description, background, frame, showToast, announceToChat, hidden));
        }

        public Skill.Task display(SkillDisplay display) {
            this.display = display;
            return this;
        }

        public Skill.Task powers(List<Identifier> powerIds) {
            this.powerIds = powerIds;
            return this;
        }

        public List<Identifier> getPowers() {
            return this.powerIds;
        }

        public boolean findParent(Function<Identifier, Skill> parentProvider) {
            if (this.parentId == null) {
                return true;
            } else {
                if (this.parentObj == null) {
                    this.parentObj = (Skill)parentProvider.apply(this.parentId);
                }

                return this.parentObj != null;
            }
        }

        public Skill build(Identifier id) {
            if (!this.findParent((idx) -> {
                return null;
            })) {
                throw new IllegalStateException("Tried to build incomplete Skill!");
            } else {
                return new Skill(id, this.parentObj, this.display, this.powerIds, this.cost);
            }
        }

        public Skill build(Consumer<Skill> consumer, String id) {
            Skill Skill = this.build(new Identifier(id));
            consumer.accept(Skill);
            return Skill;
        }

        public void toPacket(PacketByteBuf buf) {
            if (this.powerIds != null) {
                buf.writeInt(this.cost);
            } else {
                buf.writeInt(0);
            }
            if (this.powerIds == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeInt(this.powerIds.size());
                Skillful.LOGGER.info(this.powerIds.size());
                for(Identifier id : this.powerIds) {
                    Skillful.LOGGER.info(id);
                    buf.writeIdentifier(id);
                }
            }

            if (this.parentId == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeIdentifier(this.parentId);
            }

            if (this.display == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                this.display.toPacket(buf);
            }
        }

        public String toString() {
            Identifier var10000 = this.parentId;
            return "Task Skill{parentId=" + var10000 + ", display=" + this.display + "}";
        }

        public static Skill.Task fromPacket(PacketByteBuf buf) {
            int cost = buf.readInt();
            List<Identifier> powers = null;
            if(buf.readBoolean()) {
                int loop = buf.readInt();
                powers = Lists.newArrayList();
                for (int i = 0; i < loop; i++) {
                    Identifier id = buf.readIdentifier();
                    powers.add(id);
                }
            }
            Identifier parentId = buf.readBoolean() ? buf.readIdentifier() : null;
            SkillDisplay skillDisplay = buf.readBoolean() ? SkillDisplay.fromPacket(buf) : null;

            return new Skill.Task(parentId, skillDisplay, powers, cost);
        }

        //public Map<String, SkillCriterion> getCriteria() {
        //    return this.criteria;
        //}
    }
}
