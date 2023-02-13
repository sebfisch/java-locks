FROM eclipse-temurin:17
CMD bash --login

RUN apt-get update && \
  apt-get install -y \
  gawk \
  git

ARG USER=me
ARG GROUP=me
ARG HOME=/home/${USER}
ARG UID=1000
ARG GID=1000

RUN apt-get install -y sudo && \
  addgroup --gid ${GID} ${GROUP} && \
  adduser --disabled-password --gecos '' --uid ${UID} --gid ${GID} ${USER} && \
  (echo "$USER ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers)

ARG LC=de_DE.UTF-8
ENV LANG=${LC}
ENV LC_ALL=${LC}

RUN apt-get install -y locales && \
  locale-gen ${LC} && \
  update-locale LANG=${LC}

ARG TZ=Europe/Berlin

RUN ln -fs /usr/share/zoneinfo/${TZ} /etc/localtime && \
  (DEBIAN_FRONTEND=noninteractive apt-get install -y tzdata) && \
  echo ${TZ} > /etc/timezone && \
  dpkg-reconfigure --frontend noninteractive tzdata
