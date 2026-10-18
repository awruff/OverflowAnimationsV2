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

package org.polyfrost.overflowanimations.firstperson

import net.minecraft.block.Block
import net.minecraft.block.CarpetBlock
import net.minecraft.block.ChestBlock
import net.minecraft.block.DaylightDetectorBlock
import net.minecraft.block.DispenserBlock
import net.minecraft.block.EnderChestBlock
import net.minecraft.block.FurnaceBlock
import net.minecraft.block.PumpkinBlock
import net.minecraft.block.SnowLayerBlock
import net.minecraft.block.TrapdoorBlock
import net.minecraft.client.Minecraft
import net.minecraft.client.render.model.block.BakedModel
import net.minecraft.client.render.platform.GlStateManager
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.MobType
import net.minecraft.entity.living.LivingEntity
import net.minecraft.entity.living.effect.StatusEffect
import net.minecraft.entity.living.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.UseAction
import org.polyfrost.overflowanimations.OverflowAnimations
import org.polyfrost.overflowanimations.config.OverflowAnimationsConfig
import org.polyfrost.overflowanimations.handler.compatibility.HAS_OVERFLOW_PARTICLES
import org.polyfrost.overflowanimations.handler.compatibility.OverflowParticlesCompat
import org.polyfrost.overflowanimations.mixins.firstperson.MixinLivingEntity_SwingAccessor

object LegacyFirstPerson {
    @JvmStatic
    fun fakeSwing(entity: LivingEntity) {
        val duration = (entity as MixinLivingEntity_SwingAccessor).`overflowanimations$getArmSwingDuration`()
        if (!entity.armSwinging || entity.armSwingingTicks >= duration / 2 || entity.armSwingingTicks < 0) {
            entity.armSwingingTicks = -1
            entity.armSwinging = true
        }
    }

    @JvmStatic
    fun fakeAttackParticles(player: PlayerEntity, target: Entity) {
        if (player.isSpectator || !target.canBePunched()) {
            return
        }
        val living = target is LivingEntity
        if (player.fallDistance > 0.0F && !player.onGround && !player.isClimbing && !player.isInWater && !player.hasStatusEffect(StatusEffect.BLINDNESS) && player.vehicle == null && living) {
            player.addCritParticles(target)
        }
        if (EnchantmentHelper.modifyDamage(player.itemInHand, if (target is LivingEntity) target.mobType else MobType.UNDEFINED) > 0.0F) {
            player.addEnchantedCritParticles(target)
        }
        if (HAS_OVERFLOW_PARTICLES) {
            OverflowParticlesCompat.postAttack(player, target)
        }
    }

    private fun isThinBlock(block: Block) =
        block is CarpetBlock || block is SnowLayerBlock || block is DaylightDetectorBlock || block is TrapdoorBlock

    private fun isBlocking(stack: ItemStack): Boolean {
        val player = Minecraft.getInstance().player
        return player != null && player.itemUseTimer > 0 && stack.useAction == UseAction.BLOCK
    }

    @JvmStatic
    fun applyFirstPersonTransform(stack: ItemStack, model: BakedModel): Boolean {
        if (!OverflowAnimations.isEnabled()) {
            return false
        }
        val config = OverflowAnimationsConfig.instance()
        val item = stack.item
        val block = (item as? BlockItem)?.block
        if (model.isCustomRenderer) {
            if (config.itemTransformations.isOld && (block is ChestBlock || block is EnderChestBlock)) {
                GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F)
            }
            return false
        }

        if (model.isGui3d) {
            if (block == null || !config.itemTransformations.isOld) {
                return false
            }
            val frontOnSouth = block is FurnaceBlock || block is DispenserBlock || block is PumpkinBlock
            GlStateManager.rotatef(if (frontOnSouth) 270.0F else 90.0F, 0.0F, 1.0F, 0.0F)
            if (isThinBlock(block)) {
                if (block is TrapdoorBlock) {
                    GlStateManager.translatef(0.0F, 6.5F / 32.0F, 0.0F)
                }
                return true
            }
            return false
        }

        if (item.isFlippedWhenWielded) {
            if (!config.rodPosition.isOld) {
                return false
            }
            GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F)
        } else if (!(if (isBlocking(stack)) config.blockAnimation else config.firstPersonItemPositions).isOld) {
            return false
        }
        applyLegacyItemTransform()
        return true
    }

    private fun applyLegacyItemTransform() {
        GlStateManager.scalef(0.5F, 0.5F, 0.5F)
        GlStateManager.translatef(0.0F, -0.3F, 0.0F)
        GlStateManager.scalef(1.5F, 1.5F, 1.5F)
        GlStateManager.rotatef(50.0F, 0.0F, 1.0F, 0.0F)
        GlStateManager.rotatef(335.0F, 0.0F, 0.0F, 1.0F)
        GlStateManager.translatef(-0.9375F, -0.0625F, 0.0F)
        GlStateManager.translatef(0.5F, 0.5F, 0.25F)
        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F)
        GlStateManager.translatef(0.0F, 0.0F, 0.28125F)
        GlStateManager.scalef(2.0F, 2.0F, 2.0F)
    }
}
