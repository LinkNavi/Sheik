package net.minecraft.sheik.ui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.sheik.SheikClient;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.ui.render.RenderUtil;
import net.minecraft.sheik.ui.theme.Theme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ModMenuScreen extends GuiScreen {
    private static final int HEADER_HEIGHT = 32;
    private static final int FOOTER_HEIGHT = 32;
    private static final int SIDE_PANEL_WIDTH = 90;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_STEP = 22;

    private int panelX, panelY, panelWidth, panelHeight;
    private int scrollOffset = 0;
    private boolean moveHudMode = false;
    private Module draggingHudModule = null;
    private int dragOffsetX, dragOffsetY;
    private String selectedCategory = null;

    @Override
    public void initGui() {
        panelWidth = 260;
        panelHeight = 220;
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;
        scrollOffset = 0;
        moveHudMode = false;
        draggingHudModule = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Background overlay
        RenderUtil.drawShadowedRoundedRect(panelX - 8, panelY - 8, panelWidth + 16, panelHeight + 16, 12, 0xA0151518);
        RenderUtil.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 10, Theme.PANEL_COLOR);
        RenderUtil.drawOutline(panelX, panelY, panelWidth, panelHeight, Theme.BORDER_COLOR);

        this.drawCenteredString(this.fontRendererObj, "Sheik Mod Menu", panelX + panelWidth / 2, panelY + 10, Theme.TEXT_PRIMARY);

        int buttonY = panelY + panelHeight - FOOTER_HEIGHT;
        int contentBottom = buttonY - 8;

        // Side panel for categories
        int sidePanelX = panelX;
        int sidePanelY = panelY + HEADER_HEIGHT;
        int sidePanelHeight = contentBottom - sidePanelY;
        RenderUtil.drawRoundedRect(sidePanelX, sidePanelY, SIDE_PANEL_WIDTH, sidePanelHeight, 8, Theme.ROW_COLOR);

        List<Module> modules = SheikClient.getInstance().getModuleManager().getModules();
        Set<String> categories = getCategories(modules);
        String activeCategory = getActiveCategory(categories);

        int catY = sidePanelY + 8;
        for (String category : categories) {
            if (catY + ROW_HEIGHT > sidePanelY + sidePanelHeight - 8) {
                break;
            }
            boolean selected = category.equals(activeCategory);
            int catColor = selected ? Theme.ACCENT : Theme.PANEL_COLOR;
            RenderUtil.drawRoundedRect(sidePanelX + 8, catY, SIDE_PANEL_WIDTH - 16, ROW_HEIGHT, 5, catColor);
            this.fontRendererObj.drawStringWithShadow(category, sidePanelX + 16, catY + 5, Theme.TEXT_PRIMARY);
            catY += ROW_STEP;
        }

        // Module list area (filtered by selected category)
        int listX = panelX + SIDE_PANEL_WIDTH + 8;
        int listY = panelY + HEADER_HEIGHT;
        int listWidth = panelWidth - SIDE_PANEL_WIDTH - 20;
        int y = listY + 8 - scrollOffset;
        for (Module module : getModulesInCategory(modules, activeCategory)) {
            if (y + ROW_HEIGHT > contentBottom) {
                break;
            }
            if (y < listY + 8) {
                y += ROW_STEP;
                continue;
            }
            boolean hovered = mouseX >= listX && mouseX <= listX + listWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT;
            int bg = hovered ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR;
            RenderUtil.drawRoundedRect(listX, y, listWidth, ROW_HEIGHT, 5, bg);
            this.fontRendererObj.drawStringWithShadow(module.getName(), listX + 8, y + 5, Theme.TEXT_PRIMARY);
            this.fontRendererObj.drawStringWithShadow(module.isEnabled() ? "ON" : "OFF", listX + listWidth - 30, y + 5, module.isEnabled() ? Theme.ACCENT : Theme.TEXT_MUTED);
            y += ROW_STEP;
        }

        // Move HUD Modules button
        boolean moveBtnHovered = mouseX >= panelX + 20 && mouseX <= panelX + panelWidth - 20 && mouseY >= buttonY && mouseY <= buttonY + ROW_HEIGHT;
        int btnColor = moveHudMode ? Theme.ACCENT : (moveBtnHovered ? Theme.ROW_HOVER_COLOR : Theme.ROW_COLOR);
        RenderUtil.drawRoundedRect(panelX + 20, buttonY, panelWidth - 40, ROW_HEIGHT, 6, btnColor);
        String btnText = moveHudMode ? "Exit HUD Move Mode" : "Move HUD Modules";
        this.drawCenteredString(this.fontRendererObj, btnText, panelX + panelWidth / 2, buttonY + 5, Theme.TEXT_PRIMARY);

        // HUD module drag overlay
        if (moveHudMode) {
            for (Module module : modules) {
                if (module instanceof HudPositionable) {
                    HudPositionable hud = (HudPositionable) module;
                    int mx = hud.getHudX();
                    int my = hud.getHudY();
                    boolean dragging = (draggingHudModule == module);
                    int color = dragging ? Theme.ACCENT : Theme.ROW_COLOR;
                    RenderUtil.drawRoundedRect(mx, my, 80, 18, 5, color);
                    this.fontRendererObj.drawStringWithShadow(module.getName(), mx + 8, my + 5, Theme.TEXT_PRIMARY);
                    if (dragging) {
                        RenderUtil.drawOutline(mx, my, 80, 18, Theme.ACCENT);
                    }
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

       

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        List<Module> modules = SheikClient.getInstance().getModuleManager().getModules();
        Set<String> categories = getCategories(modules);
        String activeCategory = getActiveCategory(categories);

        int buttonY = panelY + panelHeight - FOOTER_HEIGHT;
        int contentBottom = buttonY - 8;
        int sidePanelY = panelY + HEADER_HEIGHT;

        // Move HUD button
        if (mouseX >= panelX + 20 && mouseX <= panelX + panelWidth - 20 && mouseY >= buttonY && mouseY <= buttonY + ROW_HEIGHT) {
            moveHudMode = !moveHudMode;
            draggingHudModule = null;
            return;
        }

        if (moveHudMode) {
            for (Module module : modules) {
                if (module instanceof HudPositionable) {
                    HudPositionable hud = (HudPositionable) module;
                    int mx = hud.getHudX();
                    int my = hud.getHudY();
                    if (mouseX >= mx && mouseX <= mx + 80 && mouseY >= my && mouseY <= my + ROW_HEIGHT) {
                        draggingHudModule = module;
                        dragOffsetX = mouseX - mx;
                        dragOffsetY = mouseY - my;
                        return;
                    }
                }
            }
        } else {
            int catY = sidePanelY + 8;
            for (String category : categories) {
                if (catY + ROW_HEIGHT > contentBottom) {
                    break;
                }
                if (mouseX >= panelX + 8 && mouseX <= panelX + SIDE_PANEL_WIDTH - 8 && mouseY >= catY && mouseY <= catY + ROW_HEIGHT) {
                    selectedCategory = category;
                    scrollOffset = 0;
                    return;
                }
                catY += ROW_STEP;
            }

            // Normal module toggle
            int listX = panelX + SIDE_PANEL_WIDTH + 8;
            int listY = panelY + HEADER_HEIGHT;
            int listWidth = panelWidth - SIDE_PANEL_WIDTH - 20;
            int y = listY + 8 - scrollOffset;
            for (Module module : getModulesInCategory(modules, activeCategory)) {
                if (y + ROW_HEIGHT > contentBottom) {
                    break;
                }
                if (y < listY + 8) {
                    y += ROW_STEP;
                    continue;
                }
                if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                    module.toggle();
                    return;
                }
                y += ROW_STEP;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (moveHudMode && draggingHudModule != null) {
            if (draggingHudModule instanceof HudPositionable) {
                HudPositionable hud = (HudPositionable) draggingHudModule;
                hud.setHudX(mouseX - dragOffsetX);
                hud.setHudY(mouseY - dragOffsetY);
                SheikClient.getInstance().getConfigManager().save(SheikClient.getInstance().getModuleManager().getModules());
            }
            draggingHudModule = null;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (moveHudMode && draggingHudModule != null && draggingHudModule instanceof HudPositionable) {
            HudPositionable hud = (HudPositionable) draggingHudModule;
            hud.setHudX(mouseX - dragOffsetX);
            hud.setHudY(mouseY - dragOffsetY);
            SheikClient.getInstance().getConfigManager().save(SheikClient.getInstance().getModuleManager().getModules());
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private Set<String> getCategories(List<Module> modules) {
        Set<String> categories = new LinkedHashSet<>();
        for (Module module : modules) {
            categories.add(module.getCategory());
        }
        return categories;
    }

    private String getActiveCategory(Set<String> categories) {
        if (categories.isEmpty()) {
            selectedCategory = null;
            return null;
        }
        if (selectedCategory == null || !categories.contains(selectedCategory)) {
            selectedCategory = categories.iterator().next();
        }
        return selectedCategory;
    }

    private List<Module> getModulesInCategory(List<Module> modules, String category) {
        List<Module> filtered = new ArrayList<>();
        if (category == null) {
            return filtered;
        }
        for (Module module : modules) {
            if (category.equals(module.getCategory())) {
                filtered.add(module);
            }
        }
        return filtered;
    }
}
