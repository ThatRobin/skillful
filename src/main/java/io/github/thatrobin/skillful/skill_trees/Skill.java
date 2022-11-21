package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.Sets;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionTypes;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;
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
    private final List<PowerType<?>> powerTypes;
    private final ConditionFactory<Entity>.Instance condition;
    private final int cost;
    private final Set<Skill> children = Sets.newLinkedHashSet();
    private final Text text;

    public Skill(Identifier id, @Nullable Skill parent, @Nullable SkillDisplay display, @Nullable List<PowerType<?>> powerTypes, int cost, ConditionFactory<Entity>.Instance condition) {
        this.id = id;
        this.cost = cost;
        this.powerTypes = powerTypes;
        this.display = display;
        this.parent = parent;
        this.condition = condition;
        if (parent != null) {
            parent.addChild(this);
        }

        if (display == null) {
            this.text = Text.literal(id.toString());
        } else {
            Text text = display.getTitle();
            Formatting formatting = display.getFrame().getTitleFormat();
            Text text2 = Texts.setStyleIfAbsent(text.copy(), Style.EMPTY.withColor(formatting)).append("\n").append(display.getDescription());
            Text text3 = text.copy().styled((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2)));
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

    public ConditionFactory<Entity>.Instance getCondition() {
        return this.condition;
    }

    public List<PowerType<?>> getPowers() {
        return this.powerTypes;
    }

    public int getCost() {
        return this.cost;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Skill Skill)) {
            return false;
        } else {
            return this.id.equals(Skill.id);
        }
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    @SuppressWarnings("unused")
    public Text toHoverableText() {
        return this.text;
    }

    public static class Task {
        @Nullable
        private Identifier parentId;
        private List<PowerType<?>> powerTypes;
        private List<Identifier> defaultPowerTypes;
        @Nullable
        private Skill parentObj;
        @Nullable
        private SkillDisplay display;
        private ConditionFactory<Entity>.Instance condition;
        private int cost;

        Task(@Nullable Identifier parentId, @Nullable SkillDisplay display, @Nullable List<PowerType<?>> powerTypes, @Nullable List<Identifier> defaultPowerTypes, int cost, ConditionFactory<Entity>.Instance condition) {
            this.parentId = parentId;
            this.display = display;
            this.powerTypes = powerTypes;
            this.defaultPowerTypes = defaultPowerTypes;
            this.cost = cost;
            this.condition = condition;
        }

        private Task() {
        }

        public static Skill.Task create() {
            return new Skill.Task();
        }

        @SuppressWarnings("unused")
        public Skill.Task parent(Skill parent) {
            this.parentObj = parent;
            return this;
        }

        public void parent(Identifier parentId) {
            this.parentId = parentId;
        }

        public Skill.Task cost(int cost) {
            this.cost = cost;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Skill.Task condition(ConditionFactory<Entity>.Instance condition) {
            this.condition = condition;
            return this;
        }

        @SuppressWarnings("unused")
        public Skill.Task display(ItemStack icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast) {
            return this.display(new SkillDisplay(icon, title, description, background, frame, showToast));
        }

        @SuppressWarnings("unused")
        public Skill.Task display(ItemConvertible icon, Text title, Text description, @Nullable Identifier background, AdvancementFrame frame, boolean showToast) {
            return this.display(new SkillDisplay(new ItemStack(icon.asItem()), title, description, background, frame, showToast));
        }

        public Skill.Task display(SkillDisplay display) {
            this.display = display;
            return this;
        }

        public void powers(List<PowerType<?>> powerTypes) {
            this.powerTypes = powerTypes;
        }

        public List<PowerType<?>> getPowers() {
            return this.powerTypes;
        }

        public List<Identifier> getDefaultPowers() {
            return this.defaultPowerTypes;
        }

        public @Nullable SkillDisplay getDisplay() {
            return this.display;
        }

        public Identifier getParent() {
            return this.parentId;
        }

        public ConditionFactory<Entity>.Instance getCondition() {
            return this.condition;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean findParent(Function<Identifier, Skill> parentProvider) {
            if (this.parentId == null) {
                return true;
            } else {
                if (this.parentObj == null) {
                    this.parentObj = parentProvider.apply(this.parentId);
                }

                return this.parentObj != null;
            }
        }

        public Skill build(Identifier id) {
            if (!this.findParent((idx) -> null)) {
                throw new IllegalStateException("Tried to build incomplete Skill!");
            } else {
                return new Skill(id, this.parentObj, this.display, this.powerTypes, this.cost, this.condition);
            }
        }

        @SuppressWarnings("unused")
        public Skill build(Consumer<Skill> consumer, String id) {
            Skill Skill = this.build(new Identifier(id));
            consumer.accept(Skill);
            return Skill;
        }

        public void toPacket(PacketByteBuf buf) {
            if (this.parentId != null) {
                buf.writeInt(this.cost);
            } else {
                buf.writeInt(0);
            }
            if (this.powerTypes == null || this.powerTypes.isEmpty()) {
                buf.writeString("none");
            } else {
                buf.writeString("powers");
                buf.writeInt(this.powerTypes.size());
                for(PowerType<?> powerType : this.powerTypes) {
                    buf.writeIdentifier(powerType.getIdentifier());
                }
            }
            if (this.defaultPowerTypes == null || this.defaultPowerTypes.isEmpty()) {
                buf.writeString("none");
            } else {
                buf.writeString("default_powers");
                buf.writeInt(this.defaultPowerTypes.size());
                for(Identifier powerType : this.defaultPowerTypes) {
                    buf.writeIdentifier(powerType);
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
            if(this.condition == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                condition.write(buf);
            }
        }

        public String toString() {
            Identifier var10000 = this.parentId;
            return "Task Skill{parentId=" + var10000 + ", display=" + this.display + "}";
        }

        public static Skill.Task fromPacket(PacketByteBuf buf) {
            int cost = buf.readInt();
            List<PowerType<?>> powers = null;
            List<Identifier> defaultPowers = null;
            if(!buf.readString().equals("none")) {
                int loop = buf.readInt();
                powers = Lists.newArrayList();
                for (int i = 0; i < loop; i++) {
                    Identifier id = buf.readIdentifier();
                    powers.add(PowerTypeRegistry.get(id));
                }
            }
            if(!buf.readString().equals("none")) {
                int loop = buf.readInt();
                defaultPowers = Lists.newArrayList();
                for (int i = 0; i < loop; i++) {
                    Identifier id = buf.readIdentifier();
                    defaultPowers.add(id);
                }
            }
            Identifier parentId = buf.readBoolean() ? buf.readIdentifier() : null;
            SkillDisplay skillDisplay = buf.readBoolean() ? SkillDisplay.fromPacket(buf) : null;
            ConditionFactory<Entity>.Instance condition = buf.readBoolean() ? ConditionTypes.ENTITY.read(buf) : null;
            return new Skill.Task(parentId, skillDisplay, powers, defaultPowers, cost, condition);
        }

        public int getCost() {
            return this.cost;
        }

    }
}
