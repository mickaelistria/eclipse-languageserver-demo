FROM eclipse/stack-base:ubuntu

ENV DEBIAN_FRONTEND noninteractive

ADD launch.sh /home/user/che/ls-alpesjug/launch.sh
ADD lsp.jar /home/user/che/ls-alpesjug/launch.jar
RUN sudo chmod 755 /home/user/che/ls-alpesjug/launch.sh
RUN sudo chown -R user.user /home/user/che
