package net.minecraft.sheik.ui;

import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.sheik.ui.render.RenderUtil;
import net.minecraft.sheik.ui.theme.Theme;

/**
 * ClientSettingScreen
 *
 * Lets the player tweak every colour in {@link Theme} live.
 * Each colour entry shows:
 *   • a labelled swatch on the left
 *   • a hex string in the middle
 *   • click the row to expand it into three R / G / B drag-sliders
 *
 * Clicking outside the expanded row collapses it.
 * A "Reset Defaults" button at the bottom restores all colours.
 * A "Back" button closes the screen.
 */
public class ClientSettingScreen extends GuiScreen {

	// -------------------------------------------------------------------------
	// Layout constants
	// -------------------------------------------------------------------------
	private static final int PANEL_WIDTH = 280;
	private static final int PADDING = 10;
	private static final int ROW_HEIGHT = 20;
	private static final int ROW_GAP = 4;
	private static final int ROW_STEP = ROW_HEIGHT + ROW_GAP;
	private static final int SWATCH_SIZE = 14;
	private static final int HEADER_H = 28;
	private static final int FOOTER_H = 36;
	private static final int SLIDER_H = 10;
	private static final int SLIDER_GAP = 6;
	private static final int EXPANDED_H = (SLIDER_H + SLIDER_GAP) * 3 + 6;
	private static final int BTN_H = 16;
	private static final int BTN_W = 110;

	// -------------------------------------------------------------------------
	// Colour entries (order determines display order)
	// -------------------------------------------------------------------------
	private static final String[] LABELS = {
		"Accent",
		"Panel",
		"Border",
		"Row",
		"Row Hover",
		"Overlay",
		"Text Primary",
		"Text Muted",
	};

	/** Returns the current value of the i-th colour slot. */
	private static int getColor(int i) {
		switch (i) {
			case 0:
				return Theme.ACCENT;
			case 1:
				return Theme.PANEL_COLOR;
			case 2:
				return Theme.BORDER_COLOR;
			case 3:
				return Theme.ROW_COLOR;
			case 4:
				return Theme.ROW_HOVER_COLOR;
			case 5:
				return Theme.OVERLAY_COLOR;
			case 6:
				return Theme.TEXT_PRIMARY;
			case 7:
				return Theme.TEXT_MUTED;
			default:
				return 0xFF000000;
		}
	}

	/** Writes a new ARGB value back into the matching Theme field. */
	private static void setColor(int i, int argb) {
		switch (i) {
			case 0:
				Theme.ACCENT = argb;
				break;
			case 1:
				Theme.PANEL_COLOR = argb;
				break;
			case 2:
				Theme.BORDER_COLOR = argb;
				break;
			case 3:
				Theme.ROW_COLOR = argb;
				break;
			case 4:
				Theme.ROW_HOVER_COLOR = argb;
				break;
			case 5:
				Theme.OVERLAY_COLOR = argb;
				break;
			case 6:
				Theme.TEXT_PRIMARY = argb;
				break;
			case 7:
				Theme.TEXT_MUTED = argb;
				break;
		}
	}

	// -------------------------------------------------------------------------
	// State
	// -------------------------------------------------------------------------
	private final GuiScreen parent;

	/** Index of the currently expanded colour row, or -1 if none. */
	private int expandedRow = -1;

	/** Which slider channel is being dragged: 0=R, 1=G, 2=B, -1=none. */
	private int draggingChannel = -1;

	private int panelX, panelY, panelHeight;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------
	public ClientSettingScreen(GuiScreen parent) {
		this.parent = parent;
	}

