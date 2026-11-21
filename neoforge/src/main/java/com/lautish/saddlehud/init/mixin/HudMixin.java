package com.lautish.saddlehud.init.mixin;

import com.lautish.saddlehud.display.MountHud;
import com.lautish.saddlehud.init.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Shadow
    public int rightHeight;

    @Shadow
    private int tickCount;

    @Unique
    private int vehicleBaseHeight;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Shadow
    protected abstract int getVehicleMaxHearts(LivingEntity vehicle);

    @Shadow
    protected abstract boolean willPrioritizeJumpInfo();

    @ModifyVariable(method = "extractFoodLevel", at = @At("STORE"), ordinal = 0)
    private int extractFoodWithMount(int vehicleHearts) {
        return ModConfig.showFoodWhileMounted ? 0 : vehicleHearts;
    }

    @Inject(method = "extractVehicleHealth", at = @At("HEAD"))
    private void rememberVehicleBase(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        vehicleBaseHeight = rightHeight;
    }

    @Inject(method = "extractVehicleHealth", at = @At("TAIL"))
    private void extractMountExtras(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        LivingEntity vehicle = getPlayerVehicleWithHealth();
        MountHud.warnOnLowHealth(vehicle);
        int hearts = getVehicleMaxHearts(vehicle);
        if (hearts == 0)
            return;
        int xRight = graphics.guiWidth() / 2 + MountHud.HOTBAR_HALF_WIDTH;
        MountHud.drawLowHealthBlink(graphics, vehicle, hearts, xRight, graphics.guiHeight() - vehicleBaseHeight, tickCount);
        if (MountHud.showsArmor(vehicle)) {
            MountHud.drawArmor(graphics, vehicle, xRight, graphics.guiHeight() - rightHeight);
            rightHeight += MountHud.ROW_HEIGHT;
        }
        MountHud.drawStats(graphics, getCameraPlayer(), vehicle);
    }

    @ModifyVariable(method = "nextContextualInfoState", at = @At("STORE"), ordinal = 1)
    private boolean showJumpBarOnlyWhileJumping(boolean canShowVehicleJumpInfo) {
        return ModConfig.experienceUntilJump ? canShowVehicleJumpInfo && willPrioritizeJumpInfo() : canShowVehicleJumpInfo;
    }
}
