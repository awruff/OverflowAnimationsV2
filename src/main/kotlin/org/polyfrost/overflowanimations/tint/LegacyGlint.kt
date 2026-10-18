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

package org.polyfrost.overflowanimations.tint

import net.minecraft.client.Minecraft
import net.minecraft.client.render.model.block.BakedModel
import net.minecraft.client.render.model.block.BakedQuad
import net.minecraft.client.render.model.block.MiningQuad
import net.minecraft.client.render.platform.GlStateManager
import net.minecraft.client.render.texture.TextureAtlasSprite
import net.minecraft.client.render.vertex.DefaultVertexFormat
import net.minecraft.client.render.vertex.Tesselator
import net.minecraft.client.resource.model.ModelTransformations
import net.minecraft.util.math.Direction
import org.lwjgl.opengl.GL11
import java.util.WeakHashMap

object LegacyGlint {
    const val ITEM_COLOR = 0xFF61309B.toInt()
    const val GUI_ITEM_COLOR = 0xFF8040CC.toInt()

    private val ITEM_SPACE = object : TextureAtlasSprite("overflowanimations:legacy_glint") {
        override fun getU(u: Double) = (-u / 16.0).toFloat()

        override fun getV(v: Double) = (v / 16.0).toFloat()
    }

    private val ITEM_SPACE_MODELS = WeakHashMap<BakedModel, BakedModel>()

    @JvmField
    var renderingGui = false

    @JvmStatic
    fun itemSpace(model: BakedModel): BakedModel =
        ITEM_SPACE_MODELS.getOrPut(model) { transform(model) { MiningQuad(it, ITEM_SPACE) } }

    private fun transform(model: BakedModel, mapper: (BakedQuad) -> BakedQuad?): BakedModel {
        val quads = (Direction.entries.flatMap { model.getQuads(it) } + model.quads).mapNotNull(mapper)
        val ambientOcclusion = model.useAmbientOcclusion()
        val gui3d = model.isGui3d
        val customRenderer = model.isCustomRenderer
        val particle = model.particleIcon
        val transformations = model.transformations
        return object : BakedModel {
            override fun getQuads(face: Direction): List<BakedQuad> = emptyList()

            override fun getQuads(): List<BakedQuad> = quads

            override fun useAmbientOcclusion() = ambientOcclusion

            override fun isGui3d() = gui3d

            override fun isCustomRenderer() = customRenderer

            override fun getParticleIcon(): TextureAtlasSprite = particle

            override fun getTransformations(): ModelTransformations = transformations
        }
    }

    @JvmStatic
    fun setupItemTexturing(layer: Int) {
        val period = if (layer == 0) 3000L else 4873L
        val offset = (Minecraft.getTime() % period) / period.toFloat() * 8.0F
        GlStateManager.scalef(0.125F, 0.125F, 0.125F)
        GlStateManager.translatef(if (layer == 0) offset else -offset, 0.0F, 0.0F)
        GlStateManager.rotatef(if (layer == 0) -50.0F else 10.0F, 0.0F, 0.0F, 1.0F)
    }

    @JvmStatic
    fun color(argb: Int) {
        GlStateManager.color4f((argb shr 16 and 255) / 255.0F, (argb shr 8 and 255) / 255.0F, (argb and 255) / 255.0F, 1.0F)
    }

    @JvmStatic
    fun renderGuiGlint(x: Int, y: Int, z: Float) {
        if (!GL11.glIsEnabled(GL11.GL_DEPTH_TEST)) {
            return
        }

        GlStateManager.depthFunc(GL11.GL_GEQUAL)
        GlStateManager.disableLighting()
        GlStateManager.depthMask(false)
        GlStateManager.enableAlphaTest()
        GlStateManager.enableBlend()
        val tesselator = Tesselator.getInstance()
        val buffer = tesselator.buffer
        val left = (x - 2).toDouble()
        val top = (y - 2).toDouble()
        val size = 20
        val texel = 1.0F / 256.0F
        for (layer in 0..1) {
            color(GUI_ITEM_COLOR)
            GlStateManager.blendFuncSeparate(GL11.GL_DST_ALPHA, GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_ZERO)
            val period = 3000.0F + layer * 1873
            val offset = (Minecraft.getTime() % period.toLong()) / period * 256.0F
            val skew = if (layer == 0) 4.0F else -1.0F
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX)
            buffer.vertex(left, top + size, z.toDouble()).texture(((offset + size * skew) * texel).toDouble(), (size * texel).toDouble()).nextVertex()
            buffer.vertex(left + size, top + size, z.toDouble()).texture(((offset + size + size * skew) * texel).toDouble(), (size * texel).toDouble()).nextVertex()
            buffer.vertex(left + size, top, z.toDouble()).texture(((offset + size) * texel).toDouble(), 0.0).nextVertex()
            buffer.vertex(left, top, z.toDouble()).texture((offset * texel).toDouble(), 0.0).nextVertex()
            tesselator.end()
        }

        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F)
        GlStateManager.disableAlphaTest()
        GlStateManager.depthMask(true)
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
    }
}
