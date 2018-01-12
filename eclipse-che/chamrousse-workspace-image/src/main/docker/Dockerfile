FROM eclipse/ubuntu_jdk8

#ENV DEBIAN_FRONTEND noninteractive

RUN mkdir /home/user/ls-chamrousse
ADD launch.sh /home/user/ls-chamrousse/launch.sh
ADD lsp.jar /home/user/ls-chamrousse/launch.jar
RUN sudo chmod 755 /home/user/ls-chamrousse/launch.sh && \
    sudo chgrp -R 0 /home/user/ls-chamrousse && \
    sudo chmod -R g+rwX /home/user/ls-chamrousse
