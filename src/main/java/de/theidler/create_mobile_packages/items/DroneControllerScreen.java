package de.theidler.create_mobile_packages.items;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderRequestPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.lang.ref.WeakReference;
import java.util.*;

public class DroneControllerScreen extends AbstractSimiContainerScreen<DroneControllerMenu> implements MenuAccess<DroneControllerMenu>, ScreenWithStencils {

    private static final AllGuiTextures NUMBERS = AllGuiTextures.NUMBERS;
    private static final AllGuiTextures HEADER = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER;
    private static final AllGuiTextures BODY = AllGuiTextures.STOCK_KEEPER_REQUEST_BODY;
    private static final AllGuiTextures FOOTER = AllGuiTextures.STOCK_KEEPER_REQUEST_FOOTER;

    StockCheckingItem blockEntity;
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
    public List<DroneControllerScreen.CategoryEntry> categories;

    public List<BigItemStack> itemsToOrder;
    public List<CraftableBigItemStack> recipesToOrder;

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
    private boolean initialized;
    DroneControllerMenu controllerMenu;

    public DroneControllerScreen(DroneControllerMenu container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.controllerMenu = container;
        //init(); // don't know why it isn't run automatically??
        displayedItems = new ArrayList<>();
        itemsToOrder = new ArrayList<>();
        recipesToOrder = new ArrayList<>();
        categories = new ArrayList<>();
        /*
        blockEntity.lastClientsideStockSnapshot = null;
        blockEntity.ticksSinceLastUpdate = 15;
        emptyTicks = 0;
        successTicks = 0;*/
        itemScroll = LerpedFloat.linear()
                .startWithValue(0);/*
        stockKeeper = new WeakReference<>(null);
        blaze = new WeakReference<>(null);
        refreshSearchNextTick = false;
        moveToTopNextTick = false;
        canRequestCraftingPackage = false;
*/
        hiddenCategories = new HashSet<>();
        /*hiddenCategories =
                new HashSet<>(blockEntity.hiddenCategoriesByPlayer.getOrDefault(menu.player.getUUID(), List.of()));*/
        forcedEntries = new InventorySummary();
    }

    @Override
    protected void init() {
        blockEntity = this.controllerMenu.droneController;
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

        //if (this.initialized) return;
        MutableComponent searchLabel = CreateLang.translateDirect("gui.stock_keeper.search_items");
        searchBox = new EditBox(new NoShadowFontWrapper(Minecraft.getInstance().font), x + 71, y + 22, 100, 9, searchLabel);
        searchBox.setMaxLength(50);
        searchBox.setBordered(false);
        searchBox.setTextColor(0x4A2D31);
        addWidget(searchBox);

        boolean initial = addressBox == null;
        String previouslyUsedAddress = /*initial ? blockEntity.previouslyUsedAddress : addressBox.getValue()*/  "";
       addressBox =
                new AddressEditBox(this, new NoShadowFontWrapper(Minecraft.getInstance().font), x + 27, y + windowHeight - 36, 92, 10, true);
        addressBox.setTextColor(0x714A40);
        addressBox.setValue(previouslyUsedAddress);
        addRenderableWidget(addressBox);

       extraAreas = new ArrayList<>();
        int leftHeight = 40;
        int rightHeight = 50;

        extraAreas.add(new Rect2i(0, y + windowHeight - 15 - leftHeight, x, height));
        if (encodeRequester)
            extraAreas.add(new Rect2i(x + windowWidth, y + windowHeight - 15 - rightHeight, rightHeight + 10, rightHeight));

        if (initial) {
            playUiSound(SoundEvents.WOOD_HIT, 0.5f, 1.5f);
            playUiSound(SoundEvents.BOOK_PAGE_TURN, 1, 1);
            //syncJEI();
        }
        this.initialized = true;
    }

