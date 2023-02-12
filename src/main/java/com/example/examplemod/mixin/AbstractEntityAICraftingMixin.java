package com.example.examplemod.mixin;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.CraftingWorkerBuildingModule;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;

@Mixin(value = AbstractEntityAICrafting.class, remap = false)
public abstract class AbstractEntityAICraftingMixin<J extends AbstractJobCrafter<?, J>, B extends AbstractBuilding> extends AbstractEntityAIInteract<J, B>{
    @Shadow
    protected IRecipeStorage currentRecipeStorage;

    @Shadow
    public IRequest<? extends PublicCrafting> currentRequest;

    @Shadow
    protected abstract int getActionRewardForCraftingSuccess();

    @Shadow
    protected abstract IAIState checkForItems(@NotNull IRecipeStorage storage);

    @Shadow
    public abstract void resetValues();

    @Shadow
    protected abstract LootContext getLootContext();

    @Shadow
    protected abstract int getRequiredProgressForMakingRawMaterial();

    @Shadow
    @Final
    public static int PROGRESS_MULTIPLIER = 3;

    @Shadow
    @Final
    protected static int MAX_LEVEL = 99;

    @Shadow
    @Final
    private static int HITTING_TIME = 1;

    public AbstractEntityAICraftingMixin(@NotNull J job) {
        super(job);
    }


    @Inject(method = "getRequiredProgressForMakingRawMaterial", at = @At("HEAD"), cancellable = true)
    public void getRequiredProgressForMakingRawMaterial(CallbackInfoReturnable<Integer> cir) {
        int jobModifier = this.worker.getCitizenData().getCitizenSkillHandler().getLevel(((CraftingWorkerBuildingModule)this.getModuleForJob()).getCraftSpeedSkill()) / 2;// 452
        int modifier = PROGRESS_MULTIPLIER / Math.min(jobModifier + 1, MAX_LEVEL) * HITTING_TIME;// 452
        cir.setReturnValue(modifier);
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    public void craft(CallbackInfoReturnable<IAIState> cir) {
        if (currentRecipeStorage == null || job.getCurrentTask() == null) {
            cir.setReturnValue(START_WORKING);
            return;
        }

        if (currentRequest == null && job.getCurrentTask() != null) {
            cir.setReturnValue(GET_RECIPE);
            return;
        }

        if (walkToBuilding()) {
            cir.setReturnValue(getState());
            return;
        }

        job.setProgress(job.getProgress() + 1);

        worker.setItemInHand(InteractionHand.MAIN_HAND,
                currentRecipeStorage.getCleanedInput().get(worker.getRandom().nextInt(currentRecipeStorage.getCleanedInput().size())).getItemStack().copy());
        worker.setItemInHand(InteractionHand.OFF_HAND, currentRecipeStorage.getPrimaryOutput().copy());
        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getPosition());

        currentRequest = job.getCurrentTask();

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED)) {
            currentRequest = null;
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            cir.setReturnValue(START_WORKING);
            return;
        }

        if (job.getProgress() >= getRequiredProgressForMakingRawMaterial()) {
            for (int i = 0; i < outputModifier(); i++) {
                final IAIState check = checkForItems(currentRecipeStorage);
                if (check == CRAFT) {
                    if (!currentRecipeStorage.fullfillRecipe(getLootContext(), ImmutableList.of(worker.getItemHandlerCitizen()))) {
                        currentRequest = null;
                        incrementActionsDone(getActionRewardForCraftingSuccess());
                        job.finishRequest(false);
                        resetValues();
                        cir.setReturnValue(START_WORKING);
                        return;
                    }

                    currentRequest.addDelivery(currentRecipeStorage.getPrimaryOutput());
                    job.setCraftCounter(job.getCraftCounter() + 1);

                    if (job.getCraftCounter() >= job.getMaxCraftingCount()) {
                        incrementActionsDone(getActionRewardForCraftingSuccess());
                        final ICraftingBuildingModule module = getOwnBuilding().getCraftingModuleForRecipe(currentRecipeStorage.getToken());
                        if (module != null) {
                            module.improveRecipe(currentRecipeStorage, job.getCraftCounter(), worker.getCitizenData());
                        }

                        currentRecipeStorage = null;
                        resetValues();

                        if (inventoryNeedsDump()) {
                            if (job.getMaxCraftingCount() == 0 && job.getProgress() == 0 && job.getCraftCounter() == 0 && currentRequest != null) {
                                job.finishRequest(true);
                                worker.getCitizenExperienceHandler().addExperience(currentRequest.getRequest().getCount() / 2.0);
                            }
                        }
                        cir.setReturnValue(START_WORKING);
                        return;
                    }

                } else {
                    currentRequest = null;
                    job.finishRequest(false);
                    incrementActionsDoneAndDecSaturation();
                    resetValues();
                    cir.setReturnValue(START_WORKING);
                    return;
                }
            }
            job.setProgress(0);
            cir.setReturnValue(GET_RECIPE);
            return;
        }

        cir.setReturnValue(getState());
        return;
    }

    @Unique
    private int outputModifier() {
        int modifier = worker.getCitizenData().getCitizenSkillHandler().getLevel(((CraftingWorkerBuildingModule) getModuleForJob()).getCraftSpeedSkill());
        return (int) Math.ceil((modifier / 3) + 1);
    }


}
