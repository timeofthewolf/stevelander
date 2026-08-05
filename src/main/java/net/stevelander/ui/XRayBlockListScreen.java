package net.stevelander.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.stevelander.Stevelander;
import net.stevelander.feature.XRay;

public class XRayBlockListScreen extends Screen {

    private final Screen lastScreen;
    private boolean selectedOnly;
    private EditBox search;
    private BlockList list;

    public XRayBlockListScreen(Screen lastScreen) {
        super(Component.translatable("stevelander.xray.blocks"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.search = new EditBox(this.font, this.width / 2 - 100, 26, 200, 20,
            Component.translatable("stevelander.xray.search"));
        this.search.setHint(Component.translatable("stevelander.xray.search"));
        this.search.setResponder(text -> this.list.refresh(text, this.selectedOnly));
        this.addRenderableWidget(this.search);

        this.list = new BlockList(this.width, this.height - 108, 56, 20);
        this.addRenderableWidget(this.list);

        final Button filter = this.addRenderableWidget(Button.builder(
            filterLabel(),
            b -> {
                this.selectedOnly = !this.selectedOnly;
                b.setMessage(filterLabel());
                this.list.refresh(this.search.getValue(), this.selectedOnly);
            }
        ).bounds(this.width / 2 - 154, this.height - 32, 150, 20).build());
        filter.setMessage(filterLabel());

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            b -> this.onClose()
        ).bounds(this.width / 2 + 4, this.height - 32, 150, 20).build());

        this.list.refresh(this.search.getValue(), this.selectedOnly);
    }

    private Component filterLabel() {
        return Component.translatable(this.selectedOnly
            ? "stevelander.xray.showingSelected"
            : "stevelander.xray.showingAll");
    }

    @Override
    public void onClose() {
        Stevelander.config().save();
        XRay.invalidate();
        this.minecraft.setScreenAndShow(this.lastScreen);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
        extractor.centeredText(this.font, this.title, this.width / 2, 12, -1);
        extractor.centeredText(this.font,
            Component.translatable("stevelander.xray.count", Stevelander.config().xray.blocks.size()),
            this.width / 2, this.height - 46, 0xFFAAAAAA);
    }

    private class BlockList extends ObjectSelectionList<BlockList.Entry> {

        BlockList(int width, int height, int y, int itemHeight) {
            super(XRayBlockListScreen.this.minecraft, width, height, y, itemHeight);
        }

        void refresh(String query, boolean onlySelected) {
            this.clearEntries();

            final String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            final List<String> selected = Stevelander.config().xray.blocks;
            final List<Block> blocks = new ArrayList<>();

            for (Block block : BuiltInRegistries.BLOCK) {
                final String id = BuiltInRegistries.BLOCK.getKey(block).toString();

                if (onlySelected && !selected.contains(id)) {
                    continue;
                }
                if (!needle.isEmpty() && !id.contains(needle)) {
                    continue;
                }

                blocks.add(block);
            }

            blocks.sort(Comparator
                .comparing((Block b) -> !selected.contains(BuiltInRegistries.BLOCK.getKey(b).toString()))
                .thenComparing(b -> BuiltInRegistries.BLOCK.getKey(b).toString()));

            for (Block block : blocks) {
                this.addEntry(new Entry(block));
            }
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {

            private final String id;

            Entry(Block block) {
                this.id = BuiltInRegistries.BLOCK.getKey(block).toString();
            }

            private boolean isSelected() {
                return Stevelander.config().xray.blocks.contains(this.id);
            }

            @Override
            public Component getNarration() {
                return Component.literal(this.id);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                final List<String> selected = Stevelander.config().xray.blocks;
                if (!selected.remove(this.id)) {
                    selected.add(this.id);
                }
                XRay.invalidate();
                return true;
            }

            @Override
            public void extractContent(
                GuiGraphicsExtractor extractor,
                int mouseX,
                int mouseY,
                boolean hovering,
                float partialTick
            ) {
                final boolean on = isSelected();
                extractor.text(
                    XRayBlockListScreen.this.font,
                    Component.literal((on ? "[x] " : "[ ] ") + this.id),
                    getContentX() + 4,
                    getContentY() + 4,
                    on ? 0xFF55FF55 : 0xFFAAAAAA
                );
            }
        }
    }
}
