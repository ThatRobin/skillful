package io.github.thatrobin.skillful.skill_trees;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.PowerTypes;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class PowerSkillManager {

    public PowerSkillManager() {
        PowerTypes.registerAdditionalData("skill", (powerTypeId, factoryId, isSubPower, data, powerType) -> {
            JsonObject jo = data.getAsJsonObject();
            Identifier itemId = Identifier.tryParse(jo.get("icon").getAsString());
            Identifier parent = null;
            if(jo.has("parent")) {
                parent = Identifier.tryParse(jo.get("parent").getAsString());
            }
            Identifier background = new Identifier("textures/block/stone.png");
            if(jo.has("background")) {
                background = Identifier.tryParse(jo.get("background").getAsString());
            }
            int cost = 0;
            if(jo.has("cost")) {
                cost = jo.get("cost").getAsInt();
            }
            ItemStack stack = Registry.ITEM.get(itemId).getDefaultStack();
            SkillDisplay display = new SkillDisplay(stack, powerTypeId, Text.translatable(powerType.getOrCreateNameTranslationKey()), Text.translatable(powerType.getOrCreateDescriptionTranslationKey()), background, AdvancementFrame.TASK, false, false, false);
            Skill.Task task = Skill.Task.create().display(display).cost(cost);
            if(parent != null) {
                task.parent(parent);
            } if (powerTypeId != null) {
                List<Identifier> powers = List.of(powerTypeId);
                task.powers(powers);
            }
            if(SkillTreeRegistry.contains(powerTypeId)) {
                SkillTreeRegistry.update(powerTypeId, task);
            } else {
                SkillTreeRegistry.register(powerTypeId, task);
            }
        });
    }
}
