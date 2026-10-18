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

package org.polyfrost.overflowanimations.mixins.firstperson;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleManager;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.world.HitResult;
import org.polyfrost.overflowanimations.OverflowAnimations;
import org.polyfrost.overflowanimations.config.OverflowAnimationsConfig;
import org.polyfrost.overflowanimations.firstperson.LegacyFirstPerson;
import org.polyfrost.overflowanimations.util.enums.MissPenaltySetting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft_Swinging {
    @Shadow
    public LocalClientPlayerEntity player;

    @Shadow
    public ClientWorld world;

    @Shadow
    public HitResult crosshairTarget;

    @Shadow
    public ParticleManager particleManager;

    @Shadow
    public ClientPlayerInteractionManager interactionManager;

    @Shadow
    private int attackCooldown;

    @Unique
    private Block overflowanimations$targetedBlock() {
        if (this.crosshairTarget == null || this.crosshairTarget.type != HitResult.Type.BLOCK) {
            return null;
        }
        final Block block = this.world.getBlockState(this.crosshairTarget.getPos()).getBlock();
        return block.getMaterial() == Material.AIR ? null : block;
    }

    @Inject(method = "handleMouseDown", at = @At("HEAD"))
    private void overflowanimations$usageSwinging(final boolean holdingAttack, final CallbackInfo ci) {
        final OverflowAnimationsConfig config = OverflowAnimationsConfig.instance();
        if (!OverflowAnimations.isEnabled() || !config.punchingDuringUsage.isOld() || !holdingAttack || this.attackCooldown > 0 || !this.player.hasItemInUse() || this.overflowanimations$targetedBlock() == null) {
            return;
        }
        if (config.punchingDuringUsageParticles.isOld()) {
            this.particleManager.addBlockMiningParticles(this.crosshairTarget.getPos(), this.crosshairTarget.face);
        }
        LegacyFirstPerson.fakeSwing(this.player);
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void overflowanimations$missPenalty(final CallbackInfo ci) {
        final MissPenaltySetting behavior = OverflowAnimationsConfig.instance().missPenaltyBehavior;
        if (!OverflowAnimations.isEnabled() || behavior == MissPenaltySetting.NOTHING || this.attackCooldown <= 0 || this.attackCooldown > 10 || this.crosshairTarget == null || !this.interactionManager.hasAttackCooldown()) {
            return;
        }
        if (this.crosshairTarget.type == HitResult.Type.BLOCK && this.overflowanimations$targetedBlock() == null) {
            return;
        }
        LegacyFirstPerson.fakeSwing(this.player);
        if (behavior == MissPenaltySetting.SWING_AND_PARTICLES && this.crosshairTarget.type == HitResult.Type.ENTITY) {
            LegacyFirstPerson.fakeAttackParticles(this.player, this.crosshairTarget.entity);
        }
    }

    @ModifyExpressionValue(method = "doUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ClientPlayerInteractionManager;isMiningBlock()Z"))
    private boolean overflowanimations$blockHitWhileMining(final boolean original) {
        if (!original || !OverflowAnimations.isEnabled() || !OverflowAnimationsConfig.instance().blockHittingAnimation.isOld()) {
            return original;
        }
        final ItemStack stack = this.player.getItemInHand();
        return stack == null || (stack.getUseAction() == UseAction.NONE && !(stack.getItem() instanceof BlockItem));
    }
}
