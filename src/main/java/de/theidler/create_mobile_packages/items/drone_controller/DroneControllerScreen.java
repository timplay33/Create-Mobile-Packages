package de.theidler.create_mobile_packages.items.drone_controller;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import de.theidler.create_mobile_packages.index.CMPPackets;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DroneControllerScreen extends AbstractSimiContainerScreen<DroneControllerMenu> implements ScreenWithStencils {

    private static final AllGuiTextures NUMBERS = AllGuiTextures.NUMBERS;
    private static final AllGuiTextures HEADER = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER;
    private static final AllGuiTextures BODY = AllGuiTextures.STOCK_KEEPER_REQUEST_BODY;
    private static final AllGuiTextures FOOTER = AllGuiTextures.STOCK_KEEPER_REQUEST_FOOTER;

    public LerpedFloat itemScroll;

    final int rows = 9;
    final int cols = 9;
    final int rowHeight = 20;
    final int colWidth = 20;
    final Couple<Integer> noneHovered = Couple.create(-1, -1);
    int itemsX;
    int itemsY;
    int orderY;
    int windowWidth;
    int windowHeight;

    public EditBox searchBox;
    EditBox addressBox;

    int emptyTicks = 0;
    int successTicks = 0;

    Inventory playerInventory;
    public List<BigItemStack> currentItemSource;
    public List<BigItemStack> displayedItems;
    public List<BigItemStack> itemsToOrder;
    private boolean scrollHandleActive;
    private InventorySummary forcedEntries;

    public boolean refreshSearchNextTick;
    public boolean moveToTopNextTick;

    public DroneControllerScreen(DroneControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        displayedItems = new ArrayList<>();
        itemsToOrder = new ArrayList<>();
        itemScroll = LerpedFloat.linear()
                .startWithValue(0);
        forcedEntries = new InventorySummary();
        this.playerInventory = playerInventory;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        addressBox.tick();
        ClientScreenStorage.tick();

        if (forcedEntries != null) {
            if (!forcedEntries.isEmpty()) {
                for (BigItemStack stack : forcedEntries.getStacks()) {
                    int limitedAmount = -stack.count - 1;
                    int actualAmount = countOf(stack);
                    if (actualAmount <= limitedAmount)
                        forcedEntries.erase(stack.stack);
                }
            }
        }

        boolean allEmpty = displayedItems.isEmpty();
        if (allEmpty)
            emptyTicks++;
        else
            emptyTicks = 0;

        if (successTicks > 0 && itemsToOrder.isEmpty())
            successTicks++;
        else
            successTicks = 0;

        List<BigItemStack> clientStockSnapshot = ClientScreenStorage.stacks;
        if (clientStockSnapshot != currentItemSource) {
            currentItemSource = clientStockSnapshot;
            refreshSearchResults(false);
            //revalidateOrders();
        }

        if (refreshSearchNextTick) {
            refreshSearchNextTick = false;
            refreshSearchResults(moveToTopNextTick);
        }

        itemScroll.tickChaser();

        if (Math.abs(itemScroll.getValue() - itemScroll.getChaseTarget()) < 1 / 16f)
            itemScroll.setValue(itemScroll.getChaseTarget());
    }

    private void refreshSearchResults(boolean scrollBackUp) {
        if (scrollBackUp)
            itemScroll.startWithValue(0);

        String valueWithPrefix = searchBox.getValue();

        // Nothing is being filtered out
        if (valueWithPrefix.isBlank()) {
            if (currentItemSource != null) {
                displayedItems = new ArrayList<>(currentItemSource);
            }
        }

        // Filter by search string
        boolean modSearch = false;
        boolean tagSearch = false;
        if ((modSearch = valueWithPrefix.startsWith("@")) || (tagSearch = valueWithPrefix.startsWith("#")))
            valueWithPrefix = valueWithPrefix.substring(1);
        final String value = valueWithPrefix.toLowerCase(Locale.ROOT);

        if (currentItemSource == null) {return;}
        displayedItems.clear();

        for (BigItemStack entry : currentItemSource) {
            ItemStack stack = entry.stack;

            if (modSearch) {
                if (ForgeRegistries.ITEMS.getKey(stack.getItem())
                        .getNamespace()
                        .contains(value)) {
                    displayedItems.add(entry);
                }
                continue;
            }

            if (tagSearch) {
                if (stack.getTags()
                        .anyMatch(key -> key.location()
                                .toString()
                                .contains(value)))
                    displayedItems.add(entry);
                continue;
            }

            if (stack.getHoverName()
                    .getString()
                    .toLowerCase(Locale.ROOT)
                    .contains(value)
                    || ForgeRegistries.ITEMS.getKey(stack.getItem())
                    .getPath()
                    .contains(value)) {
                displayedItems.add(entry);
                continue;
            }
        }

    }

    @Override
    protected void init() {
        int appropriateHeight = Minecraft.getInstance()
                .getWindow()
                .getGuiScaledHeight() - 10;
        appropriateHeight -=
                Mth.positiveModulo(appropriateHeight - HEADER.getHeight() - FOOTER.getHeight(), BODY.getHeight());
        appropriateHeight =
                Math.min(appropriateHeight, HEADER.getHeight() + FOOTER.getHeight() + BODY.getHeight() * 17);

        setWindowSize(windowWidth = 226, windowHeight = appropriateHeight);
        super.init();
        clearWidgets();

        int x = getGuiLeft();
        int y = getGuiTop();

        itemsX = x + (windowWidth - cols * colWidth) / 2 + 1;
        itemsY = y + 33;
        orderY = y + windowHeight - 72;

        MutableComponent searchLabel = CreateLang.translateDirect("gui.stock_keeper.search_items");
        searchBox = new EditBox(new NoShadowFontWrapper(font), x + 71, y + 22, 100, 9, searchLabel);
        searchBox.setMaxLength(50);
        searchBox.setBordered(false);
        searchBox.setTextColor(0x4A2D31);
        addWidget(searchBox);

        boolean initial = addressBox == null;
        String previouslyUsedAddress = initial ? menu.droneController.previouslyUsedAddress : addressBox.getValue();
        addressBox =
                new AddressEditBox(this, new NoShadowFontWrapper(font), x + 27, y + windowHeight - 36, 92, 10, true);
        addressBox.setTextColor(0x714A40);
        addressBox.setValue(previouslyUsedAddress);
        addRenderableWidget(addressBox);
        ClientScreenStorage.manualUpdate();
    }

    private Couple<Integer> getHoveredSlot(int x, int y) {
        x += 1;
        if (x < itemsX || x >= itemsX + cols * colWidth)
            return noneHovered;

        // Ordered item is hovered
        if (y >= orderY && y < orderY + rowHeight) {
            int col = (x - itemsX) / colWidth;
            if (itemsToOrder.size() <= col || col < 0)
                return noneHovered;
            return Couple.create(-1, col);
        }

        if (y < getGuiTop() + 16 || y > getGuiTop() + windowHeight - 80)
            return noneHovered;
        if (!itemScroll.settled())
            return noneHovered;

        int localY = y - itemsY;

        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {

            int row = Mth.floor((localY - (4)) / (float) rowHeight
                    + itemScroll.getChaseTarget());

            int col = (x - itemsX) / colWidth;
            int slot = row * cols + col;

            if (slot < 0)
                return noneHovered;

            return Couple.create(categoryIndex, slot);
        }

        return noneHovered;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (this != minecraft.screen)
            return; // stencil buffer does not cooperate with ponders gui fade out

        PoseStack ms = pGuiGraphics.pose();
        float currentScroll = itemScroll.getValue(partialTicks);
        Couple<Integer> hoveredSlot = getHoveredSlot(mouseX, mouseY);

        int x = getGuiLeft();
        int y = getGuiTop();

        // BG
        HEADER.render(pGuiGraphics, x - 15, y);
        y += HEADER.getHeight();
        for (int i = 0; i < (windowHeight - HEADER.getHeight() - FOOTER.getHeight()) / BODY.getHeight(); i++) {
            BODY.render(pGuiGraphics, x - 15, y);
            y += BODY.getHeight();
        }
        FOOTER.render(pGuiGraphics, x - 15, y);
        y = getGuiTop();

        // Render text input hints
        if (addressBox.getValue()
                .isBlank() && !addressBox.isFocused()) {
            pGuiGraphics.drawString(Minecraft.getInstance().font, CreateLang.translate("gui.stock_keeper.package_adress")
                    .style(ChatFormatting.ITALIC)
                    .component(), addressBox.getX(), addressBox.getY(), 0xff_CDBCA8, false);
        }

        // Render DroneController Item
        ms.pushPose();
        ms.translate(x - 50, y + windowHeight - 70, -100);
        ms.scale(3.5f, 3.5f, 3.5f);
        GuiGameElement.of(playerInventory.player.getMainHandItem())
                .render(pGuiGraphics);
        ms.popPose();

        // Render ordered items
        for (int index = 0; index < cols; index++) {
            if (itemsToOrder.size() <= index)
                break;

            BigItemStack entry = itemsToOrder.get(index);
            boolean isStackHovered = index == hoveredSlot.getSecond() && hoveredSlot.getFirst() == -1;

            ms.pushPose();
            ms.translate(itemsX + index * colWidth, orderY, 0);
            renderItemEntry(pGuiGraphics, 1, entry, isStackHovered, true);
            ms.popPose();
        }

        if (itemsToOrder.size() > 9) {
            pGuiGraphics.drawString(font, Component.literal("[+" + (itemsToOrder.size() - 9) + "]"), x + windowWidth - 40,
                    orderY + 21, 0xF8F8EC);
        }

        boolean justSent = itemsToOrder.isEmpty() && successTicks > 0;
        if (isConfirmHovered(mouseX, mouseY) && !justSent)
            AllGuiTextures.STOCK_KEEPER_REQUEST_SEND_HOVER.render(pGuiGraphics, x + windowWidth - 81,
                    y + windowHeight - 41);

        MutableComponent headerTitle = Component.translatable("item.create_mobile_packages.drone_controller.screen_title");
        pGuiGraphics.drawString(font, headerTitle, x + windowWidth / 2 - font.width(headerTitle) / 2, y + 4, 0x714A40,
                false);
        MutableComponent component =
                CreateLang.translate("gui.stock_keeper.send")
                        .component();

        if (justSent) {
            float alpha = Mth.clamp((successTicks + partialTicks - 5f) / 5f, 0f, 1f);
            ms.pushPose();
            ms.translate(alpha * alpha * 50, 0, 0);
            if (successTicks < 10)
                pGuiGraphics.drawString(font, component, x + windowWidth - 42 - font.width(component) / 2,
                        y + windowHeight - 35, new Color(0x252525).setAlpha(1 - alpha * alpha)
                                .getRGB(),
                        false);
            ms.popPose();

        } else {
            pGuiGraphics.drawString(font, component, x + windowWidth - 42 - font.width(component) / 2,
                    y + windowHeight - 35, 0x252525, false);
        }

        // Request just sent
        if (justSent) {
            Component msg = CreateLang.translateDirect("gui.stock_keeper.request_sent");
            float alpha = Mth.clamp((successTicks + partialTicks - 10f) / 5f, 0f, 1f);
            int msgX = x + windowWidth / 2 - (font.width(msg) + 10) / 2;
            int msgY = orderY + 5;
            if (alpha > 0) {
                int c3 = new Color(0x8C5D4B).setAlpha(alpha)
                        .getRGB();
                int w = font.width(msg) + 14;
                AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_L.render(pGuiGraphics, msgX - 8, msgY - 4);
                UIRenderHelper.drawStretched(pGuiGraphics, msgX, msgY - 4, w, 16, 0,
                        AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_M);
                AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_R.render(pGuiGraphics, msgX + font.width(msg) + 10, msgY - 4);
                pGuiGraphics.drawString(font, msg, msgX + 5, msgY, c3, false);
            }
        }

        int itemWindowX = x + 21;
        int itemWindowX2 = itemWindowX + 184;
        int itemWindowY = y + 17;
        int itemWindowY2 = y + windowHeight - 80;

        UIRenderHelper.swapAndBlitColor(minecraft.getMainRenderTarget(), UIRenderHelper.framebuffer);
        startStencil(pGuiGraphics, itemWindowX - 5, itemWindowY, itemWindowX2 - itemWindowX + 10,
                itemWindowY2 - itemWindowY);

        ms.pushPose();
        ms.translate(0, -currentScroll * rowHeight, 0);

        // BG
        for (int sliceY = -2; sliceY < getMaxScroll() * rowHeight + windowHeight - 72; sliceY +=
                AllGuiTextures.STOCK_KEEPER_REQUEST_BG.getHeight()) {
            if (sliceY - currentScroll * rowHeight < -20)
                continue;
            if (sliceY - currentScroll * rowHeight > windowHeight - 72)
                continue;
            AllGuiTextures.STOCK_KEEPER_REQUEST_BG.render(pGuiGraphics, x + 22, y + sliceY + 18);
        }

        // Search bar
        AllGuiTextures.STOCK_KEEPER_REQUEST_SEARCH.render(pGuiGraphics, x + 42, searchBox.getY() - 5);
        searchBox.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (searchBox.getValue()
                .isBlank() && !searchBox.isFocused())
            pGuiGraphics.drawString(font, searchBox.getMessage(),
                    x + windowWidth / 2 - font.width(searchBox.getMessage()) / 2, searchBox.getY(), 0xff4A2D31, false);

        // Something isnt right
        boolean allEmpty = displayedItems.isEmpty();
        if (allEmpty) {
            Component msg = getTroubleshootingMessage();
            float alpha = Mth.clamp((emptyTicks - 10f) / 5f, 0f, 1f);
            if (alpha > 0) {
                List<FormattedCharSequence> split = font.split(msg, 160);
                for (int i = 0; i < split.size(); i++) {
                    FormattedCharSequence sequence = split.get(i);
                    int lineWidth = font.width(sequence);
                    pGuiGraphics.drawString(font, sequence, x + windowWidth / 2 - lineWidth / 2 + 1,
                            itemsY + 20 + 1 + i * (font.lineHeight + 1), new Color(0x4A2D31).setAlpha(alpha)
                                    .getRGB(),
                            false);
                    pGuiGraphics.drawString(font, sequence, x + windowWidth / 2 - lineWidth / 2,
                            itemsY + 20 + i * (font.lineHeight + 1), new Color(0xF8F8EC).setAlpha(alpha)
                                    .getRGB(),
                            false);
                }
            }
        }

        // Items
        List<BigItemStack> category = displayedItems;
        int categoryY = 0;

        for (int index = 0; index < category.size(); index++) {
            int pY = itemsY + categoryY + (4) + (index / cols) * rowHeight;
            float cullY = pY - currentScroll * rowHeight;

            if (cullY < y)
                continue;
            if (cullY > y + windowHeight - 72)
                break;

            boolean isStackHovered = index == hoveredSlot.getSecond();
            BigItemStack entry = category.get(index);

            ms.pushPose();
            ms.translate(itemsX + (index % cols) * colWidth, pY, 0);
            renderItemEntry(pGuiGraphics, 1, entry, isStackHovered, false);
            ms.popPose();
        }

        ms.popPose();
        endStencil();

        // Scroll bar
        int windowH = windowHeight - 92;
        int totalH = getMaxScroll() * rowHeight + windowH;
        int barSize = Math.max(5, Mth.floor((float) windowH / totalH * (windowH - 2)));
        if (barSize < windowH - 2) {
            int barX = itemsX + cols * colWidth;
            int barY = y + 15;
            ms.pushPose();
            ms.translate(0, (currentScroll * rowHeight) / totalH * (windowH - 2), 0);
            AllGuiTextures pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
            pGuiGraphics.blit(pad.location, barX, barY, pad.getWidth(), barSize, pad.getStartX(), pad.getStartY(),
                    pad.getWidth(), pad.getHeight(), 256, 256);
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(pGuiGraphics, barX, barY);
            if (barSize > 16)
                AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(pGuiGraphics, barX, barY + barSize / 2 - 4);
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(pGuiGraphics, barX, barY + barSize - 5);
            ms.popPose();
        }
        UIRenderHelper.swapAndBlitColor(UIRenderHelper.framebuffer, minecraft.getMainRenderTarget());
    }

    private int getMaxScroll() {
        int visibleHeight = windowHeight - 84;
        int totalRows = 2;
        List<BigItemStack> list = displayedItems;
        totalRows++;
        totalRows += (int) Math.ceil(list.size() / (float) cols);
        return Math.max(0, (totalRows * rowHeight - visibleHeight + 50) / rowHeight);
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

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
        Couple<Integer> hoveredSlot = getHoveredSlot(mouseX, mouseY);

        // Render tooltip of hovered item
        if (hoveredSlot != noneHovered) {
            int slot = hoveredSlot.getSecond();
            boolean orderHovered = hoveredSlot.getFirst() == -1;
            try {
                BigItemStack entry = orderHovered ? itemsToOrder.get(slot)
                        : displayedItems.get(slot);
                graphics.renderTooltip(font, entry.stack, mouseX, mouseY);
            } catch (Exception ignored) {
            }
        }

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

    private boolean isConfirmHovered(int mouseX, int mouseY) {
        int confirmX = getGuiLeft() + 143;
        int confirmY = getGuiTop() + windowHeight - 39;
        int confirmW = 78;
        int confirmH = 18;

        if (mouseX < confirmX || mouseX >= confirmX + confirmW)
            return false;
        return mouseY >= confirmY && mouseY < confirmY + confirmH;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean lmb = pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT;
        boolean rmb = pButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT;

        // Search
        if (rmb && searchBox.isMouseOver(pMouseX, pMouseY)) {
            searchBox.setValue("");
            refreshSearchNextTick = true;
            moveToTopNextTick = true;
            searchBox.setFocused(true);
            //syncJEI();
            return true;
        }

        if (addressBox.isFocused()) {
            if (addressBox.isHovered())
                return addressBox.mouseClicked(pMouseX, pMouseY, pButton);
            addressBox.setFocused(false);
        }
        if (searchBox.isFocused()) {
            if (searchBox.isHovered())
                return searchBox.mouseClicked(pMouseX, pMouseY, pButton);
            searchBox.setFocused(false);
        }

        // Scroll bar
        int barX = itemsX + cols * colWidth - 1;
        if (getMaxScroll() > 0 && lmb && pMouseX > barX && pMouseX <= barX + 8 && pMouseY > getGuiTop() + 15
                && pMouseY < getGuiTop() + windowHeight - 82) {
            scrollHandleActive = true;
            if (minecraft.isWindowActive())
                GLFW.glfwSetInputMode(minecraft.getWindow()
                        .getWindow(), 208897, GLFW.GLFW_CURSOR_HIDDEN);
            return true;
        }

        // Confirm
        if (lmb && isConfirmHovered((int) pMouseX, (int) pMouseY)) {
            sendIt();
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
            return true;
        }

        Couple<Integer> hoveredSlot = getHoveredSlot((int) pMouseX, (int) pMouseY);
        if (hoveredSlot == noneHovered || !lmb && !rmb)
            return super.mouseClicked(pMouseX, pMouseY, pButton);

        // Items
        try {
            boolean orderClicked = hoveredSlot.getFirst() == -1;
            if (hoveredSlot.getSecond() > displayedItems.size()) {
                return false;
            }
            BigItemStack entry = orderClicked ? itemsToOrder.get(hoveredSlot.getSecond())
                    : displayedItems
                    .get(hoveredSlot.getSecond());

            ItemStack itemStack = entry.stack;
            int transfer = hasShiftDown() ? itemStack.getMaxStackSize() : hasControlDown() ? 10 : 1;

            BigItemStack existingOrder = getOrderForItem(entry.stack);
            if (existingOrder == null) {
                if (itemsToOrder.size() >= cols || rmb)
                    return true;
                itemsToOrder.add(existingOrder = new BigItemStack(itemStack.copyWithCount(1), 0));
                playUiSound(SoundEvents.WOOL_STEP, 0.75f, 1.2f);
                playUiSound(SoundEvents.BAMBOO_WOOD_STEP, 0.75f, 0.8f);
            }

            int current = existingOrder.count;

            if (rmb || orderClicked) {
                existingOrder.count = current - transfer;
                if (existingOrder.count <= 0) {
                    itemsToOrder.remove(existingOrder);
                    playUiSound(SoundEvents.WOOL_STEP, 0.75f, 1.8f);
                    playUiSound(SoundEvents.BAMBOO_WOOD_STEP, 0.75f, 1.8f);
                }
                return true;
            }

            existingOrder.count = current + Math.min(transfer, entry.count - current);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && scrollHandleActive) {
            scrollHandleActive = false;
            if (minecraft.isWindowActive())
                GLFW.glfwSetInputMode(minecraft.getWindow()
                        .getWindow(), 208897, GLFW.GLFW_CURSOR_NORMAL);
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (addressBox.mouseScrolled(mouseX, mouseY, delta))
            return true;

        Couple<Integer> hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
        boolean noHover = hoveredSlot == noneHovered;

        if (noHover || hoveredSlot.getFirst() >= 0 && !hasShiftDown() && getMaxScroll() != 0) {
            int maxScroll = getMaxScroll();
            int direction = (int) (Math.ceil(Math.abs(delta)) * -Math.signum(delta));
            float newTarget = Mth.clamp(Math.round(itemScroll.getChaseTarget() + direction), 0, maxScroll);
            itemScroll.chase(newTarget, 0.5, LerpedFloat.Chaser.EXP);
            return true;
        }
        try {
            boolean orderClicked = hoveredSlot.getFirst() == -1;
            BigItemStack entry = orderClicked ? itemsToOrder.get(hoveredSlot.getSecond())
                    : displayedItems
                    .get(hoveredSlot.getSecond());

            boolean remove = delta < 0;
            int transfer = Mth.ceil(Math.abs(delta)) * (hasControlDown() ? 10 : 1);

            BigItemStack existingOrder = orderClicked ? entry : getOrderForItem(entry.stack);
            if (existingOrder == null) {
                if (itemsToOrder.size() >= cols || remove)
                    return true;
                itemsToOrder.add(existingOrder = new BigItemStack(entry.stack.copyWithCount(1), 0));
                playUiSound(SoundEvents.WOOL_STEP, 0.75f, 1.2f);
                playUiSound(SoundEvents.BAMBOO_WOOD_STEP, 0.75f, 0.8f);
            }

            int current = existingOrder.count;

            if (remove) {
                existingOrder.count = current - transfer;
                if (existingOrder.count <= 0) {
                    itemsToOrder.remove(existingOrder);
                    playUiSound(SoundEvents.WOOL_STEP, 0.75f, 1.8f);
                    playUiSound(SoundEvents.BAMBOO_WOOD_STEP, 0.75f, 1.8f);
                } else if (existingOrder.count != current)
                    playUiSound(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 0.25f, 1.2f);
                return true;
            }

            existingOrder.count = current + Math.min(transfer, countOf(entry) - current);

            if (existingOrder.count != current && current != 0)
                playUiSound(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 0.25f, 1.2f);

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private int countOf(BigItemStack entry) {
        for (BigItemStack bigItemStack : displayedItems)
            if (bigItemStack.stack.getItem() == entry.stack.getItem()) {
                return bigItemStack.count;
            }
        return 0;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (pButton != GLFW.GLFW_MOUSE_BUTTON_LEFT || !scrollHandleActive)
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

        Window window = minecraft.getWindow();
        double scaleX = window.getGuiScaledWidth() / (double) window.getScreenWidth();
        double scaleY = window.getGuiScaledHeight() / (double) window.getScreenHeight();

        int windowH = windowHeight - 92;
        int totalH = getMaxScroll() * rowHeight + windowH;
        int barSize = Math.max(5, Mth.floor((float) windowH / totalH * (windowH - 2)));

        int minY = getGuiTop() + 15 + barSize / 2;
        int maxY = getGuiTop() + 15 + windowH - barSize / 2;

        if (barSize >= windowH - 2)
            return true;

        int barX = itemsX + cols * colWidth;
        double target = (pMouseY - getGuiTop() - 15 - barSize / 2.0) * totalH / (windowH - 2) / rowHeight;
        itemScroll.chase(Mth.clamp(target, 0, getMaxScroll()), 0.8, LerpedFloat.Chaser.EXP);

        if (minecraft.isWindowActive()) {
            double forceX = (barX + 2) / scaleX;
            double forceY = Mth.clamp(pMouseY, minY, maxY) / scaleY;
            GLFW.glfwSetCursorPos(window.getWindow(), forceX, forceY);
        }

        return true;
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (addressBox.isFocused() && addressBox.charTyped(pCodePoint, pModifiers))
            return true;
        String s = searchBox.getValue();
        if (!searchBox.charTyped(pCodePoint, pModifiers))
            return false;
        if (!Objects.equals(s, searchBox.getValue())) {
            refreshSearchNextTick = true;
            moveToTopNextTick = true;
            //syncJEI();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ENTER && searchBox.isFocused()) {
            searchBox.setFocused(false);
            return true;
        }

        if (pKeyCode == GLFW.GLFW_KEY_ENTER && hasShiftDown()) {
            sendIt();
            return true;
        }

        if (addressBox.isFocused() && addressBox.keyPressed(pKeyCode, pScanCode, pModifiers))
            return true;

        String s = searchBox.getValue();
        if (!searchBox.keyPressed(pKeyCode, pScanCode, pModifiers))
            return searchBox.isFocused() && searchBox.isVisible() && pKeyCode != 256 || super.keyPressed(pKeyCode, pScanCode, pModifiers);
        if (!Objects.equals(s, searchBox.getValue())) {
            refreshSearchNextTick = true;
            moveToTopNextTick = true;
            //syncJEI();
        }
        return true;
    }

    private void sendIt() {
        //revalidateOrders();
        if (itemsToOrder.isEmpty())
            return;

        /*forcedEntries = new InventorySummary();
        for (BigItemStack toOrder : itemsToOrder) {
            // momentarily cut the displayed stack size until the stock updates come in
            if (countOf(toOrder) == BigItemStack.INF)
                continue;
            forcedEntries.add(toOrder.stack.copy(), -1 - Math.max(0, countOf(toOrder) - toOrder.count));
        }*/

        CMPPackets.getChannel()
                .sendToServer(new SendPackage(new PackageOrder(itemsToOrder),
                        addressBox.getValue(), false, PackageOrder.empty()));
        menu.droneController.previouslyUsedAddress = addressBox.getValue();

        itemsToOrder = new ArrayList<>();
        //blockEntity.ticksSinceLastUpdate = 10;
        successTicks = 1;
        ClientScreenStorage.manualUpdate();

    }

    private Component getTroubleshootingMessage() {
        if (currentItemSource == null)
            return CreateLang.translate("gui.stock_keeper.checking_stocks")
                    .component();
        /*if (blockEntity.activeLinks == 0)
            return CreateLang.translate("gui.stock_keeper.no_packagers_linked")
                    .component();*/
        if (currentItemSource.isEmpty())
            return CreateLang.translate("gui.stock_keeper.inventories_empty")
                    .component();
        return CreateLang.translate("gui.stock_keeper.no_search_results")
                .component();
    }


}
