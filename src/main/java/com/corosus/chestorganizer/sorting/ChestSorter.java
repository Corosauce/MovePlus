package com.corosus.chestorganizer.sorting;

import com.corosus.chestorganizer.input.Keybinds;
import com.corosus.moveplus.util.UtilContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ChestSorter {

    private HashMap<BlockPos, InventoryStorage> storageMemory = new HashMap<>();

    //for preventing sorting from messing with items we had in our inventory before sorting
    //starts at slot id 0 so that we can fix it afterwards for dynamic slot adjusting (single vs double chest offsets)
    private HashMap<Integer, ItemStack> lookupPlayerPreSortSnapshotSlotMemoryToItem = new HashMap<>();

    //for referencing once screen closes so we can take a final snapshot to save
    private ChestContainer lastContainerOpened = null;

    private BlockPos lastLookedAtBlock = BlockPos.ZERO;
    private Screen lastScreenChecked = null;

    private boolean isSorting = false;
    private boolean wasSettingType = false;
    private boolean wasTogglingSort = false;
    private boolean wasTogglingVisual = false;

    private boolean needsInit = true;

    private boolean showVisuals = false;

    public void ChestSorter() {

    }

    public void tickScreen(RenderGameOverlayEvent.Pre event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null || mc.player == null) return;

        int scaledWidth = mc.getMainWindow().getScaledWidth();
        int scaledHeight = mc.getMainWindow().getScaledHeight();

        if (isSorting) {
            String str = "Sorting mode active";
            int x = scaledWidth / 2 - mc.fontRenderer.getStringWidth(str) / 2;
            int y = scaledHeight - 80;
            mc.fontRenderer.drawString(event.getMatrixStack(), str, x, y, Color.RED.getRGB());
        }
    }

    public void tickClient() {

        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null || mc.player == null) return;

        if (needsInit) {
            readFromFile();
            needsInit = false;
        }

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
            int blockStorageSize = screen.getContainer().getLowerChestInventory().getSizeInventory();

            if (lastScreenChecked != screen) {
                lastScreenChecked = screen;

                if (lastLookedAtBlock != BlockPos.ZERO) {

                    lastContainerOpened = screen.getContainer();
                    updateStorageInfo();


                } else {
                    log("pos was zero");
                }
            }

            if (lastLookedAtBlock != BlockPos.ZERO) {

                InventoryStorage storage = getOrAddStorageIfPresent(mc.world, lastLookedAtBlock);

                //log("blockStorageSize: " + blockStorageSize);

                //TODO: detect fullness and stop running this forever

                if (isSorting) {

                    //take the snapshot of player inventory if havent yet
                    if (lookupPlayerPreSortSnapshotSlotMemoryToItem.isEmpty()) {
                        log("making snapshot of player inventory");
                        for (Slot slot : screen.getContainer().inventorySlots) {
                            if (/*slot.getStack() != ItemStack.EMPTY && */slot.slotNumber >= blockStorageSize) {
                                lookupPlayerPreSortSnapshotSlotMemoryToItem.put(slot.slotNumber - blockStorageSize, slot.getStack());
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
                            log("lookup: " + slot.slotNumber + " - " + slot.getStack());
                            if (slot.getStack() != ItemStack.EMPTY && slot.slotNumber >= blockStorageSize) {
                                log("considering non empty slot in player inventory: " + slot.slotNumber);
                                if (lookupPlayerPreSortSnapshotSlotMemoryToItem.get(slot.slotNumber - blockStorageSize) == ItemStack.EMPTY) {
                                    log("slot was filled from dump chest: " + slot.slotNumber + ", " + slot.getStack());
                                    //log("lookup2: " + slot.getStack());
                                    //look for a match
                                    //TODO: add the extra memory check here too
                                    /*int blockStorageFoundID = UtilContainer.getFirstSlotContainingItem(screen.getContainer(), slot.getStack(), 0, blockStorageSize);
                                    if (blockStorageFoundID != -1) {
                                        UtilContainer.clickSlot(mc.player, slot.slotNumber, UtilContainer.mouseLeftClick, ClickType.QUICK_MOVE);
                                    }*/
                                    if (chestOrStackedChestsHasItemStack(mc.world, storage, slot.getStack())) {
                                        log("found match for " + slot.getStack());
                                        UtilContainer.clickSlot(mc.player, slot.slotNumber, UtilContainer.mouseLeftClick, ClickType.QUICK_MOVE);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        } else {
            //try to get a final snapshot on close of gui
            if (lastContainerOpened != null) {
                updateStorageInfo();
                lastContainerOpened = null;
            }
        }

        //TODO: validate positions incase chests move
        //storageMemory.
        if (showVisuals) {
            Iterator<Map.Entry<BlockPos, InventoryStorage>> it = storageMemory.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, InventoryStorage> entry = it.next();
                BlockPos pos = entry.getKey();
                InventoryStorage storage = entry.getValue();
                if (mc.world.getGameTime() % 5 == 0) {
                    IParticleData data = null;
                    if (storage.getType() == InventoryStorage.FlagType.UNMARKED) {
                        //data = ParticleTypes.SMOKE;
                    } else if (storage.getType() == InventoryStorage.FlagType.DUMP) {
                        data = ParticleTypes.END_ROD;
                    } else if (storage.getType() == InventoryStorage.FlagType.SORTED) {
                        data = ParticleTypes.FLAME;
                    } else if (storage.getType() == InventoryStorage.FlagType.OVERFLOW) {
                        data = ParticleTypes.CRIT;
                    }
                    if (data != null) {
                        //mc.world.addParticle(data, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0D, 0.0D, 0.0D);
                        mc.world.addParticle(data, pos.getX() + 0.0, pos.getY() + 0.0, pos.getZ() + 0.0, 0.0D, 0.0D, 0.0D);
                        mc.world.addParticle(data, pos.getX() + 1.0, pos.getY() + 0.0, pos.getZ() + 0.0, 0.0D, 0.0D, 0.0D);
                        mc.world.addParticle(data, pos.getX() + 0.0, pos.getY() + 0.0, pos.getZ() + 1.0, 0.0D, 0.0D, 0.0D);
                        mc.world.addParticle(data, pos.getX() + 1.0, pos.getY() + 0.0, pos.getZ() + 1.0, 0.0D, 0.0D, 0.0D);

                        mc.world.addParticle(data, pos.getX() + 0.0, pos.getY() + 1.0, pos.getZ() + 0.0, 0.0D, 0.0D, 0.0D);
                        mc.world.addParticle(data, pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 0.0, 0.0D, 0.0D, 0.0D);
                        mc.world.addParticle(data, pos.getX() + 0.0, pos.getY() + 1.0, pos.getZ() + 1.0, 0.0D, 0.0D, 0.0D);
                        mc.world.addParticle(data, pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }

        if (Keybinds.cycle.isKeyDown() &&
                activeLookedAtBlock != BlockPos.ZERO) {
            if (!wasSettingType) {
                InventoryStorage storage = getOrAddStorageIfPresent(mc.world, activeLookedAtBlock);
                if (storage != null) {
                    storage.cycleType();
                    writeToFile();
                }
                wasSettingType = true;
            }
        } else {
            wasSettingType = false;
        }

        if (Keybinds.sort.isKeyDown()) {
            if (!wasTogglingSort) {
                enableSorting();
                wasTogglingSort = true;
            }
        } else {
            wasTogglingSort = false;
        }

        if (Keybinds.visual.isKeyDown()) {
            if (!wasTogglingVisual) {
                showVisuals = !showVisuals;
                wasTogglingVisual = true;
            }
        } else {
            wasTogglingVisual = false;
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

    public CompoundNBT writeToNBT() {
        CompoundNBT nbt = new CompoundNBT();

        ListNBT list = new ListNBT();
        for (InventoryStorage storage : storageMemory.values()) {
            list.add(storage.writeToNBT());
        }

        nbt.put("storageMemory", list);

        return nbt;
    }

    public void writeToFile() {
        File file = new File(Minecraft.getInstance().gameDir, "chest_sorter.dat");
        try {
            CompressedStreamTools.writeCompressed(writeToNBT(), file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void readFromNBT(CompoundNBT nbt) {
        ListNBT listnbt = nbt.getList("storageMemory", 10);

        for(int k = 0; k < listnbt.size(); ++k) {
            InventoryStorage storage = InventoryStorage.fromNBT(listnbt.getCompound(k));
            storageMemory.put(storage.getPos(), storage);
        }
    }

    public void readFromFile() {
        File file = new File(Minecraft.getInstance().gameDir, "chest_sorter.dat");
        if (file.exists()) {
            try {
                FileInputStream fileinputstream = new FileInputStream(file);
                PushbackInputStream pushbackinputstream = new PushbackInputStream(fileinputstream, 2);
                readFromNBT(CompressedStreamTools.readCompressed(pushbackinputstream));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void log(Object obj) {
        System.out.println(obj);
    }

    /**
     * TODO: support connected chests
     * @param world
     * @param storage
     * @param stack
     * @return
     */
    public boolean chestOrStackedChestsHasItemStack(World world, InventoryStorage storage, ItemStack stack) {
        BlockPos posBottom = getBottomMostChest(world, storage.getPos());
        List<ItemStack> list = getMergedInventoryStorageFromStackedChests(world, posBottom);
        for (ItemStack stackEntry : list) {
            if (UtilContainer.isSame(stackEntry, stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO: support connected chests
     * @param world
     * @param pos must be bottom most chest
     * @return
     */
    public List<ItemStack> getMergedInventoryStorageFromStackedChests(World world, BlockPos pos) {
        List<ItemStack> list = new ArrayList<>();

        BlockPos lastPos = new BlockPos(pos);
        while (world.getTileEntity(lastPos) instanceof ChestTileEntity) {
            InventoryStorage storage = getOrAddStorageIfPresent(world, lastPos);
            if (storage.getType() == InventoryStorage.FlagType.SORTED || storage.getType() == InventoryStorage.FlagType.OVERFLOW) {
                List<ItemStack> list2 = storage.getMemoryActivelyStoredAsItemStacks();
                list.addAll(list2);
            }
            lastPos = lastPos.up();
        }
        return list;
    }

    public BlockPos getBottomMostChest(World world, BlockPos pos) {
        BlockPos lastPos = new BlockPos(pos);
        while (world.getTileEntity(lastPos) instanceof ChestTileEntity) {
            lastPos = lastPos.down();
        }
        return lastPos.up();
    }

    public void updateStorageInfo() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null || mc.player == null) return;
        InventoryStorage storage = getOrAddStorageIfPresent(mc.world, lastLookedAtBlock);
        storage.resetActiveMemory();
        //storage.addAllSlots(screen.getContainer().inventorySlots);
        int blockStorageSize = lastContainerOpened.getLowerChestInventory().getSizeInventory();
        for (Slot slot : lastContainerOpened.inventorySlots) {
            if (slot.inventory.getStackInSlot(slot.slotNumber) != ItemStack.EMPTY && slot.slotNumber < blockStorageSize) {
                storage.getMemoryActivelyStored().add(new SlotMemory(slot.slotNumber, slot.getStack()));
            }
        }
        writeToFile();
        log("added or updated " + storage.getMemoryActivelyStored().size());
    }

}
