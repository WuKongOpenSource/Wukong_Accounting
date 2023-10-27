#!/bin/bash

COMMAND="$1"

if [[ "$COMMAND" != "start" ]] && [[ "$COMMAND" != "stop" ]] && [[ "$COMMAND" != "restart" ]]; then
	echo "Usage: $0 start | stop | restart"
	exit 0
fi

APP_BASE_PATH=$(cd `dirname $0`; pwd)

function start()
{
    # -Xms分配堆最小内存，默认为物理内存的1/64；-Xmx分配最大内存，默认为物理内存的1/4 如果程序会崩溃请将此值调高
    nohup java -jar ${JAVA_OPTS} ${project.artifactId}-${project.version}.jar >> /dev/null 2>&1 &
    echo "--------项目启动成功--------"
    echo "--------欢迎使用悟空CRM ^_^--------"
}

function stop()
{
    P_ID=`ps -ef | grep -w ${project.artifactId}-${project.version}.jar | grep -v "grep" | awk '{print $2}'`
    kill $P_ID
    echo "项目已关闭"
}

function restart()
{
    P_ID=`ps -ef | grep -w ${project.artifactId}-${project.version}.jar | grep -v "grep" | awk '{print $2}'`
    start
    sleep 25s
    kill $P_ID
    echo "项目重启成功"
}

if [[ "$COMMAND" == "start" ]]; then
	start
elif [[ "$COMMAND" == "stop" ]]; then
    stop
else
    restart
fi
