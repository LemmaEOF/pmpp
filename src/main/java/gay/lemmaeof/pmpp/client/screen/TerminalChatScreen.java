package gay.lemmaeof.pmpp.client.screen;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.border.SimpleBorder;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.util.ColorUtil;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import gay.lemmaeof.pmpp.PMPP;
import gay.lemmaeof.pmpp.api.InboxesComponent;
import gay.lemmaeof.pmpp.api.Message;
import gay.lemmaeof.pmpp.api.MessageThread;
import gay.lemmaeof.pmpp.client.screen.background.TexturedBackground;
import gay.lemmaeof.pmpp.client.screen.widget.SubmittableTextWidget;
import gay.lemmaeof.pmpp.init.PMPPComponents;
import gay.lemmaeof.pmpp.init.PMPPNetworking;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class TerminalChatScreen extends SpruceScreen {
	private static final Identifier BACKGROUND = new Identifier(PMPP.MODID, "textures/gui/mobile_terminal.png");
	private final Screen parent;
	private SpruceTextFieldWidget textField;
	private InboxesComponent inboxes;
	//TODO: get in ctor or the like
	private MessageThread testThread;

	public TerminalChatScreen(@Nullable Screen parent) {
		super(new TranslatableText("pmpp.item_terminal.title"));
		this.parent = parent;
	}

	public void updateThread() {
		List<MessageThread> inbox = inboxes.getInbox(client.player);
		for (MessageThread thread : inbox) {
			if (thread.getMembers().size() == 1 && thread.getMembers().contains(client.player.getUuid())) {
				this.testThread = thread;
				return;
			}
		}
	}

	@Override
	protected void init() {
		super.init();
		client.keyboard.setRepeatEvents(true);
		SpruceContainerWidget containerWidget = buildTextAreaContainer(Position.of(this, (this.width / 2) - 88, (this.height / 2) - 103), 176, 206, textArea -> {
			if (this.textField != null) {
				textArea.setText(this.textField.getText());
			}
			this.textField = textArea;
		}, btn -> this.client.setScreen(this.parent), text -> {
			System.out.println("Attempting to send message!");
			Message m = new Message(new LiteralText(text), this.client.player.getUuid(), new Date(), null);
			PMPPNetworking.sendMessage(testThread, m);
		});
		this.addDrawableChild(containerWidget);
		this.inboxes = PMPPComponents.INBOXES.get(client.world.getLevelProperties());
		updateThread();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		//TODO: move to widget
		int x = (this.width / 2) - 78;
		int y = (this.height / 2) - 81;
		if (testThread != null) {
			for (Message message : testThread.getMessages()) {
				Text authorLine = inboxes.getName(message.getAuthor()).append(" - ").append(message.getTimestamp());
				Text contents = message.getMessage();
				this.textRenderer.draw(matrices, authorLine, x, y, 0xFFFFFF);
				y += 12;
				this.textRenderer.draw(matrices, contents, x + 4, y, 0xFFFFFF);
				y += 12;
			}
		}
	}

	private static SpruceContainerWidget buildTextAreaContainer(Position position, int width, int height,
																Consumer<SpruceTextFieldWidget> textFieldConsumer,
																@Nullable SpruceButtonWidget.PressAction doneButtonAction,
																Consumer<String> sendConsumer) {
		int textFieldWidth = width - 14;
		SpruceTextFieldWidget textField = new SubmittableTextWidget(Position.of(width / 2 - textFieldWidth / 2, 162), textFieldWidth, 16,
				new LiteralText("Message Input"), sendConsumer);
		textField.setText("");
		textField.setBorder(new SimpleBorder(1, ColorUtil.WHITE, ColorUtil.WHITE));
		textFieldConsumer.accept(textField);
		// Display as many lines as possible
		textField.setCursorToStart();
		SpruceContainerWidget container = new SpruceContainerWidget(position, width, height);
		container.setBackground(new TexturedBackground(BACKGROUND, 255, 255, 255, 255));
		container.addChild(textField);

		/*
		int printToConsoleX = width / 2 - (doneButtonAction == null ? 75 : 155);
		// Print to console button, may be useful for debugging.
		container.addChild(new SpruceButtonWidget(Position.of(printToConsoleX, height - 29), 150, 20, new LiteralText("Print to console"),
				btn -> {
					System.out.println("########################## START TEXT AREA CONTENT ##########################");
					System.out.println(textField.getText());
					System.out.println("##########################  END TEXT AREA CONTENT  ##########################");
				}));
		// Add done button.
		if (doneButtonAction != null)
			container.addChild(new SpruceButtonWidget(Position.of(width / 2 - 155 + 160, height - 29), 150, 20, SpruceTexts.GUI_DONE,
					doneButtonAction));
		*/
		return container;
	}
}
