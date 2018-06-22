# Snow Owl

## Introduction
Snow Owl<sup>®</sup> is a terminology server and a collaborative terminology authoring platform.  The authoring platform maintains terminology artifacts developed by a team and supported by business workflows that are driven by external task management systems like Bugzilla and JIRA.  With its modular design, the server can maintain multiple terminologies where new terminologies can be plugged-in to the platform.  The functionality of Snow Owl is exposed via a REST API.

## Getting Started

These instructions will get Snow Owl up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy Snow Owl on a live system.

### Prerequisites

From [version 5.1.0](CHANGELOG.md#510), Snow Owl requires Java 8 update 102 or later.

Snow Owl is an Equinox-OSGi based server (using either Virgo or standalone OSGi). To develop plug-ins for Snow Owl you need to use Eclipse as IDE: 
* Use latest Eclipse IDE for Eclipse Committers package: http://www.eclipse.org/downloads/eclipse-packages/

Required Eclipse plug-ins (install the listed features):

*Note: you may have to untick the `Show only the latest versions of the available software` checkbox to get older versions of a feature. Please use the exact version specified below, not the latest point release.*

* Xtext/Xtend (http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/)
  * MWE 2 language SDK 2.9.0 (MWE)
  * Xtend IDE 2.11.0 (Xtext)
  * Xtext Complete SDK 2.11.0 (Xtext)
* Optional: Maven integration (http://download.eclipse.org/technology/m2e/releases) 
 
### Eclipse Preferences

Make sure you have the following preferences enabled/disabled.
* Plug-in development API baseline errors is set to Ignored (Preferences > Plug-in Development > API Baselines)
* The *Plugin execution not covered by lifecycle configuration: org.apache.maven.plugins:maven-clean-plugin:2.5:clean* type of errors can be ignored or changed to *Warnings* in *Preferences->Maven->Errors/Warnings*.
* Set the workspace encoding to *UTF-8* (Preferences->General->Workspace)
* Set the line endings to *Unix* style (Preferences->General->Workspace)

### Git configuration

* Make sure the Git line endings are set to *input* (Preferences->Team->Git->Configuration - add key if missing *core.autocrlf = input*)

### Target platform

1. Create a <a href='#prefetched-target-platform'>prefetched target platform</a> and copy the contents of the `target_platform_<version>` directory under `<eclipse_home>/target_platform`.
2. (Re)Open Eclipse and find the `com.b2international.snowowl.server.target.update` project
3. Open the file: `com.b2international.snowowl.server.local.target`
4. Click on `Set as Target platform`

### Run from development environment

1. Find `com.b2international.snowowl.server.update` project
2. Open `so_server.product` file, click on `Launch an Eclipse application` and terminate it
3. Open Run Configurations and find the launch config `so_server.product`
4. Open Arguments tab
 * Add `-Djetty.home.bundle=org.eclipse.jetty.osgi.boot` to the end of VM arguments
 * Problems may be encountered when running classiciations.  If so, check the VM arguments against the following list which should work.  Important settings are "headless" and "server", plus the memory options (Xmx):  -Djava.awt.headless=true -Dosgi.requiredJavaVersion=1.7 -XX:MaxPermSize=512m -Xms5g -Xmx10g -server -Declipse.ignoreApp=true -Dosgi.noShutdown=true -Dorg.osgi.service.http.port=8080 -XX:+AlwaysLockClassLoader -Dosgi.classloader.type=nonparallel -XstartOnFirstThread -Dorg.eclipse.swt.internal.carbon.smallFonts -Djetty.home.bundle=org.eclipse.jetty.osgi.boot
5. Open Plug-ins tab
 1. Add `org.eclipse.jetty.osgi.boot` bundle (set Auto-Start to `true`, and Start Level to `5`)
 2. Click on Add required bundles
 3. Remove `org.eclipse.equinox.http.jetty`, `org.eclipse.jetty.annotations`, all `*jsp*` and `*jasper*` bundles
 4. Set the API bundles to start automatically (`com.b2international.snowowl.api.rest`, `com.b2international.snowowl.snomed.api.rest`) by setting Auto-Start to `true`, and Start Level to `5`.
 5. Add the `com.sun.el` bundle and remove the `org.apache.el` bundle if necessary
6. Run and point your browser to `http://localhost:8080/snowowl/snomed-ct/v2/`
7. By default Snow Owl will use an empty embedded `H2` database. To use `MySQL`, you have to configure the database in the `snowowl_config.yml` configuration file (or copy an existing `MySQL` or `H2` configuration file located in the `releng/com.b2international.snowowl.server.update/assembly/mysql or h2` directory to `<eclipse_home>/target_platform`). This also configures Snow Owl authentication.

## Build

Snow Owl uses Maven for its build system.

In order to create a distribution, simply run the `mvn clean package -Pdependencies -Psite -Pdist -Dmaven.repository.id=ihtsdo-public-nexus -Dnexus.releases.url=https://maven.ihtsdotools.org/content/repositories/releases/ -Dnexus.snapshots.url=https://maven.ihtsdotools.org/content/repositories/snapshots/` command in the cloned directory.

To run the test cases, simply run:

    mvn clean verify -Pdependencies -Psite -Pdist 

The distribution package can be found in the `releng/distribution/target` folder, when the build completes.

Note that Maven 3.1.1 should be used.  Using later versions of Maven may result in errors like:  tycho-source-plugin:0.21.0:plugin-source failed: invalid version "4.5.13.2016-03-04T15:40:17Z": invalid qualifier "2016-03-04T15:40:17Z"

## Release

It is not possible to use JGitFlow to perform a release from the develop to master branch because although it will try to update the pom files, it is not set up to set the same versions in the various MANIFEST.MF files, and so a manual merge and version update must be performed.

Is it important when an update to version numbers is done in the master branch, that this is merged back to the develop branch (and then the versions updated further to develop versions) so that future merges from develop to master can be performed without conflicts.

To modify versions, use Tycho:
```
mvn -Pdependencies -Dtycho.mode=maven \
org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=4.2.2-SNAPSHOT
```

In addition, the package org.eclipse.equinox.bundles may need updating since it will try to deploy with the rest of the code, and if the release version is not updated, Nexus will reject it as it rejects all attempted overwrites.

### Additional Build Improvements

Here are few tips to improve the quality of the default build process.

#### Nexus

We highly recommend to install a local artifact repository (`Nexus OSS` is supported), so the build can deploy and reuse (in downstream projects) `Maven` and `p2` artifacts.

1. Download and install Nexus OSS or Professional (http://www.sonatype.org/nexus/go/).
2. Install `Nexus Unzip Plugin` to easily reference p2 repositories deployed as zip: https://wiki.eclipse.org/Tycho/Nexus_Unzip_Plugin
3. Define the `nexus.url` parameter in the `settings.xml` file under `.m2` folder on your build server (use the `settings.xml` in the root of this repository as template).
4. Define a deployment user in Nexus, and reference it in the `.m2/settings.xml` file.
5. Use `mvn clean deploy` instead of `mvn clean verify` when you execute the process.
6. *Optional: deploy only if build succeeds (requires a `Jenkins CI` job with post build step to deploy artifacts to `Nexus`*

#### Prefetched target platform

The `-Pdependencies` profile includes all required third party repositories and modules as part of the build process using Tycho's p2 and Maven dependency resolution capabilities. 
While this should be enough to run the process, in production builds we recommend using a prefetched target platform, as it will ensure consistent third party versions and reduces the execution time significantly.

1. Create the target platform update site, run `mvn clean verify -Pdependencies -Ptarget_site  -Dmaven.repository.id=ihtsdo-public-nexus -Dnexus.releases.url=https://maven.ihtsdotools.org/content/repositories/releases/ -Dnexus.snapshots.url=https://maven.ihtsdotools.org/content/repositories/snapshots/` from the **releng** folder
2. Navigate to `com.b2international.snowowl.server.target.update/target` folder
3. Copy the `target_platform_<version>` folder to a webserver, or use `Nexus` to serve the site as unzipped p2 (requires Nexus OSS with Unzip Plugin installed, see previous section)
4. Define an `http` URL as `target.platform.url` parameter in the global Maven `.m2/settings.xml` file
5. Run Snow Owl maven process with `mvn clean verify -Ptp_dependencies -Psite -Pdist` (*NOTE: the tp_dependencies profile will use the prefetched local p2 repository instead of querying all remote sites*)
6. The target_platform directory is equivalent to the runtime directory in a server installation, and so an existing Snow-Owl repository can be accessed by symlinking the target_platform/resources/indexes to an existing /opt/termserver/resources/indexes folder.   You may also need to run a mysql "source" command on a snomedStore.sql file if that data is not already available locally.
7. Copy one of these two files (for H2 or MySQL support) into .../target_platform/.  Note these files also contain the username and password that will be required when you access some of the APIs
8. IHTSDO use SnoRocket as a classifier.   To include this in the Eclipse environment,  add it to the target_platform/plugins directory, and reload the target platform with the Set as Target platform action and add the classifier JAR to the launch configuration. You can verify that the JAR picked up by the runtime via the 'ontology list' command in the console.   The Jar can be copied from a production environment eg /opt/termserver/repository/usr/snorocket-protege-plugin-2.5.5.jar
```
./releng/com.b2international.snowowl.server.update/assembly/h2/snowowl_config.yml
./releng/com.b2international.snowowl.server.update/assembly/mysql/snowowl_config.yml
```

## Deployment

Please refer to the [installation guide](documentation/src/main/asciidoc/installation_guide.adoc). Prerequisites are detailed in the [software requirements](documentation/src/main/asciidoc/software_requirements.adoc) document. Then see the [configuration guide](documentation/src/main/asciidoc/configuration_guide.adoc) to configure for your particular installation.

The [SNOMED CT extension management guide](documentation/src/main/asciidoc/snomed_extension_management.adoc) provides details for managing multiple SNOMED extensions on a single terminology server.

## Administration

See the [administration guide](documentation/src/main/asciidoc/administration_guide.adoc). You may find the quick references for [console commands](documentation/src/main/asciidoc/administrative_console_reference.adoc) and the [administration REST API](documentation/src/main/asciidoc/administrative_rest_reference.adoc) helpful.

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

This project is licensed under the Apache 2.0 License. See [LICENSE](LICENSE) for details and refer to [NOTICE](NOTICE) for additional licencing notes and uses of third-party components.

## Acknowledgements

In March 2015, [SNOMED International](http://snomed.org) licensed the Snow Owl Terminology Server components supporting SNOMED CT from [B2I Healthcare](https://b2i.sg/). They subsequently made the licensed code available to their [members](http://www.snomed.org/members) and the global community under an open-source license.
