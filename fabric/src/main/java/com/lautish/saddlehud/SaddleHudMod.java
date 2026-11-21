package com.lautish.saddlehud;

import com.jahirtrap.configlib.TXFConfig;
import com.lautish.saddlehud.init.ModConfig;
import net.fabricmc.api.ModInitializer;

public class SaddleHudMod implements ModInitializer {

    public static final String MODID = "saddlehud";

    @Override
    public void onInitialize() {
        TXFConfig.init(MODID, ModConfig.class);
    }
}
