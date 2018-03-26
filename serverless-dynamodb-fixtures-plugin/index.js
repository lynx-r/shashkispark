'use strict';

const path = require('path'),
    Promise = require('bluebird'),
    fs = require('fs'),
    AWS = require('aws-sdk');


class DynamoDbFixtures {
    constructor(serverless, options) {
        this.serverless = serverless;
        this.provider = 'aws';
        this.aws = this.serverless.getProvider(this.provider);
        this.fixturesPath = path.join(this.serverless.config.projectPath, 'fixtures');
        this.name = 'serverless-dynamodb-fixtures-plugin';

        this.commands = {
            client: {
                usage: 'Load your dynamodb fixtures',
                lifecycleEvents: ['load'],
            }
        };

        this.hooks = {
            'client:client': () => {
                this.serverless.cli.log(this.commands.client.usage);
            },

            'client:client:load': () => {
                this.stage = options.stage || _.get(serverless, 'service.provider.stage');
                this.region = options.region || _.get(serverless, 'service.provider.region');
                this.loadFixture.bind(this);
            }
        };

    }

    loadFixture(evt) {

        let _this = this;
        _this.evt = evt;

        // Flow
        return _this._prompt()
            .bind(_this)
            .then(_this._validateParams)
            .then(_this._loadFixtures)
            .then(function () {
                this.serverless.cli.log('Done loading.');
                return _this.evt;

            });
    }


    _validateParams() {

        const SError = this.serverless.classes.Error;

        let _this = this;

        if (!this.serverless.utils.dirExistsSync(this.fixturesPath)) {
            return Promise.reject(new SError('Could not find "fixtures" folder in your project root.'));
        }

        // validate stage: make sure stage exists
        let project = this.serverless.functions;
        this.serverless.cli.log('PROJECT VALIDATION');
        this.serverless.cli.log(util.inspect(project));
        if (!project.validateStageExists(_this.evt.options.stage)) {
            return Promise.reject(new SError('Stage ' + _this.evt.options.stage + ' does not exist in your project', SError.errorCodes.UNKNOWN));
        }

        // make sure region exists in stage
        if (!project.validateRegionExists(_this.evt.options.stage, _this.evt.options.region)) {
            return Promise.reject(new SError('Region "' + _this.evt.options.region + '" does not exist in stage "' + _this.evt.options.stage + '"'));
        }

        _this.project = project;
        _this.aws = this.serverless.getProvider('aws');
        _this.dynamodb = new AWS.DynamoDB.DocumentClient({region: _this.evt.options.region});

        return Promise.resolve();
    }

    _loadFixture(filename) {
        let _this = this;

        this.serverless.cli.log(`Loading fixtures/${filename}`);
        let inputData = fs.readFileSync('fixtures/' + filename, 'utf8');

        // TODO: I'm pretty sure there's a better way to render templates that actually use the full set of variables
        inputData = inputData.replace(/\$\{SERVERLESS_STAGE\}/g, this.evt.options.stage);
        inputData = inputData.replace(/\$\{SERVERLESS_PROJECT\}/g, this.project.name);
        inputData = inputData.replace(/\$\{SERVERLESS_REGION\}/g, this.evt.options.region);

        const importData = JSON.parse(inputData);
        const batchWrite = Promise.promisify(_this.dynamodb.batchWrite.bind(_this.dynamodb));

        let items = importData.entries.map(function (entry) {
            return {
                PutRequest: {
                    Item: entry
                }
            }
        });
        const request = {
            RequestItems: {}
        };
        // You can only batch-write 25 entries at a time.

        const batches = [];
        for (let i = 0; i < items.length; i += 25) {
            batches.push(items.slice(i, i + 25));
        }

        return Promise.each(batches, (batch) => {
            this.serverless.cli.log(`Writing ${batch.length} entries`);
            _this._spinner.start();
            request.RequestItems[importData.tableName] = batch;
            return batchWrite(request).then(() => _this._spinner.stop(true)
            )
        })
            ;
    }

    _loadFixtures() {

        let _this = this;

        this.serverless.cli.log('Loading fixtures to stage "' + _this.evt.options.stage + '" in region "' + _this.evt.options.region + '"...');

        _this._spinner = this.serverless.cli.spinner();

        let files = fs.readdirSync(this.fixturesPath);
        return Promise.each(files, this._loadFixture.bind(this));
    }

    _prompt() {

        let _this = this;

        return _this.cliPromptSelectStage('Choose Stage: ', _this.evt.options.stage, true)
            .then(stage => {
                _this.evt.options.stage = stage;
                Promise.resolve();
            }).then(function () {
                return _this.cliPromptSelectRegion('Choose Region: ', false, true, _this.evt.options.region, _this.evt.options.stage)
                    .then(region => {
                        _this.evt.options.region = region;
                        Promise.resolve();
                    })
                    ;
            });

    }
}

module.exports = DynamoDbFixtures;
