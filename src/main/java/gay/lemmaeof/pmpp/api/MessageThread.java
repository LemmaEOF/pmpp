package gay.lemmaeof.pmpp.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class MessageThread {
	private final int threadId;
	private String title;
	private final Set<UUID> members = new HashSet<>();
	private final List<Message> messages = new ArrayList<>();
	private final List<BiConsumer<Integer, Boolean>> listeners = new ArrayList<>();

	public MessageThread(int threadId, String title, UUID... members) {
		this.threadId = threadId;
		this.title = title;
		Collections.addAll(this.members, members);
	}

	public int getThreadId() {
		return threadId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<UUID> getMembers() {
		return members;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void sendMessage(Message message) {
		messages.add(message);
		markDirty(false);
	}

	public void addParticipant(UUID id) {
		members.add(id);
		markDirty(true);
	}

	public void removeParticipant(UUID id) {
		members.remove(id);
		markDirty(true);
	}

	private void markDirty(boolean participantsChanged) {
		for (BiConsumer<Integer, Boolean> c : listeners) {
			c.accept(threadId, participantsChanged);
		}
	}

	public void listen(BiConsumer<Integer, Boolean> c) {
		listeners.add(c);
	}

	public NbtCompound toNbt() {
		NbtCompound ret = new NbtCompound();
		ret.putInt("ThreadId", threadId);
		ret.putString("Title", title);
		NbtList membersTag = new NbtList();
		for (UUID id : members) {
			NbtCompound memberTag = new NbtCompound();
			memberTag.putUuid("Id", id);
			membersTag.add(memberTag);
		}
		ret.put("Members", membersTag);
		NbtList messagesTag = new NbtList();
		for (Message message : messages) {
			messagesTag.add(message.toNbt());
		}
		ret.put("Messages", messagesTag);
		return ret;
	}

	public static MessageThread fromNbt(NbtCompound tag) {
		int threadId = tag.getInt("ThreadId");
		String title = tag.getString("Title");
		MessageThread ret = new MessageThread(threadId, title);
		NbtList membersTag = tag.getList("Members", NbtElement.COMPOUND_TYPE);
		for (NbtElement e : membersTag) {
			NbtCompound member = (NbtCompound) e;
			ret.members.add(member.getUuid("Id"));
		}
		NbtList messagesTag = tag.getList("Messages", NbtElement.COMPOUND_TYPE);
		for (NbtElement e : messagesTag) {
			NbtCompound message = (NbtCompound) e;
			ret.messages.add(Message.fromNbt(message));
		}
		return ret;
	}

	@Override
	public String toString() {
		return "MessageThread{" +
				"threadId=" + threadId +
				", title='" + title + '\'' +
				", members=" + members +
				", messages=" + messages +
				'}';
	}
}
