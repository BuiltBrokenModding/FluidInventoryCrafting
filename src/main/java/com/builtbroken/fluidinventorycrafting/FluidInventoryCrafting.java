package com.builtbroken.fluidinventorycrafting;

import com.builtbroken.fluidinventorycrafting.recipe.ShapedRecipeFluidFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod(modid = FluidInventoryCrafting.MODID, name = FluidInventoryCrafting.NAME, version = FluidInventoryCrafting.VERSION)
public class FluidInventoryCrafting {

    public static final String MODID = "fluidinventorycrafting";
    public static final String NAME = "Fluid Inventory Crafting";
    public static final String VERSION = "1.0.0";

    /**Steam to create when using a water bucket on lava*/
    public static Fluid STEAM = null;

    private static Logger logger;

    @Mod.Instance(MODID)
    public static FluidInventoryCrafting INSTANCE;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        STEAM = FluidRegistry.getFluid("steam");
    }
}
