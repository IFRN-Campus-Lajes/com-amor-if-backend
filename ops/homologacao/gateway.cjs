// Adaptador fictício exclusivo da homologação local. A API mantém JWT e autorização.
const http = require('node:http');
function json(res, data) {
  res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' });
  res.end(JSON.stringify(data));
}
http.createServer(async (req, res) => {
  if (req.url === '/suap/token') return json(res, { access_token: 'homologacao-ficticia', token_type: 'Bearer', expires_in: 86400 });
  if (req.url === '/suap/user') return json(res, {
    matricula: 'homolog-admin', nome_usual: 'Administrador de Homologação',
    tipo_vinculo: 'Servidor', campus: 'LAJ', email: 'homolog@example.invalid',
    vinculo: { categoria: 'tecnico administrativo', setor_suap: 'DG/LAJ', funcao: ['CD2 - DG/LAJ'] },
  });
  if (req.url === '/homologacao/entrar') {
    try {
      const response = await fetch('http://backend:8080/api/auth/register?code=homologacao-local');
      const session = await response.json();
      if (!response.ok || !session.access_token) throw new Error('Login indisponível');
      res.writeHead(302, { Location: '/wizard-area/automatic-rules', 'Set-Cookie': `access_token=${session.access_token}; Path=/; SameSite=Lax; Max-Age=86400`, 'Cache-Control': 'no-store' });
      return res.end();
    } catch {
      res.writeHead(503); return res.end('Homologação iniciando. Atualize em instantes.');
    }
  }
  const api = req.url.startsWith('/api/');
  const proxy = http.request({ hostname: api ? 'backend' : 'frontend', port: api ? 8080 : 3000, path: req.url, method: req.method, headers: req.headers }, upstream => {
    res.writeHead(upstream.statusCode, upstream.headers);
    upstream.pipe(res);
  });
  proxy.on('error', () => { if (!res.headersSent) res.writeHead(502); res.end('Homologação iniciando. Atualize em instantes.'); });
  req.pipe(proxy);
}).listen(8080, '0.0.0.0');
