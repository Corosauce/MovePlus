package com.syszee.mod.datagen;

import com.syszee.mod.ModMain;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagGen extends BlockTagsProvider
{
    public BlockTagGen(DataGenerator generatorIn, ExistingFileHelper existingFileHelper)
    {
        super(generatorIn, ModMain.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerTags()
    {
    }
}
