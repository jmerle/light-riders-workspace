const express = require('express');
const app = express();
const http = require('http').Server(app);
const io = require('socket.io')(http);
const LightRidersRunner = require('./src/LightRidersRunner');

const config = require('./wrapper-commands.json');
config.match.bots.forEach((bot, i) => {
  bot.id = i;
  bot.name = bot.name || `Player ${i}`;
  bot.emailHash = bot.emailHash || '';
  bot.stderrEnabled = bot.stderrEnabled || false;
});

app.use(express.static('public'));

io.on('connection', socket => {
  const runner = new LightRidersRunner(config);

  socket.on('run match', async () => {
    console.log('Running a match');

    try {
      const matchData = await runner.runMatch();
      await runner.writeToMatchViewer(matchData.resultFile);

      socket.emit('match results', matchData);
    } catch (error) {
      console.error(error);
    }
  });

  socket.on('run batch', async data => {
    console.log(`Batch running ${data.amount} matches with side switching ${data.switchSides ? 'enabled' : 'disabled'}`);

    config.match.bots.forEach(bot => bot.wins = 0);
    let draws = 0;
    let failedMatches = 0;

    let sidesSwitched = false;
    for (let i = 1; i <= data.amount; i++) {
      try {
        const matchData = await runner.runMatch(sidesSwitched);

        let winnerID = matchData.resultFile.details.winner;
        winnerID = winnerID === 'null' ? null : parseInt(winnerID);
        if (winnerID !== null) {
          if (sidesSwitched) winnerID = +!winnerID;
          config.match.bots[winnerID].wins++;
        } else {
          draws++;
        }
      } catch (error) {
        console.error(error);
        failedMatches++;
      }

      socket.emit('batch update', {
        draws,
        failedMatches,
        matchesPlayed: i,
        totalMatches: data.amount,
        bots: config.match.bots
      });

      if (data.switchSides) sidesSwitched = !sidesSwitched;
    }
  });

  socket.on('get config', () => {
    socket.emit('config', config);
  });
});

http.listen(8080, () => {
  console.log('Webserver is running at http://localhost:8080/');
});
