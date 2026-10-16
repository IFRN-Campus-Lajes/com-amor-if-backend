# Checklist de validação da Área do Mago

## Preparação

- [ ] Backend iniciado com JDK 21 e perfil de desenvolvimento.
- [ ] Frontend iniciado na branch `codex/modo-deus`.
- [ ] Usuário administrador consegue abrir a Área do Mago.
- [ ] Usuário sem perfil administrador recebe bloqueio de acesso.
- [ ] Não há erros no console do navegador nem no log do backend ao abrir a página.

## Catálogo de regras

- [ ] Os cinco sensos aparecem com suas cores e contadores.
- [ ] Trocar de senso mostra somente suas categorias e regras.
- [ ] A pesquisa encontra regras por descrição, categoria e grupo.
- [ ] O filtro alterna corretamente entre todas, ativas e inativas.
- [ ] Cada categoria abre e fecha com animação.
- [ ] A quantidade de regras no cabeçalho da categoria está correta.
- [ ] O botão geral cria uma regra no senso selecionado.
- [ ] O botão “Adicionar regra” cria uma regra já preenchida com senso e categoria corretos.

## Categorias

- [ ] A tela agrupa as categorias pelo senso correspondente.
- [ ] É possível criar duas categorias com o mesmo nome em sensos diferentes.
- [ ] Não é possível criar duas categorias com o mesmo nome no mesmo senso, inclusive variando maiúsculas e minúsculas.
- [ ] A categoria possui somente nome, senso e estado ativo.
- [ ] Editar e excluir têm botões de mesmo tamanho.
- [ ] Uma categoria sem regras pode ser renomeada, movida ou excluída.
- [ ] Uma categoria usada por regras não pode ser renomeada, movida nem excluída.
- [ ] Uma categoria usada pode ser renomeada e todas as suas regras passam a exibir o novo nome.
- [ ] Uma categoria com regras ativas não pode ser inativada.
- [ ] Após desativar todas as regras da categoria, ela pode ser inativada.
- [ ] Categorias inativas não aparecem para novas regras, mas continuam visíveis nas regras existentes.

## Cadastro e edição de regras

- [ ] A categoria pesquisável mostra apenas categorias do senso escolhido.
- [ ] Alterar o senso limpa a categoria anterior.
- [ ] O tipo de regra apresenta grupos de valor fixo e variável.
- [ ] Perfis autorizados funcionam como tags selecionáveis.
- [ ] “Aval” e “Aluno” não aparecem como perfis lançadores.
- [ ] O campo grupo, quando existente, é somente leitura.
- [ ] Uma regra fixa salva o mesmo valor mínimo e máximo.
- [ ] Uma regra variável nunca salva máximo abaixo do mínimo.
- [ ] É possível salvar uma regra de subtração, como “livro devolvido com atraso”, com 15 pontos.
- [ ] A prévia reflete operação, valores, alvo e frequência.

## Versionamento e histórico

- [ ] Editar regra sem lançamentos atualiza a própria regra e mantém v1.
- [ ] Editar regra com lançamentos desativa a versão anterior e cria v2 ativa.
- [ ] Uma nova edição da versão utilizada cria v3.
- [ ] Pontuações antigas continuam apontando para a versão antiga da regra.
- [ ] Versões anteriores aparecem somente no acordeão de histórico, sem ações de edição ou ativação.
- [ ] A API rejeita tentativas diretas de editar ou ativar uma versão anterior.
- [ ] A pontuação histórica não muda após editar descrição, valores, senso, categoria, tipo ou perfis.
- [ ] A mensagem de sucesso informa a nova versão somente quando ela foi realmente criada.

## Ativação e exclusão

- [ ] Ativar e desativar regra usa modal, não alerta do navegador.
- [ ] Cancelar o modal não altera a regra.
- [ ] Confirmar preserva pontuações existentes.
- [ ] Excluir categoria usa modal de confirmação.
- [ ] Erros aparecem acima dos modais e permanecem legíveis.

## Responsividade e acessibilidade

- [ ] Validar larguras de 320 px, 375 px, 768 px e desktop.
- [ ] Não há rolagem horizontal na página ou nos modais, exceto nas abas de sensos.
- [ ] Botões permanecem tocáveis e sem sobreposição.
- [ ] Formulários ficam em uma coluna no celular.
- [ ] Modais permitem rolar até os botões de ação.
- [ ] Abas, acordeões, tags e confirmações funcionam por teclado.
- [ ] Foco e textos continuam visíveis com zoom de 200%.
- [ ] A animação do mago e dos acordeões respeita a preferência de movimento reduzido.

## Home e regressão

- [ ] O menu de opções aparece acima do ranking na Home.
- [ ] Lançamento de pontuações continua agrupando regras por senso e categoria.
- [ ] Regras desativadas não ficam disponíveis para novos lançamentos.
- [ ] Uma regra nova e ativa aparece no lançamento para usuários com um dos perfis autorizados selecionados.
- [ ] Ranking, relatórios, validação e histórico continuam exibindo pontuações antigas.

## Produção após a migração

- [ ] A versão `1` do Flyway consta como aplicada com sucesso.
- [ ] A chave primária de `categoria_regra` é `(nome, senso_id)`.
- [ ] Todas as regras existentes têm `versao >= 1` e `codigo_versao` preenchido.
- [ ] A quantidade migrada de categorias confere com os pares distintos de categoria e senso das regras.
- [ ] O backend inicia com `ddl-auto: validate`.
- [ ] Criar, editar, versionar, ativar e desativar funciona sobre uma cópia restaurada do banco de produção.
