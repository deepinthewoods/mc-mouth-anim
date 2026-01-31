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

        String currentDevice = config.selectedMixer.isEmpty() ? "System Default" : config.selectedMixer;

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Mouth Anim Settings"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Audio Settings"))
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Slightly Open Threshold"))
                                .description(OptionDescription.of(Component.literal("RMS volume level to trigger slightly open mouth")))
                                .binding(defaults.thresholdSlightlyOpen, () -> config.thresholdSlightlyOpen, v -> config.thresholdSlightlyOpen = v)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 5000.0).step(10.0))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Open Threshold"))
                                .description(OptionDescription.of(Component.literal("RMS volume level to trigger open mouth")))
                                .binding(defaults.thresholdOpen, () -> config.thresholdOpen, v -> config.thresholdOpen = v)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 5000.0).step(10.0))
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Wide Open Threshold"))
                                .description(OptionDescription.of(Component.literal("RMS volume level to trigger wide open mouth")))
                                .binding(defaults.thresholdWideOpen, () -> config.thresholdWideOpen, v -> config.thresholdWideOpen = v)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 5000.0).step(10.0))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Component.literal("Audio Input Device"))
                                .description(OptionDescription.of(Component.literal("Select which microphone to use for mouth animation")))
                                .binding("System Default", () -> currentDevice, v -> config.selectedMixer = v.equals("System Default") ? "" : v)
                                .controller(opt -> CyclingListControllerBuilder.create(opt).values(devices))
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
