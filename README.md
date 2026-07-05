# blog-manager

A RESTful blog API built with Spring Boot, featuring JWT authentication with refresh
tokens, role-based permissions, per-post access control, and full OpenAPI/Swagger
documentation. The frontend is a thin client that simply consumes the API — all business
logic, validation, and security live on the backend.

**Live demo:** [https://blog-manager-sr6a.onrender.com/](https://blog-manager-sr6a.onrender.com/)

## Try it out

A frontend is served at the root path (`/`) of the deployed app, so you can explore the
platform directly in the browser without setting anything up locally.

You can log in with the following test account:

| Username    | Password      |
|-------------|---------------|
| `test_user` | `password123` |

Or register your own account — either way, you'll be able to create posts, comment, and
see the permission model in action: by default you can only edit or delete your own
content, but post owners can grant other users specific access to a post, and
moderators/admins have broader rights across the platform.

### API docs

Interactive API documentation (Swagger UI) is available at:

[https://blog-manager-sr6a.onrender.com/swagger-ui.html](https://blog-manager-sr6a.onrender.com/swagger-ui.html)

Swagger UI supports authorizing with a Bearer JWT directly from the docs, so you can log
in via `/api/auth/login`, grab the `accessToken` from the response, and authorize your
requests from there.

> Note: the app is hosted on Render's free tier, so the first request after a period of
> inactivity may take a few extra seconds while the instance spins up.

## Features

- **Authentication** — username/password login and registration, with JWT access tokens
  and persisted refresh tokens for session renewal.
- **Roles** — every user has one of three roles:
  - **Common** — the default role. Can modify or delete their own posts, and delete
    comments on posts they own.
  - **Moderator** — can delete any comment, on any post, regardless of ownership.
  - **Admin** — can modify or delete any post or comment, platform-wide.
- **Per-post access control** — independent of roles, a post's owner (or an admin) can
  grant any other user one or more specific permissions on that post: `MODIFY`, `DELETE`,
  or `DELETE_COMMENTS`. Grants are scoped to a single post and can be revoked at any
  time, giving fine-grained collaboration without changing anyone's global role.
- **Posts** — full CRUD. Edits and deletes are allowed for the post's author, an admin,
  or a user with an explicit grant for that action on the post.
- **Comments** — users can comment on any post, but can only modify their own comments.
  Deleting a comment is allowed for its author, the post's owner, any moderator or admin,
  or a user with a `DELETE_COMMENTS` grant on that post. Comments can also be listed per
  post or per user.
- **Centralized error handling** — a global exception handler maps domain errors (not
  found, forbidden, duplicate user, invalid/expired token, validation failures, etc.)
  to consistent JSON responses with appropriate HTTP status codes.
- **API documentation** — OpenAPI 3 spec and Swagger UI, generated automatically from
  the codebase.

## Tech stack

- **Java 25** / **Spring Boot 4**
- **Spring Security** + **JJWT** for authentication and token handling
- **Spring Data JPA** / **Hibernate** over **PostgreSQL**
- **springdoc-openapi** for API documentation
- **Testcontainers** for integration tests against a real PostgreSQL instance
- Deployed on **Render**

## API overview

All endpoints are prefixed with `/api`.

| Method | Endpoint                                | Description                                       | Auth required |
|--------|-------------------------------------------|-----------------------------------------------------|----------------|
| POST   | `/auth/register`                          | Register a new user                                  | No             |
| POST   | `/auth/login`                             | Log in and receive access/refresh tokens              | No             |
| POST   | `/auth/refresh`                           | Exchange a refresh token for a new access token       | No             |
| POST   | `/auth/logout`                            | Invalidate the current refresh token                  | No             |
| GET    | `/users/me`                               | Get the currently authenticated user                  | Yes            |
| GET    | `/users/by-username/{username}`           | Look up a user by username                            | No             |
| GET    | `/users/{userId}/comments`                | List all comments made by a user                      | No             |
| GET    | `/posts`                                  | List all posts                                        | No             |
| POST   | `/posts`                                  | Create a post                                         | Yes            |
| GET    | `/posts/{id}`                             | Get a post by ID                                      | No             |
| PUT    | `/posts/{id}`                             | Update a post (author, admin, or `MODIFY` grant)      | Yes            |
| DELETE | `/posts/{id}`                             | Delete a post (author, admin, or `DELETE` grant)      | Yes            |
| POST   | `/posts/{postId}/access`                  | Grant a user access to a post (owner or admin only)    | Yes            |
| DELETE | `/posts/{postId}/access`                  | Revoke a user's access to a post (owner or admin only) | Yes            |
| GET    | `/posts/{postId}/access`                  | List all access grants on a post (owner or admin only) | Yes            |
| GET    | `/posts/{postId}/access/mine`             | List the caller's own permissions on a post            | Yes            |
| GET    | `/posts/{postId}/comments`                | List comments on a post                                | No             |
| POST   | `/posts/{postId}/comments`                | Add a comment to a post                                 | Yes            |
| GET    | `/posts/{postId}/comments/{id}`           | Get a comment by ID                                     | No             |
| PUT    | `/posts/{postId}/comments/{id}`           | Update a comment (author only)                          | Yes            |
| DELETE | `/posts/{postId}/comments/{id}`           | Delete a comment (author, post owner, moderator, admin, or `DELETE_COMMENTS` grant) | Yes |

For full request/response schemas, use the Swagger UI linked above.

### Permission grants

`POST /posts/{postId}/access` and `DELETE /posts/{postId}/access` both accept a body of
the form:

```json
{
  "granteeUserId": "the-user-id-to-grant-access-to",
  "permissions": ["MODIFY", "DELETE", "DELETE_COMMENTS"]
}
```

## Running locally

**Requirements:** Java 25, Maven, PostgreSQL

1. Clone the repo:
   ```bash
   git clone https://github.com/TeoryMan143/blog-manager.git
   cd blog-manager
   ```
2. Set the following environment variables (or configure an `application-local.properties`):
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/blogmanager
   SPRING_DATASOURCE_USERNAME=your_db_user
   SPRING_DATASOURCE_PASSWORD=your_db_password
   JWT_SECRET=your_jwt_secret
   ```
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. The API will be available at `http://localhost:8080`, with Swagger UI at
   `http://localhost:8080/swagger-ui.html`.

## Running tests

```bash
./mvnw test
```

Integration tests spin up a real PostgreSQL instance via Testcontainers, so Docker must
be running locally.