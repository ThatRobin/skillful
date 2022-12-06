package io.github.thatrobin.skillful.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkillPointImpl implements SkillPointInterface {

    private final PlayerEntity player;
    private final ConcurrentHashMap<Identifier, Integer> skillPoints = new ConcurrentHashMap<>();

    public SkillPointImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public Integer getSkillPoints(Identifier skillTree) {
        if (this.skillPoints.containsKey(skillTree)) {
            return this.skillPoints.get(skillTree);
        }
        return 0;
    }

    @Override
    public void setSkillPoints(Identifier skillTree, Integer points) {
        //this.skillPoints = points;
        this.skillPoints.put(skillTree, points);
    }

    @Override
    public void addSkillPoints(Identifier skillTree, Integer points) {
        int oldValue = 0;
        if(this.skillPoints.containsKey(skillTree)) {
            oldValue = this.skillPoints.get(skillTree);
        }

        int newValue = oldValue + points;
        this.skillPoints.put(skillTree, newValue);
    }

    @Override
    public void removeSkillPoints(Identifier skillTree, Integer points) {
        int oldValue = 0;
        if(this.skillPoints.containsKey(skillTree)) {
            oldValue = this.skillPoints.get(skillTree);
        }
        int newValue = oldValue - points;
        if(newValue < 0) newValue = 0;
        this.skillPoints.put(skillTree, newValue);
    }

    @Override
    public void sync() {
        SkillPointInterface.sync(this.player);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.skillPoints.clear();
        NbtList skillPointsList = (NbtList) tag.get("skill_points");
        if(skillPointsList != null) {
            for (int i = 0; i < skillPointsList.size(); i++) {
                NbtCompound skillPointsTag = skillPointsList.getCompound(i);
                Identifier skillTreeID = Identifier.tryParse(skillPointsTag.getString("skill_tree"));
                if(skillTreeID != null) {
                    int points = skillPointsTag.getInt("points");
                    this.skillPoints.put(skillTreeID, points);
                }
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        NbtList powerList = new NbtList();
        for(Map.Entry<Identifier, Integer> skillPointEntry : this.skillPoints.entrySet()) {
            NbtCompound powerTag = new NbtCompound();
            powerTag.putString("skill_tree", skillPointEntry.getKey().toString());
            powerTag.putInt("points", skillPointEntry.getValue());
            powerList.add(powerTag);
        }
        tag.put("skill_points", powerList);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        NbtCompound compoundTag = buf.readNbt();
        if(compoundTag != null) {
            this.readFromNbt(compoundTag);
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        NbtCompound tag = new NbtCompound();
        this.writeToNbt(tag);
        buf.writeNbt(tag);
    }

}
