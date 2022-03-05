package gay.lemmaeof.pmpp.init;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import gay.lemmaeof.pmpp.PMPP;
import gay.lemmaeof.pmpp.api.InboxesComponent;
import gay.lemmaeof.pmpp.impl.LevelInboxComponent;

import net.minecraft.util.Identifier;

public class PMPPComponents implements LevelComponentInitializer {
	public static final ComponentKey<InboxesComponent> INBOXES = ComponentRegistry.getOrCreate(new Identifier(PMPP.MODID, "inboxes"), InboxesComponent.class);

	@Override
	public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
			registry.register(INBOXES, LevelInboxComponent::new);
	}
}
