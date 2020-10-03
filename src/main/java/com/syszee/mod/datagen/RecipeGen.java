package com.syszee.mod.datagen;

import com.syszee.mod.common.registry.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class RecipeGen extends RecipeProvider
{
	public RecipeGen(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
	{
//        ShapedRecipeBuilder.shapedRecipe(ModItems.EXAMPLE.get())
//                .patternLine("DDD")
//                .patternLine("DDD")
//                .patternLine("III")
//                .key('D', Tags.Items.GEMS_DIAMOND)
//                .key('I', Tags.Items.INGOTS_IRON)
//                .addCriterion("has_diamond", hasItem(Tags.Items.GEMS_DIAMOND))
//                .addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON))
//                .build(consumer);
	}
}