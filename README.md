# VelocitySimplePermission

A simple player permission provider for [velocity](https://github.com/PaperMC/Velocity)

Permission files: `plugins/velocitysimplepermission/permission.yml`

Tested with velocity `3.2.0`

## Permission file

The top-level yaml object is a string to object map.
The key is the permission key, e.g. `"velocity.command.glist"`. The value is the assignment of the permission.

The value can be one of the following things:

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
