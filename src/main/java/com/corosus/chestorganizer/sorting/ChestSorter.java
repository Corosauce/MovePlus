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
                    storage.getMemoryActivelyStored().addAll(screen.getContainer().inventorySlots);
                    log("added or updated " + storage.getMemoryActivelyStored().size());
                    for (Slot slot : storage.getMemoryActivelyStored()) {
                        if (!slot.inventory.isEmpty()) {
                            log("entry: " + slot.slotNumber + " - " + slot.inventory.getStackInSlot(slot.slotNumber));
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
                    if (storage.getType() == InventoryStorage.FlagType.DUMP) {

                        for (Slot slot : screen.getContainer().inventorySlots) {
                            if (slot.inventory.getStackInSlot(slot.slotNumber) != ItemStack.EMPTY && slot.slotNumber < blockStorageSize) {
                                UtilContainer.clickSlot(mc.player, slot.slotNumber, UtilContainer.mouseLeftClick, ClickType.QUICK_MOVE);
                            }
                        }

                    } else if (storage.getType() == InventoryStorage.FlagType.SORTED) {

                        //log("try sort, slots: " + screen.getContainer().inventorySlots.size());
                        for (Slot slot : screen.getContainer().inventorySlots) {
                            //log("lookup: " + slot.slotNumber + " - " + slot.getStack());
                            if (slot.getStack() != ItemStack.EMPTY && slot.slotNumber >= blockStorageSize) {
                                //log("lookup2: " + slot.getStack());
                                int blockStorageFoundID = UtilContainer.getFirstSlotContainingItem(screen.getContainer(), slot.getStack(), 0, blockStorageSize);
                                if (blockStorageFoundID != -1) {
                                    UtilContainer.clickSlot(mc.player, slot.slotNumber, UtilContainer.mouseLeftClick, ClickType.QUICK_MOVE);
                                }
                            }
                        }

                    }
                }

                /*ItemStack stack = mc.player.inventory.getCurrentItem();
                if (stack != ItemStack.EMPTY) {
                    if (storageMemory.containsKey(lastLookedAtBlock)) {
                        //InventoryStorage storage = storageMemory.get(lastLookedAtBlock);
                        for (Slot entry2 : storage.getMemoryActivelyStored()) {
                            if (entry2.inventory.getStackInSlot(entry2.slotNumber) != ItemStack.EMPTY) {
                                if (entry2.inventory.getStackInSlot(entry2.slotNumber).getItem() == stack.getItem()) {

                                }
                            }
                        }
                        System.out.println("blockStorageSize: " + blockStorageSize);
                        int blockStorageFoundID = UtilContainer.getFirstSlotContainingItem(screen.getContainer(), stack, 0, blockStorageSize);
                        System.out.println("found block storage item at slot id: " + blockStorageFoundID);
                        int playerFoundID = UtilContainer.getFirstSlotContainingItem(screen.getContainer(), stack, blockStorageSize+1, screen.getContainer().inventorySlots.size());
                        System.out.println("found player storage item at slot id: " + playerFoundID);

                        if (blockStorageFoundID != -1 && playerFoundID != -1) {
                            //transfer
                            UtilContainer.clickSlot(mc.player, playerFoundID, UtilContainer.mouseLeftClick, ClickType.QUICK_MOVE);
                        }
                    }
                }*/
            }

            //screen.getContainer().
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

            ItemStack stack = mc.player.inventory.getCurrentItem();
            if (stack != ItemStack.EMPTY) {
                for (Slot entry2 : storage.getMemoryActivelyStored()) {
                    if (entry2.inventory.getStackInSlot(entry2.slotNumber) != ItemStack.EMPTY) {
                        if (entry2.inventory.getStackInSlot(entry2.slotNumber).getItem() == stack.getItem()) {
                            //mc.world.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0D, 0.0D, 0.0D);
                        }
                    }
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
            InventoryStorage storage = new InventoryStorage(pos);
            storageMemory.put(pos, storage);
            return storage;
        }
        return null;
    }

    public void enableSorting() {
        //TODO: take snapshot of inventory before using it so you dont sort their whole inventory
        isSorting = !isSorting;
        log("sorting " + (isSorting ? "on" : "off"));
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

            if (!(mc.world.getTileEntity(pos) instanceof ChestTileEntity)) {
                it.remove();
            }
        }
    }

    public void log(Object obj) {
        System.out.println(obj);
    }

}
