package com.corosus.chestorganizer.sorting;

import com.corosus.moveplus.util.UtilContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.*;

public class ChestSorter {

    private HashMap<BlockPos, InventoryStorage> storageMemory = new HashMap<>();

    //for preventing sorting from messing with items we had in our inventory before sorting
    private HashMap<Integer, ItemStack> lookupPlayerPreSortSnapshotSlotMemoryToItem = new HashMap<>();

    private BlockPos lastLookedAtBlock = BlockPos.ZERO;
    private Screen lastScreenChecked = null;

    private boolean isSorting = false;
    private boolean wasSettingType = false;
    private boolean wasTogglingSort = false;

    public void ChestSorter() {

    }

    public void tickClient() {

        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null || mc.player == null) return;

        BlockPos activeLookedAtBlock = BlockPos.ZERO;

        if (mc.objectMouseOver != null && mc.objectMouseOver instanceof BlockRayTraceResult) {
            lastLookedAtBlock = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
            activeLookedAtBlock = lastLookedAtBlock;
        } else {
            activeLookedAtBlock = BlockPos.ZERO;
        }

        //adding new storages to memory as they are opened
        if (Minecraft.getInstance().currentScreen instanceof ChestScreen) {
            ChestScreen screen = (ChestScreen) Minecraft.getInstance().currentScreen;

            if (lastScreenChecked != screen) {
                lastScreenChecked = screen;

                if (lastLookedAtBlock != BlockPos.ZERO) {

                    InventoryStorage storage = getOrAddStorageIfPresent(mc.world, lastLookedAtBlock);
                    storage.resetActiveMemory();
                    storage.addAllSlots(screen.getContainer().inventorySlots);
                    log("added or updated " + storage.getMemoryActivelyStored().size());
                    for (SlotMemory slot : storage.getMemoryActivelyStored()) {
                        //TODO: this was incorrect, fix
                        if (!slot.isEmpty()) {
                            log("entry: " + slot.getSlotNumber() + " - " + slot.getStack());
                        }
                    }
                } else {
                    log("pos was zero");
                }
            }

            //test inv transfer
            if (lastLookedAtBlock != BlockPos.ZERO) {

                InventoryStorage storage = getOrAddStorageIfPresent(mc.world, lastLookedAtBlock);

                int blockStorageSize = screen.getContainer().getLowerChestInventory().getSizeInventory();
                //log("blockStorageSize: " + blockStorageSize);

                //TODO: detect fullness and stop running this forever

                if (isSorting) {

                    //take the snapshot of player inventory if havent yet
                    if (lookupPlayerPreSortSnapshotSlotMemoryToItem.isEmpty()) {
                        log("making snapshot of player inventory");
                        for (Slot slot : screen.getContainer().inventorySlots) {
                            if (/*slot.getStack() != ItemStack.EMPTY && */slot.slotNumber >= blockStorageSize) {
                                lookupPlayerPreSortSnapshotSlotMemoryToItem.put(slot.slotNumber, slot.getStack());
                            }
                        }
                    }

                    if (storage.getType() == InventoryStorage.FlagType.DUMP) {

                        for (Slot slot : screen.getContainer().inventorySlots) {
                            if (slot.inventory.getStackInSlot(slot.slotNumber) != ItemStack.EMPTY && slot.slotNumber < blockStorageSize) {
                                if (hasAPlaceToSortTo(slot.inventory.getStackInSlot(slot.slotNumber))) {
                                    UtilContainer.clickSlot(mc.player, slot.slotNumber, UtilContainer.mouseLeftClick, ClickType.QUICK_MOVE);
                                }
                            }
                        }

                    } else if (storage.getType() == InventoryStorage.FlagType.SORTED) {

                        //log("try sort, slots: " + screen.getContainer().inventorySlotMemorys.size());
                        for (Slot slot : screen.getContainer().inventorySlots) {
                            //log("lookup: " + slot.slotNumber + " - " + slot.getStack());
                            if (slot.getStack() != ItemStack.EMPTY && slot.slotNumber >= blockStorageSize && lookupPlayerPreSortSnapshotSlotMemoryToItem.get(slot.slotNumber) == ItemStack.EMPTY) {
                                //log("lookup2: " + slot.getStack());
                                //look for a match
                                //TODO: add the extra memory check here too
                                int blockStorageFoundID = UtilContainer.getFirstSlotContainingItem(screen.getContainer(), slot.getStack(), 0, blockStorageSize);
                                if (blockStorageFoundID != -1) {
                                    UtilContainer.clickSlot(mc.player, slot.slotNumber, UtilContainer.mouseLeftClick, ClickType.QUICK_MOVE);
                                }
                            }
                        }

                    }
                }
            }
        }

