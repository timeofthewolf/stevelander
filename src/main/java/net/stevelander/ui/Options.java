package net.stevelander.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

public final class Options {

    private Options() {
    }

    public static OptionInstance<Boolean> bool(String key, Supplier<Boolean> get, Consumer<Boolean> set) {
        return OptionInstance.createBoolean(key, get.get(), set::accept);
    }

    public static OptionInstance<Integer> intSlider(
        String key,
        int min,
        int max,
        Supplier<Integer> get,
        Consumer<Integer> set
    ) {
        return new OptionInstance<>(
            key,
            OptionInstance.noTooltip(),
            (caption, value) -> Component.translatable(key).append(": ").append(String.valueOf(value)),
            new OptionInstance.IntRange(min, max),
            get.get(),
            set::accept
        );
    }

    public static OptionInstance<Integer> floatSlider(
        String key,
        float min,
        float max,
        float step,
        Supplier<Float> get,
        Consumer<Float> set
    ) {
        final int scale = Math.round(1.0F / step);
        final int scaledMin = Math.round(min * scale);
        final int scaledMax = Math.round(max * scale);

        return new OptionInstance<>(
            key,
            OptionInstance.noTooltip(),
            (caption, value) -> Component.translatable(key)
                .append(": ")
                .append(String.format("%.2f", value / (float) scale)),
            new OptionInstance.IntRange(scaledMin, scaledMax),
            Math.round(get.get() * scale),
            value -> set.accept(value / (float) scale)
        );
    }

    public static OptionInstance<String> choice(
        String key,
        java.util.List<String> values,
        Supplier<String> get,
        Consumer<String> set
    ) {
        return new OptionInstance<>(
            key,
            OptionInstance.noTooltip(),
            (caption, value) -> Component.literal(value),
            new OptionInstance.Enum<>(values, com.mojang.serialization.Codec.STRING),
            get.get(),
            set::accept
        );
    }
}
