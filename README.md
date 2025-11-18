# features
Authentication and authorization
using JWT

# postman
https://www.postman.com/material-saganist-66970311/workspace/smart 

# debug requirements
install requirements
```
choco install docker-desktop

choco install kubernetes-cli 

kubectl version --client

choco install skaffold

skaffold version
```


# debug
connect k8s master and get ".config" file using below code.
```
kubectl config view --flatten=true
```

create ".kube" folder under your home directory on your local machine and copy ".config" file under ".kube"
```
mkdir %USERPROFILE%/.kube
```

set mvn "setting.xml" path to the skaffold.yaml
```
build:
  artifacts:
    - image: 
      jib:
        args:
          - --settings=..\..\settings.xml
          - -DskipTests=true
```

set environment variables to the "debug.ps1"

run "debug.ps1"
```
.\debug.ps1
```



# debug vscode
to enable remote debug .vscode/launch.json
```
{
    "configurations": [
        {
            "type": "java",
            "name": "debug",
            "request": "attach",
            "hostName": "debug.s3t.co",
            "port": 5007,
            "projectName": "edilek"
        }
    ]
}
```