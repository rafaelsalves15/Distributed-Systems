Distributed Ledger
================ 

Este documento descreve o projecto da cadeira de Sistemas Distribuídos 2022/2023.

1 Introdução
------------

O objetivo do projeto de Sistemas Distribuídos (SD) é desenvolver o sistema **DistLedger**, um serviço que implementa um *ledger* distribuído, sobre o qual são suportadas trocas de uma moeda digital. O serviço é fornecido por servidores através de chamadas a procedimentos remotos.

O serviço pode ser acedido por dois tipos de clientes: i) os *utilizadores*, que podem ter conta no sistema e trocar moeda entre si; ii) os *administradores* que mantêm o serviço em funcionamento.

Cada utilizador pode criar uma conta, com saldo inicial nulo. Por simplificação, cada utilizador pode ter, no máximo, uma conta.
Cada utilizador também pode apagar a sua conta, desde que o saldo da conta seja nulo.

Adicionalmente, cada utilizador pode transferir moedas da sua conta para a conta de outro utilizador.
Para que uma transferência seja executada, a conta origem precisa existir e ter saldo
superior ou igual ao montante a transferir, e a conta destino existir; caso contrário, a transferência é cancelada.

Entre os utilizadores, há um utilizador especial, chamado *broker central*, ou simplesmente *broker*.
Ao contrário dos restantes utilizadores, a conta do broker existe sempre
(ou seja, não é criada nem apagada pelo utilizador respetivo) e o seu saldo inicial é 1000 moedas.
Ou seja, quando o sistema **DistLedger** se inicia, existe uma única conta, com saldo 1000, cujo dono é
o broker.

O utilizador broker corresponde a uma entidade que corre um serviço externo (não implementado no **DistLedger**)
que permite a utilizadores comprarem moeda digital, em troca de euros.
Sempre que uma compra de moeda digital é feita com sucesso (fora do **DistLedger**), o broker solicita
ao sistema **DistLedger**
que transfira o número de moedas digitais da sua conta para a conta do utilizador em causa.
Note-se que este último passo (a transferência de moeda digital do broker para o utilizador em causa)
é o único que é observado pelo **DistLedger**.

A situação inversa também pode ocorrer, em que um utilizador transfere moedas digitais da sua conta para a do broker,
com o objetivo de receber o valor correspodente em euros (mais uma vez, a entrega do montante em euros acontece externamente ao **DistLedger**).

O sistema será concretizado através de um conjunto de serviços gRPC implementados na plataforma Java.

O projecto está estruturado em três fases.


2 Arquitetura do sistema
------------------------

O sistema usa uma arquitetura cliente-servidor. O serviço é fornecido por um ou mais servidores que podem ser
contactados por processos cliente, através de chamadas a procedimentos remotos.
Podem existir diferentes tipos de
clientes, associados a utilizadores e administradores, respectivamente.

Em cada fase do projecto será explorada uma variante desta arquitetura básica, tal como se descreve abaixo.


2.1 Fase 1
-------------------

Nesta fase o serviço é prestado por um único servidor, que aceita pedidos no endereço/porto que é conhecido de antemão
por todos os clientes.

2.2 Fase 2
-------------------

Na segunda fase, o serviço é fornecido por dois servidores: um primário e um secundário. Todas as operações que alterem
o estado do sistema (que passaremos a designar como *transações*) só podem ser realizadas no primário, que
propaga as mesmas para o secundário. Operações que não alterem o estado do sistema (que passaremos a designar como
*operações de leitura*), podem ser feitas em qualquer um dos servidores.

Serão valorizadas soluções que minimizem a quantidade de informação propagada pelo primário para atualizar o estado do secundário.

Em caso de indisponibilidade do primário, o sistema deve operar sem primário (até que este recupere). Ou seja, sem haver eleição de novo primário, o que implica que o sistema só é capaz de
responder a operações de leitura (desde que enviadas ao secundário). Não se pretende nem serão valorizadas soluções que implementem a deteção da falha do primário e respetiva substituição pelo secundário (como novo primário).

