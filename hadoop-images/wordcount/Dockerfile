FROM hadoop-base:latest

COPY WordCount.java WordCount.java

ADD run.sh /run.sh
RUN chmod a+x /run.sh

CMD ["/run.sh"]
