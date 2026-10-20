package net.bmjo.armortip.util;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.bmjo.armortip.config.ArmortipConfigMenu;

public class ArmortipModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ArmortipConfigMenu::new;
    }
}
