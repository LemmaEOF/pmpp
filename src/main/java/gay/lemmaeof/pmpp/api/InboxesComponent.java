package gay.lemmaeof.pmpp.api;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public interface InboxesComponent extends AutoSyncedComponent {
	void updateName(UUID playerId, Text name);
	Text getName(UUID playerId);
	default List<Message> getInbox(PlayerEntity player) {
		return getInbox(player.getUuid());
	}
	List<Message> getInbox(UUID playerId);
	default void sendMessage(PlayerEntity player, Message message) {
		sendMessage(player.getUuid(), message);
	}
	void sendMessage(UUID playerId, Message message);
	void setServer(MinecraftServer server);
}
