# ==============================
# ETAPA 1: COMPILAR REACT/VITE
# ==============================
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

ARG VITE_API_URL=http://localhost:8081

ENV VITE_API_URL=$VITE_API_URL

RUN npm run build


# ==============================
# ETAPA 2: SERVIR CON NGINX
# ==============================
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]