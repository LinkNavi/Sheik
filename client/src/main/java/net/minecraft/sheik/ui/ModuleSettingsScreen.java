package net.minecraft.sheik.ui;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.sheik.SheikClient;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.module.ModuleOption;
import net.minecraft.sheik.ui.render.RenderUtil;
import net.minecraft.sheik.ui.theme.Theme;
import org.lwjgl.input.Keyboard;

public class ModuleSettingsScreen extends GuiScreen {

    // -------------------------------------------------------------------------
    // Layout constants
    // -------------------------------------------------------------------------

    private static final int PANEL_WIDTH = 220;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 4;
    private static final int PADDING = 8;
    /** Vertical space consumed by a single row including the gap below it. */
    private static final int ROW_STEP = ROW_HEIGHT + ROW_GAP;

    /** Height used for every small interactive button. */
    private static final int BTN_HEIGHT = 16;
    /** Width of the [ - ] and [ + ] stepper buttons. */
    private static final int BTN_SMALL_W = 18;
    /** Width of the [ ON ] / [ OFF ] toggle button. */
    private static final int BTN_TOGGLE_W = 44;
    /** Width of the keybind display button. */
    private static final int BTN_BIND_W = 90;
    /** Width of the [ Back ] footer button. */
    private static final int BTN_BACK_W = 70;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final GuiScreen parent;
    private final Module module;

    /** True while the screen is waiting for the user to press a key to bind. */
    private boolean listeningForKeybind = false;

    // Panel geometry — computed in initGui
    private int panelX;
    private int panelY;
    private int panelHeight;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ModuleSettingsScreen(GuiScreen parent, Module module) {
        this.parent = parent;
        this.module = module;
    }

    // -------------------------------------------------------------------------
    // GuiScreen overrides
    // -------------------------------------------------------------------------

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        panelHeight = computePanelHeight();
        panelX = (this.width - PANEL_WIDTH) / 2;
        panelY = (this.height - panelHeight) / 2;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Dim the entire background
        RenderUtil.drawRect(0, 0, this.width, this.height, 0x80000000);

        // Panel shadow, body, and border
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

        int y = panelY + PADDING;

        // Header (module name + description)
        y = drawHeader(y);

        // Top divider
        RenderUtil.drawRect(
            panelX + PADDING,
            y,
            PANEL_WIDTH - PADDING * 2,
            1,
            Theme.BORDER_COLOR
        );
        y += 1 + ROW_GAP;

        // Keybind row
        y = drawKeybindRow(mouseX, mouseY, y);

        // One row per option
        List<ModuleOption<?>> options = module.getOptions();
        for (int i = 0; i < options.size(); i++) {
            y = drawOptionRow(options.get(i), mouseX, mouseY, y);
        }

        // Gap then footer divider
        y += ROW_GAP;
        RenderUtil.drawRect(
            panelX + PADDING,
            y,
            PANEL_WIDTH - PADDING * 2,
            1,
            Theme.BORDER_COLOR
        );
        y += 1 + ROW_GAP;

        // Back button
        drawBackButton(mouseX, mouseY, y);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // -------------------------------------------------------------------------
    // Section draw helpers (each returns the next y to continue drawing from)
    // -------------------------------------------------------------------------

    /**
     * Draws the module name and (optionally) its description.
     *
     * @param y top of the header block
     * @return y directly after the header block
     */
    private int drawHeader(int y) {
        // Module name
        this.fontRendererObj.drawStringWithShadow(
            module.getName(),
            panelX + PADDING,
            y + 2,
            Theme.TEXT_PRIMARY
        );
        y += this.fontRendererObj.FONT_HEIGHT + 4;

        // Description — trimmed so it never overflows the panel
        String desc = module.description;
        if (desc != null && !desc.isEmpty()) {
            String trimmed = this.fontRendererObj.trimStringToWidth(
                desc,
                PANEL_WIDTH - PADDING * 2
            );
            this.fontRendererObj.drawStringWithShadow(
                trimmed,
                panelX + PADDING,
                y,
                Theme.TEXT_MUTED
            );
            y += this.fontRendererObj.FONT_HEIGHT + 2;
        }

        y += ROW_GAP;
        return y;
    }

