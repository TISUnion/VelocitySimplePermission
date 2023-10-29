# VelocitySimplePermission

A simple player permission provider for [velocity](https://github.com/PaperMC/Velocity)

Permission files: `plugins/velocitysimplepermission/permission.yml`

Tested with velocity `3.2.0`

## Permission file

The top-level yaml object is a string to object map, defining a series of permission assignments

Here's an example of the permission file, that allows everyone to use the `/glist` command, 
and allow specified admins to use all velocity commands:

```yaml
velocity.command.glist: '*'
velocity.command.*: 
- NameOfAdmin1
- NameOfAdmin2
- 8e99eeb6-204e-4bde-9764-87e122c272ed  # uuid of the admin3
```

### Key

The key defines what the permission assignment will be applied on. It can be one of the following things:

1. A complete permission key (e.g. `example.command.foo`),
   which matches the given key exactly
2. A wildcard permission key ends with `*` (e.g. `example.command.*`), 
   which matches all sub-keys under the given wildcard key (e.g. `example.command.oof`, `example.command.foo.bar`, but not `example.action`)

The matching priority is:

1. Complete key (e.g. `example.command.foo`)
2. Longest wildcard key to the shortest wildcard key (e.g. `example.command.foo.*`, `example.command.*`, `example.*`, `*`)

If any candidate key has a valid value, and the value returns a known query result (i.e. not `Tristate.UNDEFINED`),
then the result will be returned immediately, no more further query attempts will be made

### Value

The value is the assignment of the permission. It can be one of the following things:

1. A `"*"`, means all players have this permission (`Tristate.TRUE`)

   Example:
   ```yaml
   velocity.command.glist: '*'
   ```
2. A list of strings. The string values are player names or player uuids (with dash character). Players inside the list have the permission (`Tristate.TRUE`)

   Example:
   ```yaml
   velocity.command.glist: 
   - Steve
   - Alex
   ```
3. A string to object map, containing 2 entries, `"allow"` and `"deny"`
   
   The value of the 2 entries can be a `"*"`, or a list of strings. The meaning of the value is consistent with the definitions in 1. and 2. above.
   Players in the allow list will have the permission (`Tristate.TRUE`).
   Players in the deny list will be denied (`Tristate.FALSE`)

   Example:
   ```yaml
   velocity.command.glist: 
     allow: 
     - Steve
     - Alex 
     deny: 
     - Mallory
   ```
   
If the queried permission is unknown, or the player is not included in the list, 
the plugin will simply return the undefined permission `Tristate.UNDEFINED` to velocity, 
which is also the default behavior when there's no permission provider plugin installed

## Command

To keep it simple, VelocitySimplePermission does not provide commands to modify the permission assignments

If you want to modify the permission configuration, you need edit the permission file manually, then use command to perform a reload

Require permission `velocitysimplepermission.command`

Command list:

- `/vsp reload`: reload the permission file
