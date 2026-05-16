#!/bin/bash
source /etc/profile

jdk_path=${1}
prj_name='ngx'
env='prd'
if [ ! -n "${jdk_path}" ] ;then
  jdk_path='java'
fi

ID=`ps -ef|grep ${prj_name}.jar | grep java | awk '{print $2}'`
for id in $ID
do
  kill -9 $id
  echo "killed $id"
done
sleep 1

${jdk_path} -server -Xms300m -Xmx300m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=45 -XX:MaxGCPauseMillis=200 -Xlog:gc*,gc+heap=debug:file=gc.log:time,tags,level:filecount=5,filesize=100m -jar ${prj_name}.jar ${env} --server.restart='ngx.sh '${jdk_path} >/dev/null 2>&1&
sleep 1
echo "ok......"
