package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.EntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.DroneEntity;
import net.minecraft.world.entity.MobCategory;

public class CMPEntities {
    public static final EntityEntry<DroneEntity> DRONE_ENTITY = CreateMobilePackages.REGISTRATE
            .entity("drone", DroneEntity::createEmpty, MobCategory.CREATURE)
            .properties(properties -> properties.sized(0.6F, 0.6F))
            .attributes(DroneEntity::createAttributes)
            .register();

    public static void register() {
    }
}
