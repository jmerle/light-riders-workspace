const fs = require('fs');
const spawn = require('child_process').spawn;

class LightRidersRunner {
  constructor(config) {
    this.config = config;
    this.configSidesSwitched = JSON.parse(JSON.stringify(config));
    this.configSidesSwitched.match.bots = this.configSidesSwitched.match.bots.reverse();
  }

  runMatch(switchedSides = false) {
    return new Promise((resolve, reject) => {
      const wrapper = spawn('java', ['-jar', 'engine/modified-game-wrapper-1.2.6.jar', JSON.stringify(switchedSides ? this.configSidesSwitched : this.config)]);

      const stdout = [];
      const stderr = [];

      wrapper.stdout.on('data', data => {
        data = data.toString().trim();
        if (data !== '') stdout.push(data);
      });

      wrapper.stderr.on('data', data => {
        data = data.toString().trim();
        if (data !== '') stderr.push(data);
      });

      wrapper.on('error', reject);

      wrapper.on('close', code => {
        if (code !== 0) {
          reject(new Error(`The game wrapper exited with code ${code}`));
        }

        const resultFile = JSON.parse(fs.readFileSync(this.config.wrapper.resultFile).toString());
        resultFile.game = JSON.parse(resultFile.game);
        resultFile.details = JSON.parse(resultFile.details);

        resolve({
          resultFile,
          stdout: stdout.join('\n'),
          stderr: stderr.join('\n')
        });
      });
    });
  }

  writeToMatchViewer(resultFile) {
    return new Promise((resolve, reject) => {
      const windowData = {};

      try {
        windowData.matchData = resultFile.game;
      } catch (error) {
        reject(error);
      }

      windowData.playerData = [{
        name: this.config.match.bots[0].name,
        emailHash: this.config.match.bots[0].emailHash
      }, {
        name: this.config.match.bots[1].name,
        emailHash: this.config.match.bots[1].emailHash
      }];

      const page = `
        <!DOCTYPE html>
        <html lang="en">
          <head>
            <!-- Made by the awesome people over at Riddles.io -->

            <meta charset="UTF-8">
            <meta name="robots" content="noindex, nofollow">
            <title>Light Riders Match Viewer</title>
            <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
            <link rel="stylesheet" href="css/v8.min.css">
            <link rel="stylesheet" href="css/v8-override.css">
          </head>
          <body>
            <div id="player" style="width: 100%; height: 100%;"></div>
            <script id="gameData">
              (function(){
                window.__data__ = ${JSON.stringify(windowData)};
              }());
            </script>
            <script src="js/v8.min.js"></script>
          </body>
        </html>
      `;

      fs.writeFile('public/matchviewer.html', page, error => {
        if (error) reject(error);

        resolve();
      });
    });
  }
}

module.exports = LightRidersRunner;
