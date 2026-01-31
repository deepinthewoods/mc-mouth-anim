package ninja.trek.mouthanim.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ninja.trek.mouthanim.audio.MicCapture;

import java.util.ArrayList;
import java.util.List;

public class MouthAnimConfigScreen {

    public static Screen create(Screen parent) {
        MouthAnimConfig defaults = new MouthAnimConfig();
        MouthAnimConfig config = MouthAnimConfig.HANDLER.instance();

        List<String> devices = new ArrayList<>();
        devices.add("System Default");
        devices.addAll(MicCapture.getAvailableInputDevices());

        // Ensure the saved mixer is in the list, fall back to System Default if not
        String mixer = config.selectedMixer;
        String currentDevice = (mixer == null || mixer.isEmpty()) ? "System Default" : mixer;
        if (!devices.contains(currentDevice)) {
            currentDevice = "System Default";
        }
        final String initialDevice = currentDevice;

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Mouth Anim Settings"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Audio Settings"))
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Max Volume"))
                                .description(OptionDescription.of(Component.literal("Maximum RMS volume level - increase if mouth stays wide open, decrease if it never opens")))
                                .binding(defaults.maxRms, () -> config.maxRms, v -> config.maxRms = v)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(1.0, 1000.0).step(1.0))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Slightly Open %"))
                                .description(OptionDescription.of(Component.literal("Percentage of max volume to trigger slightly open mouth")))
                                .binding(defaults.thresholdSlightlyOpen, () -> config.thresholdSlightlyOpen, v -> config.thresholdSlightlyOpen = v)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 100.0).step(1.0))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Open %"))
                                .description(OptionDescription.of(Component.literal("Percentage of max volume to trigger open mouth")))
                                .binding(defaults.thresholdOpen, () -> config.thresholdOpen, v -> config.thresholdOpen = v)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 100.0).step(1.0))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Wide Open %"))
                                .description(OptionDescription.of(Component.literal("Percentage of max volume to trigger wide open mouth")))
                                .binding(defaults.thresholdWideOpen, () -> config.thresholdWideOpen, v -> config.thresholdWideOpen = v)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 100.0).step(1.0))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Component.literal("Audio Input Device"))
                                .description(OptionDescription.of(Component.literal("Select which microphone to use for mouth animation")))
                                .binding("System Default", () -> initialDevice, v -> config.selectedMixer = "System Default".equals(v) ? "" : v)
                                .controller(opt -> CyclingListControllerBuilder.create(opt)
                                        .values(devices)
                                        .formatValue(v -> Component.literal(v)))
                                .build())
                        .build())
                .save(() -> {
                    MouthAnimConfig.HANDLER.save();
                    MicCapture.applyConfig(config);
                })
                .build()
                .generateScreen(parent);
    }
}
