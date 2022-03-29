package gay.lemmaeof.pmpp.client.screen.background;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.widget.SpruceWidget;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public record TexturedBackground(Identifier textureId, int red, int green, int blue, int alpha) implements Background {
	@Override
	public void render(MatrixStack matrices, SpruceWidget widget, int vOffset, int mouseX, int mouseY, float delta) {
		var tessellator = Tessellator.getInstance();
		var bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
		RenderSystem.setShaderTexture(0, textureId);

		int right = widget.getX() + widget.getWidth();
		int bottom = widget.getY() + widget.getHeight();

		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(widget.getX(), bottom, 0)
				.texture(0, widget.getHeight() / 256.0f + vOffset)
				.color(red, green, blue, alpha).next();
		bufferBuilder.vertex(right, bottom, 0)
				.texture(widget.getWidth() / 256.0f, widget.getHeight() / 256.0f + vOffset)
				.color(red, green, blue, alpha).next();
		bufferBuilder.vertex(right, widget.getY(), 0)
				.texture(widget.getWidth() / 256.0f, 0 / 256.0f + vOffset)
				.color(red, green, blue, alpha).next();
		bufferBuilder.vertex(widget.getX(), widget.getY(), 0)
				.texture(0, 0 / 256.0f + vOffset)
				.color(red, green, blue, alpha).next();
		tessellator.draw();
	}
}