    private void refreshSearchResults(boolean scrollBackUp) {
        displayedItems = Collections.emptyList();
        if (scrollBackUp)
            itemScroll.startWithValue(0);

        if (currentItemSource == null) {
            //clampScrollBar();
            return;
        }
/*
        if (isSchematicListMode()) {
            clampScrollBar();
            requestSchematicList();
            return;
        }*/

        categories = new ArrayList<>();
        for (int i = 0; i < blockEntity.categories.size(); i++) {
            ItemStack stack = blockEntity.categories.get(i);
            DroneControllerScreen.CategoryEntry entry = new DroneControllerScreen.CategoryEntry(i, stack.isEmpty() ? ""
                    : stack.getHoverName()
                    .getString(),
                    0);
            entry.hidden = hiddenCategories.contains(i);
            categories.add(entry);
        }
        DroneControllerScreen.CategoryEntry unsorted = new DroneControllerScreen.CategoryEntry(-1, CreateLang.translate("gui.stock_keeper.unsorted_category")
                .string(), 0);
        unsorted.hidden = hiddenCategories.contains(-1);
        categories.add(unsorted);

        String valueWithPrefix = searchBox.getValue();
        boolean anyItemsInCategory = false;

        // Nothing is being filtered out
        if (valueWithPrefix.isBlank()) {
            displayedItems = new ArrayList<>(currentItemSource);

            int categoryY = 0;
            for (int categoryIndex = 0; categoryIndex < currentItemSource.size(); categoryIndex++) {
                //categories.get(categoryIndex).y = categoryY;
                List<BigItemStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
                if (displayedItemsInCategory.isEmpty())
                    continue;
                if (categoryIndex < currentItemSource.size() - 1)
                    anyItemsInCategory = true;

                categoryY += rowHeight;
                //if (!categories.get(categoryIndex).hidden)
                //    categoryY += Math.ceil(displayedItemsInCategory.size() / (float) cols) * rowHeight;
            }

            if (!anyItemsInCategory)
                categories.clear();

            //clampScrollBar();
            //updateCraftableAmounts();
            return;
        }

        // Filter by search string
        boolean modSearch = false;
        boolean tagSearch = false;
        if ((modSearch = valueWithPrefix.startsWith("@")) || (tagSearch = valueWithPrefix.startsWith("#")))
            valueWithPrefix = valueWithPrefix.substring(1);
        final String value = valueWithPrefix.toLowerCase(Locale.ROOT);

        displayedItems = new ArrayList<>();
        currentItemSource.forEach($ -> displayedItems.add(new ArrayList<>()));

        int categoryY = 0;
        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
            List<BigItemStack> category = currentItemSource.get(categoryIndex);
            //categories.get(categoryIndex).y = categoryY;

            if (displayedItems.size() <= categoryIndex)
                break;

            List<BigItemStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
            for (BigItemStack entry : category) {
                ItemStack stack = entry.stack;

                if (modSearch) {
                    if (ForgeRegistries.ITEMS.getKey(stack.getItem())
                            .getNamespace()
                            .contains(value)) {
                        displayedItemsInCategory.add(entry);
                    }
                    continue;
                }

                if (tagSearch) {
                    if (stack.getTags()
                            .anyMatch(key -> key.location()
                                    .toString()
                                    .contains(value)))
                        displayedItemsInCategory.add(entry);
                    continue;
                }

                if (stack.getHoverName()
                        .getString()
                        .toLowerCase(Locale.ROOT)
                        .contains(value)
                        || ForgeRegistries.ITEMS.getKey(stack.getItem())
                        .getPath()
                        .contains(value)) {
                    displayedItemsInCategory.add(entry);
                    continue;
                }
            }

            if (displayedItemsInCategory.isEmpty())
                continue;
            if (categoryIndex < currentItemSource.size() - 1)
                anyItemsInCategory = true;

            categoryY += rowHeight;

            if (!categories.get(categoryIndex).hidden)
                categoryY += Math.ceil(displayedItemsInCategory.size() / (float) cols) * rowHeight;
        }

        if (!anyItemsInCategory)
            categories.clear();

        //clampScrollBar();
        //updateCraftableAmounts();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        addressBox.tick();