        //TODO: validate positions incase chests move
        //storageMemory.
        Iterator<Map.Entry<BlockPos, InventoryStorage>> it = storageMemory.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, InventoryStorage> entry = it.next();
            BlockPos pos = entry.getKey();
            InventoryStorage storage = entry.getValue();
            if (mc.world.getGameTime() % 10 == 0) {
                IParticleData data = null;
                if (storage.getType() == InventoryStorage.FlagType.UNMARKED) {
                    //data = ParticleTypes.SMOKE;
                } else if (storage.getType() == InventoryStorage.FlagType.DUMP) {
                    data = ParticleTypes.SMOKE;
                } else if (storage.getType() == InventoryStorage.FlagType.SORTED) {
                    data = ParticleTypes.FLAME;
                } else if (storage.getType() == InventoryStorage.FlagType.OVERFLOW) {
                    data = ParticleTypes.CRIT;
                }
                if (data != null) {
                    mc.world.addParticle(data, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        if (Minecraft.getInstance().gameSettings.keyBindSneak.isKeyDown() &&
                Minecraft.getInstance().gameSettings.keyBindSprint.isKeyDown() &&
                (Minecraft.getInstance().gameSettings.keyBindAttack.isKeyDown() || mc.player.isCreative()) &&
                activeLookedAtBlock != BlockPos.ZERO) {
            if (!wasSettingType) {
                InventoryStorage storage = getOrAddStorageIfPresent(mc.world, activeLookedAtBlock);
                if (storage != null) {
                    storage.cycleType();
                }
                wasSettingType = true;
            }
        } else {
            wasSettingType = false;
        }

        if (Minecraft.getInstance().gameSettings.keyBindSneak.isKeyDown() &&
                Minecraft.getInstance().gameSettings.keyBindSprint.isKeyDown() &&
                Minecraft.getInstance().gameSettings.keyBindUseItem.isKeyDown()) {
            if (!wasTogglingSort) {
                enableSorting();
                wasTogglingSort = true;
            }
        } else {
            wasTogglingSort = false;
        }

        if (isSorting) {
            tickSorting();
        }

        if (mc.world.getGameTime() % 40 == 0) {
            tickValidatePositions();
        }

    }

    public InventoryStorage getOrAddStorageIfPresent(World world, BlockPos pos) {
        if (storageMemory.containsKey(pos)) return storageMemory.get(pos);
        if (world.getTileEntity(pos) instanceof ChestTileEntity) {
            InventoryStorage storage = new InventoryStorage(pos, world.getDimensionType());
            storageMemory.put(pos, storage);
            return storage;
        }
        return null;
    }

    public void enableSorting() {
        //TODO: take snapshot of inventory before using it so you dont sort their whole inventory
        isSorting = !isSorting;
        log("sorting " + (isSorting ? "on" : "off"));
        if (isSorting) {
            lookupPlayerPreSortSnapshotSlotMemoryToItem.clear();
        }
    }

    public void tickSorting() {

    }

    public void tickValidatePositions() {
        Minecraft mc = Minecraft.getInstance();
        /**
         * remove any missing chests within 20 blocks and within same dimension
         */
        Iterator<Map.Entry<BlockPos, InventoryStorage>> it = storageMemory.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, InventoryStorage> entry = it.next();
            BlockPos pos = entry.getKey();
            InventoryStorage storage = entry.getValue();

            if (storage.getDimType() == mc.world.getDimensionType() &&
                    !(mc.world.getTileEntity(pos) instanceof ChestTileEntity) &&
            pos.withinDistance(mc.player.getPosition(), 20)) {
                it.remove();
            }
        }
    }

    public boolean hasAPlaceToSortTo(ItemStack stack) {
        Iterator<Map.Entry<BlockPos, InventoryStorage>> it = storageMemory.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, InventoryStorage> entry = it.next();
            BlockPos pos = entry.getKey();
            InventoryStorage storage = entry.getValue();

            if (storage.getType() == InventoryStorage.FlagType.SORTED ||
                    storage.getType() == InventoryStorage.FlagType.OVERFLOW) {
                for (SlotMemory slot : storage.getMemoryActivelyStored()) {
                    if (UtilContainer.isSame(stack, slot.getStack())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void log(Object obj) {
        System.out.println(obj);
    }

}
