package gay.lemmaeof.pmpp.api;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

public interface InboxesComponent extends AutoSyncedComponent {
	List<UUID> getKnownPlayers();
	void updateName(UUID playerId, Text name);
	Text getName(UUID playerId);
	//TODO: email-style or text-style? currently set up as email-style
	default List<Message> getInbox(PlayerEntity player) {
		return getInbox(player.getUuid());
	}
	List<Message> getInbox(UUID playerId);
	default void sendMessage(PlayerEntity player, Message message) {
		sendMessage(player.getUuid(), message);
	}
	void sendMessage(UUID playerId, Message message);
}
