package gay.lemmaeof.pmpp;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import net.fabricmc.api.ModInitializer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Lifecycle;
import gay.lemmaeof.pmpp.api.Attachment;
import gay.lemmaeof.pmpp.api.InboxesComponent;
import gay.lemmaeof.pmpp.api.Message;
import gay.lemmaeof.pmpp.impl.ItemStackAttachment;
import gay.lemmaeof.pmpp.init.PMPPComponents;
import net.minecraft.command.argument.ItemStackArgument;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.WorldProperties;

public class PMPP implements ModInitializer {
	public static final String MODID = "pmpp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final RegistryKey<Registry<Attachment.Serializer<?>>> ATTACHMENT_SERIALIZER_KEY = RegistryKey.ofRegistry(new Identifier(MODID, "attachment_serializers"));
	public static final Registry<Attachment.Serializer<?>> ATTACHMENT_SERIALIZER = new SimpleRegistry<>(ATTACHMENT_SERIALIZER_KEY, Lifecycle.stable(), null);

	public static final ItemStackAttachment.Serializer STACK_ATTACHMENT = new ItemStackAttachment.Serializer();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Quilt world!");

		Registry.register(ATTACHMENT_SERIALIZER, new Identifier(MODID, "item_stack"), STACK_ATTACHMENT);

		CommandRegistrationCallback.EVENT.register(((dispatcher, integrated, dedicated) -> {
			dispatcher.register(CommandManager.literal("pmpp")
					.then(CommandManager.literal("get")
							.executes(context -> {
								WorldProperties properties = context.getSource().getWorld().getLevelProperties();
								ServerPlayerEntity player = context.getSource().getPlayer();
								InboxesComponent inboxes = PMPPComponents.INBOXES.get(properties);
								List<Message> inbox = inboxes.getInbox(player);
								for (Message message : inbox) {
									MutableText text = new LiteralText("Message from ")
											.append(inboxes.getName(message.getAuthor()))
											.append(new LiteralText(" at " + message.getTimestamp() + ":\n"))
											.append(message.getMessage());
									if (message.hasAttachment()) {
										text = text.append(new LiteralText("\n Attachment: "))
												.append(message.getAttachment().toHoverableText());
									}
									context.getSource().sendFeedback(text, false);
								}
								return 1;
							})
					)
					.then(CommandManager.literal("send")
							.then(CommandManager.argument("player", EntityArgumentType.player())
									.then(CommandManager.argument("attachment", ItemStackArgumentType.itemStack())
											.then(CommandManager.argument("message", StringArgumentType.greedyString())
													.executes(ctx -> PMPP.sendMessage(ctx, true))))
									.then(CommandManager.argument("message", StringArgumentType.greedyString())
											.executes(ctx -> PMPP.sendMessage(ctx, false))
									)
							)
					)
			);
		}));
	}

	private static int sendMessage(CommandContext<ServerCommandSource> context, boolean hasAttachment) {
		try {
			WorldProperties properties = context.getSource().getWorld().getLevelProperties();
			InboxesComponent inboxes = PMPPComponents.INBOXES.get(properties);
			PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
			UUID author = context.getSource().getPlayer().getUuid();
			String message = context.getArgument("message", String.class);
			Date timestamp = new Date();
			if (hasAttachment) {
				LOGGER.info("Checking for attachment!");
				ItemStackArgument arg = ItemStackArgumentType.getItemStackArgument(context, "attachment");
				ItemStack stack = arg.createStack(1, false);
				ItemStackAttachment attachment = new ItemStackAttachment(stack);
				Message m = new Message(new LiteralText(message), author, timestamp, attachment);
				inboxes.sendMessage(player, m);
			} else {
				LOGGER.info("Attachment not found!");
				Message m = new Message(new LiteralText(message), author, timestamp, null);
				inboxes.sendMessage(player, m);
			}
			return 1;
		} catch (CommandSyntaxException e) {
			context.getSource().sendError(new LiteralText("Only players may send mail!"));
			return -1;
		}
	}
}
