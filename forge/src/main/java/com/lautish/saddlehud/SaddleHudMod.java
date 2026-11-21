package com.lautish.saddlehud;

import com.jahirtrap.configlib.TXFConfig;
import com.lautish.saddlehud.init.ModConfig;
import net.minecraftforge.fml.common.Mod;

@Mod(SaddleHudMod.MODID)
public class SaddleHudMod {

    public static final String MODID = "saddlehud";

    public SaddleHudMod() {
        TXFConfig.init(MODID, ModConfig.class);
    }
}
