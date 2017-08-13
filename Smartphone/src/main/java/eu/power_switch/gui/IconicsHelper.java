/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.shared.ThemeHelper;

/**
 * Helper class for access to often used icons
 * <p/>
 * Created by Markus on 13.12.2015.
 */
@Singleton
public final class IconicsHelper {

    @Inject
    Context context;

    @Inject
    public IconicsHelper() {
    }

    /**
     * Get an icon suitable for the swipe menu
     *
     * @param icon the icon resource
     *
     * @return the icon
     */
    public IconicsDrawable getMenuIcon(IIcon icon) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        return getIcon(icon, color, 24);
    }

    /**
     * Get an icon suitable for a simple page of the wizard
     *
     * @param icon the icon resource
     *
     * @return the icon
     */
    public IconicsDrawable getWizardIcon(IIcon icon) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        return getIcon(icon, color, 64);
    }

    /**
     * Get an icon suitable for the ConfigurationDialog control bar
     *
     * @param icon the icon resource
     *
     * @return the icon
     */
    public IconicsDrawable getConfigurationDialogControlBarIcon(IIcon icon) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        return getIcon(icon, color, 36);
    }

    /**
     * Get an icon suitable for a fab button of normal size
     *
     * @param icon the icon resource
     *
     * @return the icon
     */
    public IconicsDrawable getFabIcon(IIcon icon) {
        final int color = Color.WHITE;
        return getIcon(icon, color, 24, 5);
    }

    /**
     * Get an icon suitable for the options menu
     *
     * @param icon the icon resource
     *
     * @return the icon
     */
    public IconicsDrawable getOptionsMenuIcon(IIcon icon) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);

        int padding = 0;
        if (icon == MaterialDesignIconic.Icon.gmi_plus) {
            padding = 5;
        } else if (icon == MaterialDesignIconic.Icon.gmi_reorder || icon == MaterialDesignIconic.Icon.gmi_refresh) {
            padding = 2;
        }

        return getIcon(icon, color, 24, padding);
    }

    private IconicsDrawable getIcon(IIcon icon, @ColorInt int color, int sizeDp) {
        return getIcon(icon, color, sizeDp, 0);
    }

    private IconicsDrawable getIcon(IIcon icon, @ColorInt int color, int sizeDp, int paddingDp) {
        return new IconicsDrawable(context, icon).sizeDp(sizeDp)
                .paddingDp(paddingDp)
                .color(color);
    }

    /**
     * Get "Reorder" icon
     *
     * @param context any suitable context
     *
     * @return "Reorder" icon
     */
    public static IconicsDrawable getReorderHandleIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_reorder).sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Search" icon
     *
     * @param context any suitable context
     *
     * @return "Search" icon
     */
    public static IconicsDrawable getSearchIcon(Context context, int color) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, MaterialDesignIconic.Icon.gmi_search).color(color)
                .sizeDp(24);

        return iconicsDrawable;
    }

    /**
     * Get "Attention" icon
     *
     * @param context any suitable context
     *
     * @return "Attention" icon
     */
    public static IconicsDrawable getAttentionIcon(Context context) {
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_report_problem).color(ContextCompat.getColor(context,
                R.color.color_red_a700))
                .sizeDp(24);

        return iconicsDrawable;
    }

    /**
     * Get "Folder" icon
     *
     * @param context any suitable context
     *
     * @return "Folder" icon
     */
    public static IconicsDrawable getFolderIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_folder).sizeDp(24)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Keyboard Arrow Right" icon
     *
     * @param context any suitable context
     *
     * @return "Keyboard Arrow Right" icon
     */
    public static IconicsDrawable getKbArrowRightIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_keyboard_arrow_right).sizeDp(16)
                .color(color);

        return iconicsDrawable;
    }

    /**
     * Get "Keyboard Arrow Down" icon
     *
     * @param context any suitable context
     *
     * @return "Keyboard Arrow Down" icon
     */
    public static IconicsDrawable getKbArrowDownIcon(Context context) {
        final int color = ThemeHelper.getThemeAttrColor(context, android.R.attr.textColorPrimary);
        IconicsDrawable iconicsDrawable = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_keyboard_arrow_down).sizeDp(16)
                .color(color);

        return iconicsDrawable;
    }
}
