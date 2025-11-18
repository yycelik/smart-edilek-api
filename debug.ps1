# Set environment variables
$env:MVN_RREGISTRY_GROUP = "https://nexus.s3t.co/repository/maven-public/"
$env:MVN_RREGISTRY_SNAPSHOOT = "https://nexus.s3t.co/repository/maven-snapshots/"
$env:MVN_REGISTRY_RELEASE = "https://nexus.s3t.co/repository/maven-releases/"

$env:DOCKER_REGISTRY_GROUP = "docker-g.nexus.s3t.co"
$env:DOCKER_REGISTRY_PROXY = "docker-p.nexus.s3t.co"
$env:DOCKER_REGISTRY_SNAPSHOOT = "docker-s.nexus.s3t.co"
$env:DOCKER_REGISTRY_RELEASE = "docker-r.nexus.s3t.co"
$env:MVN_REGISTRY_GROUP = "https://nexus.s3t.co/repository/maven-public/"
$env:REGISTRY_USERNAME = "admin"
$env:REGISTRY_PASSWORD = "app321."
$env:DATASOURCE_URL = "mysql.s3t.co:3306"
$env:DATASOURCE_USERNAME = "root"
$env:DATASOURCE_PASSWORD = "app321."
$env:CERTIFICATE_PASSWORD = "159753"

# Keycloak configuration
$env:KEYCLOAK_AUTH_SERVER_URL = "https://keycloak.s3t.co"
$env:KEYCLOAK_REALM = "dilekce"
$env:KEYCLOAK_CLIENT_ID = "dev-api.edilek.com"

$Env:KUBECONFIG="$Env:KUBECONFIG;$HOME\.kube\config"

# Run Maven command
mvn install -DskipTests #--batch-mode
if (-not $?) {
    exit 1 
}

# login docker
docker login $env:DOCKER_REGISTRY_SNAPSHOOT -u $env:REGISTRY_USERNAME -p $env:REGISTRY_PASSWORD
if (-not $?) {
    exit 1
}

# build skaffold command
skaffold build #--no-prune=false --cache-artifacts=false
if (-not $?) {
    exit 1
}

# debug application
skaffold debug --no-prune=false --cache-artifacts=false --namespace=e-dilek-dev
