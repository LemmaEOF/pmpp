package gay.lemmaeof.pmpp.impl;

import gay.lemmaeof.pmpp.PMPP;
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
	public void writeNbt(NbtCompound tag) {
		NbtCompound stackTag = new NbtCompound();
		stack.writeNbt(stackTag);
		tag.put("Stack", stackTag);
	}

	@Override
	public Attachment.AttachmentType<?> getType() {
		return PMPP.STACK_ATTACHMENT;
	}

	public static final class AttachmentType implements Attachment.AttachmentType<ItemStackAttachment> {

		@Override
		public ItemStackAttachment fromNbt(NbtCompound nbt) {
			return new ItemStackAttachment(ItemStack.fromNbt(nbt.getCompound("Stack")));
		}
	}
}
