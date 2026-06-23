package com.schroepf.row.api

import io.ktor.http.HttpStatusCode
import java.io.IOException

class ApiException(val status: HttpStatusCode, message: String) : IOException(message)
