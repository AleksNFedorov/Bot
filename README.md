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

<img src="https://raw.githubusercontent.com/AleksNFedorov/Bot/docs/docs/img/use_case_diagramm.png" width="370" />

## Features 

* Set of predefined checks: Ping, HTTP, Trace
* Command line interface
* Configuration with XML config
* Modular design (just add jar with custom executors and/or reports to classpath)
* API for custom checks and reporting

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

