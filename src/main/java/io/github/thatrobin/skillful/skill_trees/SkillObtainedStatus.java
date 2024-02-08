package io.github.thatrobin.skillful.skill_trees;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.util.Identifier;

@Environment(value= EnvType.CLIENT)
public enum SkillObtainedStatus {
    OBTAINED(new Identifier("advancements/box_obtained"), new Identifier("advancements/task_frame_obtained"), new Identifier("advancements/challenge_frame_obtained"), new Identifier("advancements/goal_frame_obtained")),
    UNOBTAINED(new Identifier("advancements/box_unobtained"), new Identifier("advancements/task_frame_unobtained"), new Identifier("advancements/challenge_frame_unobtained"), new Identifier("advancements/goal_frame_unobtained")),
    LOCKED(new Identifier("advancements/box_unobtained"), new Identifier("advancements/task_frame_unobtained"), new Identifier("advancements/challenge_frame_unobtained"), new Identifier("advancements/goal_frame_unobtained"));

    private final Identifier boxTexture;
    private final Identifier taskFrameTexture;
    private final Identifier challengeFrameTexture;
    private final Identifier goalFrameTexture;

    SkillObtainedStatus(Identifier boxTexture, Identifier taskFrameTexture, Identifier challengeFrameTexture, Identifier goalFrameTexture) {
        this.boxTexture = boxTexture;
        this.taskFrameTexture = taskFrameTexture;
        this.challengeFrameTexture = challengeFrameTexture;
        this.goalFrameTexture = goalFrameTexture;
    }

    public Identifier getBoxTexture() {
        return this.boxTexture;
    }

    public Identifier getFrameTexture(AdvancementFrame frame) {
        Identifier var10000;
        switch (frame) {
            case TASK:
                var10000 = this.taskFrameTexture;
                break;
            case CHALLENGE:
                var10000 = this.challengeFrameTexture;
                break;
            case GOAL:
                var10000 = this.goalFrameTexture;
                break;
            default:
                throw new IncompatibleClassChangeError();
        }

        return var10000;
    }
}
