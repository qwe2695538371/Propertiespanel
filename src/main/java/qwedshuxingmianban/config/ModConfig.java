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
        public double resetCostPercentage = 30.0;
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

        // 定义默认属性配置
        Object[][] defaultAttributes = {
                {"max_health", "最大生命值", 1.0},
                {"armor", "护甲值", 1.0},
                {"armor_toughness", "盔甲韧性", 1.0},
                {"attack_damage", "攻击伤害", 0.1},
                {"attack_speed", "攻击速度", 0.2},
                {"movement_speed", "移动速度", 0.002},
                {"knockback_resistance", "击退抗性", 0.05},
                {"luck", "幸运", 0.1}
        };

        // 创建每个属性的配置
        for (Object[] attr : defaultAttributes) {
            AttributeConfig attrConfig = new AttributeConfig();
            attrConfig.displayName = (String) attr[1];
            attrConfig.valuePerLevel = (Double) attr[2];
            // 其他值保持默认
            // baseExperienceCost = 5
            // experienceIncrement = 2
            // maxLevel = 20
            config.attributes.put((String) attr[0], attrConfig);
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