       if (!forcedEntries.isEmpty()) {
            InventorySummary summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
            for (BigItemStack stack : forcedEntries.getStacks()) {
                int limitedAmount = -stack.count - 1;
                int actualAmount = summary.getCountOf(stack.stack);
                if (actualAmount <= limitedAmount)
                    forcedEntries.erase(stack.stack);
            }
        }

        boolean allEmpty = true;
        for (List<BigItemStack> list : displayedItems)
            allEmpty &= list.isEmpty();
        if (allEmpty)
            emptyTicks++;
        else
            emptyTicks = 0;

        if (successTicks > 0 && itemsToOrder.isEmpty())
            successTicks++;
        else
            successTicks = 0;

        List<List<BigItemStack>> clientStockSnapshot = blockEntity.getClientStockSnapshot();
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

        if (blockEntity.ticksSinceLastUpdate > 15){
            //blockEntity.refreshClientStockSnapshot();
            }
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
        ms.translate(0, 0, 190);
        if (customCount != 0 || craftable)
            graphics.renderItemDecorations(font, entry.stack, 1, 1, "");
        ms.translate(0, 0, 10);
        if (customCount > 1 || craftable)
            drawItemCount(graphics, entry.count, customCount);
        ms.popPose();
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (this != minecraft.screen)
            return; // stencil buffer does not cooperate with ponders gui fade out

        PoseStack ms = guiGraphics.pose();
        float currentScroll = itemScroll.getValue(partialTicks);
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

        if (itemsToOrder.size() > 9) {
            guiGraphics.drawString(font, Component.literal("[+" + (itemsToOrder.size() - 9) + "]"), x + windowWidth - 40,
                    orderY + 21, 0xF8F8EC);
        }

        boolean justSent = itemsToOrder.isEmpty() && successTicks > 0;
        if (isConfirmHovered(mouseX, mouseY) && !justSent)
            AllGuiTextures.STOCK_KEEPER_REQUEST_SEND_HOVER.render(guiGraphics, x + windowWidth - 81,
                    y + windowHeight - 41);

        MutableComponent headerTitle = CreateLang.translate("gui.stock_keeper.title")
                .component();
        guiGraphics.drawString(font, headerTitle, x + windowWidth / 2 - font.width(headerTitle) / 2, y + 4, 0x714A40,
                false);
        MutableComponent component =
                CreateLang.translate(encodeRequester ? "gui.stock_keeper.configure" : "gui.stock_keeper.send")
                        .component();

        if (justSent) {
            float alpha = Mth.clamp((successTicks + partialTicks - 5f) / 5f, 0f, 1f);
            ms.pushPose();
            ms.translate(alpha * alpha * 50, 0, 0);
            if (successTicks < 10)
                guiGraphics.drawString(font, component, x + windowWidth - 42 - font.width(component) / 2,
                        y + windowHeight - 35, new Color(0x252525).setAlpha(1 - alpha * alpha)
                                .getRGB(),
                        false);
            ms.popPose();

        } else {
            guiGraphics.drawString(font, component, x + windowWidth - 42 - font.width(component) / 2,
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
                AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_L.render(guiGraphics, msgX - 8, msgY - 4);
                UIRenderHelper.drawStretched(guiGraphics, msgX, msgY - 4, w, 16, 0,
                        AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_M);
                AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_R.render(guiGraphics, msgX + font.width(msg) + 10, msgY - 4);
                guiGraphics.drawString(font, msg, msgX + 5, msgY, c3, false);
            }
        }

        int itemWindowX = x + 21;
        int itemWindowX2 = itemWindowX + 184;
        int itemWindowY = y + 17;
        int itemWindowY2 = y + windowHeight - 80;

        UIRenderHelper.swapAndBlitColor(minecraft.getMainRenderTarget(), UIRenderHelper.framebuffer);
        startStencil(guiGraphics, itemWindowX - 5, itemWindowY, itemWindowX2 - itemWindowX + 10,
                itemWindowY2 - itemWindowY);

