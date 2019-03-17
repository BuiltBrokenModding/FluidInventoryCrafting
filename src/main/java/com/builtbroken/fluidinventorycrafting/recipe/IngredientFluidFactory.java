package com.builtbroken.fluidinventorycrafting.recipe;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IngredientFluidFactory implements IIngredientFactory {

    @Nonnull
    @Override
    public Ingredient parse(JsonContext context, JsonObject json) {
        //For recipes where the result is a filled container, this marks that this is the container that will be filled
        boolean result = json.has("result") ? json.get("result").getAsBoolean() : false;
        //get fluid string defined in recipe file
        String fluid = json.get("fluid").getAsString();
        //get JsonObject as string for fluidstack nbt if defined in the recipe file
        String nbt = json.has("nbt") ? json.get("nbt").toString() : null;
        return new IngredientFluid(fluid, nbt, result);
    }

    public static class IngredientFluid extends Ingredient{

        /**Contains the fluid, amount, and NBT*/
        public FluidStack fluidStack;
        /**The amount of mB in a bucket, should be 1000*/
        private static final int BUCKET = Fluid.BUCKET_VOLUME;
        /**For recipes where the result is a filled container, this marks that this is the container that will be filled*/
        public boolean result;

        public IngredientFluid(String fluid, String nbt, boolean result) {
            super(result ? new ItemStack(Items.BUCKET) : FluidUtil.getFilledBucket(new FluidStack(FluidRegistry.getFluid(fluid), BUCKET)));
            this.result = result;
            NBTTagCompound tag = null;
            if(nbt != null){
                try {
                    //Get the NBTTagCompound from a json string
                    tag = JsonToNBT.getTagFromJson(nbt);
                }catch(NBTException e){
                    e.printStackTrace();
                }
            }
            fluidStack = FluidRegistry.getFluidStack(fluid, BUCKET);
            fluidStack.tag = tag;
        }

        @Override
        public boolean apply(@Nullable ItemStack stack) {
            //If the ingredient's fluid is empty or if the given itemstack is empty, return false
            if(fluidStack == null || stack.isEmpty())return false;
            //Get the IFluidHandler for the given item
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
            if(handler == null)return false;
            if(result){
                //Returns true if 1000mB of this ingredient's fluid is able to be filled. Does not actually fill.
                return handler.fill(fluidStack, false) == BUCKET;
            }else {
                //Returns true if 1000mB of this ingredient's fluid is able to be drained. Does not actually drain.
                return fluidStack.isFluidStackIdentical(handler.drain(BUCKET, false));
            }
        }

        @Override
        public boolean isSimple() {
            return false;
        }
    }
}