Para além disso, nesta fase os clientes não sabem à partida os endereços destes servidores, tendo por isso de recorrer a um servidor de nomes **(a ser desenvolvido nas aulas teórico-práticas)** para obter esta informação.


2.3 Fase 3
-------------------

Na última fase, o serviço é fornecido por dois servidores que partilham estado usando um modelo de coerência eventual,
com capacidade de integrarem operações concorrentes. As operações de inscrição podem ser feitas em qualquer um dos
servidores, que propagam em diferido ("background") as alterações para o outro servidor. Sempre que possível, operações
executadas de forma concorrente são integradas num estado comum. Neste modelo algumas operações podem vir a ser
posteriormente canceladas de forma automática pelo sistema.

Por exemplo, considere-se que um utilizador *Alice* dispõe de 120 moedas na sua conta.
A partir desse estado, a *Alice*:

- Transfere 60 moedas para o utilizador *Bob*, usando um servidor *S1*.
- Logo de seguida, transfere 40 moedas para o utilizador *Charlie*, usando um servidor *S2*.

Temporariamente, ambos os servidores vão ter informação incoerente:

- O servidor *S1* aceitou executar a primeira transferência, e o saldo local da conta da *Alice* é 60.
- O servidor *S2* aceitou executar a segunda transferência, e o saldo local da conta da *Alice* é 80.

Após a propagação diferida das actualizações entre os servidores, ambos os servidores conhecerão e executarão ambas as transações (i.e., ambas as transferências), embora por ordens diferentes, e ambos terão o mesmo saldo, 20.

Considere agora uma variante do exemplo acima, mas em que a Alice tem um comportamento malicioso e tenta
usar mais dinheiro que aquele de que dispõe:

- Saldo inicial da conta da Alice é 120.
- Alice transfere 100 moedas para o utilizador *Bob*, usando um servidor *S1*;
- Logo de seguida, transfere 40 moedas para o utilizador *Charlie*, usando um servidor *S2*.

Neste cenário, depois de fazer a propagação diferida, ambos os servidores verificam que o saldo da conta não permite executar ambas as transferências. Consequentemente, cada servidor deve cancelar uma das transferência e só executar a outra transferência. Neste caso, a transferência cancelada deve ser colocada numa *lista de transações canceladas*, mantida por ambos os servidores. Se ambos os servidores decidirem cancelar a mesma transação (por exemplo, a transferência <Alice,Bob,100>), então ambos os servidores convergirão para um estado coerente (por exemplo, a conta da Alice ficar com saldo 80).


Nesta fase, uma das réplicas continua a ser considerada primária. As operações de criação e fecho de contas continuam a só poder ser realizadas no primário.



2.2 Componentes opcionais da fase 3
------------------- 

Os alunos poderão alterar as estruturas de dados mantidas pelo servidor, assim como a informação trocada entre
servidores, para aplicar as políticas de reconciliação que considerem mais indicadas.

Os alunos poderão também desenvolver mecanismos extra que permitam acrescentar uma terceira réplica já com o sistema em
funcionamento. Reservamos 2 valores adicionais nesta fase para os alunos que conseguirem desenvolver corretamente um
mecanismo deste tipo (por outras palavras, os alunos podem ter uma nota superior a 20 valores na fase 3, o que poderá
compensar uma nota mais baixa noutra fase).

3 Estado dos servidores
------------------------

O servidor (ou servidores, conforme a fase) mantém o estado necessário para fornecer o serviço. Este estado consiste em duas listas de operações, que inicialmente estão vazias e vão crescendo à medida que lhes são acrescentadas novas operações:

- A lista de transações aceites, também chamada *ledger*. Inclui transações de 3 tipos: criar conta, remover conta, transferir moeda entre contas.
- A lista de transações canceladas. Esta lista só será preenchida na fase 3, devido ao modelo de coerência eventual.

O servidor deve também manter, num mapa de contas, informação sobre as contas ativas neste momento e o respetivo saldo.
Esta estrutura descreve o estado que resulta da execução ordenada de todas as transações atualmente na *ledger*.
Sempre que uma nova transação é aceite na *ledger*, o estado do mapa de contas deve ser atualizado.


