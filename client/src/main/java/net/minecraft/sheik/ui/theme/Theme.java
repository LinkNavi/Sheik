package net.minecraft.sheik.ui.theme;

public final class Theme {

	private Theme() {}

	public static int OVERLAY_COLOR = 0x88000000;
	public static int PANEL_COLOR = 0xFF1C1C1F;
	public static int BORDER_COLOR = 0xFF303038;
	public static int ROW_COLOR = 0xFF24242A;
	public static int ROW_HOVER_COLOR = 0xFF30303A;

	public static int TEXT_PRIMARY = 0xFFE6E6E6;
	public static int TEXT_MUTED = 0xFF9A9AA0;
	public static int ACCENT = 0xFF5CA8FF;

	// -------------------------------------------------------------------------
	// Defaults — used by ClientSettingScreen to reset colours
	// -------------------------------------------------------------------------
	public static final int DEFAULT_OVERLAY_COLOR = 0x88000000;
	public static final int DEFAULT_PANEL_COLOR = 0xFF1C1C1F;
	public static final int DEFAULT_BORDER_COLOR = 0xFF303038;
	public static final int DEFAULT_ROW_COLOR = 0xFF24242A;
	public static final int DEFAULT_ROW_HOVER_COLOR = 0xFF30303A;
	public static final int DEFAULT_TEXT_PRIMARY = 0xFFE6E6E6;
	public static final int DEFAULT_TEXT_MUTED = 0xFF9A9AA0;
	public static final int DEFAULT_ACCENT = 0xFF5CA8FF;

	public static void resetToDefaults() {
		OVERLAY_COLOR = DEFAULT_OVERLAY_COLOR;
		PANEL_COLOR = DEFAULT_PANEL_COLOR;
		BORDER_COLOR = DEFAULT_BORDER_COLOR;
		ROW_COLOR = DEFAULT_ROW_COLOR;
		ROW_HOVER_COLOR = DEFAULT_ROW_HOVER_COLOR;
		TEXT_PRIMARY = DEFAULT_TEXT_PRIMARY;
		TEXT_MUTED = DEFAULT_TEXT_MUTED;
		ACCENT = DEFAULT_ACCENT;
	}
}
