package io.github.thatrobin.skillful.skill_trees;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value= EnvType.CLIENT)
public enum SkillObtainedStatus {
    OBTAINED(0),
    UNOBTAINED(1),
    LOCKED(2);

    private final int spriteIndex;

    SkillObtainedStatus(int spriteIndex) {
        this.spriteIndex = spriteIndex;
    }

    public int getSpriteIndex() {
        return this.spriteIndex;
    }
}
