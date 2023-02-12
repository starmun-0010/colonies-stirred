package com.example.examplemod.mixin;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingDeliveryman;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityAIWorkDeliveryman.class, remap = false)
public abstract class EntityAIWorkDeliverymanMixin extends AbstractEntityAIInteract<JobDeliveryman, BuildingDeliveryman> {
    public EntityAIWorkDeliverymanMixin( JobDeliveryman job) {
        super(job);
    }

    @Shadow  public  Class<BuildingDeliveryman> getExpectedBuildingClass(){return null;}

    @Inject(method = "cannotHoldMoreItems", at=@At("HEAD"),cancellable = true)
    public void cannotHoldMoreItems(CallbackInfoReturnable<Boolean> cir){
        if (this.getOwnBuilding().getBuildingLevel() >= this.getOwnBuilding().getMaxBuildingLevel()) {// 250
            cir.setReturnValue(false);// 252
        } else {

            boolean result= InventoryUtils.getAmountOfStacksInItemHandler(worker.getInventoryCitizen()) >= 6 * getOwnBuilding().getBuildingLevel();
            cir.setReturnValue(result);
        }
    }
}
