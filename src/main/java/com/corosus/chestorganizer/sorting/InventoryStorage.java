package com.corosus.chestorganizer.sorting;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class InventoryStorage {

    private FlagType type = FlagType.UNMARKED;
    private BlockPos pos;
    //for items actually currently in the inventory
    private List<Slot> memoryActivelyStored = new ArrayList<>();
    //for items we've ever had in the inventory
    private List<ItemStack> memoryPreviouslyStored = new ArrayList<>();

    public enum FlagType {
        UNMARKED,
        SORTED,
        OVERFLOW,
        DUMP;
    }

    public InventoryStorage(BlockPos pos) {
        this.pos = pos;
    }

    public List<Slot> getMemoryActivelyStored() {
        return memoryActivelyStored;
    }

    public void setMemoryActivelyStored(List<Slot> memoryActivelyStored) {
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

    public void setPos(BlockPos pos) {
        this.pos = pos;
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
}
