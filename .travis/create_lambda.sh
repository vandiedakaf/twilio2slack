#!/usr/bin/env bash
aws lambda create-function --region eu-west-1 --function-name blambda --runtime java8 --role arn:aws:iam::522161570381:role/dev-lambda-LambdaExecutionRole-17S53XUZ6GVT --handler FrowardSms::processSms --code S3Bucket=vdda-sandbox,S3Key=twilio2slack-1.0-SNAPSHOT.zip
