package com.example.examplemod.mixin;

import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = AbstractCraftingBuildingModule.class, remap = false)

public class AbstractCraftingBuildingModuleMixin {

    @ModifyConstant(method = "getMaxRecipes", remap = false, constant = @Constant(doubleValue = 2.0D))
    public double getMax(double oldValue){
        return 200;
    }

}