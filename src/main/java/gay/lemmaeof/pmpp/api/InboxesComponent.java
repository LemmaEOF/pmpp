package gay.lemmaeof.pmpp.api;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//TODO: custom sync code to not send the entire message ecosystem state to every player! probably a bad idea to do that!
public interface InboxesComponent extends AutoSyncedComponent {
	List<UUID> getKnownPlayers();
	void updateName(UUID playerId, MutableText name);
	MutableText getName(UUID playerId);
	default List<MessageThread> getInbox(PlayerEntity player) {
		return getInbox(player.getUuid());
	}
	List<MessageThread> getInbox(UUID playerId);
	default MessageThread createThread(String name, PlayerEntity... members) {
		return createThread(name, (UUID[]) Arrays.stream(members).map(PlayerEntity::getUuid).toArray());
	}
	MessageThread createThread(String name, UUID... members);
}
