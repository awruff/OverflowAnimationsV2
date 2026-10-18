/**
 * OverflowAnimations
 * The all-you-could-want legacy animations mod for modern minecraft versions.
 * Brings back animations from the 1.7/1.8 era and more.
 * <p>
 * Copyright (C) 2024-2027 lowercasebtw
 * Copyright (C) 2024-2027 mixces
 * Copyright (C) 2024-2027 Contributors to the project retain their copyright
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * "MINECRAFT" LINKING EXCEPTION TO THE GPL
 */

package org.polyfrost.overflowanimations.legacy.firstperson;

import net.minecraft.block.Block;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.PumpkinBlock;
import net.minecraft.block.SnowLayerBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.model.block.BakedModel;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobType;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import org.polyfrost.overflowanimations.OverflowAnimations;
import org.polyfrost.overflowanimations.config.OverflowAnimationsConfig;
import org.polyfrost.overflowanimations.handler.compatibility.ModsKt;
import org.polyfrost.overflowanimations.handler.compatibility.OverflowParticlesCompat;
import org.polyfrost.overflowanimations.mixins.legacy.firstperson.MixinLivingEntity_SwingAccessor;

public final class LegacyFirstPerson {
    private LegacyFirstPerson() {
    }

    public static void fakeSwing(LivingEntity entity) {
        final int duration = ((MixinLivingEntity_SwingAccessor) entity).overflowanimations$getArmSwingDuration();
        if (!entity.armSwinging || entity.armSwingingTicks >= duration / 2 || entity.armSwingingTicks < 0) {
            entity.armSwingingTicks = -1;
            entity.armSwinging = true;
        }
    }

    public static void fakeAttackParticles(PlayerEntity player, Entity target) {
        if (player.isSpectator() || !target.canBePunched()) {
            return;
        }
        final boolean living = target instanceof LivingEntity;
        if (player.fallDistance > 0.0F && !player.onGround && !player.isClimbing() && !player.isInWater() && !player.hasStatusEffect(StatusEffect.BLINDNESS) && player.vehicle == null && living) {
            player.addCritParticles(target);
        }
        if (EnchantmentHelper.modifyDamage(player.getItemInHand(), living ? ((LivingEntity) target).getMobType() : MobType.UNDEFINED) > 0.0F) {
            player.addEnchantedCritParticles(target);
        }
        if (ModsKt.HAS_OVERFLOW_PARTICLES) {
            OverflowParticlesCompat.postAttack(player, target);
        }
    }

    private static boolean isThinBlock(Block block) {
        return block instanceof CarpetBlock || block instanceof SnowLayerBlock || block instanceof DaylightDetectorBlock || block instanceof TrapdoorBlock;
    }

    private static boolean isBlocking(ItemStack stack) {
        final LocalClientPlayerEntity player = Minecraft.getInstance().player;
        return player != null && player.getItemUseTimer() > 0 && stack.getUseAction() == UseAction.BLOCK;
    }

    public static boolean applyFirstPersonTransform(ItemStack stack, BakedModel model) {
        if (!OverflowAnimations.isEnabled()) {
            return false;
        }
        final OverflowAnimationsConfig config = OverflowAnimationsConfig.instance();
        final Item item = stack.getItem();
        final Block block = item instanceof BlockItem ? ((BlockItem) item).getBlock() : null;
        if (model.isCustomRenderer()) {
            if (config.itemTransformations.isOld() && (block instanceof ChestBlock || block instanceof EnderChestBlock)) {
                GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            }
            return false;
        }

        if (model.isGui3d()) {
            if (block == null || !config.itemTransformations.isOld()) {
                return false;
            }
            final boolean frontOnSouth = block instanceof FurnaceBlock || block instanceof DispenserBlock || block instanceof PumpkinBlock;
            GlStateManager.rotatef(frontOnSouth ? 270.0F : 90.0F, 0.0F, 1.0F, 0.0F);
            if (isThinBlock(block)) {
                if (block instanceof TrapdoorBlock) {
                    GlStateManager.translatef(0.0F, 6.5F / 32.0F, 0.0F);
                }
                return true;
            }
            return false;
        }

        if (item.isFlippedWhenWielded()) {
            if (!config.rodPosition.isOld()) {
                return false;
            }
            GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
        } else if (!(isBlocking(stack) ? config.blockAnimation : config.firstPersonItemPositions).isOld()) {
            return false;
        }
        applyLegacyItemTransform();
        return true;
    }

    private static void applyLegacyItemTransform() {
        GlStateManager.scalef(0.5F, 0.5F, 0.5F);
        GlStateManager.translatef(0.0F, -0.3F, 0.0F);
        GlStateManager.scalef(1.5F, 1.5F, 1.5F);
        GlStateManager.rotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translatef(-0.9375F, -0.0625F, 0.0F);
        GlStateManager.translatef(0.5F, 0.5F, 0.25F);
        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(0.0F, 0.0F, 0.28125F);
        GlStateManager.scalef(2.0F, 2.0F, 2.0F);
    }
}
