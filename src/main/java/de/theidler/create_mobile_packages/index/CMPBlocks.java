package de.theidler.create_mobile_packages.index;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock.IS_OPEN_TEXTURE;


public class CMPBlocks {

    public static final BlockEntry<BeePortBlock> BEE_PORT = CreateMobilePackages.REGISTRATE.block("bee_port", BeePortBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .transform(displaySource(CMPDisplaySources.BEE_COUNT))
                    .transform(displaySource(CMPDisplaySources.BEE_ETA))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<DronePortBlock> DRONE_PORT = CreateMobilePackages.REGISTRATE.block("drone_port", DronePortBlock::new)
            .initialProperties(SharedProperties::wooden)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(DronePortItem::new)
            .removeTab(CreativeModeTabs.SEARCH)
            .removeTab(ResourceKey.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, CreateMobilePackages.asResource("create_mobile_packages_tab")))
            .transform(customItemModel())
            .register();

    public static void register() {
    }

    @Deprecated
    public static class DronePortItem extends BlockItem {
        public DronePortItem(Block pBlock, Properties pProperties) {
            super(pBlock, pProperties);
        }
        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            if (!level.isClientSide && entity instanceof Player player) {
                ItemStack replacement = new ItemStack(CMPBlocks.BEE_PORT.asStack().getItem(), stack.getCount());
                replacement.setTag(stack.getTag());
                player.getInventory().setItem(slot, replacement);
            }
        }
    }

    @Deprecated
    public static class DronePortBlock extends Block implements IBE<CMPBlockEntities.DronePortBlockEntity> {

        public DronePortBlock(Properties pProperties) {
            super(pProperties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
            super.createBlockStateDefinition(pBuilder);
            pBuilder.add(IS_OPEN_TEXTURE);
        }

        @Override
        public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
            if (!pLevel.isClientSide) {
                CMPBlockEntities.DronePortBlockEntity.tryConvert(pLevel, pPos);
            }
        }

        @Override
        public Class<CMPBlockEntities.DronePortBlockEntity> getBlockEntityClass() {
            return CMPBlockEntities.DronePortBlockEntity.class;
        }

        @Override
        public BlockEntityType<? extends CMPBlockEntities.DronePortBlockEntity> getBlockEntityType() {
            return CMPBlockEntities.DRONE_PORT.get();
        }
    }
}
