package com.example.examplemod.mixin;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.initializer.ModBuildingsInitializer;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.CraftingWorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.SettingsModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.CrafterRecipeSetting;
import com.minecolonies.coremod.colony.buildings.moduleviews.CrafterTaskModuleView;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.colony.buildings.moduleviews.SettingsModuleView;
import com.minecolonies.coremod.colony.buildings.moduleviews.WorkerBuildingModuleView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSawmill;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModBuildingsInitializer.class, remap = false)
public class ModBuildingsInitializerMixin {
    @Inject(method = "init", at=@At(value = "INVOKE", ordinal = 21, target = "Lcom/minecolonies/api/colony/buildings/registry/BuildingEntry$Builder;addBuildingModuleProducer(Ljava/util/function/Supplier;Ljava/util/function/Supplier;)Lcom/minecolonies/api/colony/buildings/registry/BuildingEntry$Builder;"))
    private static void init(RegistryEvent.Register<BuildingEntry> event, CallbackInfo ci){
     ModBuildings.sawmill = new BuildingEntry.Builder()
                .setBuildingBlock(ModBlocks.blockHutSawmill)
                .setBuildingProducer(BuildingSawmill::new)
                .setBuildingViewProducer(() -> BuildingSawmill.View::new)
                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SAWMILL_ID))
                .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.sawmill, Skill.Dexterity, Skill.Knowledge, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                .addBuildingModuleProducer(() -> new BuildingSawmill.CraftingModule(ModJobs.sawmill), () -> CraftingModuleView::new)
                .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                .createBuildingEntry();
    }

}
