package com.builtbroken.fluidinventorycrafting.recipe;

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
import net.minecraftforge.oredict.ShapelessOreRecipe;
import com.builtbroken.fluidinventorycrafting.recipe.IngredientFluidFactory.IngredientFluid;

import javax.annotation.Nonnull;

public class ShapelessRecipeFluidFactory implements IRecipeFactory {

    //The amount of mB in a bucket, should be 1000
    private static final int BUCKET = Fluid.BUCKET_VOLUME;

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");

        NonNullList<Ingredient> ings = NonNullList.create();
        for (JsonElement ele : JsonUtils.getJsonArray(json, "ingredients"))
            ings.add(CraftingHelper.getIngredient(ele, context));

        if (ings.isEmpty())
            throw new JsonParseException("No ingredients for shapeless recipe");

        ItemStack resultstack = null;
        //Is the recipe one that fills the output or not
        boolean fill = json.has("fill") && json.get("fill").getAsBoolean();
        if (fill) {
            resultstack = FluidUtil.getFilledBucket(getResultFluidIngredient(ings).fluidStack);
        } else {
            resultstack = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
        }
        return new ShapelessFluidRecipe(new ResourceLocation(group), resultstack, fill, ings.toArray());
    }
    /**
     *
     * @param ingredients Ingredients used in the recipe
     * @return The ingredient that should be filled, if any.
     */
    private static IngredientFluid getResultFluidIngredient(NonNullList<Ingredient> ingredients) {
        for (Ingredient ingredient : ingredients) {
            if (ingredient instanceof IngredientFluid) {
                IngredientFluid ing = (IngredientFluid) ingredient;
                if (ing.result) {
                    return ing;
                }
            }
        }
        return null;
    }

    public static class ShapelessFluidRecipe extends ShapelessOreRecipe {

        private boolean fill;

        public ShapelessFluidRecipe(ResourceLocation group, ItemStack result, boolean fill, Object... ingredients) {
            super(group, result, ingredients);
            this.fill = fill;
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
            boolean foundfill = false;
            //The ingredient that will turn into the result if this is a fill recipe
            IngredientFluid resultIngredient = getResultFluidIngredient(getIngredients());
            //The current player crafting
            EntityPlayer crafter = ForgeHooks.getCraftingPlayer();
            boolean inWater = crafter != null && crafter.isInWater(); //Crafter is in water
            boolean inLava = crafter != null && crafter.isInLava(); //Crafter is in lava
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                //Get the IFluidHandler for the given item
                IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
                if (!foundfill && fill && resultIngredient.apply(stack)) {
                    //This is a fill recipe and this ingredient is the one that will be filled
                    foundfill = true;
                    continue;
                }
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

        @Nonnull
        @Override
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
            //If not a fill recipe, return the regular result
            if (!fill) return super.getCraftingResult(inv);
            //The ingredient that will turn into the result
            IngredientFluid resultIngredient = getResultFluidIngredient(getIngredients());
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
                if (fill && resultIngredient.apply(stack)) {
                    //Fill the ingredient's container
                    handler.fill(resultIngredient.fluidStack, true);
                    return handler.getContainer().copy();
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }
    }

}
