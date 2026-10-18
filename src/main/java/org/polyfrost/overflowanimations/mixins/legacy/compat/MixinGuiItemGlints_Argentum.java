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

package org.polyfrost.overflowanimations.mixins.legacy.compat;

import dev.rdh.argentum.impl.render.gui.hud.item.GuiItemAtlas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.BufferUploader;
import net.minecraft.client.render.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;
import org.polyfrost.overflowanimations.OverflowAnimations;
import org.polyfrost.overflowanimations.config.OverflowAnimationsConfig;
import org.polyfrost.overflowanimations.legacy.tint.LegacyGlint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.rdh.argentum.impl.render.gui.hud.item.GuiItemGlints", remap = false)
public abstract class MixinGuiItemGlints_Argentum {
    @Shadow
    @Final
    private static VertexFormat FORMAT;

    @Shadow
    @Final
    private BufferBuilder buffer;

    @Shadow
    @Final
    private BufferUploader uploader;

    @Shadow
    private float[] data;

    @Shadow
    private int size;

    @Shadow
    protected abstract void vertex(float x, float y, float z, float maskU, float maskV, float glintU, float glintV);

    @Inject(method = "drawPass", at = @At("HEAD"), cancellable = true)
    private void overflowanimations$glintStyle(final int period, final float direction, final float rotation, final CallbackInfo ci) {
        if (!OverflowAnimations.isEnabled() || !OverflowAnimationsConfig.instance().oldEnchantmentGlint.isOld()) {
            return;
        }

        ci.cancel();
        GlStateManager.loadIdentity();
        LegacyGlint.color(LegacyGlint.GUI_ITEM_COLOR);
        GlStateManager.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_SRC_COLOR, GL11.GL_ONE);
        GlStateManager.translatef((float) (Minecraft.getTime() % period) / period, 0.0F, 0.0F);

        final float skew = direction > 0.0F ? 4.0F : -1.0F;
        final float texel = 1.0F / 256.0F;
        this.buffer.begin(GL11.GL_QUADS, FORMAT);
        for (int i = 0; i < this.size; i++) {
            final int at = i * 9;
            final float x = this.data[at];
            final float y = this.data[at + 1];
            final float z = this.data[at + 2];
            final float u = this.data[at + 3];
            final float v = this.data[at + 4];
            final float extent = GuiItemAtlas.UV_EXTENT;
            // Argentum pads the icon by 4px on each side
            final float x0 = x - 4.0F;
            final float y0 = y - 4.0F;
            final float x1 = x + 20.0F;
            final float y1 = y + 20.0F;
            final float left = x - 2.0F;
            final float top = y - 2.0F;
            this.vertex(x0, y1, z, u, v, (x0 - left + (y1 - top) * skew) * texel, (y1 - top) * texel);
            this.vertex(x1, y1, z, u + extent, v, (x1 - left + (y1 - top) * skew) * texel, (y1 - top) * texel);
            this.vertex(x1, y0, z, u + extent, v + extent, (x1 - left + (y0 - top) * skew) * texel, (y0 - top) * texel);
            this.vertex(x0, y0, z, u, v + extent, (x0 - left + (y0 - top) * skew) * texel, (y0 - top) * texel);
        }
        this.buffer.end();
        this.uploader.end(this.buffer);
    }
}
