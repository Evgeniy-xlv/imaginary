package mchorse.imaginary.entity;

import io.netty.buffer.ByteBuf;
import mchorse.imaginary.GuiHandler;
import mchorse.imaginary.network.Dispatcher;
import mchorse.imaginary.network.common.PacketModifyImage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * Entity image
 * 
 * This entity will be able to draw custom images. It also got lots of super 
 * cool stuff.
 */
public class EntityImage extends Entity implements IEntityAdditionalSpawnData
{
    /**
     * Pictures which are going to used 
     */
    public ResourceLocation picture;

    /**
     * Size width in blocks units
     */
    public float sizeW = 1;

    /**
     * Size height in blocks units 
     */
    public float sizeH = 1;

    /**
     * Hm, baby, oh, that's deep... Hm, so deep... :D 
     */
    public float deep = 0;

    public EntityImage(World worldIn)
    {
        super(worldIn);
        this.setSize(1.0F, 1.0F, 0.1F);
    }

    /**
     * Sets the width, height and deep of the entity.
     */
    public void setSize(float width, float height, float deep)
    {
        if (width != this.width || height != this.height || deep != this.deep)
        {
            this.width = width;
            this.height = height;
            this.deep = deep;

            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - width / 2, this.posY, this.posZ - deep / 2, this.posX + width / 2, this.posY + height, this.posZ + deep / 2));
        }
    }

    /**
     * Set position
     * 
     * This needs to be override for the purpose of setting the custom depth of 
     * the bounding box. 
     */
    @Override
    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        float f = this.width / 2.0F;
        float f1 = this.height;
        float f2 = this.deep / 2.0F;

        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y, z - (double) f2, x + (double) f, y + (double) f1, z + (double) f2));
    }

    /**
     * Kill this image 
     */
    @Override
    public boolean hitByEntity(Entity entityIn)
    {
        return entityIn instanceof EntityPlayer ? this.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) entityIn), 0.0F) : false;
    }

    /**
     * This is really weird, but if entity cannot be collided with, neither it 
     * can be punched...
     */
    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            if (!this.isDead && !this.worldObj.isRemote)
            {
                this.setDead();
                this.setBeenAttacked();
            }

            return true;
        }
    }

    @Override
    protected void entityInit()
    {}

    @Override
    public boolean processInitialInteract(EntityPlayer player, ItemStack stack, EnumHand hand)
    {
        GuiHandler.open(player, GuiHandler.PICTURE, this.getEntityId(), 0, 0);

        return true;
    }

    public void setPicture(String picture)
    {
        if (!picture.isEmpty())
        {
            this.picture = new ResourceLocation("imaginary.pictures", picture);
        }
        else
        {
            this.picture = null;
        }
    }

    public String getPicture()
    {
        return this.picture == null ? "" : this.picture.getResourcePath();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        this.setPicture(compound.getString("Picture"));
        this.sizeW = compound.getFloat("SizeW");
        this.sizeH = compound.getFloat("SizeH");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        compound.setString("Picture", this.getPicture());
        compound.setFloat("SizeW", this.sizeW);
        compound.setFloat("SizeH", this.sizeH);
    }

    /**
     * Read image data from the server 
     */
    @Override
    public void readSpawnData(ByteBuf buffer)
    {
        this.setPicture(ByteBufUtils.readUTF8String(buffer));

        this.sizeW = buffer.readFloat();
        this.sizeH = buffer.readFloat();

        this.setSize(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    /**
     * Write image data on the server 
     */
    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        ByteBufUtils.writeUTF8String(buffer, this.getPicture());

        buffer.writeFloat(this.sizeW);
        buffer.writeFloat(this.sizeH);

        buffer.writeFloat(this.width);
        buffer.writeFloat(this.height);
        buffer.writeFloat(this.deep);
    }

    public void modify(String picture)
    {
        this.setPicture(picture);
    }

    public void notifyTrackers()
    {
        Dispatcher.sendToServer(new PacketModifyImage(this.getEntityId(), this.picture.getResourcePath()));
    }
}