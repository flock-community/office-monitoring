import sirv from "sirv";
import polka from "polka";
import compression from "compression";
import express from "express";
import * as sapper from "@sapper/server";

const { PORT, NODE_ENV } = process.env;
const dev = NODE_ENV === "development";

express() // You can also use Express
  .use(
    compression({ threshold: 0 }),
    sirv("static", { dev }),
    sapper.middleware()
  )
  .listen(PORT, () => {
    //if (err) console.log("error", err);
  });
