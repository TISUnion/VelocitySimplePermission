package me.fallenbreath.velocitysimplepermission;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
		id = PluginMeta.ID, name = PluginMeta.NAME, version = PluginMeta.VERSION,
		url = "https://github.com/TISUnion/VelocityRememberServer",
		description = "A simple player permission provider for velocity",
		authors = {"Fallen_Breath"}
)
public class VelocitySimplePermissionPlugin
{
	private final ProxyServer server;
	private final PermissionStorage permissionStorage;

	@Inject
	public VelocitySimplePermissionPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory)
	{
		this.server = server;
		this.permissionStorage = new PermissionStorage(logger, dataDirectory.resolve("permission.yml"));
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event)
	{
		this.permissionStorage.load();
		this.server.getEventManager().register(this, PermissionsSetupEvent.class, this::setupPermission);
	}

	private void setupPermission(PermissionsSetupEvent event)
	{
		if (!(event.getSubject() instanceof Player))
		{
			return;
		}

		Player player = (Player)event.getSubject();
		event.setProvider(this.permissionStorage.createProviderFor(player));
	}
}