4 Interfaces do serviço
------------------------

Cada servidor exporta múltiplas interfaces. Cada interface está pensada para expor operações a cada tipo de cliente  (utilizadores e administradores). Para além dessas, os servidores exportam uma terceira interface pensada para ser invocada por outros
servidores (no caso em que os servidores estão replicados, e necessitam de comunicar entre si).

4.1 Interface do utilizador
-------------------

O utilizador pode invocar as seguintes funções:

- `createAccount` -- cria uma conta associada a este utilizador
- `deleteAccount` -- apaga a conta associada ao utilizador
- `balance` -- devolve o saldo atual da conta que o utilizador tem ativa neste momento
- `transferTo` -- transfere uma quantia para outro utilizador

4.2 Interface do administrador
-------------------

- `activate` -- coloca o servidor em modo **ATIVO** (este é o comportamento por omissão), em que responde a todos os
  pedidos
- `deactivate` -- coloca o servidor em modo **INATIVO**. Neste modo o servidor responde com o erro "INACTIVE_SERVER" a todos os pedidos dos utilizadores
- `getLedgerState` -- apresenta o conteúdo da *ledger* assim como a lista de operações canceladas
- `deactivateGossip` -- termina o processo de propagação diferida entre réplicas (só para a fase 3)
- `activateGossip` -- inicia o processo de propagação diferida entre réplicas (só para a fase 3)
- `gossip` -- força uma réplica a fazer uma propagação diferida para a(s) outra(s) réplica(s) (só para a fase 3)


4.3 Interface entre servidores (só fases 2 e 3)
-------------------

-`propagateState` -- um servidor envia o seu estado a outra réplica.

5 Servidor de nomes
------------------------

O servidor de nomes permite aos servidores registarem o seu endereço para ser conhecido por outros que estejam presentes
no sistema.

Um servidor, quando se regista, indica o nome do serviço (neste caso *DistLedger*), o seu endereço e um qualificador, que
pode assumir os valores 'P' (primário) ou 'S' (secundários).

Este servidor está à escuta no porto **5000** e assume-se que nunca falha.

Os clientes podem obter o endereço dos servidores, fornecendo o nome do
serviço e o qualificador.

Na fase 3, cada um dos servidores também pode usar o servidor de nomes para ficar a saber o endereço do outro servidor.

6 Processos
------------------------


O sistema será instalado recorrendo a 8 processos no máximo.

Todos os processos cliente deverão mostrar o símbolo *>* sempre que se encontrarem à espera que um comando seja
introduzido.

Para todos os comandos, os processos cliente devem imprimir a mensagem de resposta, tal como gerada pelo método toString() da classe gerada pelo compilador `protoc`. Nota: atenção que, no caso do comando 'balance' o método toString() não imprime o saldo caso este seja nulo; esse comportamento é o previsto.

Todos os processos devem poder ser lançados com uma opção "-debug". Se esta opção for seleccionada, o processo deve
imprimir para o "stderr" mensagens que descrevam as acções que executa. O formato destas mensagens é livre mas deve
ajudar a depurar o código. Deve também ser pensado para ajudar a perceber o fluxo das execuções durante a discussão
final.


6.1 Servidores primário/secundário
--------------

O servidor (ou servidores nas fases 2 e 3) devem ser lançados a partir da pasta `Server`, recebendo como argumentos o porto e uma flag que identifica se um servidor é ou não o primário. Na fase 1, o servidor será sempre primário (ou seja, a flag passada é ignorada).

Por exemplo, um servidor primário pode ser lançado da seguinte forma a partir da pasta `Server` (**$** representa a *shell* do sistema operativo):

`$ mvn exec:java -Dexec.args="2001 P"`

Um servidor secundário pode ser lançado da seguinte forma:

`$ mvn exec:java -Dexec.args="2002 S"`


6.2 Servidor de nomes (só fases 2 e 3)
-------------

