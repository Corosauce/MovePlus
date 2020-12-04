package com.corosus.chestorganizer.sorting;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotMemory {

    private final int slotNumber;
    private ItemStack stack;

    public static SlotMemory fromSlot(Slot slot) {
        return new SlotMemory(slot.slotNumber, slot.getStack());
    }

    public SlotMemory(int slotNumber, ItemStack stack) {
        this.slotNumber = slotNumber;
        this.stack = stack;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isEmpty() {
        return stack == ItemStack.EMPTY;
    }
}