        // BG
        for (int sliceY = -2; sliceY < getMaxScroll() * rowHeight + windowHeight - 72; sliceY +=
                AllGuiTextures.STOCK_KEEPER_REQUEST_BG.getHeight()) {
            if (sliceY - currentScroll * rowHeight < -20)
                continue;
            if (sliceY - currentScroll * rowHeight > windowHeight - 72)
                continue;
            AllGuiTextures.STOCK_KEEPER_REQUEST_BG.render(guiGraphics, x + 22, y + sliceY + 18);
        }

        // Search bar
        AllGuiTextures.STOCK_KEEPER_REQUEST_SEARCH.render(guiGraphics, x + 42, searchBox.getY() - 5);
        searchBox.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (searchBox.getValue()
                .isBlank() && !searchBox.isFocused())
            guiGraphics.drawString(font, searchBox.getMessage(),
                    x + windowWidth / 2 - font.width(searchBox.getMessage()) / 2, searchBox.getY(), 0xff4A2D31, false);


        // Items
        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
            List<BigItemStack> category = displayedItems.get(categoryIndex);
            DroneControllerScreen.CategoryEntry categoryEntry = categories.isEmpty() ? null :  new DroneControllerScreen.CategoryEntry(-1, CreateLang.translate("gui.stock_keeper.unsorted_category").string(), 0);//categories.get(categoryIndex);
            int categoryY = categories.isEmpty() ? 0 : categoryEntry.y;
            if (category.isEmpty())
                continue;

            /*if (!categories.isEmpty()) {
                (categoryEntry.hidden ? AllGuiTextures.STOCK_KEEPER_CATEGORY_HIDDEN
                        : AllGuiTextures.STOCK_KEEPER_CATEGORY_SHOWN).render(guiGraphics, itemsX, itemsY + categoryY + 6);
                guiGraphics.drawString(font, categoryEntry.name, itemsX + 10, itemsY + categoryY + 8, 0x4A2D31, false);
                guiGraphics.drawString(font, categoryEntry.name, itemsX + 9, itemsY + categoryY + 7, 0xF8F8EC, false);
                if (categoryEntry.hidden)
                    continue;
            }*/

