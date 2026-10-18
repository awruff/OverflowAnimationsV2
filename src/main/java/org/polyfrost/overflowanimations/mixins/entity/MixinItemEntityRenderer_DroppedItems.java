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

package org.polyfrost.overflowanimations.mixins.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.model.block.BakedModel;
import net.minecraft.entity.ItemEntity;
import org.polyfrost.overflowanimations.OverflowAnimations;
import org.polyfrost.overflowanimations.config.OverflowAnimationsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer_DroppedItems extends EntityRenderer<ItemEntity> {
    protected MixinItemEntityRenderer_DroppedItems(final EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @ModifyArg(method = "applyItemBobbing", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V"), index = 0)
    private float overflowanimations$droppedItemDirection(final float angle, @Local(argsOnly = true) final ItemEntity entity, @Local(argsOnly = true) final BakedModel model) {
        if (!OverflowAnimations.isEnabled()) {
            return angle;
        }
        return switch (OverflowAnimationsConfig.instance().droppedItemDirection) {
            case STILL_BOB -> entity.bobOffset * (180.0F / (float) Math.PI);
            case FACE_PLAYER -> model.isGui3d() ? angle : 180.0F - this.dispatcher.cameraYaw;
            case DEFAULT -> angle;
        };
    }
}
