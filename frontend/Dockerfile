FROM node:14-alpine as builder

# install dependencies
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . ./

ARG _HOST
RUN echo "_HOST=${_HOST}">> .env
RUN npm run build

FROM node:14-alpine as runner

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY static ./static
COPY --from=builder /app/__sapper__ ./__sapper__

EXPOSE 3000
ENV HOST=0.0.0.0
CMD [ "npm", "start" ]