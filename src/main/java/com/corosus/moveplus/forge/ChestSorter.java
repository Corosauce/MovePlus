package com.corosus.moveplus.forge;

import com.corosus.moveplus.util.UtilContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

import java.util.*;

public class ChestSorter {

    public static HashMap<BlockPos, InventoryStorage> storageMemory = new HashMap<>();

    public static BlockPos lastLookedAtBlock = BlockPos.ZERO;
    public static Screen lastScreenChecked = null;

    public static void tickClient() {

        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null || mc.player == null) return;

        if (mc.objectMouseOver != null && mc.objectMouseOver instanceof BlockRayTraceResult) {
            lastLookedAtBlock = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
        }

        if (Minecraft.getInstance().currentScreen instanceof ChestScreen) {
            ChestScreen screen = (ChestScreen) Minecraft.getInstance().currentScreen;

            if (lastScreenChecked != screen) {
                lastScreenChecked = screen;

                if (lastLookedAtBlock != BlockPos.ZERO) {
                    if (!storageMemory.containsKey(lastLookedAtBlock)) {
                        InventoryStorage storage = new InventoryStorage();
                        storage.setPos(lastLookedAtBlock);
                        log("add new storage memory at " + lastLookedAtBlock);

                        //TODO: packets, lag, test in multiplayer, async waiting for chest contents if needed
                        storage.getMemory().addAll(screen.getContainer().inventorySlots);
                        log("added " + storage.getMemory().size());
                        for (Slot slot : storage.getMemory()) {
                            if (!slot.inventory.isEmpty()) {
                                log("entry: " + slot.slotNumber + " - " + slot.inventory.getStackInSlot(slot.slotNumber));
                            }
                        }
                        storageMemory.put(lastLookedAtBlock, storage);

                    } else {
                        log("opened known chest, updating contents memory");
                        InventoryStorage storage = storageMemory.get(lastLookedAtBlock);
                        storage.getMemory().clear();
                        storage.getMemory().addAll(screen.getContainer().inventorySlots);
                        log("updated " + storage.getMemory().size());
                    }
                } else {
                    log("pos was zero");
                }
            }

            //test inv transfer
            if (lastLookedAtBlock != BlockPos.ZERO) {
                ItemStack stack = mc.player.inventory.getCurrentItem();
                if (stack != ItemStack.EMPTY) {
                    if (storageMemory.containsKey(lastLookedAtBlock)) {
                        InventoryStorage storage = storageMemory.get(lastLookedAtBlock);
                        for (Slot entry2 : storage.getMemory()) {
                            if (entry2.inventory.getStackInSlot(entry2.slotNumber) != ItemStack.EMPTY) {
                                if (entry2.inventory.getStackInSlot(entry2.slotNumber).getItem() == stack.getItem()) {

                                }
                            }
                        }
                        int blockStorageSize = screen.getContainer().getLowerChestInventory().getSizeInventory();
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
                }
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
                mc.world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0D, 0.0D, 0.0D);
            }

            ItemStack stack = mc.player.inventory.getCurrentItem();
            if (stack != ItemStack.EMPTY) {
                for (Slot entry2 : storage.getMemory()) {
                    if (entry2.inventory.getStackInSlot(entry2.slotNumber) != ItemStack.EMPTY) {
                        if (entry2.inventory.getStackInSlot(entry2.slotNumber).getItem() == stack.getItem()) {
                            mc.world.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0D, 0.0D, 0.0D);
                        }
                    }
                }
            }
        }


    }

    public static void log(Object obj) {
        System.out.println(obj);
    }

}
