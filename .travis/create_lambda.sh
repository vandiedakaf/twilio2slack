#!/usr/bin/env bash
aws lambda create-function --function-name blambda --runtime java8 --role dev-lambda-LambdaExecutionRole-17S53XUZ6GVT --handler FrowardSms::processSms --code S3Bucket=vdda-sandbox,S3Key=twillio2slack-1.0-SNAPSHOT.zip
