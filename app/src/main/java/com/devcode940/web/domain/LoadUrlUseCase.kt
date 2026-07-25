package com.devcode940.web.domain

/**
 * UseCase for loading a URL (Clean Architecture)
 */
class LoadUrlUseCase {

    operator fun invoke(url: String): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.contains(".") -> "https://$url"
            else -> "https://www.google.com/search?q=${url.replace(" ", "+")}"
        }
    }
}