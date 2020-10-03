package com.syszee.mod.common.registry;

import com.syszee.mod.ModMain;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModSounds
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModMain.MOD_ID);

    // public static final RegistryObject<SoundEvent> EXAMPLE = register("example");

    /* Registry Methods */

    /**
     * Registers a new sound event under the specified id.
     *
     * @param id The id of the sound event
     */
    private static RegistryObject<SoundEvent> register(String id)
    {
        return SOUNDS.register(id, () -> new SoundEvent(new ResourceLocation(ModMain.MOD_ID, id)));
    }
}