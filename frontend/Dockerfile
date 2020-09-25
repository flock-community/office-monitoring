FROM tiangolo/node-frontend:10 as build-stage

WORKDIR /app
COPY . /app
RUN npm install && npm run build

FROM nginx:1.15

ARG STAGE_ENV

COPY --from=build-stage /app/dist /usr/share/nginx/html
