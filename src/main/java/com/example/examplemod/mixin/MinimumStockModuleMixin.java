package com.example.examplemod.mixin;

import com.minecolonies.coremod.colony.buildings.modules.MinimumStockModule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MinimumStockModule.class, remap = false)

public class MinimumStockModuleMixin {
    @Shadow
    @Final
    private static int STOCK_PER_LEVEL = 50;

}