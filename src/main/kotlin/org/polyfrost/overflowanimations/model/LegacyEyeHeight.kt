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

package org.polyfrost.overflowanimations.model

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.living.player.PlayerEntity
import org.polyfrost.overflowanimations.OverflowAnimations
import org.polyfrost.overflowanimations.config.OverflowAnimationsConfig

object LegacyEyeHeight {
    private const val STANDING_EYE_HEIGHT = 1.62F
    private const val OLD_SNEAK_OFFSET = 0.08F

    private var lastEyeHeight = Float.NaN
    private var eyeHeight = 0.0F
    private var lastCamera: Entity? = null
    private var lastSleeping = false

    private fun isSmooth() = OverflowAnimations.isEnabled() && OverflowAnimationsConfig.instance().smoothSneaking.isOld

    @JvmStatic
    fun tick(camera: Entity) {
        val target = camera.eyeHeight
        val sleeping = camera is PlayerEntity && camera.isSleeping
        if (lastEyeHeight.isNaN() || camera !== lastCamera || sleeping != lastSleeping) {
            eyeHeight = target
        }
        lastCamera = camera
        lastSleeping = sleeping

        lastEyeHeight = eyeHeight
        if (isSmooth() && OverflowAnimationsConfig.instance().longerUnsneak.isOld && target > eyeHeight) {
            eyeHeight += (target - eyeHeight) * 0.6F
        } else {
            eyeHeight = target
        }
    }

    @JvmStatic
    fun get(vanillaEyeHeight: Float, tickDelta: Float) =
        if (isSmooth()) lastEyeHeight + (eyeHeight - lastEyeHeight) * tickDelta else vanillaEyeHeight

    @JvmStatic
    fun isOldSneakModel() = isSmooth()

    @JvmStatic
    fun modelOffset(entity: Entity, tickDelta: Float): Float {
        val minecraft = Minecraft.getInstance()
        if (!isOldSneakModel() || entity !== minecraft.player || minecraft.camera !== entity || minecraft.player.isSleeping) {
            return 0.0F
        }

        return (STANDING_EYE_HEIGHT - get(STANDING_EYE_HEIGHT, tickDelta)).coerceIn(0.0F, OLD_SNEAK_OFFSET)
    }
}
