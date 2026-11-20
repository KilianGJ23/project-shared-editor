package co.edu.uptc.view;

import java.awt.Color;

public class UserColorPalette {
    private static final Color[] ACCENT_COLORS = {
        new Color(211, 47, 47),
        new Color(25, 118, 210),
        new Color(56, 142, 60),
        new Color(245, 124, 0),
        new Color(123, 31, 162),
        new Color(0, 131, 143),
        new Color(194, 24, 91),
        new Color(93, 64, 55),
        new Color(69, 90, 100),
        new Color(175, 180, 43)
    };

    private static final Color[] SOFT_TINT_COLORS = {
        new Color(255, 235, 238),
        new Color(227, 242, 253),
        new Color(232, 245, 233),
        new Color(255, 243, 224),
        new Color(243, 229, 245),
        new Color(224, 247, 250),
        new Color(252, 228, 236),
        new Color(239, 235, 233),
        new Color(236, 239, 241),
        new Color(249, 251, 231)
    };

    private UserColorPalette() {
    }

    public static Color getAccentColor(int colorId) {
        int safeIndex = Math.abs(colorId) % ACCENT_COLORS.length;
        return ACCENT_COLORS[safeIndex];
    }

    public static Color getSoftTintColor(int colorId) {
        int safeIndex = Math.abs(colorId) % SOFT_TINT_COLORS.length;
        return SOFT_TINT_COLORS[safeIndex];
    }
}
