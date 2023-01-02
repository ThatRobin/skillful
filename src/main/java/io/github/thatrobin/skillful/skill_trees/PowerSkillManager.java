package io.github.thatrobin.skillful.skill_trees;

import io.github.apace100.apoli.power.PowerTypes;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.data.SkillfulDataTypes;

import java.util.List;

public class PowerSkillManager {

    public static void initializeSkillData() {
        PowerTypes.registerAdditionalData("skill", (powerTypeId, factoryId, isSubPower, data, powerType) -> {
            Skill.Task task = SkillfulDataTypes.POWER_SKILL.read(data);
            SkillDisplay display = task.getDisplay();
            task.powers(List.of(powerType.getIdentifier()));
            if(display != null) {
                display.setName(powerType.getName());
                display.setDescription(powerType.getDescription());
                task = task.display(display);
            }

            if(SkillTreeRegistry.contains(powerTypeId)) {
                Skillful.LOGGER.info("updating power " + powerTypeId);
                SkillTreeRegistry.update(powerTypeId, task);
            } else {
                SkillTreeRegistry.register(powerTypeId, task);
            }
        });
    }
}
