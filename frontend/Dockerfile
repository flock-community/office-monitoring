FROM tiangolo/node-frontend:10 as build-stage

WORKDIR /app
COPY package*.json /app/
RUN npm ci
COPY . /app
RUN npm run build

FROM nginx:1.15

ARG STAGE_ENV

COPY --from=build-stage /app/dist /usr/share/nginx/html
