package com.example.examplemod.mixin;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JobDeliveryman.class, remap = false)
public abstract class JobDeliverymanMixin extends AbstractJob<EntityAIWorkDeliveryman, JobDeliveryman> {
    public JobDeliverymanMixin(ICitizenData entity) {
        super(entity);
    }

    @Shadow public EntityAIWorkDeliveryman generateAI(){return null;}

    @ModifyConstant(method = "onLevelUp", constant = @Constant(doubleValue = 0.003D))
    public double onLevelUp(double oldValue){
        return 0.1;
    }
}
