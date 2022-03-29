package gay.lemmaeof.pmpp.impl;

import java.util.*;

import dev.onyxstudios.cca.api.v3.level.LevelComponents;
import gay.lemmaeof.pmpp.api.InboxesComponent;
import gay.lemmaeof.pmpp.api.MessageThread;
import gay.lemmaeof.pmpp.client.screen.TerminalChatScreen;
import gay.lemmaeof.pmpp.init.PMPPComponents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.WorldProperties;

public class LevelInboxComponent implements InboxesComponent {
	//TODO: DELETE ME AND ASSOCIATED LOGIC BEFORE PUBLIC RELEASE! I EXIST PURELY TO DESTROY!
	private static final int DEV_HASH = 2;
	private final WorldProperties level;
	private final Map<UUID, MutableText> cachedNames = new HashMap<>();
	//TODO: Int2ObjectMap? CIIHBM?
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
		if (this.server != null) {
			PlayerEntity player = server.getPlayerManager().getPlayer(playerId);
			if (player != null) {
				MutableText name = player.getDisplayName().copy();
				updateName(playerId, name);
				return name;
			}
		}
		return cachedNames.getOrDefault(playerId, new LiteralText(playerId.toString()));
	}

	@Override
	public List<MessageThread> getInbox(UUID playerId) {
		return inboxes.computeIfAbsent(playerId, (id) -> new ArrayList<>());
	}

	//TODO: this will only be safe on server once I have partial sync support
	@Override
	public MessageThread getThread(int threadId) {
		return allThreads.get(threadId);
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
			System.out.println("Participants changed!");
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
		LevelComponents.sync(PMPPComponents.INBOXES, server);
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
				System.out.println("Deserializing cached name! UUID: " + key + ", JSON: " + namesTag.getString(key));
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

		Set<UUID> allKnownPlayers = new HashSet<>(cachedNames.keySet());

		if (server != null) {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				allKnownPlayers.add(player.getUuid());
			}
		}

		for (UUID id : allKnownPlayers) {
			namesTag.putString(id.toString(), Text.Serializer.toJson(getName(id)));
		}
		for (MessageThread thread : allThreads) {
			threadsTag.add(thread.toNbt());
		}

		tag.put("CachedNames", namesTag);
		tag.put("Threads", threadsTag);
	}

	//TODO: fully custom sync packets sometime so I'm not doing Everything
	@Override
	public void applySyncPacket(PacketByteBuf buf) {
		InboxesComponent.super.applySyncPacket(buf);
		if (MinecraftClient.getInstance().currentScreen instanceof TerminalChatScreen s) {
			s.updateThread();
		}
	}
}
