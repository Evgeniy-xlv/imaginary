package mchorse.imaginary.network.server;

import mchorse.imaginary.entity.EntityImage;
import mchorse.imaginary.network.common.PacketModifyImage;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerModifyImage extends ServerMessageHandler<PacketModifyImage>
{
    @Override
    public void run(EntityPlayerMP player, PacketModifyImage message)
    {
        EntityImage image = (EntityImage) player.worldObj.getEntityByID(message.id);

        image.modify(message.picture);
        image.notifyTrackers();
    }
}