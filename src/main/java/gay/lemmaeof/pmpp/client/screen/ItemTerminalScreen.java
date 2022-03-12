package gay.lemmaeof.pmpp.client.screen;

import java.util.List;

import gay.lemmaeof.pmpp.api.InboxesComponent;
import gay.lemmaeof.pmpp.api.Message;
import gay.lemmaeof.pmpp.api.MessageThread;
import gay.lemmaeof.pmpp.init.PMPPComponents;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ItemTerminalScreen extends Screen {
	private TextFieldWidget messageField;
	private InboxesComponent inboxes;
	private MessageThread testThread;

	public ItemTerminalScreen() {
		super(new TranslatableText("pmpp.item_terminal.title"));
	}

	public void updateThread() {
		List<MessageThread> inbox = inboxes.getInbox(client.player);
		for (MessageThread thread : inbox) {
			if (thread.getMembers().size() == 1 && thread.getMembers().contains(client.player.getUuid())) {
				this.testThread = thread;
				break;
			}
		}
		System.out.println("TEST THREAD IS NULL! OH NO!");
	}

	@Override
	protected void init() {
		client.keyboard.setRepeatEvents(true);
		messageField = new TextFieldWidget(textRenderer, width/2 - 32, height/2 + 32, 64, 12, new LiteralText(""));
		setFocused(messageField);
		this.inboxes = PMPPComponents.INBOXES.get(client.world.getLevelProperties());
		updateThread();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.client.setScreen(null);
			return true;
		} else if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
			//TODO: keep
			if (keyCode == GLFW.GLFW_KEY_UP) {
				//TODO: message history?
//				this.setChatFromHistory(-1);
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_DOWN) {
				//TODO: message history?
//				this.setChatFromHistory(1);
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
				//TODO: chat scroll
//				this.client.inGameHud.getChatHud().scroll(this.client.inGameHud.getChatHud().getVisibleLineCount() - 1);
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
				//TODO: chat scroll
//				this.client.inGameHud.getChatHud().scroll(-this.client.inGameHud.getChatHud().getVisibleLineCount() + 1);
				return true;
			} else {
				return false;
			}
		} else {
			String string = this.messageField.getText().trim();
			if (!string.isEmpty()) {
				//TODO: send message! NYI!
				//this.sendMessage(string);
				this.messageField.setText("");
			}

			return true;
		}
	}

	@Override
	public void removed() {
		this.client.keyboard.setRepeatEvents(false);
	}

	@Override
	public void tick() {
		this.messageField.tick();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int x = 12;
		int y = 12;
		if (testThread != null) {
			for (Message message : testThread.getMessages()) {
				Text authorLine = inboxes.getName(message.getAuthor()).append(" - ").append(message.getTimestamp());
				Text contents = message.getMessage();
				this.textRenderer.draw(matrices, authorLine, x, y, 0xFFFFFF);
				y += 12;
				this.textRenderer.draw(matrices, contents, x + 4, y, 0xFFFFFF);
			}
		}
		this.messageField.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
	}
}
