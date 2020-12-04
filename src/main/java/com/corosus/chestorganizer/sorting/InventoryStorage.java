package com.corosus.chestorganizer.sorting;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;

import java.util.ArrayList;
import java.util.List;

public class InventoryStorage {

    private FlagType type = FlagType.UNMARKED;
    private final BlockPos pos;
    private final DimensionType dimType;
    //for items actually currently in the inventory
    private List<SlotMemory> memoryActivelyStored = new ArrayList<>();
    //for items we've ever had in the inventory, includes active and missing
    //TODO: implement
    private List<ItemStack> memoryPreviouslyStored = new ArrayList<>();

    public enum FlagType {
        UNMARKED,
        SORTED,
        OVERFLOW,
        DUMP;
    }

    public InventoryStorage(BlockPos pos, DimensionType dimType) {
        this.pos = pos;
        this.dimType = dimType;
    }

    public void addAllSlots(List<Slot> slots) {
        for (Slot slot : slots) {
            memoryActivelyStored.add(SlotMemory.fromSlot(slot));
        }
    }

    public List<SlotMemory> getMemoryActivelyStored() {
        return memoryActivelyStored;
    }

    public void setMemoryActivelyStored(List<SlotMemory> memoryActivelyStored) {
        this.memoryActivelyStored = memoryActivelyStored;
    }

    public List<ItemStack> getMemoryPreviouslyStored() {
        return memoryPreviouslyStored;
    }

    public void setMemoryPreviouslyStored(List<ItemStack> memoryPreviouslyStored) {
        this.memoryPreviouslyStored = memoryPreviouslyStored;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void resetActiveMemory() {
        memoryActivelyStored.clear();
    }

    public void cycleType() {
        if (type == FlagType.UNMARKED) type = FlagType.DUMP;
        else if (type == FlagType.DUMP) type = FlagType.SORTED;
        else if (type == FlagType.SORTED) type = FlagType.OVERFLOW;
        else if (type == FlagType.OVERFLOW) type = FlagType.UNMARKED;
    }

    public FlagType getType() {
        return type;
    }

    public void setType(FlagType type) {
        this.type = type;
    }

    public DimensionType getDimType() {
        return dimType;
    }
}