	// -------------------------------------------------------------------------
	// GuiScreen lifecycle
	// -------------------------------------------------------------------------
	@Override
	public void initGui() {
		panelHeight = computePanelHeight();
		panelX = (this.width - PANEL_WIDTH) / 2;
		panelY = (this.height - panelHeight) / 2;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	// -------------------------------------------------------------------------
	// Rendering
	// -------------------------------------------------------------------------
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Full-screen dimmer
		RenderUtil.drawRect(0, 0, this.width, this.height, 0x80000000);

		// Panel shadow + body + border
		RenderUtil.drawShadowedRoundedRect(
			panelX - 6,
			panelY - 6,
			PANEL_WIDTH + 12,
			panelHeight + 12,
			12,
			0xA0151518
		);
		RenderUtil.drawRoundedRect(
			panelX,
			panelY,
			PANEL_WIDTH,
			panelHeight,
			10,
			Theme.PANEL_COLOR
		);
		RenderUtil.drawOutline(
			panelX,
			panelY,
			PANEL_WIDTH,
			panelHeight,
			Theme.BORDER_COLOR
		);

		// Title
		this.fontRendererObj.drawStringWithShadow(
			"Client Settings",
			panelX + PADDING,
			panelY + (HEADER_H - this.fontRendererObj.FONT_HEIGHT) / 2,
			Theme.TEXT_PRIMARY
		);

		// Divider under title
		int divY = panelY + HEADER_H - 1;
		RenderUtil.drawRect(
			panelX + PADDING,
			divY,
			PANEL_WIDTH - PADDING * 2,
			1,
			Theme.BORDER_COLOR
		);

		// Colour rows
		int y = panelY + HEADER_H + ROW_GAP;
		for (int i = 0; i < LABELS.length; i++) {
			y = drawColorRow(i, mouseX, mouseY, y);
		}

		// Divider above footer
		int footerDivY = panelY + panelHeight - FOOTER_H;
		RenderUtil.drawRect(
			panelX + PADDING,
			footerDivY,
			PANEL_WIDTH - PADDING * 2,
			1,
			Theme.BORDER_COLOR
		);

		int btnY = footerDivY + (FOOTER_H - BTN_H) / 2;

		// Reset button
		int resetX = panelX + PADDING;
		boolean resetHov = isHovered(
			mouseX,
			mouseY,
			resetX,
			btnY,
			BTN_W,
			BTN_H
		);
		RenderUtil.drawRoundedRect(
			resetX,
			btnY,
			BTN_W,
			BTN_H,
			5,
			resetHov ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR
		);
		drawCentered(
			"Reset Defaults",
			resetX,
			btnY,
			BTN_W,
			BTN_H,
			Theme.TEXT_MUTED
		);

		// Back button
		int backX = panelX + PANEL_WIDTH - PADDING - BTN_W;
		boolean backHov = isHovered(mouseX, mouseY, backX, btnY, BTN_W, BTN_H);
		RenderUtil.drawRoundedRect(
			backX,
			btnY,
			BTN_W,
			BTN_H,
			5,
			backHov ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR
		);
		drawCentered("[ Back ]", backX, btnY, BTN_W, BTN_H, Theme.TEXT_PRIMARY);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	/**
	 * Draws a single colour row (collapsed or expanded).
	 *
	 * @return the y position after this row
	 */
	private int drawColorRow(int index, int mouseX, int mouseY, int y) {
		int argb = getColor(index);
		boolean expanded = (expandedRow == index);

		// Row background
		boolean rowHov =
			!expanded &&
			isHovered(
				mouseX,
				mouseY,
				panelX + PADDING,
				y,
				PANEL_WIDTH - PADDING * 2,
				ROW_HEIGHT
			);
		RenderUtil.drawRoundedRect(
			panelX + PADDING,
			y,
			PANEL_WIDTH - PADDING * 2,
			ROW_HEIGHT,
			4,
			rowHov ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR
		);

		// Colour swatch (show opaque RGB so the swatch is always visible)
		int swatchColor = 0xFF000000 | (argb & 0x00FFFFFF);
		int swatchX = panelX + PADDING + 4;
		int swatchY = y + (ROW_HEIGHT - SWATCH_SIZE) / 2;
		RenderUtil.drawRect(
			swatchX,
			swatchY,
			SWATCH_SIZE,
			SWATCH_SIZE,
			swatchColor
		);
		RenderUtil.drawOutline(
			swatchX,
			swatchY,
			SWATCH_SIZE,
			SWATCH_SIZE,
			Theme.BORDER_COLOR
		);

		// Label
		this.fontRendererObj.drawStringWithShadow(
			LABELS[index],
			swatchX + SWATCH_SIZE + 6,
			y + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2,
			Theme.TEXT_PRIMARY
		);

		// Hex value (right-aligned)
		String hex = String.format("#%08X", argb);
		int hexW = this.fontRendererObj.getStringWidth(hex);
		this.fontRendererObj.drawStringWithShadow(
			hex,
			panelX + PANEL_WIDTH - PADDING - 4 - hexW,
			y + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2,
			Theme.TEXT_MUTED
		);

		y += ROW_HEIGHT;

		// Expanded slider section
		if (expanded) {
			y += 2;
			int r = (argb >> 16) & 0xFF;
			int g = (argb >> 8) & 0xFF;
			int b = argb & 0xFF;

			y = drawChannelSlider(
				index,
				0,
				"R",
				r,
				mouseX,
				mouseY,
				y,
				0xFFFF4444
			);
			y = drawChannelSlider(
				index,
				1,
				"G",
				g,
				mouseX,
				mouseY,
				y,
				0xFF44FF44
			);
			y = drawChannelSlider(
				index,
				2,
				"B",
				b,
				mouseX,
				mouseY,
				y,
				0xFF4488FF
			);
			y += 4;
		}

		y += ROW_GAP;
		return y;
	}

	/**
	 * Draws one R/G/B channel slider.
	 *
	 * @return y after the slider
	 */
	private int drawChannelSlider(
		int colorIndex,
		int channel,
		String label,
		int value,
		int mouseX,
		int mouseY,
		int y,
		int trackColor
	) {
		int trackX = panelX + PADDING + 24;
		int trackW = PANEL_WIDTH - PADDING * 2 - 24 - 34;

		// Channel label (R / G / B)
		this.fontRendererObj.drawStringWithShadow(
			label,
			panelX + PADDING + 4,
			y + (SLIDER_H - this.fontRendererObj.FONT_HEIGHT) / 2 + 1,
			Theme.TEXT_MUTED
		);

		// Track background
		RenderUtil.drawRect(
			trackX,
			y + (SLIDER_H - 4) / 2,
			trackW,
			4,
			Theme.BORDER_COLOR
		);

		// Filled portion
		int fillW = (int) (trackW * (value / 255.0f));
		if (fillW > 0) {
			RenderUtil.drawRect(
				trackX,
				y + (SLIDER_H - 4) / 2,
				fillW,
				4,
				trackColor
			);
		}

		// Thumb
		int thumbX = trackX + fillW - 3;
		boolean thumbHov =
			draggingChannel == channel && expandedRow == colorIndex;
		RenderUtil.drawRect(
			thumbX,
			y,
			6,
			SLIDER_H,
			thumbHov ? 0xFFFFFFFF : 0xFFCCCCCC
		);

		// Numeric value label
		String valStr = Integer.toString(value);
		this.fontRendererObj.drawStringWithShadow(
			valStr,
			trackX + trackW + 6,
			y + (SLIDER_H - this.fontRendererObj.FONT_HEIGHT) / 2 + 1,
			Theme.TEXT_PRIMARY
		);

		return y + SLIDER_H + SLIDER_GAP;
	}

	// -------------------------------------------------------------------------
	// Mouse input
	// -------------------------------------------------------------------------
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
		throws IOException {
		if (mouseButton != 0) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}

		// Footer buttons
		int footerDivY = panelY + panelHeight - FOOTER_H;
		int btnY = footerDivY + (FOOTER_H - BTN_H) / 2;
		int resetX = panelX + PADDING;
		int backX = panelX + PANEL_WIDTH - PADDING - BTN_W;

		if (isHovered(mouseX, mouseY, resetX, btnY, BTN_W, BTN_H)) {
			Theme.resetToDefaults();
			return;
		}
		if (isHovered(mouseX, mouseY, backX, btnY, BTN_W, BTN_H)) {
			mc.displayGuiScreen(parent);
			return;
		}

		// Check sliders first (only when a row is expanded)
		if (expandedRow >= 0) {
			if (tryDragSlider(expandedRow, mouseX, mouseY)) {
				return;
			}
		}

		// Colour row hit-test
		int y = panelY + HEADER_H + ROW_GAP;
		for (int i = 0; i < LABELS.length; i++) {
			if (
				isHovered(
					mouseX,
					mouseY,
					panelX + PADDING,
					y,
					PANEL_WIDTH - PADDING * 2,
					ROW_HEIGHT
				)
			) {
				expandedRow = (expandedRow == i) ? -1 : i;
				draggingChannel = -1;
				panelHeight = computePanelHeight();
				panelY = (this.height - panelHeight) / 2;
				return;
			}
			y += ROW_HEIGHT;
			if (expandedRow == i) {
				y += EXPANDED_H + ROW_GAP;
			}
			y += ROW_GAP;
		}

		// Click outside → collapse
		expandedRow = -1;
		draggingChannel = -1;
		panelHeight = computePanelHeight();
		panelY = (this.height - panelHeight) / 2;

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		draggingChannel = -1;
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void mouseClickMove(
		int mouseX,
		int mouseY,
		int clickedMouseButton,
		long timeSinceLastClick
	) {
		if (draggingChannel >= 0 && expandedRow >= 0) {
			applySliderDrag(expandedRow, draggingChannel, mouseX);
		}
		super.mouseClickMove(
			mouseX,
			mouseY,
			clickedMouseButton,
			timeSinceLastClick
		);
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		super.handleKeyboardInput();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1) {
			// ESC
			mc.displayGuiScreen(parent);
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	// -------------------------------------------------------------------------
	// Slider logic
	// -------------------------------------------------------------------------

	/**
	 * Returns the Y coordinate where the sliders for {@code colorIndex} start,
	 * assuming the row is currently expanded.
	 */
	private int sliderStartY(int colorIndex) {
		int y = panelY + HEADER_H + ROW_GAP;
		for (int i = 0; i < colorIndex; i++) {
			y += ROW_HEIGHT;
			if (expandedRow == i) y += EXPANDED_H + ROW_GAP;
			y += ROW_GAP;
		}
		y += ROW_HEIGHT + 2; // skip the collapsed row header
		return y;
	}

	/** Returns true and begins a drag if the click lands on any slider thumb. */
	private boolean tryDragSlider(int colorIndex, int mouseX, int mouseY) {
		int argb = getColor(colorIndex);
		int[] vals = { (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF };

		int trackX = panelX + PADDING + 24;
		int trackW = PANEL_WIDTH - PADDING * 2 - 24 - 34;
		int y = sliderStartY(colorIndex);

		for (int ch = 0; ch < 3; ch++) {
			int fillW = (int) (trackW * (vals[ch] / 255.0f));
			int thumbX = trackX + fillW - 3;

			if (isHovered(mouseX, mouseY, thumbX, y, 6, SLIDER_H)) {
				draggingChannel = ch;
				applySliderDrag(colorIndex, ch, mouseX);
				return true;
			}
			// Also allow clicking anywhere on the track to jump the thumb
			if (isHovered(mouseX, mouseY, trackX, y, trackW, SLIDER_H)) {
				draggingChannel = ch;
				applySliderDrag(colorIndex, ch, mouseX);
				return true;
			}
			y += SLIDER_H + SLIDER_GAP;
		}
		return false;
	}

	/** Re-computes one RGB channel from mouseX and writes it back to Theme. */
	private void applySliderDrag(int colorIndex, int channel, int mouseX) {
		int trackX = panelX + PADDING + 24;
		int trackW = PANEL_WIDTH - PADDING * 2 - 24 - 34;

		float fraction = (float) (mouseX - trackX) / trackW;
		fraction = Math.max(0f, Math.min(1f, fraction));
		int newVal = (int) (fraction * 255);

		int argb = getColor(colorIndex);
		int a = (argb >> 24) & 0xFF;
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;

		switch (channel) {
			case 0:
				r = newVal;
				break;
			case 1:
				g = newVal;
				break;
			case 2:
				b = newVal;
				break;
		}

		setColor(colorIndex, (a << 24) | (r << 16) | (g << 8) | b);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private int computePanelHeight() {
		int h = HEADER_H + ROW_GAP;
		for (int i = 0; i < LABELS.length; i++) {
			h += ROW_HEIGHT + ROW_GAP;
			if (expandedRow == i) {
				h += EXPANDED_H + ROW_GAP;
			}
		}
		h += FOOTER_H;
		return h;
	}

	private static boolean isHovered(
		int mouseX,
		int mouseY,
		int x,
		int y,
		int w,
		int h
	) {
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
	}

	private void drawCentered(
		String text,
		int x,
		int y,
		int w,
		int h,
		int color
	) {
		int tx = x + (w - this.fontRendererObj.getStringWidth(text)) / 2;
		int ty = y + (h - this.fontRendererObj.FONT_HEIGHT) / 2;
		this.fontRendererObj.drawStringWithShadow(text, tx, ty, color);
	}
}
