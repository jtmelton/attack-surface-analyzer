# attack-surface-analyzer
A tool for analyzing the attack surface of an application.

## Supported Frameworks
#### Java 
-JAX-RS
-Spring
#### Javascript 
-[Express](https://expressjs.com/)

## Building
Maven is required for building.
```bash
mvn clean install
```

## Arguments
Arg | Description | Required
------ | ------ | ------
sourceDirectory | Directory containing source code for analysis | true
outputFile | File containing output with discovered routes | true
exclusions | Comma delimited regex pattern for excluding files from analysis | false
parser-stderr | Enable stderr logging from parsers. Off by defaultr | false
properties | Properties file to load. Use enabling/disabling analyzers | false

## JSON report
The output JSON schema is as follows
```Javascript
{
  "routes": [
    {
      "path": "my/app/route",
      "fileName": "/path/to/associated/source/file",
      "method": "GET",
      "parameters": [
        {
          "dataType": "int",
          "name": "id",
          "category": "PathParam"
        }
      ]
    }
  ]
}
```

## Properties File
Analyzers can be enabled/disabled via a properties file. If no properties file is provided, all analyzers will be enabled and be triggers if there is a relevant source file type.

```Properties
visitor.java.jaxrs=true
visitor.java.spring=true
visitor.js.express=true
```

## Docker Support
After building the app you can build your container as such.

```bash
docker build -t <tag_of_your_choice> /path/to/attack-surface-analyzer
```

Your docker container will need at least one mount point for the directory containing your app. Here is an example.
```bash
docker run --read-only -v /source/path/to/app:/path/to/app/in/container -it <tag_built_with> -sourceDirectory /path/to/app/in/container -outputFile output.json -exclusions .*test.*
```
The --mount variant of mounting a volume can also be used if desired. If you want to write the output to a location outside of your container, then you will have to set a second mount point or re-use the existing one. If memory issues are encounterd, try running container with increased memory using the -m argument.
