package gay.lemmaeof.pmpp.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/*	TODO: either simplify or complicate this a lot!
	Right now, it's kind of in a weird state where it's anything-in, ItemStack-out, but I might want to make it
	ItemStack-in, ItemStack-out or anything-in, anything-out! Which option would be better?
*/
public interface Attachment {
	ItemStack getAsStack();

	//TODO: this is ugly but there's a whooole heap of type coercion bs if I have a wildcarded type that *also* does serialization
	void writeNbt(NbtCompound tag);

	AttachmentType<?> getType();

	interface AttachmentType<T extends Attachment> {
		T fromNbt(NbtCompound nbt);
	}

}
