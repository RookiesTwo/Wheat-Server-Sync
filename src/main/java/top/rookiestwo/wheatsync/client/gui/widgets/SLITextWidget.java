package top.rookiestwo.wheatsync.client.gui.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class SLITextWidget extends TextWidget {
    private float horizontalAlignment;

    public SLITextWidget(Text message, TextRenderer textRenderer) {
        super(message, textRenderer);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        Text text = this.getMessage();
        TextRenderer textRenderer = this.getTextRenderer();
        int i = this.getX() + Math.round(this.horizontalAlignment * (float) (this.getWidth() - textRenderer.getWidth(text)));
        int j = this.getY() + (this.getHeight() - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, text, i, j, this.getTextColor(), false);
    }
}
