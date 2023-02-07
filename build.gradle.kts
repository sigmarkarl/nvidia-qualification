plugins {
    id("java")
    id("application")
    id("com.google.cloud.tools.jib") version "3.3.1"
}

group = "com.netapp.wave"
version = "1.0"

jib {
    from {
        image = "ghcr.io/graalvm/jdk:22.3.1"
        if (project.hasProperty("REGISTRY_USER")) {
            auth {
                username = project.findProperty("REGISTRY_USER")?.toString()
                password = project.findProperty("REGISTRY_PASSWORD")?.toString()
            }
        }
    }
    to {
        image = project.findProperty("APPLICATION_REPOSITORY")?.toString() ?: "netapp/wave/rapids/qualification:1.0"
        //tags = [project.findProperty("APPLICATION_TAG")?.toString() ?: "1.0"]
        if (project.hasProperty("REGISTRY_USER")) {
            auth {
                username = project.findProperty("REGISTRY_USER")?.toString()
                password = project.findProperty("REGISTRY_PASSWORD")?.toString()
            }
        }
    }
    container {
        mainClass = "com.nvidia.spark.rapids.tool.qualification.QualificationMain"
    }
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.nvidia.spark.rapids.tool.qualification.QualificationMain")
}

dependencies {
    implementation("com.fasterxml.jackson:jackson-bom:2.14.1")
    implementation("org.apache.spark:spark-core_2.12:3.3.1")
    implementation("org.apache.spark:spark-sql_2.12:3.3.1")
    implementation("org.apache.spark:spark-hadoop-cloud_2.12:3.3.1")
    implementation("com.nvidia:rapids-4-spark-tools_2.12:22.12.0")
}