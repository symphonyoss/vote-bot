#!/bin/bash



# Check that the environment variable has been set correctly
if [ -z "$BUCKET_NAME" ]; then
  echo >&2 'error: missing BUCKET_NAME environment variable'
  exit 1
fi

# Load the S3 secrets file contents into the environment variables
eval $(aws s3 cp s3://${BUCKET_NAME}/env/env.txt - | sed 's/^/export /')

aws s3 cp s3://${BUCKET_NAME}/conf/ /opt/conf --recursive
aws s3 cp s3://${BUCKET_NAME}/certs/ /opt/certs --recursive

##Copy host overrides
IFS=',' read -r -a array <<< "${ADD_TO_HOSTS}"

for i in "${!array[@]}"
do
    echo "Adding Local Host=>${array[i]}"
    echo ${array[i]} >> /etc/hosts
done


"$@"
