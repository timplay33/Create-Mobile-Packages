package de.theidler.create_mobile_packages.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.ScreenWithStencils;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

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
        successTicks = 0;
        itemScroll = LerpedFloat.linear()
                .startWithValue(0);
        stockKeeper = new WeakReference<>(null);
        blaze = new WeakReference<>(null);
        refreshSearchNextTick = false;
        moveToTopNextTick = false;
        canRequestCraftingPackage = false;
*/
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
            //itemScroll.startWithValue(0);

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
/*
        categories = new ArrayList<>();
        for (int i = 0; i < blockEntity.categories.size(); i++) {
            ItemStack stack = blockEntity.categories.get(i);
            DroneControllerScreen.CategoryEntry entry = new DroneControllerScreen.CategoryEntry(i, stack.isEmpty() ? ""
                    : stack.getHoverName()
                    .getString(),
                    0);
            entry.hidden = hiddenCategories.contains(i);
            categories.add(entry);
        }*/
/*
        DroneControllerScreen.CategoryEntry unsorted = new DroneControllerScreen.CategoryEntry(-1, CreateLang.translate("gui.stock_keeper.unsorted_category")
                .string(), 0);
        unsorted.hidden = hiddenCategories.contains(-1);
        categories.add(unsorted);*/

        String valueWithPrefix = searchBox.getValue();
        boolean anyItemsInCategory = false;

        // Nothing is being filtered out
        if (valueWithPrefix.isBlank()) {
            displayedItems = new ArrayList<>(currentItemSource);
/*
            int categoryY = 0;
            for (int categoryIndex = 0; categoryIndex < currentItemSource.size(); categoryIndex++) {
                categories.get(categoryIndex).y = categoryY;
                List<BigItemStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
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
*/
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
/*
        int categoryY = 0;
        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
            List<BigItemStack> category = currentItemSource.get(categoryIndex);
            categories.get(categoryIndex).y = categoryY;

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
*/
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

        //itemScroll.tickChaser();
/*
        if (Math.abs(itemScroll.getValue() - itemScroll.getChaseTarget()) < 1 / 16f)
            itemScroll.setValue(itemScroll.getChaseTarget());
*/
        if (blockEntity.ticksSinceLastUpdate > 15){
            //blockEntity.refreshClientStockSnapshot();
            }
    }



    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        if (this != minecraft.screen)
            return; // stencil buffer does not cooperate with ponders gui fade out

        PoseStack ms = guiGraphics.pose();

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
