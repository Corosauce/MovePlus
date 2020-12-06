package com.corosus.chestorganizer.sorting;

import com.mojang.serialization.DataResult;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;

import java.util.*;

public class InventoryStorage {

    private FlagType type = FlagType.UNMARKED;
    private final BlockPos pos;
    private final String dimName;
    //for items actually currently in the inventory
    private List<SlotMemory> memoryActivelyStored = new ArrayList<>();
    //for items we've ever had in the inventory, includes active and missing
    //TODO: implement
    private List<ItemStack> memoryPreviouslyStored = new ArrayList<>();

    public enum FlagType {
        UNMARKED,
        SORTED,
        OVERFLOW, //kinda still relevant, but we will treat vertical stacked chests as a shared collection to compare against
        DUMP;

        private static final Map<Integer, FlagType> lookup = new HashMap<Integer, FlagType>();
        static { for(FlagType e : EnumSet.allOf(FlagType.class)) { lookup.put(e.ordinal(), e); } }
        public static FlagType get(int intValue) { return lookup.get(intValue); }
    }

    public InventoryStorage(BlockPos pos, String dimType) {
        this.pos = pos;
        this.dimName = dimType;
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
        else if (type == FlagType.SORTED) type = FlagType.UNMARKED;/*;
        else if (type == FlagType.OVERFLOW) type = FlagType.UNMARKED;*/
    }

    public FlagType getType() {
        return type;
    }

    public void setType(FlagType type) {
        this.type = type;
    }

    public String getDimName() {
        return dimName;
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putInt("posX", pos.getX());
        nbt.putInt("posY", pos.getY());
        nbt.putInt("posZ", pos.getZ());

        nbt.putInt("type", type.ordinal());
        //nbt.putString("dimType", dimType.toString());

        nbt.putString("dimType", dimName);

        ListNBT list = new ListNBT();
        for (SlotMemory storage : memoryActivelyStored) {
            list.add(storage.writeToNBT());
        }

        nbt.put("activeMemory", list);

        return nbt;
    }

    public static InventoryStorage fromNBT(CompoundNBT nbt) {
        BlockPos pos = new BlockPos(nbt.getInt("posX"), nbt.getInt("posY"), nbt.getInt("posZ"));
        String dimName = nbt.getString("dimType");
        if (dimName.equals("")) dimName = "minecraft:overworld";
        InventoryStorage storage = new InventoryStorage(pos, dimName);


        storage.setType(FlagType.get(nbt.getInt("type")));

        ListNBT listnbt2 = nbt.getList("activeMemory", 10);
        for(int k = 0; k < listnbt2.size(); ++k) {
            storage.getMemoryActivelyStored().add(SlotMemory.fromNBT(listnbt2.getCompound(k)));
        }

        return storage;
    }

    public List<ItemStack> getMemoryActivelyStoredAsItemStacks() {
        List<ItemStack> list = new ArrayList<>();
        for (SlotMemory slot : memoryActivelyStored) {
            list.add(slot.getStack());
        }
        return list;
    }

    public void reset() {
        memoryActivelyStored.clear();
        memoryPreviouslyStored.clear();
    }
}
