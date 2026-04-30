package net.minecraft.sheik.external;

import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;

public class IPC {
    protected final Minecraft mc = Minecraft.getMinecraft();
    private MappedByteBuffer buf;

    public IPC() {
        try {
            RandomAccessFile file = new RandomAccessFile("/dev/shm/sheik", "rw");
            buf = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeState() {
        if (buf == null || mc.thePlayer == null) return;

        buf.position(0);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.putInt(mc.objectMouseOver != null &&
            mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK ? 1 : 0);
        buf.putInt(mc.thePlayer.inventory.currentItem);
        buf.putFloat(mc.thePlayer.rotationYaw);
        buf.putFloat(mc.thePlayer.rotationPitch);
        buf.putInt(mc.pointedEntity != null ? mc.pointedEntity.getEntityId() : -1);
        buf.putFloat(mc.pointedEntity instanceof EntityLivingBase
            ? ((EntityLivingBase) mc.pointedEntity).getHealth() : 0f);
        buf.put((byte) (mc.currentScreen != null ? 1 : 0));

        ItemStack stack = mc.thePlayer.getHeldItem();
        if (stack != null) {
            if (stack.getItem() instanceof ItemBlock) {
                buf.putInt(1);
            } else if (stack.getItem() instanceof ItemSword) {
                buf.putInt(2);
            } else if (stack.getItem() instanceof ItemAxe) {
                buf.putInt(3);
            } else {
                buf.putInt(0);
            }
        } else {
            buf.putInt(0);
        }

        buf.force();
    }
}
