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

package org.polyfrost.overflowanimations.config

import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.Properties
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.oneconfig.api.ui.v1.OneConfigUI
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ModConfigRoute
import org.polyfrost.overflowanimations.OverflowAnimations
import org.polyfrost.overflowanimations.OverflowAnimationsConstants
import org.polyfrost.overflowanimations.util.enums.DroppedItemDirectionSetting
import org.polyfrost.overflowanimations.util.enums.LegacyVersion
import org.polyfrost.overflowanimations.util.enums.MissPenaltySetting

class OverflowAnimationsConfig private constructor() :
    Config(ID, "/assets/overflowanimations/icon.svg", "OverflowAnimations", Category.VISUALS) {

    // Position
    @JvmField var firstPersonItemPositions = LegacyVersion.V1_7
    @JvmField var thirdPersonItemPositions = LegacyVersion.V1_7
    @JvmField var itemTransformations = LegacyVersion.V1_7
    @JvmField var bowPullback = LegacyVersion.V1_7
    @JvmField var blockAnimation = LegacyVersion.V1_7
    @JvmField var rodPosition = LegacyVersion.V1_7
    @JvmField var thirdPersonBlockAnimation = LegacyVersion.V1_7

    // Interaction
    @JvmField var consumeAnimation = LegacyVersion.V1_7
    @JvmField var blockHittingAnimation = LegacyVersion.V1_7
    @JvmField var smoothSneaking = LegacyVersion.V1_7
    @JvmField var longerUnsneak = LegacyVersion.V1_7
    @JvmField var punchingDuringUsage = LegacyVersion.V1_7
    @JvmField var punchingDuringUsageParticles = LegacyVersion.V1_7
    @JvmField var itemSwitchingAnimation = LegacyVersion.V1_7
    @JvmField var oldEnchantmentGlint = LegacyVersion.V1_7
    @JvmField var hitColorArmor = LegacyVersion.V1_7
    @JvmField var hitColorArmorGlint = LegacyVersion.V1_7
    @JvmField var hitColorBrightness = LegacyVersion.V1_7
    @JvmField var missPenaltyBehavior = MissPenaltySetting.SWING_AND_PARTICLES

    // Item Visual
    @JvmField var droppedItemDirection = DroppedItemDirectionSetting.DEFAULT

    // Hud
    @JvmField var healthBarFlashing = LegacyVersion.V1_7

    override fun makeTree(): Tree {
        val tree = Tree.tree(ID)
        val toggle = Properties.functional({ OverflowAnimations.isEnabled() }, { enabled: Boolean ->
            // the utility config is where this is kept, so the copy saved alongside the options is not read back
            if (loaded) {
                OverflowAnimations.enabled = enabled
                OverflowAnimations.reloadIfInWorld()
            }
        }, "modEnabled", KEY + "modEnabled", KEY + "modEnabled.description", Boolean::class.javaPrimitiveType!!)
        toggle.addMetadata("visualizer", Visualizer.SwitchVisualizer::class.java)
        toggle.addMetadata("category", KEY + "category.position")
        tree.put(toggle)

        option(tree, "position", "firstPersonItemPositions")
        option(tree, "position", "thirdPersonItemPositions")
        option(tree, "position", "itemTransformations")
        option(tree, "position", "bowPullback")
        option(tree, "position", "blockAnimation")
        option(tree, "position", "rodPosition")
        option(tree, "position", "thirdPersonBlockAnimation")

        option(tree, "interaction", "consumeAnimation")
        option(tree, "interaction", "blockHittingAnimation")
        option(tree, "interaction", "smoothSneaking")
        option(tree, "interaction", "longerUnsneak") { smoothSneaking.isOld }
        option(tree, "interaction", "punchingDuringUsage")
        option(tree, "interaction", "punchingDuringUsageParticles") { punchingDuringUsage.isOld }
        option(tree, "interaction", "itemSwitchingAnimation")
        option(tree, "interaction", "oldEnchantmentGlint")
        option(tree, "interaction", "hitColorArmor")
        option(tree, "interaction", "hitColorArmorGlint") { hitColorArmor.isOld }
        option(tree, "interaction", "hitColorBrightness")
        option(tree, "interaction", "missPenaltyBehavior")

        option(tree, "item_visual", "droppedItemDirection")

        option(tree, "hud", "healthBarFlashing")
        return tree
    }

    private fun option(tree: Tree, category: String, name: String, enabledWhen: (() -> Boolean)? = null) {
        val property = Properties.field<Any>(KEY + name, "$KEY$name.description", OverflowAnimationsConfig::class.java.getField(name), this)
        val type = property.type
        val keys = type.enumConstants.map { KEY + "enum." + type.simpleName + "." + (it as Enum<*>).name }.toTypedArray()
        property.addMetadata("visualizer", Visualizer.DropdownVisualizer::class.java)
        property.addMetadata("options", keys)
        property.addMetadata("optionsKey", keys)
        property.addMetadata("category", "${KEY}category.$category")
        if (enabledWhen != null) {
            property.addDisplayCondition { if (enabledWhen()) Property.Display.SHOWN else Property.Display.DISABLED }
        }

        tree.put(property)
    }

    override fun initialize(byConfigManager: Boolean) {
        super.initialize(byConfigManager)
        if (tree != null) disableAllUnless("modEnabled")
    }

    companion object {
        private const val ID = OverflowAnimationsConstants.MOD_ID + ".json"
        private const val KEY = OverflowAnimationsConstants.MOD_ID + "."
        private var loaded = false
        private val INSTANCE = OverflowAnimationsConfig()

        @JvmStatic
        fun load() {
            INSTANCE.preload()
            loaded = true
        }

        @JvmStatic
        fun openScreen() {
            OneConfigUI.open(ModConfigRoute(ID, null))
        }

        @JvmStatic
        fun instance() = INSTANCE
    }
}
