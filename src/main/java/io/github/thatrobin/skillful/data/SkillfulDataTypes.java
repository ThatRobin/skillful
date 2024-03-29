package io.github.thatrobin.skillful.data;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.thatrobin.skillful.skill_trees.Skill;
import io.github.thatrobin.skillful.skill_trees.SkillDisplay;
import io.github.thatrobin.skillful.skill_trees.SkillPowerRegistry;
import io.github.thatrobin.skillful.utils.KeybindingData;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public class SkillfulDataTypes {

    public static final SerializableDataType<Skill.Task> SKILL = SerializableDataType.compound(Skill.Task.class,
            new SerializableData()
                    .add("name", SerializableDataTypes.TEXT)
                    .add("description", SerializableDataTypes.TEXT)
                    .add("icon", SerializableDataTypes.ITEM_STACK, Items.GRASS_BLOCK.getDefaultStack())
                    .add("power", ApoliDataTypes.POWER_TYPE, null)
                    .add("powers", SerializableDataTypes.IDENTIFIERS, null)
                    .add("parent", SerializableDataTypes.IDENTIFIER, null)
                    .add("default_powers", SerializableDataTypes.IDENTIFIERS, null)
                    .add("background", SerializableDataTypes.IDENTIFIER, new Identifier("textures/block/stone.png"))
                    .add("condition", ApoliDataTypes.ENTITY_CONDITION, null)
                    .add("cost", SerializableDataTypes.INT, 0),
            (data) ->  {
                Text name = data.get("name");
                Text description = data.get("description");
                List<Identifier> powerTypes = new LinkedList<>();
                Identifier parent = null;
                ItemStack icon = data.get("icon");
                List<Identifier> defaultPowers = new LinkedList<>();
                if(data.isPresent("power")) {
                    powerTypes.add(data.get("power"));
                }
                if(data.isPresent("powers")) {
                    List<Identifier> powerTypesList = data.get("powers");
                    powerTypes.addAll(powerTypesList);
                }
                if(data.isPresent("parent")) {
                    parent = data.get("parent");
                }
                if(data.isPresent("default_powers")) {
                    defaultPowers = data.get("default_powers");
                }
                for (Identifier defaultPower : defaultPowers) {
                    if(SkillPowerRegistry.contains(defaultPower)) {
                        SkillPowerRegistry.update(defaultPower, PowerTypeRegistry.get(defaultPower));
                    } else {
                        SkillPowerRegistry.register(defaultPower, PowerTypeRegistry.get(defaultPower));
                    }
                    powerTypes.add(defaultPower);
                }
                Identifier background = data.get("background");
                int cost = data.getInt("cost");

                SkillDisplay display = new SkillDisplay(icon, name, description, background, AdvancementFrame.TASK, false);
                Skill.Task task = Skill.Task.create().display(display).cost(cost);
                if(parent != null) {
                    task.parent(parent);
                } if (!powerTypes.isEmpty()) {
                    task.powers(powerTypes);
                }
                task.condition(data.get("condition"));
                return task;
            },
            ((serializableData, skillTask) -> {
                SerializableData.Instance data = serializableData.new Instance();
                if(skillTask.getDisplay() != null) {
                    data.set("name", skillTask.getDisplay().getTitle());
                    data.set("description", skillTask.getDisplay().getDescription());
                    data.set("icon", skillTask.getDisplay().getIcon());
                    data.set("background", skillTask.getDisplay().getBackground());
                }
                data.set("condition", skillTask.getCondition());
                data.set("power", skillTask.getPowers().get(0));
                data.set("powers", skillTask.getPowers());
                data.set("parent", skillTask.getParent());
                data.set("default_powers", skillTask.getDefaultPowers());
                data.set("cost", skillTask.getCost());
                return data;
            }));

    public static final SerializableDataType<Skill.Task> POWER_SKILL = SerializableDataType.compound(Skill.Task.class,
            new SerializableData()
                    .add("icon", SerializableDataTypes.ITEM_STACK, Items.GRASS_BLOCK.getDefaultStack())
                    .add("parent", SerializableDataTypes.IDENTIFIER)
                    .add("background", SerializableDataTypes.IDENTIFIER, new Identifier("textures/block/stone.png"))
                    .add("condition", ApoliDataTypes.ENTITY_CONDITION, null)
                    .add("cost", SerializableDataTypes.INT, 0),
            (data) ->  {
                ItemStack icon = data.get("icon");
                Identifier parent = data.get("parent");
                Identifier background = data.get("background");
                int cost = data.getInt("cost");
                SkillDisplay display = new SkillDisplay(icon, Text.literal(""), Text.literal(""), background, AdvancementFrame.TASK, false);
                Skill.Task task = Skill.Task.create().display(display).cost(cost);
                if(parent != null) {
                    task.parent(parent);
                }
                task.condition(data.get("condition"));
                return task;
            },
            ((serializableData, skillTask) -> {
                SerializableData.Instance data = serializableData.new Instance();
                if(skillTask.getDisplay() != null) {
                    data.set("icon", skillTask.getDisplay().getIcon());
                    data.set("background", skillTask.getDisplay().getBackground());
                }
                data.set("condition", skillTask.getCondition());
                data.set("parent", skillTask.getParent());
                data.set("cost", skillTask.getCost());
                return data;
            }));

    public static final SerializableDataType<KeybindingData> KEYBINDING = SerializableDataType.compound(KeybindingData.class,
            new SerializableData()
                    .add("key", SerializableDataTypes.STRING)
                    .add("category", SerializableDataTypes.STRING),
            (data) -> new KeybindingData(data.get("key"), data.get("key"), data.get("category")),
            ((serializableData, keyBinding) -> {
                SerializableData.Instance data = serializableData.new Instance();
                data.set("key", keyBinding.keyKey());
                data.set("category", keyBinding.category());
                return data;
            }));

}
