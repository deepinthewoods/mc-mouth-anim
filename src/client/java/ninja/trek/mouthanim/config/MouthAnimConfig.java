package ninja.trek.mouthanim.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public class MouthAnimConfig {
    public static final ConfigClassHandler<MouthAnimConfig> HANDLER = ConfigClassHandler.createBuilder(MouthAnimConfig.class)
            .id(Identifier.fromNamespaceAndPath("mouth-anim", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("mouth-anim.json"))
                    .build())
            .build();

    @SerialEntry
    public double thresholdSlightlyOpen = 200.0;

    @SerialEntry
    public double thresholdOpen = 800.0;

    @SerialEntry
    public double thresholdWideOpen = 2000.0;

    @SerialEntry
    public String selectedMixer = "";
}
