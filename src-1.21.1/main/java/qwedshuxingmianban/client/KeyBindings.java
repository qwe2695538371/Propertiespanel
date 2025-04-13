package qwedshuxingmianban.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding openAttributePanel;

    public static void register() {
        openAttributePanel = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qwedshuxingmianban.open_panel", // 键位的翻译键
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P, // 默认使用P键
                "category.qwedshuxingmianban.general" // 分类的翻译键
        ));
    }
}