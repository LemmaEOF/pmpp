package gay.lemmaeof.pmpp.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface Attachment {
	ItemStack getAsStack();

	<T extends Attachment> Serializer<T> getSerializer();

	interface Serializer<T extends Attachment> {
		void toNbt(T attachment, NbtCompound nbt);
		T fromNbt(NbtCompound nbt);
	}

}
