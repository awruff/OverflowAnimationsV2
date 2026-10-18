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
import net.minecraft.client.render.platform.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.living.LivingEntity
import org.lwjgl.opengl.GL11
import org.polyfrost.overflowanimations.OverflowAnimations
import org.polyfrost.overflowanimations.compat.Argentum
import org.polyfrost.overflowanimations.config.OverflowAnimationsConfig
import java.nio.FloatBuffer

object LegacyDamageTint {
    @JvmStatic
    fun config() = OverflowAnimationsConfig.instance()

    private fun color(brightness: Float) = 0x99000000.toInt() or ((brightness * 255.0F).toInt() shl 16)

    @JvmStatic
    fun isHurt(entity: Entity) = entity is LivingEntity && (entity.damagedTimer > 0 || entity.deathTicks > 0)

    @JvmStatic
    fun flatTint() = OverflowAnimations.isEnabled() && config().hitColorBrightness.isOld && !Argentum.capturingEntity()

    @JvmStatic
    fun usesFlatPass(entity: Entity) = flatTint() && isHurt(entity)

    @JvmStatic
    fun putCombinerTint(buffer: FloatBuffer) {
        if (!OverflowAnimations.isEnabled() || !config().hitColorBrightness.isOld) {
            return
        }

        val color = color(1.0F)
        buffer.clear()
        buffer.put((color shr 16 and 0xFF) / 255.0F).put((color shr 8 and 0xFF) / 255.0F).put((color and 0xFF) / 255.0F).put(1.0F - (color ushr 24) / 255.0F)
    }

    @JvmStatic
    fun beginFlatPass(entity: Entity, tickDelta: Float): Boolean {
        if (!usesFlatPass(entity)) {
            return false
        }

        val color = color(entity.getBrightness(tickDelta))
        Minecraft.getInstance().gameRenderer.disableLightMap()
        GlStateManager.disableTexture()
        GlStateManager.disableAlphaTest()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthFunc(GL11.GL_EQUAL)
        GlStateManager.color4f((color shr 16 and 0xFF) / 255.0F, (color shr 8 and 0xFF) / 255.0F, (color and 0xFF) / 255.0F, 1.0F - (color ushr 24) / 255.0F)
        return true
    }

    @JvmStatic
    fun endFlatPass() {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F)
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        GlStateManager.disableBlend()
        GlStateManager.enableAlphaTest()
        GlStateManager.enableTexture()
        Minecraft.getInstance().gameRenderer.enableLightMap()
    }
}
