package com.lautish.saddlehud.init.mixin;

import com.lautish.saddlehud.init.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Unique
    private static final int BOTTOM_ROW_OFFSET = 39;
    @Unique
    private static final int ROW_HEIGHT = 10;
    @Unique
    private static final int SELECTED_ITEM_NAME_OFFSET = 59;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Shadow
    protected abstract int getVehicleMaxHearts(LivingEntity vehicle);

    @Shadow
    protected abstract int getVisibleVehicleHeartRows(int hearts);

    @Shadow
    protected abstract boolean willPrioritizeJumpInfo();

    @Unique
    private boolean foodWithMount() {
        return ModConfig.showFoodWhileMounted && minecraft.gameMode.canHurtPlayer() && getVehicleMaxHearts(getPlayerVehicleWithHealth()) > 0;
    }

    @ModifyVariable(method = "extractPlayerHealth", at = @At(value = "LOAD", ordinal = 0), name = "vehicleHearts")
    private int extractFoodWithMount(int vehicleHearts) {
        return ModConfig.showFoodWhileMounted ? 0 : vehicleHearts;
    }

    @ModifyConstant(method = "extractVehicleHealth", constant = @Constant(intValue = BOTTOM_ROW_OFFSET))
    private int raiseVehicleHealth(int offset) {
        return foodWithMount() ? offset + ROW_HEIGHT : offset;
    }

    @ModifyConstant(method = "extractSelectedItemName", constant = @Constant(intValue = SELECTED_ITEM_NAME_OFFSET))
    private int raiseSelectedItemName(int offset) {
        return foodWithMount() ? Math.max(offset, BOTTOM_ROW_OFFSET + ROW_HEIGHT * (getVisibleVehicleHeartRows(getVehicleMaxHearts(getPlayerVehicleWithHealth())) + 1)) : offset;
    }

    @ModifyVariable(method = "nextContextualInfoState", at = @At("STORE"), ordinal = 1)
    private boolean showJumpBarOnlyWhileJumping(boolean canShowVehicleJumpInfo) {
        return ModConfig.experienceUntilJump ? canShowVehicleJumpInfo && willPrioritizeJumpInfo() : canShowVehicleJumpInfo;
    }
}
