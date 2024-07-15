package top.rookiestwo.wheatsync.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import top.rookiestwo.wheatsync.WheatSync;
import top.rookiestwo.wheatsync.screen.SLIScreenHandler;

import java.util.Optional;

public class SLIScreen extends HandledScreen<SLIScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(WheatSync.MOD_ID, "textures/gui/container/standard_logistics_interface.png");
    private int communicationID;

    private ButtonWidget setCommunicationIDButton;
    private TextFieldWidget communicationIDInputBox;

    public SLIScreen(SLIScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, getPositionText(handler).orElse(title));
        communicationID = handler.getCommunicationID();
    }

    private static Optional<Text> getPositionText(ScreenHandler handler) {
        if (handler instanceof SLIScreenHandler) {
            String placerID = ((SLIScreenHandler) handler).getPlacerID();
            return placerID != null ? Optional.of(Text.literal(Text.translatable("title.wheatsync.standard_logistics_interface_placer").getString() + placerID)) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private void setCommunicationID() {
        WheatSync.LOGGER.info("button clicked.");
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        communicationIDInputBox.tick();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        communicationIDInputBox.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();
        playerInventoryTitleY = this.backgroundHeight - 111;
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        if (communicationID == 0) {
            setCommunicationIDButton = ButtonWidget.builder(
                            Text.translatable("text.wheatsync.sli_set_communicationID_button"), button -> {
                                setCommunicationID();
                            })
                    .dimensions(x + 133, y + 16, 34, 18)
                    .tooltip(Tooltip.of(Text.translatable("tooltip.wheatsync.sli_set_communicationID_button")))
                    .build();
            communicationIDInputBox = new TextFieldWidget(
                    this.textRenderer,
                    x + (backgroundWidth - 75) / 2,
                    y + 17, 75, 15,
                    Text.translatable("hint.wheatsync.sli_communicationID_input_box")
            );
            communicationIDInputBox.setMaxLength(5);
            communicationIDInputBox.setEditable(true);
            addDrawable(setCommunicationIDButton);
            addDrawable(communicationIDInputBox);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.player.closeHandledScreen();
        }
        WheatSync.LOGGER.info("press out." + communicationIDInputBox.isActive());
        if (communicationIDInputBox.isFocused()) {
            WheatSync.LOGGER.info("press in." + communicationIDInputBox.isActive());
            //限定输入数字
            switch (keyCode) {
                case GLFW.GLFW_KEY_0, GLFW.GLFW_KEY_KP_0 -> communicationIDInputBox.write("0");
                case GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_KP_1 -> communicationIDInputBox.write("1");
                case GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_KP_2 -> communicationIDInputBox.write("2");
                case GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_KP_3 -> communicationIDInputBox.write("3");
                case GLFW.GLFW_KEY_4, GLFW.GLFW_KEY_KP_4 -> communicationIDInputBox.write("4");
                case GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_KP_5 -> communicationIDInputBox.write("5");
                case GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_KP_6 -> communicationIDInputBox.write("6");
                case GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_KP_7 -> communicationIDInputBox.write("7");
                case GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_KP_8 -> communicationIDInputBox.write("8");
                case GLFW.GLFW_KEY_9, GLFW.GLFW_KEY_KP_9 -> communicationIDInputBox.write("9");
            }

            return communicationIDInputBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (communicationIDInputBox.mouseClicked(mouseX, mouseY, button)) {
            communicationIDInputBox.setFocused(true);
            WheatSync.LOGGER.info("clicked." + communicationIDInputBox.isActive());
            return true;
        }
        if (setCommunicationIDButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        communicationIDInputBox.setFocused(false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
