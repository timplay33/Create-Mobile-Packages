package de.theidler.create_mobile_packages.index;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.blocks.bee_port.BeePortBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
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


public class CMPBlocks {

    public static final BlockEntry<BeePortBlock> BEE_PORT = CreateMobilePackages.REGISTRATE.block("bee_port", BeePortBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .transform(displaySource(CMPDisplaySources.BEE_COUNT))
                    .transform(displaySource(CMPDisplaySources.BEE_ETA))
                    .item(LogisticallyLinkedBlockItem::new)
                    .transform(customItemModel())
                    .register();

    public static void register() {
    }
}
