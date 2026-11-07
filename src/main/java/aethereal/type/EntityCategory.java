package aethereal.type;
import aethereal.util.FriendManager;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public enum EntityCategory {
    ANIMAL {
        @Override
        public boolean matches(Entity entity) {
            return entity instanceof AnimalEntity;
        }
    },
    FRIEND {
        @Override
        public boolean matches(Entity entity) {
            return (entity instanceof PlayerEntity) && FriendManager.isFriend(entity.getName().getString());
        }
    },
    MOB {
        @Override
        public boolean matches(Entity entity) {
            return entity instanceof HostileEntity;
        }
    },
    PLAYER {
        @Override
        public boolean matches(Entity entity) {
            return entity instanceof PlayerEntity;
        }
    },
    SELF {
        @Override
        public boolean matches(Entity entity) {
            ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
            return player != null && entity != null && player.getId() == entity.getId();
        }
    };

    public abstract boolean matches(Entity entity);
}
