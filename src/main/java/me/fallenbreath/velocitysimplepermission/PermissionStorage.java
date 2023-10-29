package me.fallenbreath.velocitysimplepermission;

import com.google.common.collect.Maps;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PermissionStorage
{
	// permissionKey -> whatever
	private final Map<String, Object> permissions = Maps.newHashMap();

	private final Logger logger;
	private final Path permissionFilePath;

	public PermissionStorage(Logger logger, Path permissionFilePath)
	{
		this.logger = logger;
		this.permissionFilePath = permissionFilePath;
	}

	public void load()
	{
		this.permissions.clear();

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
				return;
			}
		}

		try
		{
			String yamlContent = Files.readString(this.permissionFilePath);
			var yaml = new Yaml().loadAs(yamlContent, Map.class);
			if (yaml != null)
			{
				((Map<?, ?>)yaml).forEach((k, v) -> {
					if (k instanceof String)
					{
						this.permissions.put(k.toString(), v);
					}
				});
			}
			this.logger.info("Loaded permission file with {} permission keys", this.permissions.size());
		}
		catch (Exception e)
		{
			this.logger.error("Failed to load permission file", e);
		}
	}

	private static boolean playerInList(Player player, List<?> list)
	{
		String name = player.getGameProfile().getName();
		UUID uuid = player.getGameProfile().getId();
		for (Object item : list)
		{
			if (item instanceof String && (item.equals(name) || item.equals(uuid.toString())))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Available objects:
	 *   - String: "*", means always true
	 *   - List of Strings, matching player name / uuid
	 */
	private static Optional<Boolean> playerInObject(Player player, Object obj)
	{
		if (obj instanceof String)  // support wildcard only, which means allow all
		{
			return Optional.of(obj.equals("*"));
		}
		else if (obj instanceof List)  // a list of names, allow list
		{
			return Optional.of(playerInList(player, (List<?>)obj));
		}
		return Optional.empty();
	}

	public PermissionProvider createProviderFor(Player player)
	{
		return subject -> {
			if (subject != player)
			{
				throw new IllegalArgumentException(String.format("subject should be player %s, but found %s", player, subject));
			}

			return permissionKey -> {
				Object obj = this.permissions.get(permissionKey);
				if (obj instanceof Map)
				{
					if (playerInObject(player, ((Map<?, ?>)obj).get("allow")).orElse(false))
					{
						return Tristate.TRUE;
					}
					if (playerInObject(player, ((Map<?, ?>)obj).get("deny")).orElse(false))
					{
						return Tristate.FALSE;
					}
				}
				else
				{
					Optional<Boolean> ok = playerInObject(player, obj);
					if (ok.isPresent() && ok.get())
					{
						return Tristate.TRUE;
					}
				}
				return Tristate.UNDEFINED;
			};
		};
	}
}
