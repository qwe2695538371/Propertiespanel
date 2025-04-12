package qwedshuxingmianban;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qwedshuxingmianban.config.ModConfig;
import qwedshuxingmianban.data.PlayerDataManager;
import qwedshuxingmianban.event.PlayerEventHandler;
import qwedshuxingmianban.network.NetworkHandler;

public class Qwedshuxingmianban implements ModInitializer {
	public static final String MOD_ID = "qwedshuxingmianban";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfig CONFIG;

	@Override
	public void onInitialize() {
		// 加载配置文件
		CONFIG = ModConfig.load();

		// 注册网络处理器
		NetworkHandler.registerServerHandlers();

		// 注册玩家加入事件
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			PlayerDataManager.getData(player); // 确保加载数据
		});

		// 注册服务器停止事件
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				PlayerDataManager.saveData(player);
			}
		});

		// 注册服务器关闭事件
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			// 保存所有在线玩家的数据
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				PlayerDataManager.saveData(player);
			}
		});

		// 注册其他事件处理器
		PlayerEventHandler.init();
		LOGGER.info("属性面板模组已启动！");
	}
}