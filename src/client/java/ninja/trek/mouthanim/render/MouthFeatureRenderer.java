package ninja.trek.mouthanim.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import ninja.trek.mouthanim.MouthAnim;
import ninja.trek.mouthanim.MouthAnimClient;
import ninja.trek.mouthanim.MouthState;

public class MouthFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final Identifier MOUTH_TEXTURE =
            Identifier.fromNamespaceAndPath(MouthAnim.MOD_ID, "textures/mouth_states.png");

    // Sprite sheet: 16x16 texture, 4 frames stacked vertically, each frame is 16x4 px
    private static final float FRAME_HEIGHT_UV = 0.25f;

    // Quad dimensions in model space (1/16 of a block per pixel)
    private static final float HALF_WIDTH = 2.0f / 16.0f;
    private static final float HALF_HEIGHT = 1.0f / 16.0f;

    // Position offset: center of face, slightly below center
    private static final float OFFSET_Y = -1.0f / 16.0f;
    private static final float OFFSET_Z = -4.01f / 16.0f;

    public MouthFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       AvatarRenderState renderState, float limbSwing, float limbSwingAmount) {

        // Look up the player UUID from the entity ID in the render state
        MouthState state = MouthState.CLOSED;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(renderState.id);
            if (entity instanceof Player player) {
                state = MouthAnimClient.getMouthStateForPlayer(player.getUUID());
            }
        }

        poseStack.pushPose();

        // Transform to follow head rotation
        getParentModel().head.translateAndRotate(poseStack);

        // Get UV coordinates for the current mouth state frame
        float vMin = state.getId() * FRAME_HEIGHT_UV;
        float vMax = vMin + FRAME_HEIGHT_UV;

        // Use submitCustomGeometry to draw the mouth quad
        final MouthState finalState = state;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(MOUTH_TEXTURE),
                (pose, consumer) -> {
                    float v0 = finalState.getId() * FRAME_HEIGHT_UV;
                    float v1 = v0 + FRAME_HEIGHT_UV;

                    // Draw quad: bottom-left, bottom-right, top-right, top-left
                    consumer.addVertex(pose, -HALF_WIDTH, OFFSET_Y - HALF_HEIGHT, OFFSET_Z)
                            .setColor(255, 255, 255, 255)
                            .setUv(0.0f, v1)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(packedLight)
                            .setNormal(pose, 0.0f, 0.0f, -1.0f);

                    consumer.addVertex(pose, HALF_WIDTH, OFFSET_Y - HALF_HEIGHT, OFFSET_Z)
                            .setColor(255, 255, 255, 255)
                            .setUv(1.0f, v1)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(packedLight)
                            .setNormal(pose, 0.0f, 0.0f, -1.0f);

                    consumer.addVertex(pose, HALF_WIDTH, OFFSET_Y + HALF_HEIGHT, OFFSET_Z)
                            .setColor(255, 255, 255, 255)
                            .setUv(1.0f, v0)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(packedLight)
                            .setNormal(pose, 0.0f, 0.0f, -1.0f);

                    consumer.addVertex(pose, -HALF_WIDTH, OFFSET_Y + HALF_HEIGHT, OFFSET_Z)
                            .setColor(255, 255, 255, 255)
                            .setUv(0.0f, v0)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(packedLight)
                            .setNormal(pose, 0.0f, 0.0f, -1.0f);
                });

        poseStack.popPose();
    }
}
