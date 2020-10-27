package com.syszee.mod.datagen;

import com.syszee.mod.ModMain;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class FluidTagGen extends FluidTagsProvider
{
    public FluidTagGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, ModMain.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerTags()
    {
    }
}
