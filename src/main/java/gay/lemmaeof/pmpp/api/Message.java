package gay.lemmaeof.pmpp.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import gay.lemmaeof.pmpp.PMPP;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class Message {
	private final Text message;
	private final UUID author;
	private final Date timestamp;
	@Nullable private Attachment attachment;

	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd-MM-yyyyy hh:mm:ss");

	public Message(Text message, UUID author, Date timestamp, @Nullable Attachment attachment) {
		this.message = message;
		this.author = author;
		this.timestamp = timestamp;
		this.attachment = attachment;
	}

	public Text getMessage() {
		return message;
	}

	public UUID getAuthor() {
		return author;
	}

	public String getTimestamp() {
		return FORMAT.format(timestamp);
	}

	public boolean hasAttachment() {
		return this.attachment != null;
	}

	public ItemStack getAttachment() {
		if (this.attachment != null) {
			return this.attachment.getAsStack();
		} else {
			return ItemStack.EMPTY;
		}
	}

	public ItemStack detach() {
		if (this.attachment != null) {
			ItemStack ret = this.attachment.getAsStack();
			this.attachment = null;
			return ret;
		} else {
			return ItemStack.EMPTY;
		}
	}

	public NbtCompound toNbt() {
		NbtCompound tag = new NbtCompound();
		tag.putString("Message", Text.Serializer.toJson(this.message));
		tag.putUuid("Author", this.author);
		tag.putString("Timestamp", FORMAT.format(this.timestamp));
		if (attachment != null) {
			NbtCompound attTag = new NbtCompound();
			attachment.getSerializer().toNbt(attachment, attTag);
			attTag.putString("type", PMPP.ATTACHMENT_SERIALIZER.getId(attachment.getSerializer()).toString());
			tag.put("Attachment", attTag);
		}
		return tag;
	}

	public static Message fromNbt(NbtCompound tag) {
		Text message = Text.Serializer.fromJson(tag.getString("Message"));
		UUID author = tag.getUuid("Author");
		try {
			Date timestamp = FORMAT.parse(tag.getString("Timestamp"));
			if (tag.contains("Attachment", NbtElement.COMPOUND_TYPE)) {
				NbtCompound attTag = tag.getCompound("Attachment");
				Identifier type = new Identifier(attTag.getString("type"));
				Attachment.Serializer<?> serializer = PMPP.ATTACHMENT_SERIALIZER.get(type);
				return new Message(message, author, timestamp, serializer.fromNbt(attTag));
			} else {
				return new Message(message, author, timestamp, null);
			}
		} catch (ParseException e) {
			throw new IllegalArgumentException("Unparseable timestamp: ", e);
		}
	}
}
