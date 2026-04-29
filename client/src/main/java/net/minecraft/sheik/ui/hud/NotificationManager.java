package net.minecraft.sheik.ui.hud;

import net.minecraft.sheik.ui.font.Fonts;
import net.minecraft.sheik.ui.font.HudFont;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class NotificationManager {

    private static final int MAX_VISIBLE   = 5;
    private static final long LIFETIME_MS  = 3000L;
    private static final int NOTIF_WIDTH   = 160;
    private static final int NOTIF_HEIGHT  = 22;
    private static final int GAP           = 3;

    private final Deque<HudNotification> queue = new ArrayDeque<>();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Adds a notification to the front of the queue.
     * If the queue is already at {@code MAX_VISIBLE}, the oldest entry (tail) is
     * dropped to make room.
     */
    public void push(HudNotification notification) {
        if (queue.size() >= MAX_VISIBLE) {
            queue.pollLast();
        }
        queue.addFirst(notification);
    }

    /**
     * Removes expired notifications, then draws each active one stacked from
     * the top-right corner of the screen.
     *
     * @param sr the current {@link ScaledResolution}
     */
    public void onRender2D(ScaledResolution sr) {
        // --- 1. Expire old notifications ---
        long now = System.currentTimeMillis();
        queue.removeIf(n -> now - n.getTimestamp() > LIFETIME_MS);

        if (queue.isEmpty()) {
            return;
        }

        // --- 2. Draw active notifications ---
        HudFont font = Fonts.defaultFont();

        int x = sr.getScaledWidth() - NOTIF_WIDTH - 6;
        int y = 6;

        // Snapshot into a list so iteration order is stable and front-to-back.
        List<HudNotification> snapshot = new ArrayList<>(queue);

        for (HudNotification notification : snapshot) {
            notification.draw(x, y, NOTIF_WIDTH, NOTIF_HEIGHT, font);
            y += NOTIF_HEIGHT + GAP;
        }
    }
}
