package de.theidler.create_mobile_packages.index;

import com.tterrag.registrate.util.entry.EntityEntry;
import de.theidler.create_mobile_packages.CreateMobilePackages;
import de.theidler.create_mobile_packages.entities.robo_bee_entity.RoboBeeEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CMPEntities {

    public static final EntityEntry<RoboBeeEntity> ROBO_BEE_ENTITY = CreateMobilePackages.REGISTRATE
            .entity("robo_bee", RoboBeeEntity::new, MobCategory.CREATURE)
            .properties(properties -> properties.sized(0.6F, 0.6F))
            .attributes(() -> Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 0D)
                    .add(Attributes.FLYING_SPEED, 1.0D))
            .register();

    public static void register() {
    }
}
