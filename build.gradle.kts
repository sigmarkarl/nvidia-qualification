plugins {
    id("java")
    id("application")
    id("com.google.cloud.tools.jib") version "3.3.1"
}

group = "com.netapp.wave"
version = "1.0"

jib {
    from {
        image = "public.ecr.aws/l8m2k1n1/netapp/spark:graalvm-22.3.1"
        if (project.hasProperty("REGISTRY_USER")) {
            auth {
                username = project.findProperty("REGISTRY_USER")?.toString()
                password = project.findProperty("REGISTRY_PASSWORD")?.toString()
            }
        }
    }
    to {
        image = project.findProperty("APPLICATION_REPOSITORY")?.toString() ?: "public.ecr.aws/l8m2k1n1/netapp/nvidia-qualification:1.1"
        //"ghcr.io/sigmarkarl/nvidia-qualification:1.1"
        //tags = [project.findProperty("APPLICATION_TAG")?.toString() ?: "1.0"]
        if (project.hasProperty("REGISTRY_USER")) {
            var reg_user = project.findProperty("REGISTRY_USER")?.toString()
            var reg_pass = project.findProperty("REGISTRY_PASSWORD")?.toString()
            auth {
                username = reg_user
                password = reg_pass
            }
        }
    }
    containerizingMode = "packaged"
    container {
        user = "app"
        entrypoint = listOf("/opt/entrypoint.sh")
        workingDirectory = "/opt/spark/work-dir/"
        appRoot = "/opt/spark/"
        //mainClass = "org.simmi.SparkFlightConnect"
        environment = mapOf("JAVA_TOOL_OPTIONS" to "-Djdk.lang.processReaperUseDefaultStackSize=true --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED",
            "SPARK_EXTRA_CLASSPATH" to "/opt/spark/classes/*")
    }
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.netapp.wave.rapids.QualificationTest")
    applicationDefaultJvmArgs = listOf("--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/sun.security.action=ALL-UNNAMED")
}

dependencies {
    implementation("com.fasterxml.jackson:jackson-bom:2.14.2")
    implementation("org.apache.spark:spark-core_2.12:3.3.2")
    implementation("org.apache.spark:spark-sql_2.12:3.3.2")
    implementation("org.apache.spark:spark-kubernetes_2.12:3.3.2")
    implementation("org.apache.spark:spark-hadoop-cloud_2.12:3.3.2")
    implementation("org.apache.hadoop:hadoop-aws:3.3.4")
    implementation("joda-time:joda-time:2.12.2")
    implementation("com.nvidia:rapids-4-spark-tools_2.12:22.12.0")
    implementation("com.amazonaws:aws-java-sdk:1.12.408")
}