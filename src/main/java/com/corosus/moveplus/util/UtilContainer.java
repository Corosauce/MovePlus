package com.corosus.moveplus.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class UtilContainer {
	
	public static int mouse2StepClick = 0;
	public static int mouseShiftClick = 1;
	
	public static int mouseLeftClick = 0;
	public static int mouseRightClick = 1;

	public static void clickSlot(PlayerEntity player, int slot, int mouseButton, ClickType clickModifier) {
		Minecraft mc = Minecraft.getInstance();
		
		if (player.openContainer != null) {
			mc.playerController.windowClick(player.openContainer.windowId, slot, mouseButton, clickModifier, player);
		} else {
			System.out.println("WARNING: Tried to click slot without an open container");
		}
		
	}

	public static int getFirstSlotContainingItem(Container container, ItemStack itemStack, int findStart, int findEnd) {
		int index = -1;

		for (int i = findStart; i < findEnd; i++) {
			ItemStack stack = (ItemStack) ((Slot)container.inventorySlots.get(i)).getStack();

			if (isSame(itemStack, stack)) {
				index = i;
				break;
			}
		}

		return index;
	}

	public static boolean isSame(ItemStack stack1, ItemStack stack2) {
		return (stack1.getItem() == stack2.getItem());
		/*if (stack1 == null || stack2 == null) return false;
		if (stack1.getItem() == null || stack2.getItem() == null) {
			//UtilDbg.out("WARNING! plan has a null item! or we are at least comparing against a null item somewhere!");
			return false;
		}
		//why do we do stacklimit == stacklimit, that should only be fore knowing if we need to compare meta
		//return stack1.getItem() == stack2.getItem() && ((stack1.getItem().getItemStackLimit(stack1) == 1 && stack2.getItem().getItemStackLimit(stack2) == 1) || stack1.getItemDamage() == stack2.getItemDamage() || stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE || stack2.getItemDamage() == OreDictionary.WILDCARD_VALUE);
		return stack1.getItem() == stack2.getItem() &&
				((!stack1.getItem().getHasSubtypes() && !stack2.getItem().getHasSubtypes()) ||
						(stack1.getItem().getItemStackLimit(stack1) != 1 && stack2.getItem().getItemStackLimit(stack2) != 1) ||
						(stack1.getItemDamage() == stack2.getItemDamage() || stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE || stack2.getItemDamage() == OreDictionary.WILDCARD_VALUE));*/
	}
	
	/*public static void openContainer(int x, int y, int z) {
		EntityPlayer player = Corobot.playerAI.bridgePlayer.getPlayer();
		Minecraft mc = Minecraft.getMinecraft();
		mc.playerController.onPlayerRightClick(player, player.worldObj, null, x, y, z, 0, Vec3.createVectorHelper(x, y, z));
		//c_AIP.i.mc.playerController.onPlayerRightClick(c_AIP.i.player, c_AIP.i.worldObj, c_AIP.i.player.getCurrentEquippedItem(), x, y, z, 0, Vec3.createVectorHelper(x, y, z));
	}*/
	
}
