package com.monstruos.client;

import com.monstruos.MonstruoEntity;
import com.monstruos.MonstruosMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class MonstruoRenderer extends MobEntityRenderer<MonstruoEntity, MonstruoModel> {
    private final Identifier texture;
    private final float modelScale;

    public MonstruoRenderer(EntityRendererFactory.Context ctx, int variant) {
        super(ctx, new MonstruoModel(ctx.getPart(MonstruosClient.layer(variant))), 0.6f);
        this.texture = new Identifier(MonstruosMod.MOD_ID, "textures/entity/monstruo_" + variant + ".png");
        // Convierte los píxeles del modelo a la altura real en bloques
        this.modelScale = MonstruosMod.HEIGHT[variant - 1] * 16f / MonstruosMod.MODEL_H[variant - 1];
    }

    @Override
    public Identifier getTexture(MonstruoEntity entity) {
        return texture;
    }

    @Override
    protected void scale(MonstruoEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(modelScale, modelScale, modelScale);
    }
}
