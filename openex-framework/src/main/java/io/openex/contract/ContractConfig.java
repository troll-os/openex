package io.openex.contract;

import io.openex.helper.SupportedLanguage;
import lombok.Getter;

import java.util.Map;

@Getter
public class ContractConfig {
    private final String type;
    private final boolean expose;
    private final Map<SupportedLanguage, String> label;
    private final String color_dark;
    private final String color_light;
    private final String icon;

    public ContractConfig(String type, Map<SupportedLanguage, String> label, String color_dark, String color_light, String icon, boolean expose) {
        this.type = type;
        this.expose = expose;
        this.color_dark = color_dark;
        this.color_light = color_light;
        this.icon = icon;
        this.label = label;
    }

}
