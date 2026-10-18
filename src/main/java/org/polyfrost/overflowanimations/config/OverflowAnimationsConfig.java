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

package org.polyfrost.overflowanimations.config;

import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.Properties;
import org.polyfrost.oneconfig.api.config.v1.Property;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.Visualizer;
import org.polyfrost.oneconfig.api.ui.v1.OneConfigUI;
import org.polyfrost.oneconfig.internal.ui.navigation.graph.ModConfigRoute;
import org.polyfrost.overflowanimations.OverflowAnimations;
import org.polyfrost.overflowanimations.OverflowAnimationsConstants;
import org.polyfrost.overflowanimations.util.enums.DroppedItemDirectionSetting;
import org.polyfrost.overflowanimations.util.enums.LegacyVersion;
import org.polyfrost.overflowanimations.util.enums.MissPenaltySetting;

import java.util.Arrays;
import java.util.function.Supplier;

public final class OverflowAnimationsConfig extends Config {
    private static final String ID = OverflowAnimationsConstants.MOD_ID + ".json";
    private static final String KEY = OverflowAnimationsConstants.MOD_ID + ".";
    private static boolean loaded = false;
    private static final OverflowAnimationsConfig INSTANCE = new OverflowAnimationsConfig();

    // Position
    public LegacyVersion firstPersonItemPositions = LegacyVersion.V1_7;
    public LegacyVersion thirdPersonItemPositions = LegacyVersion.V1_7;
    public LegacyVersion itemTransformations = LegacyVersion.V1_7;
    public LegacyVersion bowPullback = LegacyVersion.V1_7;
    public LegacyVersion blockAnimation = LegacyVersion.V1_7;
    public LegacyVersion rodPosition = LegacyVersion.V1_7;
    public LegacyVersion thirdPersonBlockAnimation = LegacyVersion.V1_7;

    // Interaction
    public LegacyVersion consumeAnimation = LegacyVersion.V1_7;
    public LegacyVersion blockHittingAnimation = LegacyVersion.V1_7;
    public LegacyVersion smoothSneaking = LegacyVersion.V1_7;
    public LegacyVersion longerUnsneak = LegacyVersion.V1_7;
    public LegacyVersion punchingDuringUsage = LegacyVersion.V1_7;
    public LegacyVersion punchingDuringUsageParticles = LegacyVersion.V1_7;
    public LegacyVersion itemSwitchingAnimation = LegacyVersion.V1_7;
    public LegacyVersion oldEnchantmentGlint = LegacyVersion.V1_7;
    public LegacyVersion hitColorArmor = LegacyVersion.V1_7;
    public LegacyVersion hitColorArmorGlint = LegacyVersion.V1_7;
    public LegacyVersion hitColorBrightness = LegacyVersion.V1_7;
    public MissPenaltySetting missPenaltyBehavior = MissPenaltySetting.SWING_AND_PARTICLES;

    // Item Visual
    public DroppedItemDirectionSetting droppedItemDirection = DroppedItemDirectionSetting.DEFAULT;

    // Hud
    public LegacyVersion healthBarFlashing = LegacyVersion.V1_7;

    private OverflowAnimationsConfig() {
        super(ID, "/assets/overflowanimations/icon.svg", "OverflowAnimations", Category.VISUALS);
    }

    @Override
    protected Tree makeTree() {
        final Tree tree = Tree.tree(ID);
        final Property<Boolean> toggle = Properties.functional(OverflowAnimations::isEnabled, enabled -> {
            // the utility config is where this is kept, so the copy saved alongside the options is not read back
            if (!loaded) return;
            OverflowAnimations.setEnabled(enabled);
            OverflowAnimations.reloadIfInWorld();
        }, "modEnabled", KEY + "modEnabled", KEY + "modEnabled.description", boolean.class);
        toggle.addMetadata("visualizer", Visualizer.SwitchVisualizer.class);
        toggle.addMetadata("category", KEY + "category.position");
        tree.put(toggle);

        this.option(tree, "position", "firstPersonItemPositions");
        this.option(tree, "position", "thirdPersonItemPositions");
        this.option(tree, "position", "itemTransformations");
        this.option(tree, "position", "bowPullback");
        this.option(tree, "position", "blockAnimation");
        this.option(tree, "position", "rodPosition");
        this.option(tree, "position", "thirdPersonBlockAnimation");

        this.option(tree, "interaction", "consumeAnimation");
        this.option(tree, "interaction", "blockHittingAnimation");
        this.option(tree, "interaction", "smoothSneaking");
        this.option(tree, "interaction", "longerUnsneak", () -> this.smoothSneaking.isOld());
        this.option(tree, "interaction", "punchingDuringUsage");
        this.option(tree, "interaction", "punchingDuringUsageParticles", () -> this.punchingDuringUsage.isOld());
        this.option(tree, "interaction", "itemSwitchingAnimation");
        this.option(tree, "interaction", "oldEnchantmentGlint");
        this.option(tree, "interaction", "hitColorArmor");
        this.option(tree, "interaction", "hitColorArmorGlint", () -> this.hitColorArmor.isOld());
        this.option(tree, "interaction", "hitColorBrightness");
        this.option(tree, "interaction", "missPenaltyBehavior");

        this.option(tree, "item_visual", "droppedItemDirection");

        this.option(tree, "hud", "healthBarFlashing");
        return tree;
    }

    private void option(final Tree tree, final String category, final String name) {
        this.option(tree, category, name, null);
    }

    private void option(final Tree tree, final String category, final String name, final Supplier<Boolean> enabledWhen) {
        final Property<Object> property;
        try {
            property = Properties.field(KEY + name, KEY + name + ".description", OverflowAnimationsConfig.class.getField(name), this);
        } catch (final NoSuchFieldException exception) {
            throw new IllegalStateException(exception);
        }

        final Class<?> type = property.type;
        final String[] keys = Arrays.stream(type.getEnumConstants()).map(value -> KEY + "enum." + type.getSimpleName() + "." + ((Enum<?>) value).name()).toArray(String[]::new);
        property.addMetadata("visualizer", Visualizer.DropdownVisualizer.class);
        property.addMetadata("options", keys);
        property.addMetadata("optionsKey", keys);
        property.addMetadata("category", KEY + "category." + category);
        if (enabledWhen != null) {
            property.addDisplayCondition(() -> enabledWhen.get() ? Property.Display.SHOWN : Property.Display.DISABLED);
        }

        tree.put(property);
    }

    @Override
    protected void initialize(boolean byConfigManager) {
        super.initialize(byConfigManager);
        if (this.getTree() != null) this.disableAllUnless("modEnabled");
    }

    public static void load() {
        INSTANCE.preload();
        loaded = true;
    }

    public static void openScreen() {
        OneConfigUI.open(new ModConfigRoute(ID, null));
    }

    public static OverflowAnimationsConfig instance() {
        return INSTANCE;
    }
}
