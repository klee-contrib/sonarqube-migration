# Sonarqube migration app

## Purpose

This program migrate "won't fix" and "false positive" issues from a sonarqube project to another one.
This is for example useful in case of a server migration on a different sonarqube version.

## Getting Started

Start the program (a java 17 jvm is needed) :

```
java -jar sonarqube-migration-1.0.0.jar
```

Avalable commands can be displayed with the "help" command.

Most useful ones are :
* configure: Configure the origin and destination server/project
* prepare migration: Prepare the migration (find issues we want to migrate from the origin)
* dry run migrate: Show what will be the result of the migration.
* migrate: Do the migration

Do them in order for a successful migration! 

### configure 

A prompt will ask you sequentially for informations about the origin server, then the destination server.
- host
- project id 
- authentication mode and token (bearer or jwt)

Configuration is then saved in data/configuration.json in order to be potentially re-used (in case you close the program at some point).

### prepare migration 

Will scan the origin server and print informations about the first step of migration.

![prepare migration output](doc/screenshot_3.png?raw=true)

Complete list of issues we will want to migrate is available in data/issues.json.

### dry run migrate

Scan the destination server and tells you what is the expected result of the migration.

![dry run migrate output](doc/screenshot_4.png?raw=true)

Complete list of transferable and non transferable issues are available in data/transferable-issues.json and data/non-transferable-issues.json.

### migrate

Execute the migration and print some informations.

![migrate output](doc/screenshot_5.png?raw=true)

## Extra notes

### Authentication

I would recommand to choose the jwt cookie authentication method when ask in the configuration.
I have experienced bugs in the sonarqube search api when using tokens (for exemple when we want to filter on a project id).

![JWT setting](doc/screenshot_1.png?raw=true)

JWT can be found in the cookie section of your browser "network" console - for any request to sonarqube.
The whole red section should be copied ("=" and ";" excluded").

![Where to find jwt section](doc/screenshot_2.png?raw=true)

### Comments migration

The original comment creator name is added to the text of the comment since there is no good way to keep a link with the real comment creator account.

See : 
![comment migration info](doc/screenshot_6.png?raw=true)

### A bit more on migration 

Rules keys, names and descriptions can change between versions.

This program handles :
- rules with the same key
- rules with the same name
- keys renameded from "squid:S" to "java:S"
- And a list of rules where key and name is different but a the same. In order to complete this list, please create an issue with both rules keys.