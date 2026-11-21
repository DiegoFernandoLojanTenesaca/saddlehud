package com.lautish.saddlehud.init.mixin;

import com.lautish.saddlehud.display.MountHud;
import com.lautish.saddlehud.init.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Unique
    private static final int BOTTOM_ROW_OFFSET = 39;
    @Unique
    private static final int SELECTED_ITEM_NAME_OFFSET = 59;
    @Unique
    private static final String EXTRACT_AIR_BUBBLES = "Lnet/minecraft/client/gui/Hud;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V";

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private int tickCount;

    @Shadow
    protected abstract Player getCameraPlayer();

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

    @Unique
    private int mountBottomOffset() {
        return foodWithMount() ? BOTTOM_ROW_OFFSET + MountHud.ROW_HEIGHT : BOTTOM_ROW_OFFSET;
    }

    @ModifyVariable(method = "extractPlayerHealth", at = @At(value = "LOAD", ordinal = 0), name = "vehicleHearts")
    private int extractFoodWithMount(int vehicleHearts) {
        return ModConfig.showFoodWhileMounted ? 0 : vehicleHearts;
    }

    @ModifyArg(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = EXTRACT_AIR_BUBBLES), index = 2)
    private int keepAirBubblesOverMountHearts(int vehicleHearts) {
        return getVehicleMaxHearts(getPlayerVehicleWithHealth());
    }

    @ModifyArg(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = EXTRACT_AIR_BUBBLES), index = 3)
    private int raiseAirBubblesOverMountArmor(int yLineAir) {
        return MountHud.showsArmor(getPlayerVehicleWithHealth()) ? yLineAir - MountHud.ROW_HEIGHT : yLineAir;
    }

    @ModifyConstant(method = "extractVehicleHealth", constant = @Constant(intValue = BOTTOM_ROW_OFFSET))
    private int raiseVehicleHealth(int offset) {
        return foodWithMount() ? offset + MountHud.ROW_HEIGHT : offset;
    }

    @Inject(method = "extractVehicleHealth", at = @At("TAIL"))
    private void extractMountExtras(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        LivingEntity vehicle = getPlayerVehicleWithHealth();
        MountHud.warnOnLowHealth(vehicle);
        int hearts = getVehicleMaxHearts(vehicle);
        if (hearts == 0)
            return;
        int xRight = graphics.guiWidth() / 2 + MountHud.HOTBAR_HALF_WIDTH;
        int bottom = graphics.guiHeight() - mountBottomOffset();
        MountHud.drawLowHealthBlink(graphics, vehicle, hearts, xRight, bottom, tickCount);
        if (MountHud.showsArmor(vehicle))
            MountHud.drawArmor(graphics, vehicle, xRight, bottom - MountHud.ROW_HEIGHT * getVisibleVehicleHeartRows(hearts));
        MountHud.drawStats(graphics, getCameraPlayer(), vehicle);
    }

    @ModifyConstant(method = "extractSelectedItemName", constant = @Constant(intValue = SELECTED_ITEM_NAME_OFFSET))
    private int raiseSelectedItemName(int offset) {
        LivingEntity vehicle = getPlayerVehicleWithHealth();
        int hearts = getVehicleMaxHearts(vehicle);
        if (hearts == 0 || !minecraft.gameMode.canHurtPlayer())
            return offset;
        int rows = getVisibleVehicleHeartRows(hearts) + (MountHud.showsArmor(vehicle) ? 1 : 0);
        return Math.max(offset, mountBottomOffset() + MountHud.ROW_HEIGHT * rows);
    }

    @ModifyVariable(method = "nextContextualInfoState", at = @At("STORE"), ordinal = 1)
    private boolean showJumpBarOnlyWhileJumping(boolean canShowVehicleJumpInfo) {
        return ModConfig.experienceUntilJump ? canShowVehicleJumpInfo && willPrioritizeJumpInfo() : canShowVehicleJumpInfo;
    }
}
