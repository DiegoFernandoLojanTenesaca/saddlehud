package com.lautish.saddlehud.init.mixin;

import com.lautish.saddlehud.init.ModConfig;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Hud.class)
public abstract class HudMixin {

    @ModifyVariable(method = "extractFoodLevel", at = @At("STORE"), ordinal = 0)
    private int extractFoodWithMount(int vehicleHearts) {
        return ModConfig.showFoodWhileMounted ? 0 : vehicleHearts;
    }
}
