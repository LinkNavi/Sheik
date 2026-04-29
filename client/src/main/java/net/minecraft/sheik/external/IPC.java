package net.minecraft.sheik.external;

import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

public class IPC {

	protected final Minecraft mc = Minecraft.getMinecraft();
	private MappedByteBuffer buf;

	public IPC() {
		try {
			RandomAccessFile file = new RandomAccessFile(
				"/dev/shm/sheik",
				"rw"
			);
			buf = file
				.getChannel()
				.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeState() {
		if (buf == null || Minecraft.getMinecraft().thePlayer == null) return;
		Minecraft mc = Minecraft.getMinecraft();
		buf.position(0);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(
			mc.objectMouseOver != null &&
				mc.objectMouseOver.typeOfHit ==
				MovingObjectPosition.MovingObjectType.BLOCK
				? 1
				: 0
		);
		buf.putInt(mc.thePlayer.inventory.currentItem);
		buf.putFloat(mc.thePlayer.rotationYaw);
		buf.putFloat(mc.thePlayer.rotationPitch);
		buf.putInt(
			mc.pointedEntity != null ? mc.pointedEntity.getEntityId() : -1
		);
		buf.putFloat(
			mc.pointedEntity instanceof EntityLivingBase
				? ((EntityLivingBase) mc.pointedEntity).getHealth()
				: 0f
		);
		buf.put((byte) (mc.currentScreen != null ? 1 : 0));
		buf.force();
	}
}
