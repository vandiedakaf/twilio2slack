#!/usr/bin/env bash
aws lambda create-function --function-name blambda --runtime java8 --role arn:aws:iam::522161570381:role/dev-lambda-LambdaExecutionRole-17S53XUZ6GVT --handler ForwardSms::processSms --code S3Bucket=vdda-sandbox,S3Key=lambda/twilio2slack-1.0-SNAPSHOT.zip
