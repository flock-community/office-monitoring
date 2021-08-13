package flock.community.office.monitoring.backend.utils.client

class ResourceNotFoundException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class HttpServerException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class HttpClientException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