    /**
     * Draws the keybind label and its interactive button.
     *
     * @param y top of this row
     * @return y after this row
     */
    private int drawKeybindRow(int mouseX, int mouseY, int y) {
        // "Keybind" label on the left
        this.fontRendererObj.drawStringWithShadow(
            "Keybind",
            panelX + PADDING,
            y + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2,
            Theme.TEXT_PRIMARY
        );

        // Button on the right
        int btnX = panelX + PANEL_WIDTH - PADDING - BTN_BIND_W;
        int btnY = y + (ROW_HEIGHT - BTN_HEIGHT) / 2;
        boolean hovered = isHovered(
            mouseX,
            mouseY,
            btnX,
            btnY,
            BTN_BIND_W,
            BTN_HEIGHT
        );

        int bgColor = (hovered || listeningForKeybind)
            ? Theme.ROW_HOVER_COLOR
            : Theme.ROW_COLOR;
        RenderUtil.drawRoundedRect(
            btnX,
            btnY,
            BTN_BIND_W,
            BTN_HEIGHT,
            4,
            bgColor
        );

        // Accent outline while actively listening
        if (listeningForKeybind) {
            RenderUtil.drawOutline(
                btnX,
                btnY,
                BTN_BIND_W,
                BTN_HEIGHT,
                Theme.ACCENT
            );
        }

        String label = listeningForKeybind
            ? "[Press key...]"
            : "[" +
              (module.getKeybind() > 0
                  ? Keyboard.getKeyName(module.getKeybind())
                  : "NONE") +
              "]";
        int labelColor = listeningForKeybind
            ? Theme.ACCENT
            : Theme.TEXT_PRIMARY;
        drawCenteredInRect(
            label,
            btnX,
            btnY,
            BTN_BIND_W,
            BTN_HEIGHT,
            labelColor
        );

        return y + ROW_STEP;
    }

    /**
     * Draws a single option row, choosing the appropriate widget based on the
     * option's current value type.
     *
     * @param y top of this row
     * @return y after this row
     */
    private int drawOptionRow(
        ModuleOption<?> option,
        int mouseX,
        int mouseY,
        int y
    ) {
        // Label on the left
        this.fontRendererObj.drawStringWithShadow(
            option.getName(),
            panelX + PADDING,
            y + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2,
            Theme.TEXT_PRIMARY
        );

        // Widget on the right, dispatched by value type
        Object val = option.getValue();

        if (val instanceof Boolean) {
            drawToggleWidget((Boolean) val, mouseX, mouseY, y);
        } else if (val instanceof Integer) {
            drawIntWidget((Integer) val, mouseX, mouseY, y);
        } else if (val instanceof Float) {
            drawFloatWidget((Float) val, mouseX, mouseY, y);
        } else {
            // String — read-only; display value right-aligned
            String text = val != null ? val.toString() : "";
            int tx =
                panelX +
                PANEL_WIDTH -
                PADDING -
                this.fontRendererObj.getStringWidth(text);
            this.fontRendererObj.drawStringWithShadow(
                text,
                tx,
                y + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2,
                Theme.TEXT_MUTED
            );
        }

        return y + ROW_STEP;
    }

    /**
     * Draws a coloured [ ON ] / [ OFF ] toggle button flush to the right of the
     * row.
     */
    private void drawToggleWidget(
        boolean on,
        int mouseX,
        int mouseY,
        int rowY
    ) {
        int btnX = panelX + PANEL_WIDTH - PADDING - BTN_TOGGLE_W;
        int btnY = rowY + (ROW_HEIGHT - BTN_HEIGHT) / 2;
        boolean hovered = isHovered(
            mouseX,
            mouseY,
            btnX,
            btnY,
            BTN_TOGGLE_W,
            BTN_HEIGHT
        );

        int bgColor;
        int textColor;
        if (on) {
            bgColor = hovered ? brighten(Theme.ACCENT) : Theme.ACCENT;
            textColor = 0xFF101010; // dark text on bright accent background
        } else {
            bgColor = hovered ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR;
            textColor = Theme.TEXT_MUTED;
        }

        RenderUtil.drawRoundedRect(
            btnX,
            btnY,
            BTN_TOGGLE_W,
            BTN_HEIGHT,
            4,
            bgColor
        );
        drawCenteredInRect(
            on ? "[ ON ]" : "[ OFF ]",
            btnX,
            btnY,
            BTN_TOGGLE_W,
            BTN_HEIGHT,
            textColor
        );
    }

