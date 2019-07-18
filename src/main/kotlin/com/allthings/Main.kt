package com.allthings

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.crud
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documentCrud
import io.javalin.plugin.openapi.dsl.documented
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info

fun main() {

    val getUserParamDoc = document().pathParam<Int>("id").json<User>("200")

    val getUserHeaderDoc = document().header<Int>("id").json<User>("200").operation { openApiOperation ->
        openApiOperation.description("My custom operation")
        openApiOperation.operationId("myCustomOperationId")
        openApiOperation.summary("My custom summary")
        openApiOperation.addTagsItem("Users")
    }

    val userDocumentation = documentCrud()
        .getAll(document().jsonArray<User>("200"))
        .getOne(document().pathParam<String>("id").json<User>("200"))
        .create(document().body<User>().json<User>("200"))
        .update(document().pathParam<String>("id").body<User>().result<User>("200"))
        .delete(document().pathParam<String>("id").result<User>("200"))

    val app = Javalin.create { config -> config.registerPlugin(OpenApiPlugin(getOpenApiOptions())) }.start(7000)

    app.routes {
        get("/userByParam/:id", documented(getUserParamDoc) { ctx ->
            val id = ctx.pathParam("id")
            ctx.json(User("anyone$id", "anyone$id@example.com", id.toInt()))
        })

        get("/userByHeader", documented(getUserHeaderDoc) { ctx ->
            val id = ctx.header("id")
            ctx.json(User("anyone$id", "anyone$id@example.com", id!!.toInt()))
        })

        crud("/users/:id", documented(userDocumentation, UserCrudHandler()))
    }
}

fun getOpenApiOptions(): OpenApiOptions {

    val appInfo = Info().version("1.0").description("TDC's example")
    return OpenApiOptions(appInfo).path("/swagger-docs").swagger(SwaggerOptions("/swagger").title("TDC Docs"))

}
