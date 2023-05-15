<h1 align="center">
  API autenticação Oauth
</h1>

<h4 align="center">Status: ✔ Concluído</h4>

---

<p align="center">
 <a href="#user-content-sobre-o-projeto">Sobre o projeto</a> |
 <a href="#user-content-executando-o-projeto">Executando o projeto</a> |
 <a href="#user-content-end-points">End-points</a> |
 <a href="#user-content-tecnologias">Tecnologias</a>
</p>

---

## **Sobre o projeto**

API desenvolvida em Spring Boot para cadastro e autenticação de usuários. Neste projeto os dados de usuários serão gravados no banco de dados MongoDB, assim como a lista de envio de e-mails de recuperação de senha. 

## **Executando o projeto**

### Pré-requisitos

- Java 19 ( versão utilizada: 19.0.1 );
- Maven ( utilizei o Maven Wrapper que vem com o Spring )
- MongoDB ( versão utilizada: 6.0.5 Community Edition)

### Instruções e configurações

O arquivo de configuração do projeto está em `src/main/resources/application.yml`.

#### MongoDB

A configuração do banco MongoDB é feita por meio das propriedades `spring.data.mongodb.uri` e `spring.data.mongodb.database`. 

```yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017
      database: autenticacao
      uuid-representation: standard
```

#### Porta aplicação

O Tomcat irá iniciar por padrão na porta `8080`, caso seja necessário alterá-la, altere a propriedade `port`.

#### CORS 

A configuração do CORS irá permitir a origem `http://localhost:4200` por padrão. É possível alterá-la através da propriedade `autenticacao.cors.origem-permitida` do arquivo de configurações ou, ao rodar o projeto, modificando a origem permitida através da linha de comando.
Exemplo:

```bash
$ ./mvnw spring-boot:run -Dspring-boot.run.arguments=--autenticacao.cors.origem-permitida=http://localhost:8000
```

#### Recuperação de senha

A propriedade `autenticacao.recuperacao_senha.url` indica qual o url que será enviada por email para acessar o front da aplicação, para recuperar a senha. Já a propriedade `autenticacao.recuperacao_senha.token_validity_minutes` configura o tempo (em minutos) de validade do token de recuperação de senha. Exemplo:

```yml
autenticacao:
  recuperacao_senha:
    url: http://localhost:4200/usuarios/redefinir-senha
    token_validity_minutes: 120
```

#### JWT

As configurações dos tokens JWT são feitas por meio da configuração `autenticacao.jwt`, nela podemos configurar o tempo de validade do access token na propriedade `autenticacao.jwt.access_token_validity_seconds`, assim como o tempo do refresh token na propriedade `autenticacao.jwt.refresh_token_validity_seconds`. Exemplo:

```yml
autenticacao:
  jwt:
    audience: Autenticacao.API
    issuer: http://localhost:8080
    access_token_validity_seconds: 10
    refresh_token_validity_seconds: 604_800
    secret: Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=
    secure: false
```

#### Envio de email

A configuração do email para a recuperação de senha é feita por meio da configuração `autenticacao.mail`. Exemplo:

```yml
autenticacao:
  mail:
    host: smtp-mail.outlook.com
    port: 587
    username: email@email.com
    password: mystrongpassword
    remetente: Autenticacao
 ```

### Instruções de execução do projeto

```bash
# Na pasta raíz do projeto
# Execute o projeto (utilizando o maven wrapper)
$ ./mvnw spring-boot:run

# Execute o projeto (com o maven instalado)
$ mvn spring-boot:run
```

## **End-points**

Para ver a documentação por Swagger, acesso a url `http://localhost:8080/swagger-ui.html`.

### Autenticação

#### Obtenção de access token

Executar requisição HTTP POST em `/oauth/token`.

Parâmatros para requisição:

- `email`: Email do usuário
- `password`: Senha do usuário

Requisição:
```
POST /oauth/token HTTP/1.1
Host: localhost:8080
User-Agent: insomnia/2023.1.0
Content-Type: application/json
Accept: */*
Content-Length: 63
```
Corpo requisição:
```json
{
	"email": "vinicius@email.com",
	"password": "123"
}
```

