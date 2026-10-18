package com.monstruos.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.monstruos.MonstruosMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.Random;

public class MonstruosClient implements ClientModInitializer {
    public static final int SCARE_DURATION = 45; // ticks (~2.2 s)
    private static int scareTicks = 0;
    private static int scareVariant = 1;
    private static final Random RANDOM = new Random();

    public static EntityModelLayer layer(int variant) {
        return new EntityModelLayer(new Identifier(MonstruosMod.MOD_ID, "monstruo_" + variant), "main");
    }

    @Override
    public void onInitializeClient() {
        for (int v = 1; v <= MonstruosMod.COUNT; v++) {
            final int variant = v;
            EntityModelLayerRegistry.registerModelLayer(layer(variant), () -> MonstruoModel.createData(variant));
            EntityRendererRegistry.register(MonstruosMod.TYPES.get(variant - 1),
                    ctx -> new MonstruoRenderer(ctx, variant));
        }

        // Llega el paquete del servidor: ¡SUSTO!
        ClientPlayNetworking.registerGlobalReceiver(MonstruosMod.JUMPSCARE_PACKET,
                (client, handler, buf, responseSender) -> {
                    int v = buf.readInt();
                    client.execute(() -> {
                        scareVariant = v;
                        scareTicks = SCARE_DURATION;
                        client.getSoundManager().play(
                                PositionedSoundInstance.master(SoundEvents.ENTITY_GHAST_SCREAM, 0.6f, 1.5f));
                        client.getSoundManager().play(
                                PositionedSoundInstance.master(SoundEvents.ENTITY_ENDERMAN_SCREAM, 0.5f, 1.5f));
                    });
                });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (scareTicks > 0) {
                scareTicks--;
            }
        });

        HudRenderCallback.EVENT.register(MonstruosClient::renderScare);
    }

    private static void renderScare(DrawContext ctx, float tickDelta) {
        if (scareTicks <= 0) {
            return;
        }
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        float progress = 1f - (scareTicks / (float) SCARE_DURATION); // 0 -> 1

        // Fondo negro
        ctx.fill(0, 0, sw, sh, 0xFF000000);

        int idx = scareVariant - 1;
        int tw = MonstruosMod.GUI_W[idx];
        int th = MonstruosMod.GUI_H[idx];
        float scale = sh * (1.0f + progress * 0.6f) / th; // se acerca a la cara
        int dw = (int) (tw * scale);
        int dh = (int) (th * scale);
        int shake = 8;
        int x = (sw - dw) / 2 + RANDOM.nextInt(shake * 2 + 1) - shake;
        int y = (sh - dh) / 2 + RANDOM.nextInt(shake * 2 + 1) - shake;

        Identifier tex = new Identifier(MonstruosMod.MOD_ID, "textures/gui/jumpscare_" + scareVariant + ".png");
        RenderSystem.enableBlend();
        ctx.drawTexture(tex, x, y, dw, dh, 0, 0, tw, th, tw, th);
        RenderSystem.disableBlend();

        // Destello rojo que se apaga
        int alpha = (int) (90 * (1f - progress));
        ctx.fill(0, 0, sw, sh, (alpha << 24) | 0x00AA0000);
    }
}
