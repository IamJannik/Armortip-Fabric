package net.bmjo.armortip.config;

import net.bmjo.armortip.Armortip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class ArmortipConfigMenu extends OptionsSubScreen {
    public ArmortipConfigMenu(Screen previous) {
        super(previous, Minecraft.getInstance().options, Component.translatable(Armortip.MOD_ID + ".options"));
    }

    @Override
    protected void addOptions() {
        if (this.list != null) {
            this.list.addBig(ArmortipConfig.isEnabled);
            this.list.addSmall(ArmortipConfig.getOptions().toArray(new OptionInstance[0]));
        }
    }

    @Override
    public void removed() {
        ArmortipConfig.save();
    }
}