Retorno:
```
    HTTP/1.1 200 
    Vary: Origin
    Vary: Access-Control-Request-Method
    Vary: Access-Control-Request-Headers
    Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJhdWQiOiJDYXJ0ZWlyYS5BUEkiLCJzdWIiOiJiYjkxMjhlYi1iMDUzLTQ3YTktOTc0ZS1kZGJmODBlOTViYzIiLCJuYW1lIjoiU2lyaXVzIiwiaWF0IjoxNjgxNDMyNDQzLCJleHAiOjE2ODIwMzcyNDN9.QMyNTAOiP5JWiJCfaZsf7nXTwLnQVkodLKrg7gBeuTg; Max-Age=604800; Expires=Fri, 21 Apr 2023 00:34:03 GMT; Path=/oauth/refresh-token; HttpOnly
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 0
    Cache-Control: no-cache, no-store, max-age=0, must-revalidate
    Pragma: no-cache
    Expires: 0
    X-Frame-Options: DENY
    Content-Type: application/json
    Transfer-Encoding: chunked
    Date: Fri, 14 Apr 2023 00:34:03 GMT
```
Corpo retorno:
```json
{
  "tipo": "Bearer",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJhdWQiOiJDYXJ0ZWlyYS5BUEkiLCJzdWIiOiJiYjkxMjhlYi1iMDUzLTQ3YTktOTc0ZS1kZGJmODBlOTViYzIiLCJuYW1lIjoiU2lyaXVzIiwiaWF0IjoxNjgxNDMyNDQzLCJleHAiOjE2ODE0MzI0NTN9.OzCNRafaaMH4FQw4RoJ4T57QZe914QoU8mLBS6u5mdQ"
}
```

Se ocorrer algum erro ao autenticar usuário a API não, será retornado código de status 400 Bad Request.

#### Renovar autenticação com refresh token

Executar requisição HTTP POST em `/oauth/refresh-token`
passando o refresh token no cookie chamado `refreshToken` no header da requisição:

Requisição:
```
POST /oauth/refresh-token HTTP/1.1
Host: localhost:8080
User-Agent: insomnia/2023.1.0
Cookie: refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJhdWQiOiJDYXJ0ZWlyYS5BUEkiLCJzdWIiOiJiYjkxMjhlYi1iMDUzLTQ3YTktOTc0ZS1kZGJmODBlOTViYzIiLCJuYW1lIjoiU2lyaXVzIiwiaWF0IjoxNjgxNDMyNDQzLCJleHAiOjE2ODIwMzcyNDN9.QMyNTAOiP5JWiJCfaZsf7nXTwLnQVkodLKrg7gBeuTg
Accept: */*
Content-Length: 0
```

Retorno:
```
    HTTP/1.1 200 
    Vary: Origin
    Vary: Access-Control-Request-Method
    Vary: Access-Control-Request-Headers
    Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJhdWQiOiJDYXJ0ZWlyYS5BUEkiLCJzdWIiOiJiYjkxMjhlYi1iMDUzLTQ3YTktOTc0ZS1kZGJmODBlOTViYzIiLCJuYW1lIjoiU2lyaXVzIiwiaWF0IjoxNjgxNDMyNDQ1LCJleHAiOjE2ODIwMzcyNDV9.j-3E6AX-4Lcuf5BiWs_CZ4hcYFTqVc2JIu0Md3oKmA4; Max-Age=604800; Expires=Fri, 21 Apr 2023 00:34:05 GMT; Path=/oauth/refresh-token; HttpOnly
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 0
    Cache-Control: no-cache, no-store, max-age=0, must-revalidate
    Pragma: no-cache
    Expires: 0
    X-Frame-Options: DENY
    Content-Type: application/json
    Transfer-Encoding: chunked
    Date: Fri, 14 Apr 2023 00:34:05 GMT
```
Corpo retorno:
```json
{
  "tipo": "Bearer",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJhdWQiOiJDYXJ0ZWlyYS5BUEkiLCJzdWIiOiJiYjkxMjhlYi1iMDUzLTQ3YTktOTc0ZS1kZGJmODBlOTViYzIiLCJuYW1lIjoiU2lyaXVzIiwiaWF0IjoxNjgxNDMyNDQ1LCJleHAiOjE2ODE0MzI0NTV9.xK_zRe5Lz09vELTUCPDhvBx0YZfsx1Zlk4nIR7GB1Ws"
}
```

Se ocorrer algum erro ao renovar a autenticação, será retornado código de status 400 Bad Request.

#### Limpar autenticação

Executar requisição HTTP DELETE em `/oauth/revoke`.

Este endpoint irá limpar o refresh token do cookie que é enviado para o endpoint `/oauth/refresh-token`.

