package de.theidler.create_mobile_packages.items;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

public class DroneControllerScreen extends AbstractSimiContainerScreen<DroneControllerMenu> implements MenuAccess<DroneControllerMenu>{//, ScreenWithStencils {

    private static final AllGuiTextures NUMBERS = AllGuiTextures.NUMBERS;
    private static final AllGuiTextures HEADER = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER;
    private static final AllGuiTextures BODY = AllGuiTextures.STOCK_KEEPER_REQUEST_BODY;
    private static final AllGuiTextures FOOTER = AllGuiTextures.STOCK_KEEPER_REQUEST_FOOTER;

    StockTickerBlockEntity blockEntity;
    public LerpedFloat itemScroll;

    final int rows = 9;
    final int cols = 9;
    final int rowHeight = 20;
    final int colWidth = 20;
    final Couple<Integer> noneHovered = Couple.create(-1, -1);
    int itemsX;
    int itemsY;
    int orderY;
    int lockX;
    int lockY;
    int windowWidth;
    int windowHeight;

    public EditBox searchBox;
    EditBox addressBox;

    int emptyTicks = 0;
    int successTicks = 0;

    public List<List<BigItemStack>> currentItemSource;
    public List<List<BigItemStack>> displayedItems;
    public List<CategoryEntry> categories;

    public List<BigItemStack> itemsToOrder;
    public List<CraftableBigItemStack> recipesToOrder;

    WeakReference<LivingEntity> stockKeeper;
    WeakReference<BlazeBurnerBlockEntity> blaze;

    boolean encodeRequester; // Redstone requesters
    ItemStack itemToProgram;
    List<List<ClipboardEntry>> clipboardItem;

    private boolean isAdmin;
    private boolean isLocked;
    private boolean scrollHandleActive;

    public boolean refreshSearchNextTick;
    public boolean moveToTopNextTick;
    private List<Rect2i> extraAreas = Collections.emptyList();

    private Set<Integer> hiddenCategories;
    private InventorySummary forcedEntries;
    private boolean canRequestCraftingPackage;

