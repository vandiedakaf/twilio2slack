# [twilio2slack](https://vandiedakaf.github.io/)

![alt tag](https://travis-ci.org/vandiedakaf/twilio2slack.svg?branch=master)

twilio2slack is a pet project and experiment in serverless solutions. This project makes use of Twilio, AWS API Gateway, AWS Lambda and Slack with the goal to forward any SMSes sent to a specific Twilio number to a Slack channel.

This readme only contains setup instructions; for some high level ramblings see the blog post at https://vandiedakaf.github.io/.

## Requirements
You need accounts for the following services:
- Twilio (they have a free trial account)
- Slack (with appropriate permissions that allow you to get a webhook for a channel)
- AWS
- Travis-CI

## Initial Setup
1. Create a S3 bucket for configuration (replace the variable CONFIG_BUCKET in the lambda code with the bucket's name, er, this should be replaced with something more elegant).
1. Create a `config.properties` file (with only one variable `slack.web_hook=https://hooks.slack.com/services/***`) and place it in the configuration bucket.
1. Create a S3 bucket for lambda function code uploads (the name of this bucket will be your S3_BUCKET environment variable).
1. Set a local environment variable S3_BUCKET with the value set to your code upload bucket name.
1. Build the project with `gradle build`.
1. Copy the zip file from the distributions folder to your S3_BUCKET (this is so that the initial lambda function creation has its required code).
1. Run `gradle createStack` (you can change the variables in the file create_stack.sh to fit your needs; the defaults should suffice though).
1. Link your project in Travis-CI and set the environment variables AWS_ACCESS_KEY_ID, AWS_DEFAULT_REGION, AWS_SECRET_ACCESS_KEY & S3_BUCKET.
1. Wait for the stack creation to be completed (check the status in the AWS console).
1. In Twilio, configure your [Phone Number](https://www.twilio.com/console/phone-numbers/incoming). Configure it so that incoming messages trigger a HTTP GET WebHook to the published API Gateway endpoint, e.g. `https://***.execute-api.eu-west-1.amazonaws.com/stage/sms`.
1. Push your code.

Subsequent pushes will automatically update the Lambda Function during the Travis-CI build.
