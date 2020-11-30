package com.corosus.moveplus.forge;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class InventoryStorage {

    public List<Slot> memory = new ArrayList<>();

    public List<Slot> getMemory() {
        return memory;
    }

    public void setMemory(List<Slot> memory) {
        this.memory = memory;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos pos;

}
