FROM tiangolo/node-frontend:10 as build-stage

WORKDIR /app
COPY package*.json /app/
COPY . /app

ARG _HOST
RUN printenv
RUN npx sapper export

FROM nginx:1.15



ARG STAGE_ENV

COPY --from=build-stage /app/__sapper__/export /usr/share/nginx/html