            for (int index = 0; index < category.size(); index++) {
                int pY = itemsY + categoryY + (categories.isEmpty() ? 4 : rowHeight) + (index / cols) * rowHeight;
                float cullY = pY - currentScroll * rowHeight;

                if (cullY < y)
                    continue;
                if (cullY > y + windowHeight - 72)
                    break;

                boolean isStackHovered = index == hoveredSlot.getSecond() && categoryIndex == hoveredSlot.getFirst();
                BigItemStack entry = category.get(index);

                ms.pushPose();
                ms.translate(itemsX + (index % cols) * colWidth, pY, 0);
                renderItemEntry(guiGraphics, 1, entry, isStackHovered, false);
                ms.popPose();
            }
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
            guiGraphics.blit(pad.location, barX, barY, pad.getWidth(), barSize, pad.getStartX(), pad.getStartY(),
                    pad.getWidth(), pad.getHeight(), 256, 256);
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(guiGraphics, barX, barY);
            if (barSize > 16)
                AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(guiGraphics, barX, barY + barSize / 2 - 4);
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(guiGraphics, barX, barY + barSize - 5);
            ms.popPose();
        }
        UIRenderHelper.swapAndBlitColor(UIRenderHelper.framebuffer, minecraft.getMainRenderTarget());

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

    private int getMaxScroll() {
        int visibleHeight = windowHeight - 84;
        int totalRows = 2;
        for (int i = 0; i < displayedItems.size(); i++) {
            List<BigItemStack> list = displayedItems.get(i);
            if (list.isEmpty())
                continue;
            totalRows++;
            if (categories.size() > i && categories.get(i).hidden)
                continue;
            totalRows += Math.ceil(list.size() / (float) cols);
        }
        int maxScroll = (int) Math.max(0, (totalRows * rowHeight - visibleHeight + 50) / rowHeight);
        return maxScroll;
    }

    private Couple<Integer> getHoveredSlot(int x, int y) {
        x += 1;
        if (x < itemsX || x >= itemsX + cols * colWidth) //|| isSchematicListMode())
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
            DroneControllerScreen.CategoryEntry entry = /*categories.isEmpty() ?*/ new DroneControllerScreen.CategoryEntry(0, "", 0); //: categories.get(categoryIndex);
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

    private boolean isConfirmHovered(int mouseX, int mouseY) {
        int confirmX = getGuiLeft() + 143;
        int confirmY = getGuiTop() + windowHeight - 39;
        int confirmW = 78;
        int confirmH = 18;

        if (mouseX < confirmX || mouseX >= confirmX + confirmW)
            return false;
        if (mouseY < confirmY || mouseY >= confirmY + confirmH)
            return false;
        return true;
    }

    private void revalidateOrders() {
        Set<BigItemStack> invalid = new HashSet<>(itemsToOrder);
        InventorySummary summary = blockEntity.lastClientsideStockSnapshotAsSummary;
        if (currentItemSource == null || summary == null) {
            itemsToOrder.removeAll(invalid);
            return;
        }
        for (BigItemStack entry : itemsToOrder) {
            entry.count = Math.min(summary.getCountOf(entry.stack), entry.count);
            if (entry.count > 0)
                invalid.remove(entry);
        }

        itemsToOrder.removeAll(invalid);
    }

    private void sendIt() {
        revalidateOrders();
        if (itemsToOrder.isEmpty())
            return;

        forcedEntries = new InventorySummary();
        InventorySummary summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
        for (BigItemStack toOrder : itemsToOrder) {
            // momentarily cut the displayed stack size until the stock updates come in
            int countOf = summary.getCountOf(toOrder.stack);
            if (countOf == BigItemStack.INF)
                continue;
            forcedEntries.add(toOrder.stack.copy(), -1 - Math.max(0, countOf - toOrder.count));
        }

        PackageOrder craftingRequest = PackageOrder.empty();
        if (canRequestCraftingPackage && !itemsToOrder.isEmpty() && !recipesToOrder.isEmpty())
            if (recipesToOrder.get(0).recipe instanceof CraftingRecipe cr)
                craftingRequest = new PackageOrder(FactoryPanelScreen.convertRecipeToPackageOrderContext(cr, itemsToOrder));

        AllPackets.getChannel()
                .sendToServer(new PackageOrderRequestPacket(new BlockPos(0,0,0), new PackageOrder(itemsToOrder),
                        addressBox.getValue(), encodeRequester, craftingRequest));

        itemsToOrder = new ArrayList<>();
        recipesToOrder = new ArrayList<>();
        blockEntity.ticksSinceLastUpdate = 10;
        successTicks = 1;

        /*if (isSchematicListMode())
            menu.player.closeContainer();*/
    }

    private BigItemStack getOrderForItem(ItemStack stack) {
        for (BigItemStack entry : itemsToOrder)
            if (ItemHandlerHelper.canItemStacksStack(stack, entry.stack))
                return entry;
        return null;
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

        Couple<Integer> hoveredSlot = getHoveredSlot((int) pMouseX, (int) pMouseY);

        if (lmb && isConfirmHovered((int) pMouseX, (int) pMouseY)) {
            sendIt();
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
            return true;
        }

        if (hoveredSlot == noneHovered || !lmb && !rmb)
            return super.mouseClicked(pMouseX, pMouseY, pButton);

        // Items
        boolean orderClicked = hoveredSlot.getFirst() == -1;
        boolean recipeClicked = hoveredSlot.getFirst() == -2;
        BigItemStack entry = recipeClicked ? recipesToOrder.get(hoveredSlot.getSecond())
                : orderClicked ? itemsToOrder.get(hoveredSlot.getSecond())
                : displayedItems.get(hoveredSlot.getFirst())
                .get(hoveredSlot.getSecond());

        ItemStack itemStack = entry.stack;
        int transfer = hasShiftDown() ? itemStack.getMaxStackSize() : hasControlDown() ? 10 : 1;

        if (recipeClicked && entry instanceof CraftableBigItemStack cbis) {
            if (rmb && cbis.count == 0) {
                recipesToOrder.remove(cbis);
                return true;
            }
            //requestCraftable(cbis, rmb ? -transfer : transfer);
            return true;
        }

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
}
