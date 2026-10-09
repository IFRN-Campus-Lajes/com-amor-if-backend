# API do Modo Deus

Todos os endpoints abaixo exigem `ROLE_ADMINISTRADOR`.

## Regras

- `GET /api/admin/regras`: lista versões ativas e inativas.
- `GET /api/admin/regras/metadados`: lista sensos, tipos, perfis e categorias.
- `POST /api/admin/regras`: cria uma regra.
- `PUT /api/admin/regras/{id}`: atualiza uma regra. Se ela já possuir lançamentos, a versão anterior é desativada e uma nova versão é criada.
- `PATCH /api/admin/regras/{id}/status/{ativo}`: ativa ou desativa uma regra sem remover o histórico.

O corpo de criação e edição contém:

```json
{
  "descricao": "5 pontos por aluno atuante em núcleo",
  "grupo": null,
  "categoria": "Atividades extracurriculares",
  "operacao": "SUM",
  "valorMinimo": 5,
  "valorMaximo": 5,
  "sensoId": 4,
  "tipoRegraId": 5,
  "roleIds": [1, 10],
  "ativo": true
}
```

## Categorias

- `GET /api/admin/categorias-regras`
- `POST /api/admin/categorias-regras`
- `PUT /api/admin/categorias-regras/{id}`
- `DELETE /api/admin/categorias-regras/{id}`

Categorias usadas por regras não podem ser excluídas ou renomeadas; elas podem ser desativadas. Na primeira consulta dos metadados, nomes legados existentes nas próprias regras são registrados no catálogo de categorias.

## Fonte de verdade

O perfil de produção não popula nem sincroniza regras durante a inicialização. O catálogo persistido no banco é a única fonte de verdade; as antigas definições em `ProdConfig`, `RegraCategorias` e `RegraCatalogoSincronizador` permanecem apenas como histórico do sistema.
