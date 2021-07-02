package flock.community.office.monitoring.backend.utils.client

fun String.garbled(): String =
    if (this.length > 8) {
        this.substring(0..2) + "**...**" + this.substring(this.length - 3)
    } else {
        this.substring(this.length / 3)
    }
