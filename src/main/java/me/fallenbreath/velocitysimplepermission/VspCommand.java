package me.fallenbreath.velocitysimplepermission;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;

public class VspCommand
{
	private final PermissionStorage permissionStorage;

	public VspCommand(PermissionStorage permissionStorage)
	{
		this.permissionStorage = permissionStorage;
	}

	public void register(CommandManager commandManager)
	{
		var root = LiteralArgumentBuilder.<CommandSource>literal("vsp").
				requires(s -> s.hasPermission(PluginMeta.ID + ".command")).
				then(LiteralArgumentBuilder.<CommandSource>literal("reload").
						executes(c -> reloadPermissionFile(c.getSource()))
				);

		commandManager.register(new BrigadierCommand(root.build()));
	}

	private int reloadPermissionFile(CommandSource source)
	{
		var ok = this.permissionStorage.load();
		if (ok)
		{
			source.sendMessage(Component.text("Permission reloaded"));
		}
		else
		{
			source.sendMessage(Component.text("Permission reload failed, check console for more information"));
		}
		return ok ? 0 : 1;
	}
}
