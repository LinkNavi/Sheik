package net.minecraft.sheik.module.modules.combat;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.module.ModuleOption;
import net.minecraft.util.AxisAlignedBB;

public class AimDisplay extends Module {

	private final ModuleOption<Float> activationDistance = new ModuleOption<>(
		"activationDistance",
		5.0f,
		5.0f,
		Float.class
	);

	private final ModuleOption<Integer> indicatorSize = new ModuleOption<>(
		"indicatorSize",
		5,
		5,
		Integer.class
	);

	private final ModuleOption<Boolean> playersOnly = new ModuleOption<>(
		"playersOnly",
		true,
		true,
		Boolean.class
	);

	private Entity target;

	public AimDisplay() {
		super(
			"AimDisplay",
			"COMBAT",
			"Displays the best place to aim per target",
			-1
		);
		addOption(activationDistance);
		addOption(indicatorSize);
		addOption(playersOnly);
	}

	@Override
	public void onHit(Entity entity) {
		if (
			playersOnly.getValue() &&
			!(entity instanceof net.minecraft.entity.player.EntityPlayer)
		) {
			return;
		}
		this.target = entity;
	}

	@Override
	public void onTick() {
		if (!(target instanceof EntityLivingBase)) {
			target = null;
			return;
		}
		EntityLivingBase living = (EntityLivingBase) target;
		if (
			living.isDead ||
			living.getDistanceToEntity(mc.thePlayer) >
			activationDistance.getValue()
		) {
			target = null;
			return;
		}
	}

