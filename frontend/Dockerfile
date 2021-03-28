FROM node:14-alpine as build-stage

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . ./

ARG _HOST
RUN printenv
RUN npm run export

FROM nginx:1.15



ARG STAGE_ENV

COPY --from=build-stage /app/__sapper__/export /usr/share/nginx/html
