package net.minecraft.sheik.module.modules.combat;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.module.ModuleOption;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class AimDisplay extends Module {

    private final ModuleOption<Float> activationDistance = new ModuleOption<>("activationDistance", 5.0f, 5.0f,
            Float.class);

    private final ModuleOption<Integer> indicatorSize = new ModuleOption<>("indicatorSize", 5, 5, Integer.class);

    private Entity target;

    // The optimal world-space aim point (updated every tick)
    private double aimX, aimY, aimZ;

    // Projected screen position (updated in onRender3D)
    private int screenX, screenY;
    private boolean onScreen;

    public AimDisplay() {
        super("AimDisplay", "COMBAT", "Displays the best place to aim per target", -1);

        addOption(activationDistance);
        addOption(indicatorSize);
    }

    @Override
    public void onHit(Entity entity) {
        this.target = entity;
    }

    @Override
    public void onTick() {
        if (!(target instanceof EntityLivingBase)) {
            target = null;
            return;
        }
        EntityLivingBase living = (EntityLivingBase) target;
        if (living.isDead || living.getDistanceToEntity(mc.thePlayer) > activationDistance.getValue()) {
            target = null;
            return;
        }

        // Closest point on the target's AABB to our eye — maximum reach for minimum
        // distance
        AxisAlignedBB bb = living.getEntityBoundingBox();
        double eyeX = mc.thePlayer.posX;
        double eyeY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        double eyeZ = mc.thePlayer.posZ;

        aimX = Math.max(bb.minX, Math.min(eyeX, bb.maxX));
        aimY = Math.max(bb.minY, Math.min(eyeY, bb.maxY));
        aimZ = Math.max(bb.minZ, Math.min(eyeZ, bb.maxZ));
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (target == null)
            return;

        FloatBuffer model = BufferUtils.createFloatBuffer(16);
        FloatBuffer proj = BufferUtils.createFloatBuffer(16);
        IntBuffer view = BufferUtils.createIntBuffer(16);
        FloatBuffer win = BufferUtils.createFloatBuffer(3);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, proj);
        GL11.glGetInteger(GL11.GL_VIEWPORT, view);

        double rx = mc.getRenderManager().viewerPosX;
        double ry = mc.getRenderManager().viewerPosY;
        double rz = mc.getRenderManager().viewerPosZ;

        onScreen = GLU.gluProject(
                (float) (aimX - rx),
                (float) (aimY - ry),
                (float) (aimZ - rz),
                model, proj, view, win)
                && win.get(2) < 1.0f;

        if (onScreen) {
            screenX = (int) win.get(0);
            screenY = (int) (view.get(3) - win.get(1)); // flip Y (GL origin is bottom-left)
        }
    }

    @Override
    public void onRender2D(ScaledResolution sr, float partialTicks) {
        if (target == null || !onScreen)
            return;

        // Distance from eye to the closest AABB point (what the server actually checks)
        double eyeX = mc.thePlayer.posX;
        double eyeY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        double eyeZ = mc.thePlayer.posZ;
        double dist = Math.sqrt(
                (aimX - eyeX) * (aimX - eyeX) +
                        (aimY - eyeY) * (aimY - eyeY) +
                        (aimZ - eyeZ) * (aimZ - eyeZ));

        float reach = mc.playerController.getCurrentGameType().isCreative() ? 5.0f : 3.0f;
        int color = dist <= reach ? 0xFF00FF00 : 0xFFFF4444; // green = in range, red = out of range

        int size = indicatorSize.getValue();
        Gui.drawRect(screenX - size, screenY - size, screenX + size, screenY + size, color);
    }
}
