package gay.lemmaeof.pmpp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import gay.lemmaeof.pmpp.api.Message;
import gay.lemmaeof.pmpp.api.InboxesComponent;
import gay.lemmaeof.pmpp.api.MessageThread;
import gay.lemmaeof.pmpp.client.screen.ItemTerminalScreen;
import gay.lemmaeof.pmpp.init.PMPPComponents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.WorldProperties;

public class LevelInboxComponent implements InboxesComponent {
	//TODO: DELETE ME AND ASSOCIATED LOGIC BEFORE PUBLIC RELEASE! I EXIST PURELY TO DESTROY!
	private static final int DEV_HASH = 2;
	private final WorldProperties level;
	private final Map<UUID, MutableText> cachedNames = new HashMap<>();
	private final List<MessageThread> allThreads = new ArrayList<>();
	private final Map<UUID, List<MessageThread>> inboxes = new HashMap<>();
	private MinecraftServer server;

	public LevelInboxComponent(WorldProperties level) {
		this.level = level;
	}

	@Override
	public List<UUID> getKnownPlayers() {
		return cachedNames.keySet().stream().toList();
	}

	@Override
	public void updateName(UUID playerId, MutableText name) {
		cachedNames.put(playerId, name);
	}

	@Override
	public MutableText getName(UUID playerId) {
		PlayerEntity player = server.getPlayerManager().getPlayer(playerId);
		if (player != null) {
			MutableText name = player.getDisplayName().copy();
			updateName(playerId, name);
			return name;
		}
		return cachedNames.getOrDefault(playerId, new LiteralText(playerId.toString()));
	}

	@Override
	public List<MessageThread> getInbox(UUID playerId) {
		return inboxes.computeIfAbsent(playerId, (id) -> new ArrayList<>());
	}

	@Override
	public MessageThread createThread(String name, UUID... members) {
		int threadId = allThreads.size();
		MessageThread thread = new MessageThread(threadId, name, members);
		allThreads.add(thread);
		for (UUID id : members) {
			getInbox(id).add(thread);
		}
		thread.listen(this::markDirty);
		return thread;
	}

	public void setServer(MinecraftServer server) {
		this.server = server;
	}

	private void markDirty(int threadId, boolean participantsChanged) {
		if (participantsChanged) {
			MessageThread thread = allThreads.get(threadId);
			for (UUID id : inboxes.keySet()) {
				List<MessageThread> inbox = inboxes.get(id);
				boolean playerHasThread = inbox.contains(thread);
				boolean threadHasPlayer = thread.getMembers().contains(id);
				if (threadHasPlayer && !playerHasThread) {
					inbox.add(thread);
				}
				if (!threadHasPlayer && playerHasThread) {
					inbox.remove(thread);
				}
			}
		}
		PMPPComponents.INBOXES.sync(this.level);
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		cachedNames.clear();
		allThreads.clear();
		inboxes.clear();

		int hash = tag.getInt("DevHash");

		if (hash == DEV_HASH) {
			NbtCompound namesTag = tag.getCompound("CachedNames");
			NbtList threadsTag = tag.getList("Threads", NbtElement.COMPOUND_TYPE);

			for (String key : namesTag.getKeys()) {
				cachedNames.put(UUID.fromString(key), Text.Serializer.fromJson(namesTag.getString(key)));
			}
			for (NbtElement e : threadsTag) {
				NbtCompound threadTag = (NbtCompound) e;
				MessageThread thread = MessageThread.fromNbt(threadTag);
				allThreads.add(thread);
				for (UUID id : thread.getMembers()) {
					getInbox(id).add(thread);
				}
				thread.listen(this::markDirty);
			}
		}
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		tag.putInt("DevHash", DEV_HASH);

		NbtCompound namesTag = new NbtCompound();
		NbtList threadsTag = new NbtList();

		for (UUID id : cachedNames.keySet()) {
			namesTag.putString(id.toString(), Text.Serializer.toJson(cachedNames.get(id)));
		}
		for (MessageThread thread : allThreads) {
			threadsTag.add(thread.toNbt());
		}

		tag.put("CachedNames", namesTag);
		tag.put("Threads", threadsTag);
	}

	@Override
	public void applySyncPacket(PacketByteBuf buf) {
		InboxesComponent.super.applySyncPacket(buf);
		if (MinecraftClient.getInstance().currentScreen instanceof ItemTerminalScreen s) {
			s.updateThread();
		}
	}
}
