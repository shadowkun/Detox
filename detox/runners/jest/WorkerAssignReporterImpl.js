const _ = require('lodash');
const chalk = require('chalk').default;
const ReporterBase = require('./ReporterBase');
const log = require('../../src/utils/logger').child();

class WorkerAssignReporterImpl extends ReporterBase {
  constructor(detox) {
    super();
    this.device = detox.device;
  }

  report(workerName) {
    let deviceName = _.attempt(() => this.device.name);
    if (!_.isError(deviceName)) {
      deviceName = undefined;
    }

    log.info({event: 'WORKER_ASSIGN'}, `${chalk.whiteBright(workerName)} assigned to ${chalk.blueBright(deviceName)}`);
  }
}

module.exports = WorkerAssignReporterImpl;
