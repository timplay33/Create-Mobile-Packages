package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.EntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.RoboBeeEntity;
import net.minecraft.world.entity.MobCategory;

public class CMPEntities {
    public static final EntityEntry<RoboBeeEntity> ROBO_BEE_ENTITY = CreateMobilePackages.REGISTRATE
            .entity("robo_bee", RoboBeeEntity::createEmpty, MobCategory.CREATURE)
            .properties(properties -> properties.sized(0.6F, 0.6F))
            .attributes(RoboBeeEntity::createAttributes)
            .register();

    public static void register() {
    }
}
