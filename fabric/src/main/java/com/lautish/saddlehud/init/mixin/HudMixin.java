package com.lautish.saddlehud.init.mixin;

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Unique
    private static final int BOTTOM_ROW_OFFSET = 39;
    @Unique
    private static final int HOTBAR_HALF_WIDTH = 91;
    @Unique
    private static final int ROW_HEIGHT = 10;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract LivingEntity getPlayerVehicleWithHealth();

    @Shadow
    protected abstract int getVehicleMaxHearts(LivingEntity vehicle);

    @Shadow
    protected abstract void extractFood(GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight);

    @Unique
    private boolean foodWithMount() {
        return ModConfig.showFoodWhileMounted && getVehicleMaxHearts(getPlayerVehicleWithHealth()) > 0;
    }

    @Inject(method = "extractPlayerHealth", at = @At("TAIL"))
    private void extractFoodWithMount(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        Player player = getCameraPlayer();
        if (player != null && foodWithMount())
            extractFood(graphics, player, graphics.guiHeight() - BOTTOM_ROW_OFFSET, graphics.guiWidth() / 2 + HOTBAR_HALF_WIDTH);
    }

    @ModifyArg(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V"), index = 3)
    private int raiseAirBubbles(int yLineAir) {
        return foodWithMount() ? yLineAir - ROW_HEIGHT : yLineAir;
    }

    @ModifyConstant(method = "extractVehicleHealth", constant = @Constant(intValue = BOTTOM_ROW_OFFSET))
    private int raiseVehicleHealth(int offset) {
        return foodWithMount() && minecraft.gameMode.canHurtPlayer() ? offset + ROW_HEIGHT : offset;
    }
}