    /**
     * Draws a [ - ] N [ + ] stepper for an integer value.
     */
    private void drawIntWidget(int value, int mouseX, int mouseY, int rowY) {
        int btnY = rowY + (ROW_HEIGHT - BTN_HEIGHT) / 2;
        int rightX = panelX + PANEL_WIDTH - PADDING;

        // [ + ] — rightmost element
        int plusX = rightX - BTN_SMALL_W;
        boolean plusHov = isHovered(
            mouseX,
            mouseY,
            plusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT
        );
        RenderUtil.drawRoundedRect(
            plusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT,
            4,
            plusHov ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR
        );
        drawCenteredInRect(
            "+",
            plusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT,
            Theme.TEXT_PRIMARY
        );

        // Value label
        String valStr = Integer.toString(value);
        int valW = this.fontRendererObj.getStringWidth(valStr) + 4;
        int valX = plusX - 2 - valW;
        this.fontRendererObj.drawStringWithShadow(
            valStr,
            valX + 2,
            rowY + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2,
            Theme.TEXT_PRIMARY
        );

        // [ - ]
        int minusX = valX - 2 - BTN_SMALL_W;
        boolean minusHov = isHovered(
            mouseX,
            mouseY,
            minusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT
        );
        RenderUtil.drawRoundedRect(
            minusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT,
            4,
            minusHov ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR
        );
        drawCenteredInRect(
            "-",
            minusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT,
            Theme.TEXT_PRIMARY
        );
    }

    /**
     * Draws a [ - ] F.f [ + ] stepper for a float value (1 decimal place).
     */
    private void drawFloatWidget(
        float value,
        int mouseX,
        int mouseY,
        int rowY
    ) {
        int btnY = rowY + (ROW_HEIGHT - BTN_HEIGHT) / 2;
        int rightX = panelX + PANEL_WIDTH - PADDING;

        // [ + ] — rightmost element
        int plusX = rightX - BTN_SMALL_W;
        boolean plusHov = isHovered(
            mouseX,
            mouseY,
            plusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT
        );
        RenderUtil.drawRoundedRect(
            plusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT,
            4,
            plusHov ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR
        );
        drawCenteredInRect(
            "+",
            plusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT,
            Theme.TEXT_PRIMARY
        );

        // Value label
        String valStr = String.format("%.1f", value);
        int valW = this.fontRendererObj.getStringWidth(valStr) + 4;
        int valX = plusX - 2 - valW;
        this.fontRendererObj.drawStringWithShadow(
            valStr,
            valX + 2,
            rowY + (ROW_HEIGHT - this.fontRendererObj.FONT_HEIGHT) / 2,
            Theme.TEXT_PRIMARY
        );

        // [ - ]
        int minusX = valX - 2 - BTN_SMALL_W;
        boolean minusHov = isHovered(
            mouseX,
            mouseY,
            minusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT
        );
        RenderUtil.drawRoundedRect(
            minusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT,
            4,
            minusHov ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR
        );
        drawCenteredInRect(
            "-",
            minusX,
            btnY,
            BTN_SMALL_W,
            BTN_HEIGHT,
            Theme.TEXT_PRIMARY
        );
    }

    /**
     * Draws the centred [ Back ] button at the bottom of the panel.
     */
    private void drawBackButton(int mouseX, int mouseY, int btnTop) {
        int btnX = panelX + (PANEL_WIDTH - BTN_BACK_W) / 2;
        boolean hovered = isHovered(
            mouseX,
            mouseY,
            btnX,
            btnTop,
            BTN_BACK_W,
            BTN_HEIGHT
        );
        RenderUtil.drawRoundedRect(
            btnX,
            btnTop,
            BTN_BACK_W,
            BTN_HEIGHT,
            5,
            hovered ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR
        );
        drawCenteredInRect(
            "[ Back ]",
            btnX,
            btnTop,
            BTN_BACK_W,
            BTN_HEIGHT,
            Theme.TEXT_PRIMARY
        );
    }

