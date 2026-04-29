package net.minecraft.sheik.ui.hud;

import net.minecraft.sheik.ui.font.HudFont;

public class HudNotification {

    public enum Type {
        INFO("Info", HudColors.NOTIF_INFO),
        SUCCESS("OK", HudColors.NOTIF_SUCCESS),
        WARNING("Warn", HudColors.NOTIF_WARNING),
        ERROR("Error", HudColors.NOTIF_ERROR);

        public final String label;
        public final int accentColor;

        Type(String label, int accentColor) {
            this.label = label;
            this.accentColor = accentColor;
        }
    }

    private static final int ACCENT_BAR_WIDTH = 3;
    private static final long LIFETIME_MS = 3000L;

    private final String message;
    private final Type type;
    private final long timestamp;

    public HudNotification(String message) {
        this(message, Type.INFO);
    }

    public HudNotification(String message, Type type) {
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Draws the notification at (x, y) with the given dimensions.
     *
     * Layout:
     *  ┌───┬──────────────────────────────────┐
     *  │▌▌▌│  [TypeLabel]  message text        │
     *  │   │▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░  │  ← lifetime bar
     *  └───┴──────────────────────────────────┘
     *   ^ 3 px accent bar in the type colour
     */
    public void draw(int x, int y, int width, int height, HudFont font) {
        // Background + border
        HudUi.rect(x, y, width, height, HudColors.NOTIFICATION_BG);
        HudUi.outline(x, y, width, height, HudColors.NOTIFICATION_BORDER);

        // Left accent bar
        HudUi.rect(x, y, ACCENT_BAR_WIDTH, height, type.accentColor);

        int textX = x + ACCENT_BAR_WIDTH + 5;
        int textY = y + (height - font.getFontHeight()) / 2 - 2;

        // Coloured type label (e.g. "Info", "OK") followed by message
        String label = type.label + " ";
        font.drawStringWithShadow(label, textX, textY, type.accentColor);

        int labelWidth = font.getStringWidth(label);
        font.drawStringWithShadow(
            message,
            textX + labelWidth,
            textY,
            HudColors.NOTIFICATION_TEXT
        );

        // Lifetime progress bar — shrinks left-to-right as time passes
        long elapsed = System.currentTimeMillis() - timestamp;
        float remaining = 1.0f - Math.min((float) elapsed / LIFETIME_MS, 1.0f);
        int barX = x + ACCENT_BAR_WIDTH + 3;
        int barY = y + height - 3;
        int barMaxW = width - ACCENT_BAR_WIDTH - 6;
        int barW = (int) (barMaxW * remaining);

        // Track (dim background)
        HudUi.rect(barX, barY, barMaxW, 2, HudColors.NOTIFICATION_BORDER);
        // Fill (accent colour)
        if (barW > 0) {
            HudUi.rect(barX, barY, barW, 2, type.accentColor);
        }
    }
}
