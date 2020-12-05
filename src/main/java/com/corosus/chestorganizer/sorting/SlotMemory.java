package com.corosus.chestorganizer.sorting;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class SlotMemory {

    private final int slotNumber;
    private ItemStack stack;

    public static SlotMemory fromSlot(Slot slot) {
        return new SlotMemory(slot.slotNumber, slot.getStack());
    }

    public static SlotMemory fromNBT(CompoundNBT nbt) {
        int slotNumber = nbt.getInt("slotNumber");
        ItemStack stack = nbt.contains("stack") ? ItemStack.read(nbt.getCompound("stack")) : ItemStack.EMPTY;
        return new SlotMemory(slotNumber, stack);
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

    public CompoundNBT writeToNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putInt("slotNumber", slotNumber);
        if (!stack.isEmpty()) {
            nbt.put("stack", stack.write(new CompoundNBT()));
        }

        return nbt;
    }
}
