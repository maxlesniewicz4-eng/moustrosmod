package com.monstruos.client;

import com.monstruos.MonstruoEntity;
import com.monstruos.MonstruosMod;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/** Modelo "cartón": una lámina fina con la imagen del monstruo en el frente y la espalda. */
public class MonstruoModel extends EntityModel<MonstruoEntity> {
    private final ModelPart body;

    public MonstruoModel(ModelPart root) {
        this.body = root.getChild("body");
    }

    public static TexturedModelData createData(int variant) {
        int w = MonstruosMod.MODEL_W[variant - 1];
        int h = MonstruosMod.MODEL_H[variant - 1];
        ModelData data = new ModelData();
        data.getRoot().addChild("body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-w / 2f, -h, -0.5f, w, h, 1f),
                ModelTransform.pivot(0f, 24f, 0f));
        return TexturedModelData.of(data, 2 * w + 2, h + 1);
    }

    @Override
    public void setAngles(MonstruoEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // Se balancea al caminar
        this.body.roll = MathHelper.sin(limbAngle * 0.6662f) * 0.12f * limbDistance;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
                       float red, float green, float blue, float alpha) {
        this.body.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