    // -------------------------------------------------------------------------
    // Input — keyboard
    // -------------------------------------------------------------------------

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (listeningForKeybind) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                // Cancel listening; do NOT close the screen
                listeningForKeybind = false;
            } else {
                module.setKeybind(keyCode);
                listeningForKeybind = false;
                saveConfig();
            }
            // Either way: consume the key and return — no further handling
            return;
        }

        // Default behaviour (ESC closes the screen, etc.)
        super.keyTyped(typedChar, keyCode);
    }

    // -------------------------------------------------------------------------
    // Input — mouse
    // -------------------------------------------------------------------------

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
        throws IOException {
        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        // Reconstruct the exact same y-cursor used by drawScreen so that
        // every hit rectangle matches what was drawn on screen.
        int y = panelY + PADDING;

        // Advance past header
        y = skipHeader(y);

        // Advance past top divider
        y += 1 + ROW_GAP;

        // Keybind row
        if (hitKeybindRow(mouseX, mouseY, y)) return;
        y += ROW_STEP;

        // Option rows
        List<ModuleOption<?>> options = module.getOptions();
        for (int i = 0; i < options.size(); i++) {
            if (hitOptionRow(options.get(i), mouseX, mouseY, y)) return;
            y += ROW_STEP;
        }

        // Advance past gap + footer divider
        y += ROW_GAP + 1 + ROW_GAP;

        // Back button
        int btnX = panelX + (PANEL_WIDTH - BTN_BACK_W) / 2;
        if (isHovered(mouseX, mouseY, btnX, y, BTN_BACK_W, BTN_HEIGHT)) {
            mc.displayGuiScreen(parent);
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    // -------------------------------------------------------------------------
    // Hit-test helpers
    // -------------------------------------------------------------------------

    /**
     * Tests whether the user clicked the keybind button and, if so, enters
     * listening mode.
     *
     * @return true if the click was consumed
     */
    private boolean hitKeybindRow(int mouseX, int mouseY, int rowY) {
        int btnX = panelX + PANEL_WIDTH - PADDING - BTN_BIND_W;
        int btnY = rowY + (ROW_HEIGHT - BTN_HEIGHT) / 2;
        if (isHovered(mouseX, mouseY, btnX, btnY, BTN_BIND_W, BTN_HEIGHT)) {
            listeningForKeybind = true;
            return true;
        }
        return false;
    }

    /**
     * Tests whether the user clicked an interactive control inside one option
     * row, mutates the option accordingly, and saves config.
     *
     * <p>The unchecked casts here are all guarded by prior {@code instanceof}
     * checks on the raw value, so they are safe despite the erasure of the
     * wildcard.
     *
     * @return true if the click was consumed
     */
    @SuppressWarnings("unchecked")
    private boolean hitOptionRow(
        ModuleOption<?> option,
        int mouseX,
        int mouseY,
        int rowY
    ) {
        Object val = option.getValue();

        // --- Boolean toggle ---
        if (val instanceof Boolean) {
            int btnX = panelX + PANEL_WIDTH - PADDING - BTN_TOGGLE_W;
            int btnY = rowY + (ROW_HEIGHT - BTN_HEIGHT) / 2;
            if (
                isHovered(mouseX, mouseY, btnX, btnY, BTN_TOGGLE_W, BTN_HEIGHT)
            ) {
                ModuleOption<Boolean> boolOpt = (ModuleOption<Boolean>) option;
                boolOpt.setValue(!((Boolean) boolOpt.getValue()));
                saveConfig();
                return true;
            }

            // --- Integer stepper ---
        } else if (val instanceof Integer) {
            int btnY = rowY + (ROW_HEIGHT - BTN_HEIGHT) / 2;
            int rightX = panelX + PANEL_WIDTH - PADDING;
            int current = (Integer) val;

            // Reproduce the same geometry as drawIntWidget
            String valStr = Integer.toString(current);
            int valW = this.fontRendererObj.getStringWidth(valStr) + 4;
            int plusX = rightX - BTN_SMALL_W;
            int valX = plusX - 2 - valW;
            int minusX = valX - 2 - BTN_SMALL_W;

            ModuleOption<Integer> intOpt = (ModuleOption<Integer>) option;
            if (
                isHovered(mouseX, mouseY, plusX, btnY, BTN_SMALL_W, BTN_HEIGHT)
            ) {
                intOpt.setValue(current + 1);
                saveConfig();
                return true;
            }
            if (
                isHovered(mouseX, mouseY, minusX, btnY, BTN_SMALL_W, BTN_HEIGHT)
            ) {
                intOpt.setValue(current - 1);
                saveConfig();
                return true;
            }

            // --- Float stepper ---
        } else if (val instanceof Float) {
            int btnY = rowY + (ROW_HEIGHT - BTN_HEIGHT) / 2;
            int rightX = panelX + PANEL_WIDTH - PADDING;
            float current = (Float) val;

            // Reproduce the same geometry as drawFloatWidget
            String valStr = String.format("%.1f", current);
            int valW = this.fontRendererObj.getStringWidth(valStr) + 4;
            int plusX = rightX - BTN_SMALL_W;
            int valX = plusX - 2 - valW;
            int minusX = valX - 2 - BTN_SMALL_W;

            ModuleOption<Float> floatOpt = (ModuleOption<Float>) option;
            if (
                isHovered(mouseX, mouseY, plusX, btnY, BTN_SMALL_W, BTN_HEIGHT)
            ) {
                floatOpt.setValue(roundToOneDecimal(current + 0.1f));
                saveConfig();
                return true;
            }
            if (
                isHovered(mouseX, mouseY, minusX, btnY, BTN_SMALL_W, BTN_HEIGHT)
            ) {
                floatOpt.setValue(roundToOneDecimal(current - 0.1f));
                saveConfig();
                return true;
            }
        }

        // String options are read-only — click not consumed
        return false;
    }

    // -------------------------------------------------------------------------
    // Layout computation
    // -------------------------------------------------------------------------

    /**
     * Computes the total height needed for the panel given the current module's
     * name, description, and option list.
     *
     * <p>This method is called from {@link #initGui()}, at which point
     * {@code fontRendererObj} is already initialised by the superclass.
     */
    private int computePanelHeight() {
        int fontH =
            this.fontRendererObj != null ? this.fontRendererObj.FONT_HEIGHT : 9;

        int h = PADDING;

        // Header: module name
        h += fontH + 4;
        // Header: description (only if present)
        String desc = module.description;
        if (desc != null && !desc.isEmpty()) {
            h += fontH + 2;
        }
        h += ROW_GAP;

        // Top divider
        h += 1 + ROW_GAP;

        // Keybind row
        h += ROW_STEP;

        // One row per option
        h += module.getOptions().size() * ROW_STEP;

        // Gap + footer divider
        h += ROW_GAP + 1 + ROW_GAP;

        // Back button + bottom padding
        h += BTN_HEIGHT + PADDING;

        return h;
    }

    /**
     * Mirrors the vertical arithmetic of {@link #drawHeader(int)} without
     * performing any drawing, so that the mouse-click cursor stays aligned with
     * the render cursor.
     *
     * @param y starting y (same value passed to drawHeader)
     * @return y after the header block
     */
    private int skipHeader(int y) {
        int fontH =
            this.fontRendererObj != null ? this.fontRendererObj.FONT_HEIGHT : 9;

        // Name line
        y += fontH + 4;
        // Description line
        String desc = module.description;
        if (desc != null && !desc.isEmpty()) {
            y += fontH + 2;
        }
        y += ROW_GAP;
        return y;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * AABB check: returns {@code true} when ({@code mouseX}, {@code mouseY})
     * lies strictly inside the rectangle.
     */
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

    /**
     * Draws {@code text} centred both horizontally and vertically within the
     * given rectangle using {@code fontRendererObj}.
     */
    private void drawCenteredInRect(
        String text,
        int x,
        int y,
        int w,
        int h,
        int color
    ) {
        int tx = x + (w - this.fontRendererObj.getStringWidth(text)) / 2;
        int ty = y + (h - this.fontRendererObj.FONT_HEIGHT) / 2 + 1;
        this.fontRendererObj.drawStringWithShadow(text, tx, ty, color);
    }

    /**
     * Adds {@code +30} to each RGB channel of an ARGB colour, clamped at 255.
     * Used to produce a lighter hover state for the accent-coloured ON button.
     */
    private static int brighten(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 30);
        int g = Math.min(255, ((argb >> 8) & 0xFF) + 30);
        int b = Math.min(255, (argb & 0xFF) + 30);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Rounds {@code v} to one decimal place to prevent floating-point drift
     * that would otherwise accumulate over repeated + / - clicks.
     */
    private static float roundToOneDecimal(float v) {
        return Math.round(v * 10f) / 10f;
    }

    /**
     * Persists all module configuration via the {@link SheikClient} config
     * manager.
     */
    private void saveConfig() {
        SheikClient.getInstance()
            .getConfigManager()
            .save(SheikClient.getInstance().getModuleManager().getModules());
    }
}
