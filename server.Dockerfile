FROM python:3.8
WORKDIR /workspace/server

COPY server .

RUN pip install flask pyngrok        
RUN ngrok authtoken 2614b9U4eEQWOMrbA2pMAkjJegX_6tpeasKtu4xfxRAs2b1KD
EXPOSE 5000

CMD flask run & ngrok http 5000
