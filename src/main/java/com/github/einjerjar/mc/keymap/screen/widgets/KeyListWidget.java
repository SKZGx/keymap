package com.github.einjerjar.mc.keymap.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class KeyListWidget extends EntryListWidget<KeyListWidget.KeyListEntry> {
    TextRenderer tr;
    public KeyListEntry selected = null;

    public KeyListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.tr = client.textRenderer;
    }

    public int addEntry(KeyListEntry entry) {
        return super.addEntry(entry);
    }

    public boolean removeEntry(KeyListEntry entry) {
        return super.removeEntry(entry);
    }

    public void _clearEntries() {
        super.clearEntries();
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public int getRowWidth() {
        return width - 10;
    }

    @Override
    protected int getScrollbarPositionX() {
        return width - 6 + left;
    }

    @Override
    public void render(MatrixStack m, int mouseX, int mouseY, float delta) {
        Tessellator ts = Tessellator.getInstance();
        BufferBuilder bb = ts.getBuffer();

        renderList(m, this.left, this.top, mouseX, mouseY, delta);
        renderScrollBar(bb, ts);
    }

    @Override
    public void setSelected(KeyListEntry selected) {
        if (this.selected != null) this.selected.selected = false;
        this.selected = selected;
        if (selected != null) selected.selected = true;
    }

    public void renderScrollBar(BufferBuilder bb, Tessellator ts) {
        int scrollbarStartX = this.getScrollbarPositionX();
        int scrollbarEndX = scrollbarStartX + 6;
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            int p = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getMaxPosition());
            p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
            int q = (int) this.getScrollAmount() * (this.bottom - this.top - p) / maxScroll + this.top;
            if (q < this.top) {
                q = this.top;
            }

            int colScrollBg = 0x44_000000;
            int colScrollFg = 0xaa_ffffff;

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bb.vertex(scrollbarStartX, this.bottom, 0.0D).color(colScrollBg).next();
            bb.vertex(scrollbarEndX, this.bottom, 0.0D).color(colScrollBg).next();
            bb.vertex(scrollbarEndX, this.top, 0.0D).color(colScrollBg).next();
            bb.vertex(scrollbarStartX, this.top, 0.0D).color(colScrollBg).next();

            bb.vertex(scrollbarStartX, q + p, 0.0D).color(colScrollFg).next();
            bb.vertex(scrollbarEndX, q + p, 0.0D).color(colScrollFg).next();
            bb.vertex(scrollbarEndX, q, 0.0D).color(colScrollFg).next();
            bb.vertex(scrollbarStartX, q, 0.0D).color(colScrollFg).next();
            ts.draw();
            RenderSystem.disableBlend();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        } else if (button == 0 && isScrolling(mouseX, mouseY, button)) {
            if (mouseY < (double) this.top) {
                this.setScrollAmount(0.0D);
            } else if (mouseY > (double) this.bottom) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double d = Math.max(1, this.getMaxScroll());
                int i = this.bottom - this.top;
                int j = MathHelper.clamp((int) ((float) (i * i) / (float) this.getMaxPosition()), 32, i - 8);
                double e = Math.max(1.0D, d / (double) (i - j));
                this.setScrollAmount(this.getScrollAmount() + deltaY * e);
            }

            return true;
        }
        return false;
    }

    public boolean isScrolling(double mouseX, double mouseY, int button) {
        return button == 0 && mouseX >= this.getScrollbarPositionX() && mouseX < (this.getScrollbarPositionX() + 6);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        KeyListEntry e = getEntryAtPosition(mouseX, mouseY);
        if (!isMouseOver(mouseX, mouseY)) return false;
        if (e != null) {
            if (selected != null) selected.selected = false;
            selected = e;

            if (e.mouseClicked(mouseX, mouseY, button)) {
                setFocused(e);
                setDragging(true);
                return true;
            }
        }

        setDragging(true);
        return isScrolling(mouseX, mouseY, button);
    }

    public static class KeyListEntry extends EntryListWidget.Entry<KeyListEntry> {
        TextRenderer tr;
        public KeyBinding key;
        public boolean selected;

        public KeyListEntry(KeyBinding key) {
            this.tr = MinecraftClient.getInstance().textRenderer;
            this.key = key;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            selected = true;
            return true;
        }

        @Override
        public void render(MatrixStack m, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int col = 0xff_ffffff;
            if (key.boundKey.getCode() != -1) {
                col = 0xff_55ff55;
            }
            Text text = new TranslatableText(key.getTranslationKey());
            String trimmed = tr.trimToWidth(String.format("%s [%s]", text.getString(), key.getBoundKeyLocalizedText().getString()), entryWidth);
            String[] sTrim = trimmed.split(" \\[", 2);
            if (sTrim.length < 2) {
                sTrim = new String[]{sTrim[0], ""};
            } else {
                sTrim[1] = "[" + sTrim[1];
            }
            int tx = tr.getWidth(sTrim[0]);
            tr.drawWithShadow(m, sTrim[0], x - 2, y, selected ? 0xff_ff8888 : 0xff_ffffff);
            tr.drawWithShadow(m, sTrim[1], x + tx + 4 - 2, y, selected ? 0xff_ff8888 : col);
        }
    }
}
