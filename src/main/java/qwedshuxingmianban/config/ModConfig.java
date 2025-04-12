package qwedshuxingmianban.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import qwedshuxingmianban.Qwedshuxingmianban;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve(Qwedshuxingmianban.MOD_ID + ".json").toFile();

    public Map<String, AttributeConfig> attributes = new HashMap<>();
    public GeneralConfig general = new GeneralConfig();

    public static class AttributeConfig {
        public int baseExperienceCost = 5;
        public int experienceIncrement = 2;
        public int maxLevel = 20;
        public double valuePerLevel = 1.0;
        public String displayName = "";
    }

    public static class GeneralConfig {
        public boolean allowReset = true;
        public double resetCostPercentage = 30.0; // 30% 损耗
        public int attributePriority = 800;
    }

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                Qwedshuxingmianban.LOGGER.error("加载配置文件失败", e);
            }
        }
        return createDefault();
    }

    private static ModConfig createDefault() {
        ModConfig config = new ModConfig();

        // 设置默认属性配置
        String[][] defaultAttributes = {
                {"max_health", "最大生命值"},
                {"armor", "护甲值"},
                {"armor_toughness", "盔甲韧性"},
                {"attack_damage", "攻击伤害"},
                {"attack_speed", "攻击速度"},
                {"movement_speed", "移动速度"},
                {"knockback_resistance", "击退抗性"},
                {"luck", "幸运"}
        };

        for (String[] attr : defaultAttributes) {
            AttributeConfig attrConfig = new AttributeConfig();
            attrConfig.displayName = attr[1];
            config.attributes.put(attr[0], attrConfig);
        }

        // 保存默认配置
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            Qwedshuxingmianban.LOGGER.error("保存配置文件失败", e);
        }
    }
}