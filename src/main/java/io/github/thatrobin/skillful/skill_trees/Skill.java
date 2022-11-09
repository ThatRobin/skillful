package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Skill {
    @Nullable
    private final Skill parent;
    @Nullable
    private final SkillDisplay display;
    private final Identifier id;
    @Nullable
    private final Identifier powerId;
    private final int cost;
    private final Set<Skill> children = Sets.newLinkedHashSet();
    private final Text text;

    public Skill(Identifier id, @Nullable Skill parent, @Nullable SkillDisplay display, @Nullable Identifier powerId, int cost) {
        this.id = id;
        this.cost = cost;
        this.powerId = powerId;
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

    public Identifier getPowerId() {
        return this.powerId;
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
        private Identifier powerId;
        @Nullable
        private Skill parentObj;
        @Nullable
        private SkillDisplay display;
        private int cost;

        Task(@Nullable Identifier parentId, @Nullable SkillDisplay display, @Nullable Identifier powerId, int cost) {
            this.parentId = parentId;
            this.display = display;
            this.powerId = powerId;
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

        public Skill.Task power(Identifier powerId) {
            this.powerId = powerId;
            return this;
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
                return new Skill(id, this.parentObj, this.display, this.powerId, this.cost);
            }
        }

        public Skill build(Consumer<Skill> consumer, String id) {
            Skill Skill = this.build(new Identifier(id));
            consumer.accept(Skill);
            return Skill;
        }

        public void toPacket(PacketByteBuf buf) {
            if (this.powerId != null) {
                buf.writeInt(this.cost);
            } else {
                buf.writeInt(0);
            }
            if (this.powerId == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeIdentifier(this.powerId);
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

        /*
        public static Skill.Task fromJson(JsonObject obj) {
            Identifier identifier = obj.has("parent") ? new Identifier(JsonHelper.getString(obj, "parent")) : null;
            SkillDisplay SkillDisplay = obj.has("display") ? SkillDisplay.fromJson(JsonHelper.getObject(obj, "display")) : null;
            SkillRewards SkillRewards = obj.has("rewards") ? SkillRewards.fromJson(JsonHelper.getObject(obj, "rewards")) : SkillRewards.NONE;
            Map<String, SkillCriterion> map = SkillCriterion.criteriaFromJson(JsonHelper.getObject(obj, "criteria"), predicateDeserializer);
            if (map.isEmpty()) {
                throw new JsonSyntaxException("Skill criteria cannot be empty");
            } else {
                JsonArray jsonArray = JsonHelper.getArray(obj, "requirements", new JsonArray());
                String[][] strings = new String[jsonArray.size()][];

                int i;
                int j;
                for(i = 0; i < jsonArray.size(); ++i) {
                    JsonArray jsonArray2 = JsonHelper.asArray(jsonArray.get(i), "requirements[" + i + "]");
                    strings[i] = new String[jsonArray2.size()];

                    for(j = 0; j < jsonArray2.size(); ++j) {
                        strings[i][j] = JsonHelper.asString(jsonArray2.get(j), "requirements[" + i + "][" + j + "]");
                    }
                }

                if (strings.length == 0) {
                    strings = new String[map.size()][];
                    i = 0;

                    String string;
                    for(Iterator var16 = map.keySet().iterator(); var16.hasNext(); strings[i++] = new String[]{string}) {
                        string = (String)var16.next();
                    }
                }

                String[][] var17 = strings;
                int var18 = strings.length;

                int var13;
                for(j = 0; j < var18; ++j) {
                    String[] strings2 = var17[j];
                    if (strings2.length == 0 && map.isEmpty()) {
                        throw new JsonSyntaxException("Requirement entry cannot be empty");
                    }

                    String[] var12 = strings2;
                    var13 = strings2.length;

                    for(int var14 = 0; var14 < var13; ++var14) {
                        String string2 = var12[var14];
                        if (!map.containsKey(string2)) {
                            throw new JsonSyntaxException("Unknown required criterion '" + string2 + "'");
                        }
                    }
                }

                Iterator var19 = map.keySet().iterator();

                String string3;
                boolean bl;
                do {
                    if (!var19.hasNext()) {
                        return new Skill.Task(identifier, SkillDisplay, SkillRewards, map, strings);
                    }

                    string3 = (String)var19.next();
                    bl = false;
                    String[][] var22 = strings;
                    int var24 = strings.length;

                    for(var13 = 0; var13 < var24; ++var13) {
                        String[] strings3 = var22[var13];
                        if (ArrayUtils.contains(strings3, string3)) {
                            bl = true;
                            break;
                        }
                    }
                } while(bl);

                throw new JsonSyntaxException("Criterion '" + string3 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
            }
        }
        */
        public static Skill.Task fromPacket(PacketByteBuf buf) {
            int cost = buf.readInt();
            Identifier powerId = buf.readBoolean() ? buf.readIdentifier() : null;
            Identifier parentId = buf.readBoolean() ? buf.readIdentifier() : null;
            SkillDisplay skillDisplay = buf.readBoolean() ? SkillDisplay.fromPacket(buf) : null;

            return new Skill.Task(parentId, skillDisplay, powerId, cost);
        }

        //public Map<String, SkillCriterion> getCriteria() {
        //    return this.criteria;
        //}
    }
}
