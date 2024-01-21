# Coding Challenge

## Prerequisites

- [scala](https://www.scala-lang.org/download/)
- [sbt](https://www.scala-sbt.org/download.html)
- [docker](https://docs.docker.com/install/)
- [docker-compose](https://docs.docker.com/compose/install/)
- [git-lfs](https://git-lfs.github.com/) (for large files)

To install git-lfs on Ubuntu __21.04+__:

```bash
sudo apt-get install git-lfs
```

To install git-lfs on macOS:

```bash
brew install git-lfs
```

You should then be able to clone the repo as usual.

## Running the application

After cloning the repo, run the following commands to start the application from the repo root:

```bash
docker-compose up -d & sbt run
```

### Docker Services

The docker compose file spins up a postgres database and a redis cache, it also when creating the database executes a script
to create the schema and load the loan data located in the `sql` folder. The `init.sql` script reads in the file using a `COPY` command.

It will rerun the script ONLY when the container is removed via `docker-compose down` and then "reupped." 
The application will be available at [http://localhost:4041](http://localhost:4041)

### Server Configuration

In the `application.conf` file, one can choose the server-type, either `rest` or `graphql`. The default is `graphql`.
If using `rest`, then you can send POST requests to `/api/loans` (see more in the Example Requests section below). 
If using `graphql`, then you can send graphql queries to `/api/graphql`.

### GraphiQL

The application has a GraphiQL interface available at [http://localhost:4041/graphiql](http://localhost:4041/graphiql)

### Example Requests:

Example requests are located in the `loans-api-graphql.http` or in the `loans-api.http` file and leverage 
[IntelliJ's HttpClient](https://www.jetbrains.com/help/objc/http-client-in-product-code-editor.html) to execute requests. 
The only required request parameter is `size` which represents the maximum number of loans to return. When
you run the requests, the responses are saved to json files in the `.idea` folder under `httpRequests`

## Running the tests

To run the tests, run the following command:

```bash
sbt test
```
Some of the tests use `testcontainers` for Postgres and Redis. 

## Things I wished I had more time for

- There are some tests, and they are useful, but I wished I had some time to request body deserialization tests, also
  fewer "magic" primitives.
- Graphql tests
- Validation of Payload could stand to use Validated instead of Either



