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

package org.polyfrost.overflowanimations.mixins.legacy.firstperson;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.ItemInHandRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import org.polyfrost.overflowanimations.OverflowAnimations;
import org.polyfrost.overflowanimations.config.OverflowAnimationsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer_ItemPositions {
    @WrapOperation(method = "applyBowNocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;scalef(FFF)V"))
    private void overflowanimations$legacyBowPull(final float x, final float y, final float z, final Operation<Void> original) {
        final boolean legacy = OverflowAnimations.isEnabled() && OverflowAnimationsConfig.instance().bowPullback.isOld();
        if (legacy) {
            GlStateManager.rotatef(-335.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotatef(-50.0F, 0.0F, 1.0F, 0.0F);
        }
        original.call(x, y, z);
        if (legacy) {
            GlStateManager.rotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotatef(335.0F, 0.0F, 0.0F, 1.0F);
        }
    }
}
