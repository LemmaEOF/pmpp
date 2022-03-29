package gay.lemmaeof.pmpp.client.screen.widget;

import java.util.function.Consumer;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import org.lwjgl.glfw.GLFW;

import net.minecraft.SharedConstants;
import net.minecraft.text.Text;

public class SubmittableTextWidget extends SpruceTextFieldWidget {
	private final Consumer<String> sendConsumer;

	public SubmittableTextWidget(Position position, int width, int height, Text title, Consumer<String> sendConsumer) {
		super(position, width, height, title);
		this.sendConsumer = sendConsumer;
	}

	@Override
	protected boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
		if (!this.isEditorActive())
			return false;

		//TODO: multi-line messages? check for modifiers?
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			this.sendConsumer.accept(this.getText());
			this.setText("");
			return true;
		}

		return super.onKeyPress(keyCode, scanCode, modifiers);
	}
}
