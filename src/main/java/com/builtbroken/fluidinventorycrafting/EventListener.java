package com.builtbroken.fluidinventorycrafting;

import com.builtbroken.fluidinventorycrafting.recipe.ShapedRecipeFluidFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber
public class EventListener {

    /**
     * If the player crafts cobblestone using a ShapedFluidRecipe and is in lava, set the result stack size to one
     */
    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent e) {
        if (e.craftMatrix instanceof InventoryCrafting) {
            //Get the current recipe
            IRecipe recipe = CraftingManager.findMatchingRecipe((InventoryCrafting) e.craftMatrix, e.player.world);
            if(recipe instanceof ShapedRecipeFluidFactory.ShapedFluidRecipe){
                //Make sure the result is cobblestone
                if(e.crafting.getItem() == Item.getItemFromBlock(Blocks.COBBLESTONE)){
                    //If the player is in lava, set the result stack size to only one
                    if(e.player.isInLava())e.crafting.setCount(1);
                }
            }
        }
    }

    /**
     * If the player right clicks a block of lava with a bucket of water, turn the water into steam
     */
    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem e) {
        EntityPlayer player = e.getEntityPlayer();
        World w = player.getEntityWorld();
        //Make sure that some fluid 'steam' exists
        if(FluidInventoryCrafting.STEAM != null){
            //Make sure the player is holding a bucket
            if(e.getItemStack().getItem() == Items.WATER_BUCKET){
                //The reach distance of the player (Better to use attributes than a hard coded value since other mods can change the value)
                double distance = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
                //The position of the eyes as a vector
                Vec3d vecEyes = player.getPositionEyes(0);
                //The direction the player is facing as a vector
                Vec3d vecDir = player.getLook(0);
                //The farthest that the player can reach in the current direction they are looking in
                Vec3d vecEnd = vecEyes.addVector(vecDir.x * distance, vecDir.y * distance, vecDir.z * distance);
                //Raytrace from the eyes to the end, stopping when any block (including a liquid) is hit
                RayTraceResult result = w.rayTraceBlocks(vecEyes, vecEnd, true);
                //Make sure the raytrace hit a block
                if(result != null && result.typeOfHit == RayTraceResult.Type.BLOCK){
                    //Check if the hit block is lava
                    if(w.getBlockState(result.getBlockPos()).getBlock().getRegistryName().equals(Blocks.LAVA.getRegistryName())){
                        //Set the held item to steam
                        player.setHeldItem(e.getHand(), FluidUtil.getFilledBucket(new FluidStack(FluidInventoryCrafting.STEAM, Fluid.BUCKET_VOLUME)));
                        w.playSound(player, result.getBlockPos(), SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 1f, 1f);
                        e.setCanceled(true);
                    }
                }
            }
        }
    }
}
