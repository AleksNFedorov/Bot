# Bot

Bot is a small application to help keep an eye over network resources using variety predefined checks and same time be extensible for custom needs. 

In a broad context app might be used as a scheduler for any kind of tasks

## Project goals
1. Provide easy way to check network resource availability with different checks
  * Ping
  * Http
  * Trace
2. Make it runnable on major OS: Windows, Mac OS, *nix
3. Easy customizable and manageable

### Use case

(clickable)
<img src="https://raw.githubusercontent.com/AleksNFedorov/Bot/docs/docs/img/use_case_diagramm.png" width="370" />

## Features 

* Set of predefined checks: Ping, HTTP, Trace
* Command line interface
* Configuration with XML config
* Modular design (just add jar with custom executors and/or reports to classpath)
* API for custom checks and reporting

## Getting started

### Create config file

```xml
<config>
    <!-- Task group config -->
    <group id="GitHub.com">
        <!--
            'gitHubPing' task, runs every 10 seconds with PING executor,
            execution deadline - 3 secs
        -->
        <task id="gitHubPing" executor="PING">
            <run>10</run>
            <deadline>3</deadline>
    </task>
        <!--
            'gitHubTrace' task, runs every 30 seconds with TRACE executor,
            execution deadline - 10 secs
        -->
        <task id="gitHubTrace" executor="TRACE">
            <run>30</run>
            <deadline>10</deadline>
        </task>
    </group>
    <!--
        Un grouped 'Java.com' task, runs every 2 seconds with CUSTOM_EX executor,
        execution deadline - 1 secs
        Contains extra parameters for executor in executorConfig node
    -->
    <task id="Java.com" executor="CUSTOM_EX">
        <run>2</run>
        <deadline>1</deadline>
        <executorConfig>
            <property key="key">value</property>
        </executorConfig>
    </task>
   <!--
       Un grouped 'runOnce' task
       Runs only once with CUSTOM_ONE_RUN executor
   -->
   <task id="runOnce" executor="CUSTOM_ONE_RUN">
       <run>-1</run>
   </task>    
</config>
```

### Build project

#### Build core module (Step 1)

```cmd
#Build common module
cd <project_path>/common
mvn install

#Build worker module
cd <project_path>/worker
mvn install
```

#### Build moudules with checkes (Step 2)

`Not implemented yet`

#### Build custom modules `if any` (Step 3)

Build modules with custom checks and reports, look into [sample](https://github.com/AleksNFedorov/Bot/tree/master/sample) project for more info

1. To add custom check
 * Implement [TaskExecutor](https://github.com/AleksNFedorov/Bot/blob/master/common/src/main/java/com/bot/common/TaskExecutor.java)
 * Annotate it with [AutoService](https://github.com/google/auto/tree/master/service)
2. To add custom report
 * Implement [TaskResultProcessor](https://github.com/AleksNFedorov/Bot/blob/master/common/src/main/java/com/bot/common/TaskResultProcessor.java)
 * Annotate it with [AutoService](https://github.com/google/auto/tree/master/service)

## Run project

###Windows
``` cmd
java -cp "common.jar;worker.jar;checks.jar;custom.jar" com.bot.worker.BotStarter --task-config-file=path_to_config_file
```
###Unix 
``` cmd
java -cp "common.jar:worker.jar:checks.jar:custom.jar" com.bot.worker.BotStarter --task-config-file=path_to_config_file
```

###Where 

* worker.jar and common.jar - artifacts from Step 1
* checks.jar - artifact from Step 2
* checks.jar - artifact from Step 3
* path_to_config_file - path to tasks XML config

### Important

``` cmd
By default logs are written to app home folder under ‘log’. 
Console is used for command line interface. Type --help to get command list.
```

<img src="https://raw.githubusercontent.com/AleksNFedorov/Bot/docs/docs/img/consoleHelp.png" width="370" />


## More info

- [Design details](https://github.com/AleksNFedorov/Bot/wiki)
- [Java Doc](https://aleksnfedorov.github.io/javadoc/)


## Beta notice

Bot is still under development. Core functionality already implemented and not significant code change expecting, however some minor changes are likely. Stay tuned.

## License


    Copyright (C) 2017 Aleks Fedorov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

