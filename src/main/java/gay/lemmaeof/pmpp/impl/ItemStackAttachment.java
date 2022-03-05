package gay.lemmaeof.pmpp.impl;

import gay.lemmaeof.pmpp.api.Attachment;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemStackAttachment implements Attachment {
	private final ItemStack stack;

	public ItemStackAttachment(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public ItemStack getAsStack() {
		return stack;
	}

	@Override
	public <T extends Attachment> Attachment.Serializer<T> getSerializer() {
		return null;
	}

	public static final class Serializer implements Attachment.Serializer<ItemStackAttachment> {

		@Override
		public void toNbt(ItemStackAttachment attachment, NbtCompound nbt) {
			NbtCompound stackTag = new NbtCompound();
			attachment.stack.writeNbt(stackTag);
			nbt.put("Stach", stackTag);
		}

		@Override
		public ItemStackAttachment fromNbt(NbtCompound nbt) {
			return new ItemStackAttachment(ItemStack.fromNbt(nbt.getCompound("Stack")));
		}
	}
}