Requisição:
```
DELETE /oauth/revoke HTTP/1.1
Host: localhost:8080
User-Agent: insomnia/2022.7.0
Accept: */*
```

Resposta:
```
HTTP/1.1 204
Set-Cookie: refreshToken=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:10 GMT; Path=/oauth/refresh-token
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Sat, 14 Jan 2023 17:51:55 GMT
```

### Cadastro de usuário e recuperação de senha

#### Cadastro de novo usuário

Executar requisição HTTP POST em `/usuarios`.

Parâmetros:

- `name`: Nome do usuário
- `email`: Email do usuário
- `password`: Senha do usuário

Corpo Requisição: 
```json
{
	"name": "Vinicius",
	"email": "vinicius@email.com",
	"password": "12345"
}
```

Resposta (201 Created):
```
HTTP/1.1 201 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Fri, 14 Apr 2023 00:45:26 GMT
```

Caso o email informado já foi cadastrado, será retornado uma resposta de erro com código de status 400 (Bad Request) e corpo:
```json
[
	{
		"message": "O email 'vinicius@email.com' já foi cadastrado."
	}
]
```

#### Solicitar recuperação de senha

Executar requisição HTTP POST em `/usuarios/recuperar-senha`.

Este endpoint irá criar uma solicitação de recuperação de senha e enviará um email com o link de redefinição de senha.
Este link de redefinição de senha será a url do front end do projeto, configurado na propriedade `autenticacao.recuperacao_senha.url`,
mais o token de redefinição de senha gerado por este endpoint.

Parâmetros:

- `email`: Email vinculado ao usuário para recuperação de senha

Corpo Requisição:
```json
{
	"email": "vinicius@email.com"
}
```

Resposta (204 No Content):
```
HTTP/1.1 204 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Date: Sat, 04 Feb 2023 18:34:15 GMT
```

Caso o email informado não está cadastrado,
será retornado uma resposta de erro com código de status 404 Not Found e corpo:
```json
[
	{
		"message": "O email 'vinicius@email.com' não possui cadastro."
	}
]
```

#### Redefinir a senha do usuário

Executar requisição HTTP POST em `/usuarios/redefinir-senha`.

Este endpoint é utilizado para efetivação da recuperação de senha.

Parâmetros:

- `token`: Token de redefinição de senha gerado pelo sistema, após a chamada do endpoint `/usuarios/recuperar-senha`.
- `password`: Nova senha do usuário

Corpo Requisição:
```json
{
  "token": "e621e1f8-c36c-495a-93fc-0c247a3e6e5f",
  "password": "minhaNovaSenha"
}
```

Resposta (204 No Content):
```
HTTP/1.1 204 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Date: Sat, 13 May 2023 15:25:16 GMT
```

Caso seja fornecido um token inválido ou expirado, será retornado uma respota de erro com código de status 401 Unauthorized com o corpo:
```json
[
	{
		"message": "O token de redefinição de senha está expirado."
	}
]
```

#### Verificar se email já foi cadastrado

Executar requisição HTTP GET em `/usuarios/email-disponivel`.

Endpoint para verificar se o email informado já foi cadastrado.
Se o email já foi cadastrado, a resposta da API terá código de status 400 (Bad Request),
caso o email esteja disponível, código de status 204 (No Content).

Parâmetro:
- `email`: Email que será verificado

Requisição:
```
GET /usuarios/email-disponivel?email=vinicius%40email.com HTTP/1.1
Host: localhost:8080
User-Agent: insomnia/2022.7.5
Content-Type: application/json
Accept: */*
Content-Length: 79
```

Resposta (204 No Content):
```
HTTP/1.1 204 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Date: Sat, 13 May 2023 15:40:43 GMT
```

Resposta (400 Bad Request):
```
HTTP/1.1 400
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Sat, 04 Feb 2023 13:49:40 GMT
```

## **Tecnologias**

Este projeto foi construído com as seguintes ferramentas/tecnologias:

- **[Spring Boot](https://spring.io/projects/spring-boot)**
- **[Spring Security](https://spring.io/projects/spring-security)**
- **[Lombok](https://projectlombok.org/)**
- **[MapStruct](https://mapstruct.org/)**
- **[Mockito](https://site.mockito.org/)**
- **[MongoDB](https://www.mongodb.com/)**
- **[Junit5](https://junit.org/junit5/)**
