package io.github.thatrobin.skillful.factories;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.components.SkillPointInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.registry.Registry;

public class EntityActions {

    public static void register() {
        register(new ActionFactory<>(Skillful.identifier("add_skill_points"), new SerializableData()
                .add("skill_tree", SerializableDataTypes.IDENTIFIER)
                .add("points", SerializableDataTypes.INT),
                (data, entity) -> {
                    if (entity instanceof PlayerEntity player) {
                        if(!entity.world.isClient) {
                            SkillPointInterface skillPointInterface = SkillPointInterface.INSTANCE.get(player);
                            skillPointInterface.addSkillPoints(data.getId("skill_tree"), data.getInt("points"));
                            skillPointInterface.sync();
                        }
                    }
                }
        ));
    }

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