O servidor de nomes deve ser lançado sem argumentos e ficará à escuta no porto `5000`, podendo ser lançado a partir da pasta `NamingServer` da seguinte forma:

`$ mvn exec:java`

6.3 Cliente *utilizador*
-------------

O cliente utilizador deve ser lançado a partir da pasta `User`.
Na fase 1, este programa recebe como argumentos o nome endereço da máquina e porto onde o servidor do DistLedger pode ser encontrado. Por exemplo:

`$ mvn exec:java -Dexec.args="localhost 2001"`

A partir da fase 2, o cliente utilizador deixa de ter quaisquer argumentos.

Quando lançado, o cliente utilizador oferece uma interface de linha de comandos, através da consola deste processo.
Para cada operação na interface utilizador do *DistLedger*, existe um comando que permite invocar essa operação.
Todos esses comandos recebem, como primeiro argumento, o nome do utilizador que está a solicitar a operação.

Adicionalmente, existe também um comando `exit` para terminar o cliente.

Exemplo de uma interação com o cliente utilizador:

```
> createAccount Alice
code: OK
> createAccount Bob
code: OK
> transferTo broker Alice 60
code: OK
> transferTo Alice Bob 50
code: OK
> balance Alice
code: OK
10
> balance Bob
code: OK
50
> closeAccount Alice
code: OK
> exit
```


6.4 Cliente *administrador*
---------

O cliente administrador deve ser lançado a partir da pasta `Admin`. Tal como o cliente utilizador, na fase 1 o cliente administrador recebe o endereço/porto do servidor *DistLedger*; nas fases 2 e 3, o cliente deixa de receber argumentos.

Este cliente também implementa uma interface de linha de comandos através da consola em que é lançado. Todos os comandos do administrador **podem** receber como argumento `P|S` indicando se o servidor alvo dessa operação é o primário ou o secundário, se tal argumento não for indicado, a operação deve ser realizada no primário.

Exemplo:

```
> deactivate P
code: OK
> getLedgerState
code: OK
ledgerState {
  ledger {
    type: OP_CREATE_ACCOUNT
    userId: "Alice"
  }
  ledger {
    type: OP_CREATE_ACCOUNT
    userId: "Bob"
  }
  ledger {
    type: OP_TRANSFER_TO
    userId: "broker"
    destUserId: "Alice"
    amount: 60
  }
  ledger {
    type: OP_TRANSFER_TO
    userId: "Alice"
    destUserId: "Bob"
    amount: 60
  }
  ledger {
    type: OP_DELETE_ACCOUNT
    userId: "Alice"
  }
}
> activate P
code: OK
> exit
```

7 Tecnologia
------------