    public DroneControllerScreen(DroneControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        displayedItems = new ArrayList<>();
        itemsToOrder = new ArrayList<>();
        recipesToOrder = new ArrayList<>();
        categories = new ArrayList<>();
        emptyTicks = 0;
        successTicks = 0;
        itemScroll = LerpedFloat.linear()
                .startWithValue(0);
        stockKeeper = new WeakReference<>(null);
        blaze = new WeakReference<>(null);
        refreshSearchNextTick = false;
        moveToTopNextTick = false;
        canRequestCraftingPackage = false;

        forcedEntries = new InventorySummary();
        encodeRequester =
                AllTags.AllItemTags.TABLE_CLOTHS.matches(itemToProgram) || AllBlocks.REDSTONE_REQUESTER.isIn(itemToProgram);

        // Find the keeper for rendering
        for (int yOffset : Iterate.zeroAndOne) {
            for (Direction side : Iterate.horizontalDirections) {
                BlockPos seatPos = blockEntity.getBlockPos()
                        .below(yOffset)
                        .relative(side);
                for (SeatEntity seatEntity : blockEntity.getLevel()
                        .getEntitiesOfClass(SeatEntity.class, new AABB(seatPos)))
                    if (!seatEntity.getPassengers()
                            .isEmpty()
                            && seatEntity.getPassengers()
                            .get(0) instanceof LivingEntity keeper)
                        stockKeeper = new WeakReference<>(keeper);
                if (yOffset == 0 && blockEntity.getLevel()
                        .getBlockEntity(seatPos) instanceof BlazeBurnerBlockEntity bbbe) {
                    blaze = new WeakReference<>(bbbe);
                    return;
                }
            }
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        addressBox.tick();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (this != minecraft.screen)
            return; // stencil buffer does not cooperate with ponders gui fade out

        PoseStack ms = guiGraphics.pose();
        Couple<Integer> hoveredSlot = getHoveredSlot(mouseX, mouseY);

        int x = getGuiLeft();
        int y = getGuiTop();

        // BG
        HEADER.render(guiGraphics, x - 15, y);
        y += HEADER.getHeight();
        for (int i = 0; i < (windowHeight - HEADER.getHeight() - FOOTER.getHeight()) / BODY.getHeight(); i++) {
            BODY.render(guiGraphics, x - 15, y);
            y += BODY.getHeight();
        }
        FOOTER.render(guiGraphics, x - 15, y);
        y = getGuiTop();

        if (encodeRequester) {
            ms.pushPose();
            ms.translate(x + windowWidth + 5, y + windowHeight - 70, 0);
            ms.scale(3.5f, 3.5f, 3.5f);
            GuiGameElement.of(itemToProgram)
                    .render(guiGraphics);
            ms.popPose();
        }

        // Render ordered items
        for (int index = 0; index < cols; index++) {
            if (itemsToOrder.size() <= index)
                break;

            BigItemStack entry = itemsToOrder.get(index);
            boolean isStackHovered = index == hoveredSlot.getSecond() && hoveredSlot.getFirst() == -1;

            ms.pushPose();
            ms.translate(itemsX + index * colWidth, orderY, 0);
            renderItemEntry(guiGraphics, 1, entry, isStackHovered, true);
            ms.popPose();
        }

    }

    @Override
    protected void init() {
        int appropriateHeight = Minecraft.getInstance()
                .getWindow()
                .getGuiScaledHeight() - 10;
        appropriateHeight -= Mth.positiveModulo(appropriateHeight - HEADER.getHeight() - FOOTER.getHeight(), BODY.getHeight());
        appropriateHeight = Math.min(appropriateHeight, HEADER.getHeight() + FOOTER.getHeight() + BODY.getHeight() * 17);
        setWindowSize(windowWidth = 226, windowHeight = appropriateHeight);
        super.init();

        clearWidgets();

        int x = getGuiLeft();
        int y = getGuiTop();

        itemsX = x + (windowWidth - cols * colWidth) / 2 + 1;
        itemsY = y + 33;
        orderY = y + windowHeight - 72;
        lockX = x + 186;
        lockY = y + 18;

        addressBox =
                new AddressEditBox(this, new NoShadowFontWrapper(font), x + 27, y + windowHeight - 36, 92, 10, true);
        addressBox.setTextColor(0x714A40);
        addressBox.setValue(addressBox.getValue());
        addRenderableWidget(addressBox);
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);


        // Render tooltip of address input
        if (addressBox.getValue()
                .isBlank() && !addressBox.isFocused() && addressBox.isHovered()) {
            graphics.renderComponentTooltip(font, List.of(CreateLang.translate("gui.factory_panel.restocker_address")
                                    .color(ScrollInput.HEADER_RGB)
                                    .component(),
                            CreateLang.translate("gui.schedule.lmb_edit")
                                    .style(ChatFormatting.DARK_GRAY)
                                    .style(ChatFormatting.ITALIC)
                                    .component()),
                    mouseX, mouseY);
        }

    }
    @Nullable
    private BigItemStack getOrderForItem(ItemStack stack) {
        for (BigItemStack entry : itemsToOrder)
            if (ItemHandlerHelper.canItemStacksStack(stack, entry.stack))
                return entry;
        return null;
    }

    private void renderItemEntry(GuiGraphics graphics, float scale, BigItemStack entry, boolean isStackHovered,
                                 boolean isRenderingOrders) {
        int customCount = entry.count;
        if (!isRenderingOrders) {
            BigItemStack order = getOrderForItem(entry.stack);
            if (entry.count < BigItemStack.INF) {
                int forcedCount = forcedEntries.getCountOf(entry.stack);
                if (forcedCount != 0)
                    customCount = Math.min(customCount, -forcedCount - 1);
                if (order != null)
                    customCount -= order.count;
                customCount = Math.max(0, customCount);
            }
            AllGuiTextures.STOCK_KEEPER_REQUEST_SLOT.render(graphics, 0, 0);
        }

        boolean craftable = entry instanceof CraftableBigItemStack;
        PoseStack ms = graphics.pose();
        ms.pushPose();

        float scaleFromHover = 1;
        if (isStackHovered)
            scaleFromHover += .075f;

        ms.translate((colWidth - 18) / 2.0, (rowHeight - 18) / 2.0, 0);
        ms.translate(18 / 2.0, 18 / 2.0, 0);
        ms.scale(scale, scale, scale);
        ms.scale(scaleFromHover, scaleFromHover, scaleFromHover);
        ms.translate(-18 / 2.0, -18 / 2.0, 0);
        if (customCount != 0 || craftable)
            GuiGameElement.of(entry.stack)
                    .render(graphics);
        ms.popPose();

        ms.pushPose();
        ms.translate(0, 0, 190);
        if (customCount != 0 || craftable)
            graphics.renderItemDecorations(font, entry.stack, 1, 1, "");
        ms.translate(0, 0, 10);
        if (customCount > 1 || craftable)
            drawItemCount(graphics, entry.count, customCount);
        ms.popPose();

    }

