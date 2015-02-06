# API

Everything Playmatcher does, apart from logging in, is powered by its REST API. All responses are encoded as JSON.

## Types

In type names, `[A]` will be used as a shorthand for list of `A`. For brevity, other JSON core types will not be defined here.

### Game

What Playmatcher knows about a single game.

Fields:

* `id` :: `long` - Internal game ID.
* `name` :: `string` - Display name.
* `icon` :: `string` - The absolute path to the icon to display, may be null.

### UserGame

The relationship between the owner user and a `Game`. A `UserGame` object is always in the context of yourself.

Fields:

* `id` :: `long` - Internal ID of the relationship.
* `enabled` :: `bool` - Whether or not this game should be taken into account for matchmaking.
* `game` :: `Game` - The `Game` that this relationship represents.

## Endpoints

### `/api/user/me/games`

#### GET

Returns all `UserGame`s for the current user.

### `/api/user/me/game/all/enabled`

#### POST

Disable or enable all `UserGame`s for the current user.

Parameters:

* `enable` :: `bool` - Whether to enable or disable the games.

### `/api/user/me/game/:gameID/enabled`

#### POST

Disable or enable the `UserGame` for the current user corresponding to the `Game` with the id `gameID`.

Parameters:

* `enable` :: `bool` - Whether to enable or disable the game.

## Authentication

Authentication is initiated by browsing to the `/login/steam` address, which should direct the user to Steam's OpenID login page. An address to go to in the case of success can be given through the `returnTo` parameter.