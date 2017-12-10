package com.axel_stein.noteapp.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class MenuUtil {

    private MenuUtil() {
    }

    public static void tintMenuIconsColor(@Nullable Context context, @Nullable Menu menu, @ColorInt int color) {
        if (context != null) {
            tintMenuIcons(menu, color);
        }
    }

    public static void tintMenuIconsColorRes(@Nullable Context context, @Nullable Menu menu, @ColorRes int colorRes) {
        if (context != null) {
            tintMenuIcons(menu, ContextCompat.getColor(context, colorRes));
        }
    }

    public static void tintMenuIconsAttr(@Nullable Context context, @Nullable Menu menu, @AttrRes int colorAttr) {
        tintMenuIcons(menu, ColorUtil.getColorAttr(context, colorAttr));
    }

    public static void tintAttr(@Nullable Context context, @Nullable MenuItem item, int colorAttr) {
        if (item != null) {
            Drawable icon = item.getIcon();
            if (icon != null) {
                icon.mutate().setColorFilter(ColorUtil.getColorAttr(context, colorAttr), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    private static void tintMenuIcons(@Nullable Menu menu, int color) {
        if (menu == null) {
            return;
        }

        for (int i = 0, count = menu.size(); i < count; i++) {
            MenuItem item = menu.getItem(i);
            if (item != null) {
                Drawable icon = item.getIcon();
                if (icon != null) {
                    icon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    public static void inflateMenuFromArray(@NonNull Menu menu, @NonNull String[] array) {
        for (int i = 0; i < array.length; i++) {
            menu.add(0, i, 0, array[i]);
        }
    }

    @NonNull
    public static Menu inflateMenuFromResource(@NonNull View view, @MenuRes int menuRes) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(menuRes);
        return popup.getMenu();
    }

    @NonNull
    public static ArrayList<MenuItem> getVisibleMenuItems(@NonNull Menu menu) {
        ArrayList<MenuItem> items = new ArrayList<>();

        for (int i = 0, count = menu.size(); i < count; i++) {
            MenuItem item = menu.getItem(i);
            if (item.isVisible()) {
                items.add(item);
            }
        }

        return items;
    }

    public static boolean enabled(Menu menu, int id) {
        if (menu != null) {
            MenuItem item = menu.findItem(id);
            if (item != null) {
                return item.isEnabled();
            }
        }
        return false;
    }

    public static void enable(@Nullable Menu menu, boolean enable) {
        if (menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                enableMenuItemImpl(menu.getItem(i), enable);
            }
        }
    }

    public static void enable(@Nullable Menu menu, boolean enable, int... itemIds) {
        if (menu == null || itemIds == null) {
            return;
        }
        for (int id : itemIds) {
            enableMenuItemImpl(menu.findItem(id), enable);
        }
    }

    public static void enable(boolean enable, @Nullable MenuItem... items) {
        if (items != null) {
            for (MenuItem item : items) {
                enableMenuItemImpl(item, enable);
            }
        }
    }

    private static void enableMenuItemImpl(@Nullable MenuItem item, boolean enable) {
        if (item != null) {
            item.setEnabled(enable);
            Drawable icon = item.getIcon();
            if (icon != null) {
                icon.mutate().setAlpha(enable ? 255 : 124);
            }
        }
    }

    public static void show(@Nullable Menu menu, boolean show, int... itemIds) {
        if (menu == null || itemIds == null) {
            return;
        }
        for (int id : itemIds) {
            show(menu.findItem(id), show);
        }
    }

    public static void show(@Nullable MenuItem item, boolean show) {
        if (item != null) {
            item.setVisible(show);
        }
    }

    public static void check(@Nullable Menu menu, @IdRes int itemId, boolean checked) {
        if (menu != null) {
            check(menu.findItem(itemId), checked);
        }
    }

    public static void check(@Nullable MenuItem item, boolean checked) {
        if (item != null) {
            item.setChecked(checked);
        }
    }

    public static void show(@Nullable Menu menu, boolean show) {
        if (menu != null) {
            ArrayList<MenuItem> items = getVisibleMenuItems(menu);
            for (MenuItem item : items) {
                show(item, show);
            }
        }
    }

    public static void remove(@Nullable Menu menu) {
        if (menu != null) {
            for (int count = menu.size(), i = count - 1; i >= 0; i--) {
                MenuItem item = menu.getItem(i);
                if (item != null) {
                    menu.removeItem(item.getItemId());
                }
            }
        }
    }

    public static void removeGroup(@Nullable Menu menu, int groupId) {
        if (menu != null) {
            for (int count = menu.size(), i = count - 1; i >= 0; i--) {
                MenuItem item = menu.getItem(i);
                if (item != null && item.getGroupId() == groupId) {
                    menu.removeItem(item.getItemId());
                }
            }
        }
    }

    public static void showGroup(Menu menu, int groupId, boolean show) {
        if (menu != null) {
            menu.setGroupVisible(groupId, show);
        }
    }

    @NonNull
    public static ArrayList<MenuItem> getGroupMenuItems(Menu menu, int group) {
        ArrayList<MenuItem> res = new ArrayList<>();
        if (menu != null) {
            ArrayList<MenuItem> items = getVisibleMenuItems(menu);
            for (MenuItem item : items) {
                if (item.getGroupId() == group) {
                    res.add(item);
                }
            }
        }
        return res;
    }

    @Nullable
    public static MenuItem findGroupMenuItem(Menu menu, String title, int group) {
        ArrayList<MenuItem> items = getGroupMenuItems(menu, group);
        for (MenuItem item : items) {
            if (TextUtils.equals(item.getTitle(), title)) {
                return item;
            }
        }
        return null;
    }

}


