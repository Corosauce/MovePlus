package com.syszee.mod.datagen;

import com.syszee.mod.ModMain;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.EntityTypeTagsProvider;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EntityTypeTagGen extends EntityTypeTagsProvider
{
    public EntityTypeTagGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, ModMain.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerTags()
    {
    }
}
