package net.minecraft.sheik.module.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.sheik.module.Module;
import org.lwjgl.opengl.GL11;

public class ReachVisualizer extends Module {

    private static final double REACH      = 3.0;
    private static final int    LATITUDES  = 8;
    private static final int    LONGITUDES = 8;
    private static final int    SEGMENTS   = 24;

    private Entity target;

    public ReachVisualizer() {
        super("ReachVisualizer", "COMBAT", "Shows the opponent's 3-block melee reach as a wireframe sphere", -1);
    }

    @Override
    public void onHit(Entity entity) {
        if (entity instanceof net.minecraft.entity.player.EntityPlayer) {
            this.target = entity;
        }
    }

    @Override
    public void onTick() {
        if (!(target instanceof EntityLivingBase)) { target = null; return; }
        EntityLivingBase living = (EntityLivingBase) target;
        if (living.isDead || living.getDistanceToEntity(mc.thePlayer) > 20.0f) target = null;
    }

    @Override
    public void onRender3D(float partialTicks) {
        if (mc.getMinecraft().theWorld == null || mc.getMinecraft().thePlayer == null) return;
        if (target == null) return;

        EntityLivingBase living = (EntityLivingBase) target;
        double ex = target.lastTickPosX + (target.posX - target.lastTickPosX) * partialTicks;
        double ey = target.lastTickPosY + (target.posY - target.lastTickPosY) * partialTicks
                  + living.getEyeHeight();
        double ez = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partialTicks;

        double rx = mc.getRenderManager().viewerPosX;
        double ry = mc.getRenderManager().viewerPosY;
        double rz = mc.getRenderManager().viewerPosZ;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 0.3f, 0.3f, 0.35f);

        drawSphere(ex - rx, ey - ry, ez - rz, REACH);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawSphere(double cx, double cy, double cz, double radius) {
        for (int lat = 0; lat <= LATITUDES; lat++) {
            double phi = Math.PI * lat / LATITUDES - Math.PI / 2;
            double r   = Math.cos(phi) * radius;
            double y   = Math.sin(phi) * radius;
            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (int i = 0; i < SEGMENTS; i++) {
                double theta = 2 * Math.PI * i / SEGMENTS;
                GL11.glVertex3d(cx + r * Math.cos(theta), cy + y, cz + r * Math.sin(theta));
            }
            GL11.glEnd();
        }

        for (int lon = 0; lon < LONGITUDES; lon++) {
            double theta = Math.PI * lon / LONGITUDES;
            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (int i = 0; i <= SEGMENTS; i++) {
                double phi    = Math.PI * i / SEGMENTS - Math.PI / 2;
                double cosPhi = Math.cos(phi);
                double sinPhi = Math.sin(phi);
                GL11.glVertex3d(
                    cx + cosPhi * radius * Math.cos(theta),
                    cy + sinPhi * radius,
                    cz + cosPhi * radius * Math.sin(theta)
                );
            }
            GL11.glEnd();
        }
    }
}
