package com.corosus.chestorganizer.input;

import net.java.games.input.Controller;
import net.java.games.input.Keyboard;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

import java.awt.event.KeyEvent;

public class Keybinds {

    public static KeyBinding sort = new KeyBinding("Toggle chest sorting mode", KeyEvent.VK_H, "key.categories.mp");
    public static KeyBinding cycle = new KeyBinding("Cycle chest type", KeyEvent.VK_J, "key.categories.mp");
    public static KeyBinding visual = new KeyBinding("Show chest types", KeyEvent.VK_K, "key.categories.mp");

}