    private void drawItemCount(GuiGraphics graphics, int count, int customCount) {
        count = customCount;
        String text = count >= 1000000 ? (count / 1000000) + "m"
                : count >= 10000 ? (count / 1000) + "k"
                : count >= 1000 ? ((count * 10) / 1000) / 10f + "k" : count >= 100 ? count + "" : " " + count;

        if (count >= BigItemStack.INF)
            text = "+";

        if (text.isBlank())
            return;

        int x = (int) Math.floor(-text.length() * 2.5);
        for (char c : text.toCharArray()) {
            int index = c - '0';
            int xOffset = index * 6;
            int spriteWidth = NUMBERS.getWidth();

            switch (c) {
                case ' ':
                    x += 4;
                    continue;
                case '.':
                    spriteWidth = 3;
                    xOffset = 60;
                    break;
                case 'k':
                    xOffset = 64;
                    break;
                case 'm':
                    spriteWidth = 7;
                    xOffset = 70;
                    break;
                case '+':
                    spriteWidth = 9;
                    xOffset = 84;
                    break;
            }

            RenderSystem.enableBlend();
            graphics.blit(NUMBERS.location, 14 + x, 10, 0, NUMBERS.getStartX() + xOffset, NUMBERS.getStartY(),
                    spriteWidth, NUMBERS.getHeight(), 256, 256);
            x += spriteWidth - 1;
        }

    }

    public boolean isSchematicListMode() {
        return clipboardItem != null;
    }

    public static class CategoryEntry {
        boolean hidden;
        String name;
        int y;
        int targetBECategory;

        public CategoryEntry(int targetBECategory, String name, int y) {
            this.targetBECategory = targetBECategory;
            this.name = name;
            hidden = false;
            this.y = y;
        }
    }

    private Couple<Integer> getHoveredSlot(int x, int y) {
        x += 1;
        if (x < itemsX || x >= itemsX + cols * colWidth || isSchematicListMode())
            return noneHovered;

        // Ordered item is hovered
        if (y >= orderY && y < orderY + rowHeight) {
            int col = (x - itemsX) / colWidth;
            if (itemsToOrder.size() <= col || col < 0)
                return noneHovered;
            return Couple.create(-1, col);
        }

        // Ordered recipe is hovered
        if (y >= orderY - 31 && y < orderY - 31 + rowHeight) {
            int jeiX = getGuiLeft() + (windowWidth - colWidth * recipesToOrder.size()) / 2 + 1;
            int col = Mth.floorDiv(x - jeiX, colWidth);
            if (recipesToOrder.size() > col && col >= 0)
                return Couple.create(-2, col);
        }

        if (y < getGuiTop() + 16 || y > getGuiTop() + windowHeight - 80)
            return noneHovered;
        if (!itemScroll.settled())
            return noneHovered;

        int localY = y - itemsY;

        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
            CategoryEntry entry = categories.isEmpty() ? new CategoryEntry(0, "", 0) : categories.get(categoryIndex);
            if (entry.hidden)
                continue;

            int row = Mth.floor((localY - (categories.isEmpty() ? 4 : rowHeight) - entry.y) / (float) rowHeight
                    + itemScroll.getChaseTarget());

            int col = (x - itemsX) / colWidth;
            int slot = row * cols + col;

            if (slot < 0)
                return noneHovered;
            if (displayedItems.get(categoryIndex)
                    .size() <= slot)
                continue;

            return Couple.create(categoryIndex, slot);
        }

        return noneHovered;
    }
}
