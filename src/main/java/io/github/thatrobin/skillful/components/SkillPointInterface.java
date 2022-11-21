package io.github.thatrobin.skillful.components;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.thatrobin.skillful.Skillful;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public interface SkillPointInterface extends AutoSyncedComponent {

    ComponentKey<SkillPointInterface> INSTANCE = ComponentRegistry.getOrCreate(Skillful.identifier("skill_points"), SkillPointInterface.class);

    Integer getSkillPoints(Identifier skillTree);
    @SuppressWarnings("unused")
    void setSkillPoints(Identifier skillTree, Integer points);
    void addSkillPoints(Identifier skillTree, Integer points);
    void removeSkillPoints(Identifier skillTree, Integer points);

    void sync();

    static void sync(PlayerEntity player) {
        INSTANCE.sync(player);
    }
}
