package me.fallenbreath.velocitysimplepermission;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SimplePermissionProvider implements PermissionProvider, PermissionFunction
{
	private final Player player;
	private final String playerName;
	private final UUID playerUuid;
	private final Map<String, Object> permissions;

	public SimplePermissionProvider(Map<String, Object> permissions, Player player)
	{
		this.permissions = permissions;
		this.player = player;
		this.playerName = player.getGameProfile().getName();
		this.playerUuid = player.getGameProfile().getId();
	}

	@Override
	public PermissionFunction createFunction(PermissionSubject subject)
	{
		if (subject != this.player)
		{
			throw new IllegalArgumentException(String.format("subject should be player %s, but found %s", this.player, subject));
		}

		return this;
	}

	private boolean playerInList(List<?> list)
	{
		for (Object item : list)
		{
			if (item instanceof String && (item.equals(this.playerName) || item.equals(this.playerUuid.toString())))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Available objects:
	 * - String: "*", means always true
	 * - List of Strings, matching player name / uuid
	 */
	private Optional<Boolean> playerInObject(Object obj)
	{
		if (obj instanceof String)  // support wildcard only, which means allow all
		{
			return Optional.of(obj.equals("*"));
		}
		else if (obj instanceof List)  // a list of names, allow list
		{
			return Optional.of(playerInList((List<?>)obj));
		}
		return Optional.empty();
	}

	public Tristate getPermissionFromKey(String permissionKey)
	{
		Object obj = this.permissions.get(permissionKey);
		if (obj instanceof Map)
		{
			if (playerInObject(((Map<?, ?>)obj).get("allow")).orElse(false))
			{
				return Tristate.TRUE;
			}
			if (playerInObject(((Map<?, ?>)obj).get("deny")).orElse(false))
			{
				return Tristate.FALSE;
			}
		}
		else
		{
			Optional<Boolean> ok = playerInObject(obj);
			if (ok.isPresent() && ok.get())
			{
				return Tristate.TRUE;
			}
		}
		return Tristate.UNDEFINED;
	}

	@Override
	public Tristate getPermissionValue(String permissionKey)
	{
		// try the exact full key
		Tristate tristate = getPermissionFromKey(permissionKey);
		if (tristate != Tristate.UNDEFINED)
		{
			return tristate;
		}

		// try the full key with wildcard
		tristate = getPermissionFromKey(permissionKey + ".*");
		if (tristate != Tristate.UNDEFINED)
		{
			return tristate;
		}

		// try sub-key with wildcard
		int i = permissionKey.length();
		do
		{
			i = permissionKey.lastIndexOf('.', i - 1);
			String wildcardKey = permissionKey.substring(0, i + 1) + "*";
			tristate = getPermissionFromKey(wildcardKey);
			if (tristate != Tristate.UNDEFINED)
			{
				return tristate;
			}
		}
		while (i >= 0);

		return Tristate.UNDEFINED;
	}
}
