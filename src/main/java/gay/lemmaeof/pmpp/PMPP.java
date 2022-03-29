package gay.lemmaeof.pmpp;

import java.util.Date;
import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Lifecycle;
import gay.lemmaeof.pmpp.api.Attachment;
import gay.lemmaeof.pmpp.api.InboxesComponent;
import gay.lemmaeof.pmpp.api.Message;
import gay.lemmaeof.pmpp.api.MessageThread;
import gay.lemmaeof.pmpp.impl.ItemStackAttachment;
import gay.lemmaeof.pmpp.init.PMPPComponents;
import gay.lemmaeof.pmpp.init.PMPPNetworking;
import gay.lemmaeof.pmpp.item.TerminalItem;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import net.fabricmc.loader.api.ModContainer;


public class PMPP implements ModInitializer {
	public static final String MODID = "pmpp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final RegistryKey<Registry<Attachment.AttachmentType<?>>> ATTACHMENT_TYPE_KEY = RegistryKey.ofRegistry(new Identifier(MODID, "attachment_types"));
	public static final Registry<Attachment.AttachmentType<?>> ATTACHMENT_TYPE = new SimpleRegistry<>(ATTACHMENT_TYPE_KEY, Lifecycle.stable(), null);

	public static final ItemStackAttachment.AttachmentType STACK_ATTACHMENT = new ItemStackAttachment.AttachmentType();

	@Override
	public void onInitialize(ModContainer container) {

		LOGGER.info("Hello Quilt world!");

		PMPPNetworking.init();

		Registry.register(Registry.ITEM, new Identifier(MODID, "test_terminal"), new TerminalItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1)));

		Registry.register(ATTACHMENT_TYPE, new Identifier(MODID, "item_stack"), STACK_ATTACHMENT);
		CommandRegistrationCallback.EVENT.register(((dispatcher, integrated, dedicated) -> {
			dispatcher.register(
					CommandManager.literal("pmpp")
							.then(CommandManager.literal("nts")
									.then(CommandManager.argument("contents", StringArgumentType.greedyString())
											.executes(context -> {
												String message = context.getArgument("contents", String.class);
												try {
													PlayerEntity player = context.getSource().getPlayer();
													Message m = new Message(
															new LiteralText(message),
															player.getUuid(),
															new Date(),
															null
													);
													InboxesComponent INBOXES = PMPPComponents.INBOXES.get(player.getWorld().getLevelProperties());
													List<MessageThread> threads = INBOXES.getInbox(player);
													for (MessageThread thread : threads) {
														if (thread.getMembers().size() == 1 && thread.getMembers().contains(player.getUuid())) {
															thread.sendMessage(m);
															context.getSource().sendFeedback(new LiteralText("Message sent!"), true);
															return 1;
														}
													}
													//did not find a thread, make a new one!
													MessageThread thread = INBOXES.createThread("Self-message", player);
													thread.sendMessage(m);
													context.getSource().sendFeedback(new LiteralText("Message sent!"), true);
													return 1;
												} catch (CommandSyntaxException e) {
													context.getSource().sendError(new LiteralText("Must be run as player!"));
													return -1;
												}
											})
									)
							)
			);
		}));
	}

}
