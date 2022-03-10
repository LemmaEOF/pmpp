package gay.lemmaeof.pmpp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import gay.lemmaeof.pmpp.api.Message;
import gay.lemmaeof.pmpp.api.InboxesComponent;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.WorldProperties;

public class LevelInboxComponent implements InboxesComponent {
	private final WorldProperties level;
	private final Map<UUID, Text> cachedNames = new HashMap<>();
	private final Map<UUID, List<Message>> inboxes = new HashMap<>();
	private MinecraftServer server;

	public LevelInboxComponent(WorldProperties level) {
		this.level = level;
	}

	@Override
	public List<UUID> getKnownPlayers() {
		return cachedNames.keySet().stream().toList();
	}

	@Override
	public void updateName(UUID playerId, Text name) {
		cachedNames.put(playerId, name);
	}

	@Override
	public Text getName(UUID playerId) {
		PlayerEntity player = server.getPlayerManager().getPlayer(playerId);
		if (player != null) {
			Text name = player.getDisplayName();
			updateName(playerId, name);
			return name;
		}
		return cachedNames.getOrDefault(playerId, new LiteralText(playerId.toString()));
	}

	@Override
	public List<Message> getInbox(UUID playerId) {
		return inboxes.computeIfAbsent(playerId, (id) -> new ArrayList<>());
	}

	@Override
	public void sendMessage(UUID playerId, Message message) {
		getInbox(playerId).add(message);
	}


	public void setServer(MinecraftServer server) {
		this.server = server;
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		cachedNames.clear();
		inboxes.clear();

		NbtCompound namesTag = tag.getCompound("CachedNames");
		NbtCompound inboxesTag = tag.getCompound("Inboxes");

		for (String key : namesTag.getKeys()) {
			cachedNames.put(UUID.fromString(key), Text.Serializer.fromJson(namesTag.getString(key)));
		}

		for (String key : inboxesTag.getKeys()) {
			NbtList inboxTag = inboxesTag.getList(key, NbtElement.COMPOUND_TYPE);
			List<Message> messages = new ArrayList<>();
			for (NbtElement e : inboxTag) {
				messages.add(Message.fromNbt((NbtCompound) e));
			}
			inboxes.put(UUID.fromString(key), messages);
		}
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		NbtCompound namesTag = new NbtCompound();
		NbtCompound inboxesTag = new NbtCompound();

		for (UUID id : cachedNames.keySet()) {
			namesTag.putString(id.toString(), Text.Serializer.toJson(cachedNames.get(id)));
		}
		for (UUID id : inboxes.keySet()) {
			List<Message> inbox = inboxes.get(id);
			NbtList inboxTag = new NbtList();
			for (Message m : inbox) {
				inboxTag.add(m.toNbt());
			}
			inboxesTag.put(id.toString(), inboxTag);
		}

		tag.put("CachedNames", namesTag);
		tag.put("Inboxes", inboxesTag);
	}

}
