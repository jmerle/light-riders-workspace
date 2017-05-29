const setMatchRunning = state => {
  if (state) {
    $('.log.segment > pre').html('&nbsp;');
    $('.log.segment > .dimmer, #viewer > .dimmer').addClass('active');
    $('#run-match').addClass('disabled loading');
  } else {
    $('.log.segment > .dimmer, #viewer > .dimmer').removeClass('active');
    $('#run-match').removeClass('disabled loading');
  }
};

const setBatchRunning = state => {
  if (state) {
    $('#run-batch').addClass('disabled loading');
  } else {
    $('#run-batch').removeClass('disabled loading');
  }
};

const socket = io();

$('.ui.menu .item').tab();
$('.ui.progress').progress();
$('.ui.checkbox').checkbox();

$('#run-match').on('click', e => {
  setMatchRunning(true);
  socket.emit('run match');
});

$('#batch-settings').on('submit', e => {
  e.preventDefault();

  setBatchRunning(true);

  $(`#batch-table > tbody > tr`).each((i, elem) => {
    $(elem).find('td:eq(1)').html('0');
    $(elem).find('td:eq(2)').html('0.00%');
  });

  const amount = parseInt($('#batch-amount').val());
  const switchSides = $('#batch-switch-sides').is(':checked');

  $('#batch-progress').progress('set percent', 0);
  $('#batch-progress').progress('set label', `Running match 1 of ${amount} (0 draws and 0 failed matches)`);

  socket.emit('run batch', { amount, switchSides });
});

socket.on('match results', matchData => {
  $('.log.segment[data-tab="bot-stderr"] > pre').html(matchData.stderr);
  $('.log.segment[data-tab="engine-stdout"] > pre').html(matchData.stdout);
  $('.log.segment[data-tab="resultfile"] > pre').html(JSON.stringify(matchData.resultFile, null, 2));

  $('#viewer iframe').attr('src', $('#viewer iframe').attr('src'));

  setMatchRunning(false);
});

socket.on('batch update', data => {
  data.bots.forEach(bot => {
    const $row = $(`#batch-table > tbody > tr:eq(${bot.id})`);
    $row.find('td:eq(0)').html(bot.name);
    $row.find('td:eq(1)').html(bot.wins);
    $row.find('td:eq(2)').html((bot.wins / (data.totalMatches - data.failedMatches) * 100).toFixed(2) + '%');
  });

  $('#batch-progress').progress('set percent', data.matchesPlayed / data.totalMatches * 100);

  if (data.matchesPlayed === data.totalMatches) {
    setBatchRunning(false);
    $('#batch-progress').progress('set label', `Finished with ${data.draws} draws and ${data.failedMatches} failed matches`);
  } else {
    $('#batch-progress').progress('set label', `Running match ${data.matchesPlayed + 1} of ${data.totalMatches} (${data.draws} draws and ${data.failedMatches} failed matches)`);
  }
});

socket.on('config', config => {
  config.match.bots.forEach(bot => {
    const $row = $(`#batch-table > tbody > tr:eq(${bot.id}) > td:first-child`).html(bot.name);
  });
});

setMatchRunning(true);
socket.emit('run match');
socket.emit('get config');
