package com.usagin.juicecraft.client.renderer.blockrenderers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.usagin.juicecraft.CommonEvents;
import com.usagin.juicecraft.Init.BlockEntityInit;
import com.usagin.juicecraft.blocks.FriendBedBlock;
import com.usagin.juicecraft.blocks.FriendBedEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.usagin.juicecraft.particles.AlteLightningParticle.LOGGER;

@OnlyIn(Dist.CLIENT)
public class FriendBedRenderer implements BlockEntityRenderer<FriendBedEntity> {
    private final ModelPart headRoot;
    private final ModelPart footRoot;

    public FriendBedRenderer(BlockEntityRendererProvider.Context pContext) {
        this.headRoot = pContext.bakeLayer(CommonEvents.FRIEND_BED_HEAD);
        this.footRoot = pContext.bakeLayer(CommonEvents.FRIEND_BED_FOOT);
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 6).addBox(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F), PartPose.rotation(((float) Math.PI / 2F), 0.0F, ((float) Math.PI / 2F)));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 18).addBox(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F), PartPose.rotation(((float) Math.PI / 2F), 0.0F, (float) Math.PI));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createFootLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 0).addBox(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F), PartPose.rotation(((float) Math.PI / 2F), 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 12).addBox(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F), PartPose.rotation(((float) Math.PI / 2F), 0.0F, ((float) Math.PI * 1.5F)));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void render(FriendBedEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Material material = Sheets.BED_TEXTURES[pBlockEntity.getColor().getId()];
        Level level = pBlockEntity.getLevel();
        if (level != null) {
            BlockState blockstate = pBlockEntity.getBlockState();
            DoubleBlockCombiner.NeighborCombineResult<? extends FriendBedEntity> neighborcombineresult = DoubleBlockCombiner.combineWithNeigbour(BlockEntityInit.FRIEND_BED.get(), FriendBedBlock::getBlockType, FriendBedBlock::getConnectedDirection, ChestBlock.FACING, blockstate, level, pBlockEntity.getBlockPos(), (p_112202_, p_112203_) -> {
                return false;
            });
            int i = neighborcombineresult.apply(new BrightnessCombiner<>()).get(pPackedLight);
            this.renderPiece(pPoseStack, pBuffer, blockstate.getValue(FriendBedBlock.PART) == BedPart.HEAD ? this.headRoot : this.footRoot, blockstate.getValue(BedBlock.FACING), material, i, pPackedOverlay, false);
        } else {
            this.renderPiece(pPoseStack, pBuffer, this.headRoot, Direction.SOUTH, material, pPackedLight, pPackedOverlay, false);
            this.renderPiece(pPoseStack, pBuffer, this.footRoot, Direction.SOUTH, material, pPackedLight, pPackedOverlay, true);
        }

    }

    /**
     * @param pFoot {@code true} if piece to render is the foot of the bed, {@code false} otherwise or if being rendered
     *              by a {@link net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer}
     */
    private void renderPiece(PoseStack pPoseStack, MultiBufferSource pBufferSource, ModelPart pModelPart, Direction pDirection, Material pMaterial, int pPackedLight, int pPackedOverlay, boolean pFoot) {
        pPoseStack.pushPose();
        pPoseStack.translate(0.0F, 0.5625F, pFoot ? -1.0F : 0.0F);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F + pDirection.toYRot()));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        VertexConsumer vertexconsumer = pMaterial.buffer(pBufferSource, RenderType::entitySolid);
        pModelPart.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }
}
