# PokeTeam

Aplicativo Android em Kotlin para registrar a equipe favorita do usuário, com até seis Pokémon.

## Etapa parcial - acompanhamento de 03/11

Esta versão implementa a base de consumo da [PokéAPI](https://pokeapi.co/):

- Listagem dos primeiros 20 Pokémon, com nome, número e imagem.
- Busca por nome ou número, mostrando também tipos, altura e peso.
- Estados de carregamento, erro e opção de tentar novamente.
- Separação em `data` (modelos e Repository), `network`, `viewmodel` e `userinterface`.

Base da API: `https://pokeapi.co/api/v2/`. Usamos `GET pokemon?limit=20` para listar e `GET pokemon/{nomeOuNumero}` para consultar um Pokémon. As imagens são carregadas com Coil.

O projeto ainda está em desenvolvimento. As próximas etapas são login e sessão local, persistência com Room, cadastro da equipe com 1 a 6 Pokémon sem repetidos e navegação por três abas e uma tela de detalhes com rotas tipadas.

## Executar no Android Studio

1. Abra a pasta raiz do projeto, onde está `settings.gradle.kts`.
2. Aguarde a sincronização do Gradle. Instale o Android SDK 35 quando solicitado.
3. Selecione um emulador ou celular Android conectado e execute o módulo `app`.

O código também pode ser editado no VS Code. Para compilar pelo terminal, configure um JDK 17 e o Android SDK 35, incluindo o caminho do SDK em `local.properties` (`sdk.dir=...`) ou em `ANDROID_HOME`. Depois execute:

```sh
./gradlew assembleDebug
```

O APK gerado fica em `app/build/outputs/apk/debug/app-debug.apk`. A execução no emulador ou celular depende de um ambiente Android configurado.

## Conferir esta etapa

1. Ao abrir, aguarde a lista e toque em um Pokémon para consultar seus dados na mesma tela.
2. Busque `pikachu` ou `25`: devem aparecer número, imagem, tipo, altura e peso.
3. Busque um nome inexistente para conferir a mensagem de Pokémon não encontrado.
4. Para testar erro de conexão, desligue a internet e busque um Pokémon que ainda não consultou. Religue a internet e toque em **Tentar novamente**.
5. Toque em **Ver lista** para voltar à listagem inicial.

As respostas ficam em cache na memória durante o uso da tela. Isso evita chamadas repetidas; ainda não é persistência com Room.

Os testes de Repository e ViewModel verificam cache, leitura do JSON, carregamento, erros, repetição e cancelamento de consultas. Com o ambiente Android configurado, execute:

```sh
./gradlew testDebugUnitTest
```

## Referências

- [Arquitetura em pacotes](https://rclepf.github.io/ArquiteturaPacotesAndroid/)
- [Consumo de API no Android](https://rclepf.github.io/PokedexApiAndroid/)
- [Persistência local](https://rclepf.github.io/PersistenciaAndroid/) e [Room](https://rclepf.github.io/RoomAndroid/)
- [Projeto SQLiteAndroid](https://github.com/trvzera/SQLiteAndroid)