Todos os componentes do projeto têm de ser implementados na linguagem de
programação [Java](https://docs.oracle.com/javase/specs/).

A ferramenta de construção a usar, obrigatoriamente, é o [Maven](https://maven.apache.org/).

### Invocações remotas

A invocação remota de serviços deve ser suportada por serviços [gRPC](https://grpc.io/).

Os serviços implementados devem obedecer aos *protocol buffers* fornecidos no código base disponível no repositório github do projeto.


### Persistência

Não se exige nem será valorizado o armazenamento persistente do estado dos servidores.

### Validações

Os argumentos das operações devem ser validados obrigatóriamente e de forma estrita pelo servidor.

Os clientes podem optar por também validar, de modo a evitar pedidos desnecessários para o servidor, mas podem optar por
uma versão mais simples da validação.

### Faltas

Se durante a execução surgirem faltas, ou seja, acontecimentos inesperados, o programa deve apanhar a exceção, imprimir
informação sucinta e pode parar de executar.

Se for um servidor, o programa deve responder ao cliente com um código de erro adequado.

Se for um dos clientes, pode decidir parar com o erro recebido ou fazer novas tentativas de pedido.


8 Resumo
------------

Em resumo, é necessário implementar:

o servidor;

o cliente utilizador;

o cliente administrador.

Ambos os clientes oferecem uma interface-utilizador baseada na linha de comandos.


9 Avaliação
------------

9.1 Fotos
---------

Cada membro da equipa tem que atualizar o Fénix com uma foto, com qualidade, tirada nos últimos 2 anos, para facilitar a
identificação e comunicação.

9.2 Identificador de grupo
--------------------------

O identificador do grupo tem o formato `GXX`, onde `G` representa o campus e `XX` representa o número do grupo de SD
atribuído pelo Fénix. Por exemplo, o grupo A22 corresponde ao grupo 22 sediado no campus Alameda; já o grupo T07
corresponde ao grupo 7 sediado no Taguspark.

O grupo deve identificar-se no documento `README.md` na pasta raíz do projeto.

Em todos os ficheiros de configuração `pom.xml` e de código-fonte, devem substituir `GXX` pelo identificador de grupo.

Esta alteração é importante para a gestão de dependências, para garantir que os programas de cada grupo utilizam sempre
os módulos desenvolvidos pelo próprio grupo.

9.3 Colaboração
---------------

O [Git](https://git-scm.com/doc) é um sistema de controlo de versões do código fonte que é uma grande ajuda para o
trabalho em equipa.

Toda a partilha de código para trabalho deve ser feita através do [GitHub](https://github.com).

O repositório de cada grupo está disponível em: https://github.com/tecnico-distsys/GXX-DistLedger/ (substituir `GXX` pelo
identificador de grupo).

A atualização do repositório deve ser feita com regularidade, correspondendo à distribuição de trabalho entre os membros
da equipa e às várias etapas de desenvolvimento.

Cada elemento do grupo deve atualizar o repositório do seu grupo à medida que vai concluindo as várias tarefas que lhe
foram atribuídas.

9.4 Entregas
------------

As entregas do projeto serão feitas também através do repositório GitHub.

A cada parte do projeto a entregar estará associada uma [*tag*](https://git-scm.com/book/en/v2/Git-Basics-Tagging).

Cada grupo tem que marcar o código que representa cada entrega a realizar com uma *tag* específica -- `SD_P1` -- antes
da hora limite de entrega.

9.5 Valorização
---------------


As datas limites de entrega estão definidas no site dos laboratórios: (https://tecnico-distsys.github.io)

### Qualidade do código

A avaliação da qualidade engloba os seguintes aspetos:

- Configuração correta (POMs);

- Código legível (incluindo comentários relevantes);

- [Tratamento de exceções adequado](http://disciplinas.tecnico.ulisboa.pt/leic-sod/2019-2020/labs/03-tools-sockets/exceptions/index.html)
  ;

- [Sincronização correta](http://disciplinas.tecnico.ulisboa.pt/leic-sod/2019-2020/labs/03-tools-sockets/java-synch/index.html)
  .

9.6 Instalação e demonstração
-----------------------------

As instruções de instalação e configuração de todo o sistema, de modo a que este possa ser colocado em funcionamento,
devem ser colocadas no documento `README.md`.

Este documento tem de estar localizado na raiz do projeto e tem que ser escrito em formato [*
MarkDown*](https://guides.github.com/features/mastering-markdown/).

Cada grupo deve preparar também um mini relatório, que não deve exceder as **500** palavras, explicando a sua solução para a fase 3,
e que não deve repetir informação que já esteja no enunciado. Este documento deve estar na raiz do projeto e deve ter o nome `REPORT.md`

9.7 Discussão
-------------

As notas das várias partes são indicativas e sujeitas a confirmação na discussão final, na qual todo o trabalho
desenvolvido durante o semestre será tido em conta.

As notas a atribuir serão individuais, por isso é importante que a divisão de tarefas ao longo do trabalho seja
equilibrada pelos membros do grupo.

Todas as discussões e revisões de nota do trabalho devem contar com a participação obrigatória de todos os membros do
grupo.

9.8 Atualizações
----------------

Para acompanhar as novidades sobre o projeto, consultar regularmente
a [página Web dos laboratórios](https://tecnico-distsys.github.io).

Caso venham a surgir correções ou clarificações neste documento, podem ser consultadas no histórico (_History_).

**Bom trabalho!**