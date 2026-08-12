# Com Amor, IF - Backend

API do sistema **Com Amor, IF**, responsável por autenticação, regras,
lançamentos, validação de pontuações, turmas, anos letivos e relatórios.

## Versão 1.0.0

Esta versão consolida o catálogo de regras do sistema e preserva os registros
históricos de pontuação. Os principais recursos incluem:

- autenticação integrada ao SUAP e autorização por perfil;
- regras organizadas por senso e categoria;
- lançamentos fixos, variáveis, por aluno, por turno e por bimestre;
- regras agrupadas para cenários mutuamente exclusivos;
- regras automáticas, validação e anulação de pontuações;
- API para relatórios e acompanhamento das turmas.

## Tecnologias

- Java 21
- Spring Boot 3
- Spring Security e JWT
- Spring Data JPA
- PostgreSQL em desenvolvimento e produção
- H2 nos testes
- Maven Wrapper

## Requisitos

- JDK 21
- PostgreSQL para os perfis `dev` e `prod`
- Credenciais de integração com o SUAP

## Configuração

O projeto carrega variáveis de ambiente a partir de um arquivo `.env` opcional.
Crie-o na raiz com valores apropriados ao ambiente:

```env
JWT_SECRET_KEY=
DB_USER=
DB_URL_DEV=
DB_PASSWORD=
DB_URL_PROD=
DB_PASSWORD_PROD=
SUAP_USER_INFO_URL=
SUAP_TOKEN_URL=
SUAP_CLIENT_ID=
SUAP_CLIENT_SECRET=
SUAP_REDIRECT_URI=
```

Os perfis disponíveis são:

- `dev`: banco configurado por `DB_URL_DEV`, com esquema recriado a cada execução;
- `prod`: banco configurado por `DB_URL_PROD`, com atualização de esquema;
- `test`: dados de demonstração e banco H2 usados durante os testes.

## Execução local

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

A API é exposta na porta padrão `8080`.

## Testes e build

```bash
./mvnw test
./mvnw package
```

## Docker

O Dockerfile utiliza o JAR já empacotado. Gere-o antes de construir a imagem:

```bash
./mvnw package
docker build -t com-amor-if-backend .
docker run --rm -p 8080:8080 --env-file .env com-amor-if-backend
```

## Projetos relacionados

- Frontend: [IFRN-Campus-Lajes/com-amor-if-frontend](https://github.com/IFRN-Campus-Lajes/com-amor-if-frontend)
- Documentação do projeto: mantida no Google Docs da equipe.

## Licença

Consulte a organização IFRN-Campus-Lajes para as condições de uso e manutenção
do sistema.
