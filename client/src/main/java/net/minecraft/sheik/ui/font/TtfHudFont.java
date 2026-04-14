package net.minecraft.sheik.ui.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TtfHudFont implements HudFont {
    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 126;
    private static final int ATLAS_SIZE = 512;

    private final Map<Character, Glyph> glyphs = new HashMap<>();
    private final ResourceLocation textureLocation;
    private final int fontHeight;

    public TtfHudFont(String resourcePath, float size) throws IOException, FontFormatException {
        Minecraft mc = Minecraft.getMinecraft();
        Font baseFont;
        try (InputStream stream = mc.getResourceManager().getResource(new ResourceLocation(resourcePath)).getInputStream()) {
            baseFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
        }

        BufferedImage canvas = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setFont(baseFont);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics metrics = graphics.getFontMetrics();
        int ascent = metrics.getAscent();
        this.fontHeight = metrics.getHeight();

        int x = 2;
        int y = 2;
        for (char c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            int glyphWidth = Math.max(1, metrics.charWidth(c));
            int glyphHeight = this.fontHeight;

            if (x + glyphWidth + 2 >= ATLAS_SIZE) {
                x = 2;
                y += this.fontHeight + 3;
            }
            if (y + glyphHeight + 2 >= ATLAS_SIZE) {
                break;
            }

            graphics.drawString(String.valueOf(c), x, y + ascent);
            glyphs.put(c, new Glyph(x, y, glyphWidth, glyphHeight, glyphWidth));
            x += glyphWidth + 3;
        }
        graphics.dispose();

        DynamicTexture dynamicTexture = new DynamicTexture(canvas);
        this.textureLocation = mc.getTextureManager().getDynamicTextureLocation("sheik_ttf_font", dynamicTexture);
    }

    @Override
    public int drawStringWithShadow(String text, int x, int y, int color) {
        drawString(text, x + 1, y + 1, darken(color));
        drawString(text, x, y, color);
        return x + getStringWidth(text);
    }

    public void drawString(String text, int x, int y, int color) {
        if (text == null || text.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(textureLocation);

        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        setGlColor(color);

        int drawX = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphs.get(c);
            if (glyph == null) {
                glyph = glyphs.get('?');
            }
            if (glyph == null) {
                drawX += fontHeight / 2;
                continue;
            }

            Gui.drawModalRectWithCustomSizedTexture(drawX, y, glyph.u, glyph.v, glyph.width, glyph.height, ATLAS_SIZE, ATLAS_SIZE);
            drawX += glyph.advance;
        }
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            Glyph glyph = glyphs.get(text.charAt(i));
            width += glyph != null ? glyph.advance : fontHeight / 2;
        }
        return width;
    }

    @Override
    public int getFontHeight() {
        return fontHeight;
    }

    private static int darken(int color) {
        int a = (color >>> 24) & 0xFF;
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;
        r = (int)(r * 0.25f);
        g = (int)(g * 0.25f);
        b = (int)(b * 0.25f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void setGlColor(int color) {
        float a = ((color >>> 24) & 0xFF) / 255.0F;
        float r = ((color >>> 16) & 0xFF) / 255.0F;
        float g = ((color >>> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        GlStateManager.color(r, g, b, a);
    }

    private static class Glyph {
        final int u;
        final int v;
        final int width;
        final int height;
        final int advance;

        Glyph(int u, int v, int width, int height, int advance) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
            this.advance = advance;
        }
    }
}
