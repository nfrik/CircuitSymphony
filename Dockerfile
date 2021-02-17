FROM openjdk:8-jre
MAINTAINER Nikolay Frick <nifrick@freescale.com>

ENTRYPOINT ["/usr/local/openjdk-8/bin/java","-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap","-XX:+PrintFlagsFinal","-XX:+CrashOnOutOfMemoryError","-Xmx64G","-Xms64G", "-jar", "/usr/share/circuitsymphony/circuitsymphony.jar"]


RUN apt-get update \
    && apt-get install --no-install-recommends -y \
#        openjfx \
        unzip \
        build-essential \
        lsb-core \
        libgfortran3 \
    && rm -f /var/lib/apt/lists/*_dists_*

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
# ADD target/lib           /usr/share/myservice/lib
ADD setuplist.txt /usr/share/circuitsymphony/
ADD /circuits /usr/share/circuitsymphony/circuits

# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/circuitsymphony/circuitsymphony.jar

