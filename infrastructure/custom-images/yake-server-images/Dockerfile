FROM library/python:3.7

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ="Asia/Tehran" \
    YAKE_REPO_ADDRESS="https://github.com/LIAAD/yake.git" \
    YAKE_REPO_CONTENT_BASE_URL="https://raw.githubusercontent.com/LIAAD/yake/master" \
    YAKE_REST_API_PORT=5000

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    bash \
    git \
    tzdata \
    && rm -rf /var/lib/apt/lists/*

RUN wget "${YAKE_REPO_CONTENT_BASE_URL}/requirements.txt" -O /tmp/requirements.txt \
    && pip install --no-cache-dir -r /tmp/requirements.txt \
    && rm /tmp/requirements.txt

RUN pip --no-cache-dir install flasgger \
    && pip --no-cache-dir install git+${YAKE_REPO_ADDRESS}

COPY yake-rest-api.py /opt/yake-rest-api.py

EXPOSE "${YAKE_REST_API_PORT}"

CMD [ "python", "/opt/yake-rest-api.py" ]