// Servidor estático mínimo (sin dependencias) para mostrar el frontend en local.
// Uso:  node frontend/servidor.js   ->  http://localhost:8081
// (El backend ya permite ese origen por CORS: app.cors.allowed-origins).
const http = require('http');
const fs = require('fs');
const path = require('path');

const RAIZ = __dirname;
const PUERTO = Number(process.env.PORT) || 8081;
const TIPOS = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.ico': 'image/x-icon',
};

http.createServer((req, res) => {
  const ruta = decodeURIComponent(req.url.split('?')[0]);
  const archivo = path.join(RAIZ, ruta === '/' ? 'index.html' : ruta);
  // solo se sirven archivos dentro de la carpeta frontend, y nunca este mismo script
  const permitido = archivo.startsWith(RAIZ + path.sep) && path.basename(archivo) !== 'servidor.js';
  if (permitido && fs.existsSync(archivo) && fs.statSync(archivo).isFile()) {
    res.writeHead(200, {
      'Content-Type': TIPOS[path.extname(archivo)] || 'application/octet-stream',
      'Cache-Control': 'no-store',
    });
    fs.createReadStream(archivo).pipe(res);
  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('No encontrado');
  }
}).listen(PUERTO, () => {
  console.log(`Frontend listo en http://localhost:${PUERTO}  (Ctrl+C para detener)`);
});
