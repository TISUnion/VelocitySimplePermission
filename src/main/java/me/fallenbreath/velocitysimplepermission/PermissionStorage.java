package me.fallenbreath.velocitysimplepermission;

import com.google.common.collect.Maps;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PermissionStorage
{
	// permissionKey -> whatever
	private final Map<String, Object> permissions = Maps.newConcurrentMap();

	private final Logger logger;
	private final Path permissionFilePath;

	public PermissionStorage(Logger logger, Path permissionFilePath)
	{
		this.logger = logger;
		this.permissionFilePath = permissionFilePath;
	}

	public boolean load()
	{
		if (!this.permissionFilePath.toFile().exists())
		{
			try
			{
				var dir = this.permissionFilePath.getParent().toFile();
				if (!dir.exists() && !dir.mkdir())
				{
					throw new IOException(String.format("Create directory %s failed", dir));
				}
				Files.writeString(this.permissionFilePath, "");
			}
			catch (IOException e)
			{
				this.logger.error("Failed to create default permission file", e);
				return false;
			}
		}

		try
		{
			String yamlContent = Files.readString(this.permissionFilePath);
			Map<String, Object> permissions = Maps.newHashMap();
			var yaml = new Yaml().loadAs(yamlContent, Map.class);
			if (yaml != null)
			{
				((Map<?, ?>)yaml).forEach((k, v) -> {
					if (k instanceof String)
					{
						permissions.put(k.toString(), v);
					}
				});
			}

			this.permissions.clear();
			this.permissions.putAll(permissions);
			this.logger.info("Loaded permission file with {} permission keys", this.permissions.size());
			return true;
		}
		catch (Exception e)
		{
			this.logger.error("Failed to load permission file", e);
			return false;
		}
	}

	public PermissionProvider createProviderFor(Player player)
	{
		return new SimplePermissionProvider(this.permissions, player);
	}
}
