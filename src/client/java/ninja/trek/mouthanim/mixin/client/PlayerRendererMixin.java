package ninja.trek.mouthanim.mixin.client;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.LivingEntity;
import ninja.trek.mouthanim.render.MouthFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer {

    public PlayerRendererMixin(EntityRendererProvider.Context context, Object model, float shadowRadius) {
        super(context, null, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addMouthLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        this.addLayer(new MouthFeatureRenderer((AvatarRenderer) (Object) this));
    }
}
