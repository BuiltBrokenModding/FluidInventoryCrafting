package com.builtbroken.fluidinventorycrafting.recipe;

import com.builtbroken.fluidinventorycrafting.FluidInventoryCrafting;
import com.builtbroken.fluidinventorycrafting.recipe.IngredientFluidFactory.IngredientFluid;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

public class ShapedRecipeFluidFactory implements IRecipeFactory {

    //The amount of mB in a bucket, should be 1000
    private static final int BUCKET = Fluid.BUCKET_VOLUME;

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        //Parse the recipe as a shaped oredictionary recipe
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);
        //Transfer the info from the shaped oredictionary recipe
        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
        primer.input = recipe.getIngredients(); //The ingredients of the recipe
        primer.width = recipe.getRecipeWidth(); //The width of the minimum required crafting grid
        primer.height = recipe.getRecipeHeight(); //The height of the minimum required crafting grid
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", false); //If the recipe can be mirrored and still be valid
        return new ShapedFluidRecipe(new ResourceLocation(recipe.getGroup()), recipe.getRecipeOutput(), primer);
    }

    public static class ShapedFluidRecipe extends ShapedOreRecipe {

        public ShapedFluidRecipe(ResourceLocation group, ItemStack result, CraftingHelper.ShapedPrimer primer) {
            super(group, result, primer);
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
            //The current player crafting
            EntityPlayer crafter = ForgeHooks.getCraftingPlayer();
            boolean inWater = crafter != null && crafter.isInWater(); //Crafter is in water
            boolean inLava = crafter != null && crafter.isInLava(); //Crafter is in lava
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                //Get the IFluidHandler for the given item
                IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
                if (handler == null) {
                    remaining.set(i, ForgeHooks.getContainerItem(stack));
                } else {
                    FluidStack drained = handler.drain(BUCKET, false);
                    if(drained != null) {
                        if (drained.getFluid() == FluidRegistry.WATER) {
                            //Drain if the container has water and the player is not water
                            if (!inWater) handler.drain(BUCKET, true);
                        }else if ( drained.getFluid() == FluidRegistry.LAVA) {
                            //Drain if the container has lava and the player is not lava
                            if (!inLava) handler.drain(BUCKET, true);
                        }else{
                            //Drain if the container
                            handler.drain(BUCKET, true);
                        }
                    }
                    remaining.set(i, handler.getContainer().copy());
                }
            }
            return remaining;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }
    }

}
