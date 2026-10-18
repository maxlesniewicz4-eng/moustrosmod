package com.monstruos;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class MonstruoEntity extends HostileEntity {
    private final int variant;      // 1..4 (qué monstruo es)
    private int scareCooldown = 0;  // ticks hasta el próximo susto

    public MonstruoEntity(EntityType<? extends HostileEntity> type, World world, int variant) {
        super(type, world);
        this.variant = variant;
    }

    public int getVariant() {
        return variant;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                // 10 de daño = 5 corazones = la MITAD de la vida de un jugador (20 = 10 corazones)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 16.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (scareCooldown > 0) {
            scareCooldown--;
        }
        LivingEntity target = this.getTarget();
        // Susto cuando se acerca a un jugador que lo ve
        if (scareCooldown <= 0 && target instanceof ServerPlayerEntity player
                && this.squaredDistanceTo(player) < 36.0 && this.canSee(player)) {
            scare(player);
        }
    }

    /** Manda el jumpscare a pantalla completa al jugador. */
    private void scare(ServerPlayerEntity player) {
        scareCooldown = 200; // 10 segundos entre sustos
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(variant);
        ServerPlayNetworking.send(player, MonstruosMod.JUMPSCARE_PACKET, buf);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 80, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
    }

    /** Ataque: daño fuerte + "te come" (sonidos de comer y se cura). */
    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.5f, 0.6f);
            this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1.0f, 0.5f);
            this.heal(4.0f);
            if (target instanceof ServerPlayerEntity player && scareCooldown <= 100) {
                scare(player);
            }
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMAN_DEATH;
    }
}
