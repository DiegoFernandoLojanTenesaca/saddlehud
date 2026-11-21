package com.lautish.saddlehud.display;

import com.lautish.saddlehud.init.ModConfig;
import java.util.Locale;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class MountHud {

    public static final int ROW_HEIGHT = 10;
    public static final int HOTBAR_HALF_WIDTH = 91;
    private static final Identifier ARMOR_FULL = Identifier.withDefaultNamespace("hud/armor_full");
    private static final Identifier ARMOR_HALF = Identifier.withDefaultNamespace("hud/armor_half");
    private static final Identifier ARMOR_EMPTY = Identifier.withDefaultNamespace("hud/armor_empty");
    private static final Identifier HEART_BLINK = Identifier.withDefaultNamespace("hud/heart/container_blinking");
    private static final int ICON_SIZE = 9;
    private static final int ICON_STEP = 8;
    private static final int ICONS_PER_ROW = 10;
    private static final float LOW_HEALTH_RATIO = 0.3F;
    private static final int BLINK_TICKS = 4;
    private static final float WARNING_PITCH = 0.6F;
    private static final double SPEED_TO_BLOCKS_PER_SECOND = 42.16;
    private static final double[] JUMP_HEIGHT_POLYNOMIAL = {-0.343930367, 2.128599134, 3.689713992, -0.1817584952};
    private static final int STATS_GAP = 6;
    private static final int ATTACK_INDICATOR_WIDTH = 22;
    private static final int STATS_BOTTOM_OFFSET = 20;
    private static final int SCREEN_MARGIN = 2;
    private static boolean warned;

    private MountHud() {
    }

    public static boolean showsArmor(LivingEntity vehicle) {
        return ModConfig.showMountArmor && vehicle != null && vehicle.getArmorValue() > 0;
    }

    public static void drawArmor(GuiGraphicsExtractor graphics, LivingEntity vehicle, int xRight, int y) {
        int armor = vehicle.getArmorValue();
        for (int i = 0; i < ICONS_PER_ROW; i++) {
            Identifier sprite = i * 2 + 1 < armor ? ARMOR_FULL : i * 2 + 1 == armor ? ARMOR_HALF : ARMOR_EMPTY;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, xRight - i * ICON_STEP - ICON_SIZE, y, ICON_SIZE, ICON_SIZE);
        }
    }

    public static void drawLowHealthBlink(GuiGraphicsExtractor graphics, LivingEntity vehicle, int hearts, int xRight, int yBottom, int ticks) {
        if (!ModConfig.lowHealthWarning || !lowHealth(vehicle) || ticks / BLINK_TICKS % 2 == 0)
            return;
        for (int first = 0, y = yBottom; first < hearts; first += ICONS_PER_ROW, y -= ROW_HEIGHT)
            for (int i = 0; i < Math.min(ICONS_PER_ROW, hearts - first); i++)
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_BLINK, xRight - i * ICON_STEP - ICON_SIZE, y, ICON_SIZE, ICON_SIZE);
    }

    public static void warnOnLowHealth(LivingEntity vehicle) {
        boolean low = ModConfig.lowHealthWarning && vehicle != null && lowHealth(vehicle);
        if (low && !warned)
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BELL, WARNING_PITCH));
        warned = low;
    }

    public static void drawStats(GuiGraphicsExtractor graphics, Player player, LivingEntity vehicle) {
        if (!ModConfig.showMountStats || player == null || !(vehicle instanceof AbstractHorse horse))
            return;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Component speed = Component.translatable("saddlehud.hud.speed", oneDecimal(horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_TO_BLOCKS_PER_SECOND));
        Component jump = Component.translatable("saddlehud.hud.jump", oneDecimal(jumpHeight(horse.getAttributeValue(Attributes.JUMP_STRENGTH))));
        int gap = STATS_GAP + (minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR ? ATTACK_INDICATOR_WIDTH : 0);
        int width = Math.max(font.width(speed), font.width(jump));
        int center = graphics.guiWidth() / 2;
        int x = player.getMainArm() == HumanoidArm.RIGHT
                ? Math.min(center + HOTBAR_HALF_WIDTH + gap, graphics.guiWidth() - width - SCREEN_MARGIN)
                : Math.max(center - HOTBAR_HALF_WIDTH - gap - width, SCREEN_MARGIN);
        int y = graphics.guiHeight() - STATS_BOTTOM_OFFSET;
        graphics.text(font, speed, x, y, CommonColors.WHITE, true);
        graphics.text(font, jump, x, y + ROW_HEIGHT, CommonColors.WHITE, true);
    }

    public static double jumpHeight(double strength) {
        double height = 0;
        double power = 1;
        for (double coefficient : JUMP_HEIGHT_POLYNOMIAL) {
            height += coefficient * power;
            power *= strength;
        }
        return height;
    }

    private static boolean lowHealth(LivingEntity vehicle) {
        return vehicle.isAlive() && vehicle.getHealth() <= vehicle.getMaxHealth() * LOW_HEALTH_RATIO;
    }

    private static String oneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
