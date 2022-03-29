package gay.lemmaeof.pmpp.init;

import java.util.Date;
import java.util.UUID;

import gay.lemmaeof.pmpp.PMPP;
import gay.lemmaeof.pmpp.api.Attachment;
import gay.lemmaeof.pmpp.api.InboxesComponent;
import gay.lemmaeof.pmpp.api.Message;
import gay.lemmaeof.pmpp.api.MessageThread;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PMPPNetworking {
	public static final Identifier MESSAGE_SEND = new Identifier(PMPP.MODID, "message_send");
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(MESSAGE_SEND, ((server, player, handler, buf, responseSender) -> {
			System.out.println("Received packet!");
			Message message;
			int threadId = buf.readVarInt();
			Text contents = buf.readText();
			UUID author = buf.readUuid();
			Date timestamp = new Date(buf.readVarLong());
			if (buf.readBoolean()) {
				Attachment.AttachmentType<?> type = PMPP.ATTACHMENT_TYPE.get(buf.readIdentifier());
				Attachment att = type.fromNbt(buf.readNbt());
				message = new Message(contents, author, timestamp, att);
			} else {
				message = new Message(contents, author, timestamp, null);
			}
			//weirdly servers don't have a good level properties access...
			InboxesComponent inboxes = PMPPComponents.INBOXES.get(player.world.getLevelProperties());
			inboxes.getThread(threadId).sendMessage(message);
		}));
	}

	public static void sendMessage(MessageThread thread, Message message) {
		System.out.println("Constructing buf and sending!");
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(thread.getThreadId());
		buf.writeText(message.getMessage());
		buf.writeUuid(message.getAuthor());
		buf.writeVarLong(message.getRawTimestamp());
		if (message.hasAttachment()) {
			buf.writeBoolean(true);
			Attachment attachment = message.getRawAttachment();
			buf.writeIdentifier(PMPP.ATTACHMENT_TYPE.getId(attachment.getType()));
			NbtCompound attTag = new NbtCompound();
			attachment.writeNbt(attTag);
			buf.writeNbt(attTag);
		} else {
			buf.writeBoolean(false);
		}
		ClientPlayNetworking.send(MESSAGE_SEND, buf);
	}
}
