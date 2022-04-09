
# App Engine Java Courteline site.

Copyright (C)2022


[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Prerequisites

### Download Maven

These samples use the [Apache Maven][maven] build system. Before getting
started, be sure to [download][maven-download] and [install][maven-install] it.
When you use Maven as described here, it will automatically download the needed
client libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Create a Project in the Google Cloud Platform Console

If you haven't already created a project, create one now. Projects enable you to
manage all Google Cloud Platform resources for your app, including deployment,
access control, billing, and services.

1. Open the [Cloud Platform Console][cloud-console].
1. In the drop-down menu at the top, select **Create a project**.
1. Give your project a name.
1. Make a note of the project ID, which might be different from the project
   name. The project ID is used in commands and in configurations.

[cloud-console]: https://console.cloud.google.com/


### Setup

Use either:

* `gcloud init`
* `gcloud auth application-default login`

## Maven
### Running locally

```shell
    mvn appengine:run
```

### Deploying

```shell
    mvn clean package appengine:deploy  -Ddeploy.projectId=courteline
```
