package net.minecraft.sheik.module.modules.performance;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.sheik.module.Module;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skips rendering entities that are fully occluded by solid blocks.
 *
 * A daemon thread raycasts from the player's eye to each nearby entity
 * every 50 ms and caches the result.  The render thread reads from the
 * cache without blocking, so the worst case is one frame of stale data.
 */
public class EntityCulling extends Module {

    private static EntityCulling INSTANCE;

    /** entity ID → true means "render it" */
    private final ConcurrentHashMap<Integer, Boolean> visibilityCache = new ConcurrentHashMap<>();

    private Thread cullingThread;
    private volatile boolean running = false;

    public EntityCulling() {
        super("EntityCulling", "Performance", "Skip rendering entities hidden behind solid blocks", 0);
        INSTANCE = this;
    }

    /** Called from the render thread — safe to read without locking. */
    public static boolean isVisible(Entity entity) {
        if (INSTANCE == null || !INSTANCE.isEnabled()) return true;
        // Default true so entities are never accidentally hidden before first scan.
        return INSTANCE.visibilityCache.getOrDefault(entity.getEntityId(), true);
    }

    @Override
    public void onEnable() {
        running = true;
        cullingThread = new Thread(this::cullLoop, "sheik-entity-culling");
        cullingThread.setDaemon(true);
        cullingThread.start();
    }

    @Override
    public void onDisable() {
        running = false;
        visibilityCache.clear();
        if (cullingThread != null) {
            cullingThread.interrupt();
            cullingThread = null;
        }
    }

    private void cullLoop() {
        while (running) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                break;
            }

            Minecraft mc = Minecraft.getMinecraft();

            if (mc.theWorld == null || mc.thePlayer == null) {
                visibilityCache.clear();
                continue;
            }

            try {
                double eyeX = mc.thePlayer.posX;
                double eyeY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
                double eyeZ = mc.thePlayer.posZ;
                Vec3 eye = new Vec3(eyeX, eyeY, eyeZ);

                List<Entity> entities = mc.theWorld.getLoadedEntityList();

                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = entities.get(i);

                    if (entity == null) continue;

                    // Always render the local player (third-person) and
                    // entities that have explicitly opted out of culling.
                    if (entity == mc.thePlayer || entity.ignoreFrustumCheck) {
                        visibilityCache.put(entity.getEntityId(), true);
                        continue;
                    }

                    // Aim at the upper body — hitting legs or crown is less reliable.
                    Vec3 target = new Vec3(
                        entity.posX,
                        entity.posY + entity.height * 0.85,
                        entity.posZ
                    );

                    try {
                        MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(
                            eye, target,
                            false,  // stopOnLiquid
                            false,  // ignoreBlockWithoutBoundingBox
                            false   // returnLastUncollidable
                        );
                        // null → ray reached entity unobstructed → visible
                        visibilityCache.put(entity.getEntityId(), hit == null);
                    } catch (Exception ignored) {
                        // Chunk unloaded or world data race; assume visible.
                        visibilityCache.put(entity.getEntityId(), true);
                    }
                }
            } catch (Exception ignored) {
                // World reference changed mid-iteration; will retry next cycle.
                visibilityCache.clear();
            }
        }
    }
}
