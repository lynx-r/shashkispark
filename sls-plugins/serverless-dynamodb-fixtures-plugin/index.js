'use strict';

const path = require('path'),
    Promise = require('bluebird'),
    fs = require('fs'),
    _ = require('lodash'),
    AWS = require('aws-sdk');

class ServerlessPlugin {
    constructor(serverless, options) {
        this.options = options;
        this.serverless = serverless;
        this.service = serverless.service;
        this.config = this.service.custom && this.service.custom.dynamodb || {};

        this.serverlessLog = serverless.cli.log.bind(serverless.cli);
        this.provider = this.serverless.getProvider('aws');
        this.fixturesPath = path.join(this.serverless.config.servicePath, 'fixtures');
        this.name = 'serverless-dynamodb-plugin-fixtures';

        this.commands = {
            dynamodb: {
                commands: {
                    load: {
                        usage: 'Load your dynamodb fixtures',
                        lifecycleEvents: ['load']
                    }
                }
            },
        };

        this.hooks = {
            'dynamodb:load:load': this.loadFixture.bind(this),
        };
    }


    loadFixture() {
        let _this = this;

        // Flow
        return _this._validateParams()
            .bind(_this)
            .then(_this._loadFixtures)
            .then(function () {
                this.serverlessLog('Done loading.');
            });
    }


    _validateParams() {
        let _this = this;

        const SError = this.serverless.classes.Error;

        if (!this.serverless.utils.dirExistsSync(this.fixturesPath)) {
            return Promise.reject(new SError('Could not find "fixtures" folder in your project root.'));
        }

        this.dynamodb = this._dynamodbOptions(this.options).doc;

        return Promise.resolve();
    }

    _loadFixture(filename) {
        let _this = this;

        this.serverlessLog(`Loading fixtures/${filename}`);
        let inputData = fs.readFileSync('fixtures/' + filename, 'utf8');

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
            _this.serverlessLog(`Writing ${batch.length} entries`);
            request.RequestItems[importData.tableName] = batch;
            return batchWrite(request);
        });
    }

    _loadFixtures() {
        let _this = this;

        this.serverlessLog('Loading fixtures to stage "' + _this.provider.getStage() + '" in region "' + _this.provider.getRegion() + '"...');

        let files = fs.readdirSync(this.fixturesPath);
        return Promise.each(files, this._loadFixture.bind(this));
    }

    get port() {
        const config = this.config;
        const port = _.get(config, "start.port", 8000);
        return port;
    }

    get host() {
        const config = this.config;
        const host = _.get(config, "start.host", "localhost");
        return host;
    }

    _dynamodbOptions(options) {
        let dynamoOptions = {};

        if(options && options.online){
            this.serverlessLog("Connecting to online tables...");
            if (!options.region) {
                throw new Error("please specify the region");
            }
            dynamoOptions = {
                region: options.region,
            };
        } else {
            dynamoOptions = {
                endpoint: `http://${this.host}:${this.port}`,
                region: "localhost",
                accessKeyId: "MOCK_ACCESS_KEY_ID",
                secretAccessKey: "MOCK_SECRET_ACCESS_KEY"
            };
        }

        return {
            doc: new AWS.DynamoDB.DocumentClient(dynamoOptions)
        };
    }
}

module.exports = ServerlessPlugin;
