# Monitor de deploy

O script `monitorar-deploy.sh` verifica uma vez as branches `deploy` dos
repositórios em `/home/manoel.cunha/deploy/backend` e
`/home/manoel.cunha/deploy/frontend`. Quando encontra uma
atualização, ele executa `docker compose up --build --detach` e confirma que
`postgres`, `backend` e `frontend` estão em execução.

## Instalação na VM

```bash
sudo install -m 755 /home/manoel.cunha/deploy/backend/ops/monitorar-deploy.sh /usr/local/bin/monitorar-deploy
sudo cp /home/manoel.cunha/deploy/backend/ops/.env.monitor.example /home/manoel.cunha/deploy/.env.monitor
sudo chown manoel.cunha:manoel.cunha /home/manoel.cunha/deploy/.env.monitor
sudo chmod 600 /home/manoel.cunha/deploy/.env.monitor
```

Edite `/home/manoel.cunha/deploy/.env.monitor` com os dados reais do SMTP. Para Gmail, use uma
senha de aplicativo. O arquivo contém segredo e não deve ser versionado.

## Teste de e-mail

```bash
sudo -u manoel.cunha /usr/local/bin/monitorar-deploy --test-email
```

## Execução manual

```bash
sudo -u manoel.cunha /usr/local/bin/monitorar-deploy
```

O script não atualiza um repositório que tenha alterações locais ou que não
esteja na branch `deploy`.

## Execução automática

Instale as unidades do `systemd`:

```bash
sudo install -m 644 /home/manoel.cunha/deploy/backend/ops/systemd/com-amor-if-deploy-monitor.service /etc/systemd/system/com-amor-if-deploy-monitor.service
sudo install -m 644 /home/manoel.cunha/deploy/backend/ops/systemd/com-amor-if-deploy-monitor.timer /etc/systemd/system/com-amor-if-deploy-monitor.timer
sudo chown manoel.cunha:manoel.cunha /home/manoel.cunha/deploy/.env.monitor
sudo chmod 600 /home/manoel.cunha/deploy/.env.monitor
sudo systemctl daemon-reload
sudo systemctl enable --now com-amor-if-deploy-monitor.timer
```

O usuário `manoel.cunha` precisa ter acesso ao Docker. Confirme com:

```bash
groups manoel.cunha
```

A saída deve conter o grupo `docker`. Para incluir o usuário, se necessário:

```bash
sudo usermod -aG docker manoel.cunha
```

Após incluir o grupo, encerre e inicie novamente a sessão SSH do usuário.

Confira o timer e os logs:

```bash
systemctl list-timers com-amor-if-deploy-monitor.timer
sudo journalctl -u com-amor-if-deploy-monitor.service -f
```
