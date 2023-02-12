package com.example.examplemod.mixin;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.client.gui.modules.FarmerFieldsModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.FarmerFieldModuleView;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = FarmerFieldsModuleWindow.class, remap = false)

public class FarmerFieldsModuleWindowMixin extends AbstractModuleWindow {


    @Shadow
    private ScrollingList fieldList;
    @Shadow
    private List<BlockPos> fields;
    @Shadow
    private ClientLevel world;
    @Shadow
    private FarmerFieldModuleView moduleView;

    public FarmerFieldsModuleWindowMixin(IBuildingView building, String res) {
        super(building, res);
    }

    @Inject(method = "onOpened", at=@At("TAIL"))
    public void assignField(CallbackInfo ci){
        this.fieldList.setDataProvider(new ScrollingList.DataProvider() {// 197
            public int getElementCount() {
                return FarmerFieldsModuleWindowMixin.this.fields.size();// 202
            }

            public void updateElement(int index, @NotNull Pane rowPane) {
                BlockPos field = FarmerFieldsModuleWindowMixin.this.fields.get(index);// 208
                String distance = Integer.toString((int)Math.sqrt((double) BlockPosUtil.getDistanceSquared(field, FarmerFieldsModuleWindowMixin.this.buildingView.getPosition())));// 209
                String direction = BlockPosUtil.calcDirection(FarmerFieldsModuleWindowMixin.this.buildingView.getPosition(), field);// 210
                BlockEntity entity = FarmerFieldsModuleWindowMixin.this.world.getBlockEntity(field);// 211
                if (entity instanceof ScarecrowTileEntity) {// 212
                    String owner = ((ScarecrowTileEntity)entity).getOwner().isEmpty() ? "<" + LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.farmerhut.unused", new Object[0]) + ">" : ((ScarecrowTileEntity)entity).getOwner();// 214 215 216 217
                    rowPane.findPaneOfTypeByID("worker", Text.class).setText(owner);// 219
                    rowPane.findPaneOfTypeByID("dist", Text.class).setText(distance + "m");// 220
                    rowPane.findPaneOfTypeByID("dir", Text.class).setText(direction);// 222
                    Button assignButton = rowPane.findPaneOfTypeByID("assignFarm", Button.class);// 224
                    assignButton.setEnabled(FarmerFieldsModuleWindowMixin.this.moduleView.assignFieldManually());// 226
                    if (((ScarecrowTileEntity)entity).isTaken()) {// 228
                        assignButton.setText("§n§4X");// 230
                    } else {
                        assignButton.setText("✓");// 234
                        if (FarmerFieldsModuleWindowMixin.this.buildingView.getBuildingLevel() * 5 <= FarmerFieldsModuleWindowMixin.this.moduleView.getAmountOfFields()) {// 235
                            assignButton.disable();// 237
                        }
                    }

                    if (((ScarecrowTileEntity)entity).getSeed() != null) {// 241
                        rowPane.findPaneOfTypeByID("icon", ItemIcon.class).setItem(((ScarecrowTileEntity)entity).getSeed());// 243
                    }
                }

            }// 246
        });
    }
}