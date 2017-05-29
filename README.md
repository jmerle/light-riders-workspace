# Light Riders Workspace
A testing workspace for Riddles.io's [Light Riders](https://starapple.riddles.io/competitions/light-riders) contest.

![Screenshot](http://i.imgur.com/34eKRU0.png)

## Installation
1. Clone or download this repository.
2. Run `npm install`.
3. Copy *wrapper-commands.example.json* to *wrapper-commands.json* and change the values in the bots part to your settings. More on that below.
4. Run `npm start` and go to `http://localhost:8080/`.

## Wrapper-commands configuration
The wrapper-commands is used by the game wrapper which communicates with the game engine and by the application to serve as a configuration file. The only things you have to modify are the values in the `bots` part. The following properties are necessary.

<dl>
  <dt>command</dt>
  <dd>The command used to run this bot. This property is the same in the original wrapper-commands.

  <dt>name</dt>
  <dd>The name of this bot. This value is shown in the match viewer and the batch runner.</dd>

  <dt>emailHash</dt>
  <dd>The avatars in the match viewer are coming from Gravatar. To get your Gravatar in the match viewer, the md5 hash of the email address associated with your Gravatar account is necessary. If no hash or an invalid hash is given, a default avatar is shown instead.</dd>

  <dt>stderrEnabled</dt>
  <dd>A property which is read by the modified game wrapper. If set to true, this bot's stderr will be written to the game wrapper's stderr, and therefore show up in the *Bot stderr* tab next to the match viewer. Set this value to `true` on the bot you are working on.</dd>
</dl>

*Note: changes made to the wrapper-commands while the application is running aren't used by the application until it's restarted.*

## Thanks to Riddles.io
This application would not be possible without the game engine, game wrapper and match viewer. All of these were made by the awesome people over at @riddlesio.
