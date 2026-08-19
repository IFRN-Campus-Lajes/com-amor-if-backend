# Monitor de deploy

O script `monitorar-deploy.sh` verifica uma vez as branches `deploy` dos
repositórios em `/deploy/backend` e `/deploy/frontend`. Quando encontra uma
atualização, ele executa `docker compose up --build --detach` e confirma que
`postgres`, `backend` e `frontend` estão em execução.

## Instalação na VM

```bash
sudo install -m 755 /deploy/backend/ops/monitorar-deploy.sh /usr/local/bin/monitorar-deploy
sudo cp /deploy/backend/ops/.env.monitor.example /deploy/.env.monitor
sudo chmod 600 /deploy/.env.monitor
```

Edite `/deploy/.env.monitor` com os dados reais do SMTP. Para Gmail, use uma
senha de aplicativo. O arquivo contém segredo e não deve ser versionado.

## Teste de e-mail

```bash
sudo /usr/local/bin/monitorar-deploy --test-email
```

## Execução manual

```bash
sudo /usr/local/bin/monitorar-deploy
```

O script não atualiza um repositório que tenha alterações locais ou que não
esteja na branch `deploy`.