	/**
	 * Projects a world-space point onto the 2D screen using the player's
	 * camera yaw/pitch and the game's FOV. Returns null if the point is
	 * behind the camera.
	 *
	 * @param wx        world X
	 * @param wy        world Y
	 * @param wz        world Z
	 * @param sr        current ScaledResolution
	 * @param partialTicks interpolation factor
	 * @return int[]{screenX, screenY} in scaled GUI coordinates, or null if behind camera
	 */
	private int[] worldToScreen(
		double wx,
		double wy,
		double wz,
		ScaledResolution sr,
		float partialTicks
	) {
		// Interpolated eye position
		double eyeX =
			mc.thePlayer.lastTickPosX +
			(mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
		double eyeY =
			mc.thePlayer.lastTickPosY +
			(mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks +
			mc.thePlayer.getEyeHeight();
		double eyeZ =
			mc.thePlayer.lastTickPosZ +
			(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

		// Vector from eye to target point
		double dx = wx - eyeX;
		double dy = wy - eyeY;
		double dz = wz - eyeZ;

		// Camera yaw & pitch (interpolated)
		float yaw =
			mc.thePlayer.prevRotationYaw +
			(mc.thePlayer.rotationYaw - mc.thePlayer.prevRotationYaw) *
			partialTicks;
		float pitch =
			mc.thePlayer.prevRotationPitch +
			(mc.thePlayer.rotationPitch - mc.thePlayer.prevRotationPitch) *
			partialTicks;

		double yawRad = Math.toRadians(yaw);
		double pitchRad = Math.toRadians(pitch);

		double sinYaw = Math.sin(yawRad);
		double cosYaw = Math.cos(yawRad);
		double sinPitch = Math.sin(pitchRad);
		double cosPitch = Math.cos(pitchRad);

		// Forward (look direction)
		double fwdX = -sinYaw * cosPitch;
		double fwdY = -sinPitch;
		double fwdZ = cosYaw * cosPitch;

		// Right — horizontal plane only. At yaw=0 we look +Z, so right = -X.
		double rightX = -cosYaw;
		double rightY = 0.0;
		double rightZ = -sinYaw;

		// Up = right × forward (gives correct camera up for any pitch)
		double upX = rightY * fwdZ - rightZ * fwdY;
		double upY = rightZ * fwdX - rightX * fwdZ;
		double upZ = rightX * fwdY - rightY * fwdX;

		// Project delta onto camera axes
		double forward = dx * fwdX + dy * fwdY + dz * fwdZ;
		double right = dx * rightX + dy * rightY + dz * rightZ;
		double up = dx * upX + dy * upY + dz * upZ;

		// Behind the camera — don't draw
		if (forward <= 0.0) return null;

		// Field of view — Minecraft stores it in degrees
		double fovDeg = mc.gameSettings.fovSetting;
		double fovRad = Math.toRadians(fovDeg);

		int screenW = sr.getScaledWidth();
		int screenH = sr.getScaledHeight();

		// Half-screen sizes in "forward=1" NDC units
		double halfH = Math.tan(fovRad / 2.0);
		double halfW = halfH * ((double) screenW / (double) screenH);

		// NDC [-1..1]
		double ndcX = right / (forward * halfW);
		double ndcY = -up / (forward * halfH);

		// Guard against wildly off-screen values
		if (ndcX < -4.0 || ndcX > 4.0 || ndcY < -4.0 || ndcY > 4.0) return null;

		int gx = (int) ((ndcX + 1.0) * 0.5 * screenW);
		int gy = (int) ((ndcY + 1.0) * 0.5 * screenH);

		return new int[] { gx, gy };
	}

	@Override
	public void onRender2D(ScaledResolution sr, float partialTicks) {
		if (
			mc.getMinecraft().theWorld == null ||
			mc.getMinecraft().thePlayer == null
		) return;
		if (!(target instanceof EntityLivingBase)) return;

		EntityLivingBase living = (EntityLivingBase) target;

		// Interpolated target position
		double tx =
			living.lastTickPosX +
			(living.posX - living.lastTickPosX) * partialTicks;
		double ty =
			living.lastTickPosY +
			(living.posY - living.lastTickPosY) * partialTicks;
		double tz =
			living.lastTickPosZ +
			(living.posZ - living.lastTickPosZ) * partialTicks;

		// Rebuild AABB around interpolated position
		AxisAlignedBB bb = living.getEntityBoundingBox();
		double hw = (bb.maxX - bb.minX) / 2.0;
		double height = bb.maxY - bb.minY;
		double hz = (bb.maxZ - bb.minZ) / 2.0;

		double bbMinX = tx - hw,
			bbMaxX = tx + hw;
		double bbMinY = ty,
			bbMaxY = ty + height;
		double bbMinZ = tz - hz,
			bbMaxZ = tz + hz;

		// Interpolated eye position for closest-point calculation
		double eyeX =
			mc.thePlayer.lastTickPosX +
			(mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
		double eyeY =
			mc.thePlayer.lastTickPosY +
			(mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks +
			mc.thePlayer.getEyeHeight();
		double eyeZ =
			mc.thePlayer.lastTickPosZ +
			(mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

		// Closest point on the AABB to our eye — this is what the server checks
		double aimX = Math.max(bbMinX, Math.min(eyeX, bbMaxX));
		double aimY = Math.max(bbMinY, Math.min(eyeY, bbMaxY));
		double aimZ = Math.max(bbMinZ, Math.min(eyeZ, bbMaxZ));

		int[] screen = worldToScreen(aimX, aimY, aimZ, sr, partialTicks);
		if (screen == null) return;

		int gx = screen[0];
		int gy = screen[1];

		// Distance from (non-interpolated) eye to closest AABB point
		double eyeXt = mc.thePlayer.posX;
		double eyeYt = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
		double eyeZt = mc.thePlayer.posZ;
		double dist = Math.sqrt(
			(aimX - eyeXt) * (aimX - eyeXt) +
				(aimY - eyeYt) * (aimY - eyeYt) +
				(aimZ - eyeZt) * (aimZ - eyeZt)
		);

		float reach = mc.playerController.getCurrentGameType().isCreative()
			? 5.0f
			: 3.0f;
		int color = dist <= reach ? 0xFF00FF00 : 0xFFFF4444;

		int size = indicatorSize.getValue();
		Gui.drawRect(gx - size, gy - size, gx + size, gy + size, color);
	}
}
